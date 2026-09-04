package com.holidayaware.scheduler.order;

import java.util.List;

import com.holidayaware.scheduler.order.dto.CreateOrderRequest;
import com.holidayaware.scheduler.order.dto.OrderResponse;
import com.holidayaware.scheduler.order.dto.UpdateOrderRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    List<OrderResponse> list() {
        return service.list();
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
