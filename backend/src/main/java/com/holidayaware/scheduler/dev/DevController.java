package com.holidayaware.scheduler.dev;

import com.holidayaware.scheduler.common.BadRequestException;
import com.holidayaware.scheduler.holiday.HolidayAdmin;
import com.holidayaware.scheduler.order.OrderSeeder;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/dev")
@ConditionalOnProperty(name = "scheduler.dev-tools.enabled", havingValue = "true", matchIfMissing = true)
class DevController {

    private static final int MAX_SEED = 200;

    private final HolidayAdmin holidayAdmin;
    private final OrderSeeder orderSeeder;

    DevController(HolidayAdmin holidayAdmin, OrderSeeder orderSeeder) {
        this.holidayAdmin = holidayAdmin;
        this.orderSeeder = orderSeeder;
    }

    @GetMapping("/status")
    DevStatus status() {
        return currentStatus();
    }

    @PostMapping("/holiday-api")
    DevStatus setHolidayApi(@RequestParam boolean enabled) {
        holidayAdmin.setApiEnabled(enabled);
        return currentStatus();
    }

    @PostMapping("/seed")
    DevStatus seed(@RequestParam(defaultValue = "10") int count) {
        if (count < 1 || count > MAX_SEED) {
            throw new BadRequestException("count must be between 1 and " + MAX_SEED);
        }
        orderSeeder.seed(count);
        return currentStatus();
    }

    @DeleteMapping("/holiday-cache")
    DevStatus clearHolidayCache() {
        holidayAdmin.clearCache();
        return currentStatus();
    }

    private DevStatus currentStatus() {
        return new DevStatus(
                holidayAdmin.isApiEnabled(),
                orderSeeder.count(),
                holidayAdmin.cachedHolidayCount(),
                holidayAdmin.trackedCalendarCount());
    }
}
