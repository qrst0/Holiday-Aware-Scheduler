package com.holidayaware.scheduler.order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import java.time.temporal.ChronoUnit;

import com.holidayaware.scheduler.order.dto.CreateOrderRequest;
import com.holidayaware.scheduler.order.dto.OrderResponse;

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
            CreateOrderRequest request = randomOrder();
            OrderResponse response = workOrderService.create(request);
            backdate(response.id(), request.startDate());
            created++;
        }
        return created;
    }

    public long count() {
        return repository.count();
    }

    private void backdate(Long id, LocalDate startDate) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime productionStart = startDate.atStartOfDay();
        LocalDateTime ceiling = productionStart.isBefore(now) ? productionStart : now;

        LocalDateTime createdAt = ceiling
                .minusDays(random.nextInt(5, 60))
                .withHour(random.nextInt(8, 18))
                .withMinute(random.nextInt(0, 60));

        long slackMinutes = Math.max(1, ChronoUnit.MINUTES.between(createdAt, ceiling));
        LocalDateTime updatedAt = createdAt.plusMinutes(random.nextLong(0, slackMinutes));

        repository.findById(id).ifPresent(order -> {
            order.backdate(createdAt, updatedAt.atZone(ZoneId.systemDefault()).toInstant());
            repository.save(order);
        });
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
