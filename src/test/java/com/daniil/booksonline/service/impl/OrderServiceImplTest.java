package com.daniil.booksonline.service.impl;

import com.daniil.booksonline.dto.CreateOrderItemRequestDto;
import com.daniil.booksonline.dto.CreateOrderRequestDto;
import com.daniil.booksonline.dto.OrderResponseDto;
import com.daniil.booksonline.entity.Order;
import com.daniil.booksonline.entity.OrderStatus;
import com.daniil.booksonline.entity.Product;
import com.daniil.booksonline.entity.ProductType;
import com.daniil.booksonline.exception.InsufficientStockException;
import com.daniil.booksonline.mapper.OrderMapper;
import com.daniil.booksonline.repository.OrderRepository;
import com.daniil.booksonline.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, productRepository, orderMapper, Clock.fixed(Instant.parse("2025-01-01T10:15:30Z"), ZoneOffset.UTC));
    }

    @Test
    void createOrderShouldPersistOrderAndDecreaseStock() {
        Product firstProduct = new Product(1L, "Book A", "Desc", new BigDecimal("10.00"), 5, ProductType.BOOK);
        Product secondProduct = new Product(2L, "License B", "Desc", new BigDecimal("25.50"), 2, ProductType.SOFTWARE_LICENSE);

        when(productRepository.findAllForUpdate(anyCollection())).thenReturn(List.of(firstProduct, secondProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toDto(any(Order.class))).thenReturn(OrderResponseDto.builder().id(99L).build());

        CreateOrderRequestDto request = new CreateOrderRequestDto();
        CreateOrderItemRequestDto item1 = new CreateOrderItemRequestDto();
        item1.setProductId(1L);
        item1.setQuantity(2);
        CreateOrderItemRequestDto item2 = new CreateOrderItemRequestDto();
        item2.setProductId(2L);
        item2.setQuantity(1);
        request.setItems(List.of(item1, item2));

        OrderResponseDto response = orderService.createOrder(request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(firstProduct.getStock()).isEqualTo(3);
        assertThat(secondProduct.getStock()).isEqualTo(1);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(savedOrder.getTotalPrice()).isEqualByComparingTo("45.50");
        assertThat(savedOrder.getItems()).hasSize(2);
        assertThat(savedOrder.getCreatedAt()).isEqualTo(Instant.parse("2025-01-01T10:15:30Z"));
        verify(productRepository).saveAll(anyIterable());
    }

    @Test
    void createOrderShouldRejectInsufficientStock() {
        Product product = new Product(1L, "Book A", "Desc", new BigDecimal("10.00"), 1, ProductType.BOOK);
        when(productRepository.findAllForUpdate(anyCollection())).thenReturn(List.of(product));

        CreateOrderRequestDto request = new CreateOrderRequestDto();
        CreateOrderItemRequestDto item = new CreateOrderItemRequestDto();
        item.setProductId(1L);
        item.setQuantity(2);
        request.setItems(List.of(item));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }
}
