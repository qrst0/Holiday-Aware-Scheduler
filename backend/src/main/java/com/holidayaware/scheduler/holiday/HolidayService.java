package com.holidayaware.scheduler.holiday;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HolidayService {

    private final NagerDateClient nagerDateClient;
    private final HolidayCacheStore store;
    private final int cacheTtlDays;
    private final Duration retryBackoff;

    HolidayService(NagerDateClient nagerDateClient,
                   HolidayCacheStore store,
                   @Value("${holiday.cache.ttl-days}") int cacheTtlDays,
                   @Value("${holiday.cache.retry-backoff}") Duration retryBackoff) {
        this.nagerDateClient = nagerDateClient;
        this.store = store;
        this.cacheTtlDays = cacheTtlDays;
        this.retryBackoff = retryBackoff;
    }

    public HolidayWindow evaluate(String countryCode, LocalDate start, LocalDate end) {
        if (countryCode == null || start == null || end == null || end.isBefore(start)) {
            return new HolidayWindow(0, List.of(), HolidaySource.UNAVAILABLE, null);
        }

        String country = countryCode.toUpperCase(Locale.ROOT);
        List<PublicHoliday> collected = new ArrayList<>();
        HolidaySource source = HolidaySource.LIVE;
        LocalDateTime fetchedAt = null;

        for (int year = start.getYear(); year <= end.getYear(); year++) {
            YearLookup lookup = resolveYear(country, year);
            collected.addAll(lookup.holidays());
            source = mostDegraded(source, lookup.source());
            fetchedAt = oldest(fetchedAt, lookup.fetchedAt());
        }

        List<PublicHoliday> inWindow = collected.stream()
                .filter(holiday -> !holiday.date().isBefore(start) && !holiday.date().isAfter(end))
                .sorted(Comparator.comparing(PublicHoliday::date))
                .toList();
        Set<LocalDate> holidayDates = inWindow.stream()
                .map(PublicHoliday::date)
                .collect(Collectors.toSet());

        int workingDays = WorkingDayCalculator.countWorkingDays(start, end, holidayDates);
        return new HolidayWindow(workingDays, inWindow, source, fetchedAt);
    }

    private YearLookup resolveYear(String country, int year) {
        LocalDateTime now = LocalDateTime.now();
        Optional<HolidaySync> syncRow = store.findSync(country, year);
        HolidaySync sync = syncRow.orElse(null);

        if (sync != null && sync.getLastSuccessAt() != null
                && sync.getLastSuccessAt().isAfter(now.minusDays(cacheTtlDays))) {
            return new YearLookup(store.findHolidays(country, year), HolidaySource.CACHE_FRESH,
                    sync.getLastSuccessAt());
        }

        if (!isBackingOff(sync, now)) {
            Optional<List<PublicHoliday>> fetched = nagerDateClient.fetchHolidays(country, year);
            if (fetched.isPresent()) {
                List<PublicHoliday> holidays = fetched.get();
                SyncOutcome outcome = holidays.isEmpty() ? SyncOutcome.EMPTY : SyncOutcome.SUCCESS;
                LocalDateTime storedAt = store.replaceHolidays(country, year, holidays, outcome);
                return new YearLookup(holidays, HolidaySource.LIVE, storedAt);
            }
            store.recordFailure(country, year);
        }

        List<PublicHoliday> cached = store.findHolidays(country, year);
        boolean answeredBefore = (sync != null && sync.getLastSuccessAt() != null) || !cached.isEmpty();
        if (answeredBefore) {
            return new YearLookup(cached, HolidaySource.CACHE_STALE,
                    sync == null ? null : sync.getLastSuccessAt());
        }
        return new YearLookup(List.of(), HolidaySource.UNAVAILABLE, null);
    }

    private boolean isBackingOff(HolidaySync sync, LocalDateTime now) {
        return sync != null
                && sync.getLastOutcome() == SyncOutcome.FAILED
                && sync.getLastAttemptAt().isAfter(now.minus(retryBackoff));
    }

    private static HolidaySource mostDegraded(HolidaySource left, HolidaySource right) {
        return left.ordinal() >= right.ordinal() ? left : right;
    }

    private static LocalDateTime oldest(LocalDateTime left, LocalDateTime right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isBefore(right) ? left : right;
    }

    private record YearLookup(List<PublicHoliday> holidays, HolidaySource source, LocalDateTime fetchedAt) {
    }
}
