package com.holidayaware.scheduler.dev;

public record DevStatus(
        boolean holidayApiEnabled,
        long orderCount,
        long cachedHolidays,
        long trackedCalendars) {
}
