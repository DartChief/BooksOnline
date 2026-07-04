package com.daniil.booksonline.service;

import com.daniil.booksonline.dto.ProductResponseDto;
import java.util.List;

public interface ProductService {

    List<ProductResponseDto> getAllProducts();

    ProductResponseDto getProductById(Long id);
}

