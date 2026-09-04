package com.holidayaware.scheduler.order.dto;

import java.time.LocalDate;

import com.holidayaware.scheduler.order.OrderStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotBlank @Size(max = 50) String productCode,
        @NotNull @Positive Integer quantity,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$", message = "2-letter ISO country code")
        String countryCode,
        @NotNull LocalDate startDate,
        @NotNull LocalDate dueDate,
        @NotNull @Positive Integer requiredDays,
        OrderStatus status) {
}
