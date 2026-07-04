package com.daniil.booksonline.mapper;

import com.daniil.booksonline.dto.OrderItemResponseDto;
import com.daniil.booksonline.dto.OrderResponseDto;
import com.daniil.booksonline.entity.Order;
import com.daniil.booksonline.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "items", source = "items")
    OrderResponseDto toDto(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productType", source = "product.type")
    OrderItemResponseDto toDto(OrderItem orderItem);
}

