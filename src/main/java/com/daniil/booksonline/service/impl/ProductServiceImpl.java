package com.daniil.booksonline.service.impl;

import com.daniil.booksonline.dto.ProductResponseDto;
import com.daniil.booksonline.entity.Product;
import com.daniil.booksonline.exception.ResourceNotFoundException;
import com.daniil.booksonline.mapper.ProductMapper;
import com.daniil.booksonline.repository.ProductRepository;
import com.daniil.booksonline.service.ProductService;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream()
                .sorted(Comparator.comparing(Product::getId))
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        log.debug("Loaded product {}", id);
        return productMapper.toDto(product);
    }
}

