package com.holidayaware.scheduler.holiday;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class HolidayCacheStore {

    private final HolidayCacheRepository holidayCacheRepository;
    private final HolidaySyncRepository holidaySyncRepository;

    HolidayCacheStore(HolidayCacheRepository holidayCacheRepository,
                      HolidaySyncRepository holidaySyncRepository) {
        this.holidayCacheRepository = holidayCacheRepository;
        this.holidaySyncRepository = holidaySyncRepository;
    }

    @Transactional(readOnly = true)
    Optional<HolidaySync> findSync(String countryCode, int year) {
        return holidaySyncRepository.findById(new HolidaySync.Key(countryCode, year));
    }

    @Transactional(readOnly = true)
    List<PublicHoliday> findHolidays(String countryCode, int year) {
        return holidayCacheRepository.findByCountryCodeAndHolidayYearOrderByHolidayDate(countryCode, year)
                .stream()
                .map(cached -> new PublicHoliday(cached.getHolidayDate(), cached.getHolidayName()))
                .toList();
    }

    @Transactional
    LocalDateTime replaceHolidays(String countryCode, int year, List<PublicHoliday> holidays,
                                  SyncOutcome outcome) {
        LocalDateTime now = LocalDateTime.now();
        holidayCacheRepository.deleteByCountryCodeAndHolidayYear(countryCode, year);
        holidayCacheRepository.saveAll(holidays.stream()
                .map(holiday -> new HolidayCache(countryCode, year, holiday.date(), holiday.name(), now))
                .toList());
        HolidaySync sync = findOrCreateSync(countryCode, year);
        sync.recordAnswer(now, outcome);
        holidaySyncRepository.save(sync);
        return now;
    }

    @Transactional
    void recordFailure(String countryCode, int year) {
        HolidaySync sync = findOrCreateSync(countryCode, year);
        sync.recordFailure(LocalDateTime.now());
        holidaySyncRepository.save(sync);
    }

    private HolidaySync findOrCreateSync(String countryCode, int year) {
        return holidaySyncRepository.findById(new HolidaySync.Key(countryCode, year))
                .orElseGet(() -> new HolidaySync(countryCode, year));
    }
}
