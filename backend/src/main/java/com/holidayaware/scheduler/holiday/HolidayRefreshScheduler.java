package com.holidayaware.scheduler.holiday;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
class HolidayRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(HolidayRefreshScheduler.class);

    private final HolidayCacheStore store;
    private final HolidayService holidayService;

    HolidayRefreshScheduler(HolidayCacheStore store, HolidayService holidayService) {
        this.store = store;
        this.holidayService = holidayService;
    }

    @Scheduled(cron = "${holiday.cache.refresh-cron}")
    void refreshCachedCalendars() {
        List<HolidaySync> tracked = store.findAllSync();
        int currentYear = LocalDate.now().getYear();
        int refreshed = 0;
        int skipped = 0;

        for (HolidaySync sync : tracked) {
            if (sync.getHolidayYear() < currentYear) {
                skipped++;
                continue;
            }
            if (refresh(sync.getCountryCode(), sync.getHolidayYear())) {
                refreshed++;
            }
        }

        log.info("holiday refresh swept {} cached calendars, {} refreshed, {} past years skipped",
                tracked.size(), refreshed, skipped);
    }

    private boolean refresh(String countryCode, int year) {
        try {
            HolidayWindow window = holidayService.evaluate(
                    countryCode,
                    LocalDate.of(year, Month.JANUARY, 1),
                    LocalDate.of(year, Month.DECEMBER, 31));
            return window.source() == HolidaySource.LIVE;
        } catch (RuntimeException e) {
            log.warn("holiday refresh failed for {} {}: {}", countryCode, year, e.getMessage());
            return false;
        }
    }
}
