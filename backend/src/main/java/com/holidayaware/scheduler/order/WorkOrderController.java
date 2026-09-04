package com.holidayaware.scheduler.order;

import java.util.List;

import com.holidayaware.scheduler.order.dto.CreateOrderRequest;
import com.holidayaware.scheduler.order.dto.OrderResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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
    OrderResponse create(@RequestBody CreateOrderRequest request) {
        return service.create(request);
    }

    @GetMapping
    List<OrderResponse> list() {
        return service.list();
    }
}
