package com.daniil.booksonline.mapper;

import com.daniil.booksonline.dto.ProductResponseDto;
import com.daniil.booksonline.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "available", expression = "java(product.getStock() != null && product.getStock() > 0)")
    ProductResponseDto toDto(Product product);
}

