package com.holidayaware.scheduler.holiday;

import org.springframework.stereotype.Component;

@Component
public class HolidayAdmin {

    private final HolidayApiToggle toggle;
    private final HolidayCacheStore store;

    HolidayAdmin(HolidayApiToggle toggle, HolidayCacheStore store) {
        this.toggle = toggle;
        this.store = store;
    }

    public boolean isApiEnabled() {
        return toggle.isEnabled();
    }

    public void setApiEnabled(boolean enabled) {
        toggle.setEnabled(enabled);
        if (enabled) {
            store.clearFailureBackoff();
        }
    }

    public void clearCache() {
        store.clearAll();
    }

    public long cachedHolidayCount() {
        return store.countCachedHolidays();
    }

    public long trackedCalendarCount() {
        return store.countTrackedCalendars();
    }
}
