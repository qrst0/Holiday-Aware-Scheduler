package com.holidayaware.scheduler.order;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.holidayaware.scheduler.order.dto.CreateOrderRequest;

import org.springframework.stereotype.Component;

@Component
public class OrderSeeder {

    private static final List<String> PRODUCTS =
            List.of("VALVE", "GASKET", "ROTOR", "BEARING", "PISTON", "FLANGE", "SPINDLE", "COUPLER");

    private static final List<String> COUNTRIES = List.of("ID", "KR", "JP", "SG", "MY", "DE", "GB", "US");

    private final WorkOrderService workOrderService;
    private final WorkOrderRepository repository;

    OrderSeeder(WorkOrderService workOrderService, WorkOrderRepository repository) {
        this.workOrderService = workOrderService;
        this.repository = repository;
    }

    public int seed(int count) {
        int created = 0;
        for (int i = 0; i < count; i++) {
            workOrderService.create(randomOrder());
            created++;
        }
        return created;
    }

    public long count() {
        return repository.count();
    }

    private CreateOrderRequest randomOrder() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        LocalDate start = LocalDate.of(2026, 1, 1).plusDays(random.nextInt(0, 300));
        LocalDate due = start.plusDays(random.nextInt(14, 70));

        int requiredDays = random.nextInt(5, 45);

        return new CreateOrderRequest(
                PRODUCTS.get(random.nextInt(PRODUCTS.size())) + "-" + random.nextInt(100, 1000),
                random.nextInt(10, 500),
                COUNTRIES.get(random.nextInt(COUNTRIES.size())),
                start,
                due,
                requiredDays,
                OrderStatus.values()[random.nextInt(OrderStatus.values().length)]);
    }
}
