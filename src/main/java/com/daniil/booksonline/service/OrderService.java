package com.daniil.booksonline.service;

import com.daniil.booksonline.dto.CreateOrderRequestDto;
import com.daniil.booksonline.dto.OrderResponseDto;
import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(CreateOrderRequestDto request);

    OrderResponseDto getOrderById(Long id);

    List<OrderResponseDto> getAllOrders();
}

