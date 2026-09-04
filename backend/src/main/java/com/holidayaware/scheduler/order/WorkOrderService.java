package com.holidayaware.scheduler.order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.holidayaware.scheduler.common.BadRequestException;
import com.holidayaware.scheduler.common.NotFoundException;
import com.holidayaware.scheduler.common.PageResponse;
import com.holidayaware.scheduler.holiday.HolidayService;
import com.holidayaware.scheduler.holiday.HolidayWindow;
import com.holidayaware.scheduler.order.dto.CreateOrderRequest;
import com.holidayaware.scheduler.order.dto.OrderResponse;
import com.holidayaware.scheduler.order.dto.UpdateOrderRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

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
        requireOrderedWindow(request.startDate(), request.dueDate());

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

    PageResponse<OrderResponse> list(OrderStatus status, String countryCode, RiskFlag riskFlag,
                                     Pageable pageable) {
        Page<WorkOrder> page = repository.findAll(matching(status, countryCode, riskFlag), pageable);
        List<OrderResponse> content = page.getContent().stream()
                .map(this::refreshAndMap)
                .toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.hasNext());
    }

    private Specification<WorkOrder> matching(OrderStatus status, String countryCode, RiskFlag riskFlag) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (countryCode != null) {
                predicates.add(builder.equal(root.get("countryCode"), countryCode.toUpperCase(Locale.ROOT)));
            }
            if (riskFlag != null) {
                predicates.add(builder.equal(root.get("riskFlag"), riskFlag));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    OrderResponse get(Long id) {
        return refreshAndMap(findOrThrow(id));
    }

    OrderResponse update(Long id, UpdateOrderRequest request) {
        WorkOrder order = findOrThrow(id);

        String productCode = valueOr(request.productCode(), order.getProductCode());
        int quantity = valueOr(request.quantity(), order.getQuantity());
        String countryCode = request.countryCode() == null
                ? order.getCountryCode()
                : request.countryCode().toUpperCase(Locale.ROOT);
        LocalDate startDate = valueOr(request.startDate(), order.getStartDate());
        LocalDate dueDate = valueOr(request.dueDate(), order.getDueDate());
        int requiredDays = valueOr(request.requiredDays(), order.getRequiredDays());
        OrderStatus status = valueOr(request.status(), order.getStatus());

        requireOrderedWindow(startDate, dueDate);

        order.update(productCode, quantity, countryCode, startDate, dueDate, requiredDays, status);

        HolidayWindow window = evaluate(order);
        order.setRiskFlag(riskFlagFor(window, order.getRequiredDays()));
        return toResponse(repository.save(order), window);
    }

    private static <T> T valueOr(T patched, T current) {
        return patched == null ? current : patched;
    }

    void delete(Long id) {
        repository.delete(findOrThrow(id));
    }

    private WorkOrder findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work order " + id + " not found"));
    }

    private void requireOrderedWindow(LocalDate startDate, LocalDate dueDate) {
        if (dueDate.isBefore(startDate)) {
            throw new BadRequestException("dueDate must not be before startDate");
        }
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
