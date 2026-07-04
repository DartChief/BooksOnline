package com.daniil.booksonline.service.impl;

import com.daniil.booksonline.dto.CreateOrderItemRequestDto;
import com.daniil.booksonline.dto.CreateOrderRequestDto;
import com.daniil.booksonline.dto.OrderResponseDto;
import com.daniil.booksonline.entity.Order;
import com.daniil.booksonline.entity.OrderItem;
import com.daniil.booksonline.entity.OrderStatus;
import com.daniil.booksonline.entity.Product;
import com.daniil.booksonline.exception.InsufficientStockException;
import com.daniil.booksonline.exception.ResourceNotFoundException;
import com.daniil.booksonline.mapper.OrderMapper;
import com.daniil.booksonline.repository.OrderRepository;
import com.daniil.booksonline.repository.ProductRepository;
import com.daniil.booksonline.service.OrderService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final Clock clock;

    @Override
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto request) {
        Map<Long, Integer> requestedQuantities = aggregateRequestedQuantities(request.getItems());
        Map<Long, Product> productsById = lockAndLoadProducts(requestedQuantities.keySet());

        validateAllProductsExist(requestedQuantities.keySet(), productsById.keySet());
        requestedQuantities.forEach((productId, quantity) -> {
            Product product = productsById.get(productId);
            if (product.getStock() < quantity) {
                throw new InsufficientStockException(
                        "Insufficient stock for product " + product.getName() + ". Requested " + quantity + ", available " + product.getStock()
                );
            }
        });

        Instant now = Instant.now(clock);
        Order order = new Order(now, OrderStatus.PAID, BigDecimal.ZERO);
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
            Product product = productsById.get(entry.getKey());
            Integer quantity = entry.getValue();

            product.setStock(product.getStock() - quantity);
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            totalPrice = totalPrice.add(lineTotal);

            order.addItem(new OrderItem(product, quantity, product.getPrice()));
        }

        order.setTotalPrice(totalPrice);
        productRepository.saveAll(productsById.values());
        Order savedOrder = orderRepository.save(order);
        log.info("Created order {} with total {}", savedOrder.getId(), savedOrder.getTotalPrice());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findByIdWithItemsAndProducts(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        return orderRepository.findAllWithItemsAndProducts().stream()
                .map(orderMapper::toDto)
                .toList();
    }

    private Map<Long, Integer> aggregateRequestedQuantities(List<CreateOrderItemRequestDto> items) {
        if (CollectionUtils.isEmpty(items)) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        return items.stream()
                .collect(Collectors.toMap(
                        CreateOrderItemRequestDto::getProductId,
                        CreateOrderItemRequestDto::getQuantity,
                        Integer::sum,
                        LinkedHashMap::new
                ));
    }

    private Map<Long, Product> lockAndLoadProducts(Collection<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        return productRepository.findAllForUpdate(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product, (left, right) -> left, LinkedHashMap::new));
    }

    private void validateAllProductsExist(Collection<Long> requestedIds, Collection<Long> loadedIds) {
        if (!loadedIds.containsAll(requestedIds)) {
            Collection<Long> missing = requestedIds.stream()
                    .filter(id -> !loadedIds.contains(id))
                    .toList();
            throw new ResourceNotFoundException("Products not found with ids " + missing);
        }
    }
}

