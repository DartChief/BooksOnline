package com.daniil.booksonline.dto;

import com.daniil.booksonline.entity.ProductType;
import java.math.BigDecimal;
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
public class OrderItemResponseDto {

    private Long id;
    private Long productId;
    private String productName;
    private ProductType productType;
    private Integer quantity;
    private BigDecimal price;
}

