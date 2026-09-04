package com.holidayaware.scheduler.holiday;

import java.time.LocalDateTime;
import java.util.List;

public record HolidayWindow(
        int workingDays,
        List<PublicHoliday> holidays,
        HolidaySource source,
        LocalDateTime fetchedAt) {

    public boolean isUnavailable() {
        return source == HolidaySource.UNAVAILABLE;
    }
}
