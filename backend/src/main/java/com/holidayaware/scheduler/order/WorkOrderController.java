package com.holidayaware.scheduler.order;

import com.holidayaware.scheduler.common.PageResponse;
import com.holidayaware.scheduler.order.dto.CreateOrderRequest;
import com.holidayaware.scheduler.order.dto.OrderResponse;
import com.holidayaware.scheduler.order.dto.UpdateOrderRequest;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
class WorkOrderController {

    private final WorkOrderService service;

    WorkOrderController(WorkOrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return service.create(request);
    }

    @GetMapping
    PageResponse<OrderResponse> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) RiskFlag riskFlag,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.list(status, countryCode, riskFlag, pageable);
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PatchMapping("/{id}")
    OrderResponse update(@PathVariable Long id, @Valid @RequestBody UpdateOrderRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
