package com.holidayaware.scheduler.order.dto;

import java.time.LocalDate;

import com.holidayaware.scheduler.order.OrderStatus;

public record CreateOrderRequest(
        String productCode,
        Integer quantity,
        String countryCode,
        LocalDate startDate,
        LocalDate dueDate,
        Integer requiredDays,
        OrderStatus status) {
}
