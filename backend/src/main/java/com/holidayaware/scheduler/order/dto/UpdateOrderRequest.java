package com.holidayaware.scheduler.order.dto;

import java.time.LocalDate;

import com.holidayaware.scheduler.order.OrderStatus;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateOrderRequest(
        @Size(max = 50) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String productCode,
        @Positive Integer quantity,
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "must be a 2-letter ISO country code")
        String countryCode,
        LocalDate startDate,
        LocalDate dueDate,
        @Positive Integer requiredDays,
        OrderStatus status) {
}
