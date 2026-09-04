package com.holidayaware.scheduler.holiday;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;


@Component
class HolidayApiToggle {

    private final AtomicBoolean enabled = new AtomicBoolean(true);

    boolean isEnabled() {
        return enabled.get();
    }

    void setEnabled(boolean value) {
        enabled.set(value);
    }
}
