package com.daniil.booksonline.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrderRequestDto {

    @NotEmpty
    @Valid
    private List<CreateOrderItemRequestDto> items;
}

