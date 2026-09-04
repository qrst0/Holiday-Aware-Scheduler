package com.holidayaware.scheduler.order.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.holidayaware.scheduler.holiday.HolidaySource;
import com.holidayaware.scheduler.holiday.PublicHoliday;
import com.holidayaware.scheduler.order.OrderStatus;
import com.holidayaware.scheduler.order.RiskFlag;

public record OrderResponse(
        Long id,
        String productCode,
        int quantity,
        String countryCode,
        LocalDate startDate,
        LocalDate dueDate,
        int requiredDays,
        OrderStatus status,
        RiskFlag riskFlag,
        int workingDays,
        HolidaySource holidaySource,
        List<PublicHoliday> holidaysInWindow,
        LocalDateTime createdAt) {
}
