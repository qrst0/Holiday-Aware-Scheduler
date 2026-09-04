package com.holidayaware.scheduler.holiday;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface HolidayCacheRepository extends JpaRepository<HolidayCache, Long> {

    List<HolidayCache> findByCountryCodeAndHolidayYearOrderByHolidayDate(String countryCode, int holidayYear);

    void deleteByCountryCodeAndHolidayYear(String countryCode, int holidayYear);
}
