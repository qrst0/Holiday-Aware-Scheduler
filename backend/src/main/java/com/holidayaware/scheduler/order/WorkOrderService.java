package com.holidayaware.scheduler.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import com.holidayaware.scheduler.holiday.HolidayService;
import com.holidayaware.scheduler.holiday.HolidayWindow;
import com.holidayaware.scheduler.order.dto.CreateOrderRequest;
import com.holidayaware.scheduler.order.dto.OrderResponse;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
class WorkOrderService {

    private final WorkOrderRepository repository;
    private final HolidayService holidayService;

    WorkOrderService(WorkOrderRepository repository, HolidayService holidayService) {
        this.repository = repository;
        this.holidayService = holidayService;
    }

    OrderResponse create(CreateOrderRequest request) {
        WorkOrder order = new WorkOrder(
                request.productCode(),
                request.quantity(),
                request.countryCode().toUpperCase(Locale.ROOT),
                request.startDate(),
                request.dueDate(),
                request.requiredDays(),
                request.status() == null ? OrderStatus.PLANNED : request.status(),
                LocalDateTime.now());

        HolidayWindow window = evaluate(order);
        order.setRiskFlag(riskFlagFor(window, order.getRequiredDays()));
        return toResponse(repository.save(order), window);
    }

    List<OrderResponse> list() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(this::refreshAndMap)
                .toList();
    }

    private OrderResponse refreshAndMap(WorkOrder order) {
        HolidayWindow window = evaluate(order);
        RiskFlag recomputed = riskFlagFor(window, order.getRequiredDays());
        if (recomputed != order.getRiskFlag()) {
            order.setRiskFlag(recomputed);
            repository.save(order);
        }
        return toResponse(order, window);
    }

    private HolidayWindow evaluate(WorkOrder order) {
        return holidayService.evaluate(order.getCountryCode(), order.getStartDate(), order.getDueDate());
    }

    private RiskFlag riskFlagFor(HolidayWindow window, int requiredDays) {
        if (window.isUnavailable()) {
            return RiskFlag.UNKNOWN;
        }
        return window.workingDays() < requiredDays ? RiskFlag.AT_RISK : RiskFlag.ON_TRACK;
    }

    private OrderResponse toResponse(WorkOrder order, HolidayWindow window) {
        return new OrderResponse(
                order.getId(),
                order.getProductCode(),
                order.getQuantity(),
                order.getCountryCode(),
                order.getStartDate(),
                order.getDueDate(),
                order.getRequiredDays(),
                order.getStatus(),
                order.getRiskFlag(),
                window.workingDays(),
                window.source(),
                window.holidays(),
                order.getCreatedAt());
    }
}
