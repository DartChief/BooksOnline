package com.daniil.booksonline.dto;

import com.daniil.booksonline.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {

    private Long id;
    private Instant createdAt;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private List<OrderItemResponseDto> items;
}

