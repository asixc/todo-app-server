package dev.jotxee.todo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record Quantity(
        @NotNull
        @DecimalMin(value = "0.01", inclusive = true)
        @Digits(integer = 6, fraction = 2)
        BigDecimal value,
        @NotBlank
        @Pattern(regexp = "unit|g|kg|ml|l")
        String unit
) {
}
