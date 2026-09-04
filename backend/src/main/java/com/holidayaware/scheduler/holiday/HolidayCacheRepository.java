package com.holidayaware.scheduler.holiday;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

interface HolidayCacheRepository extends JpaRepository<HolidayCache, Long> {

    List<HolidayCache> findByCountryCodeAndHolidayYearOrderByHolidayDate(String countryCode, int holidayYear);

    @Modifying
    @Query("delete from HolidayCache h where h.countryCode = :countryCode and h.holidayYear = :holidayYear")
    void deleteCachedYear(String countryCode, int holidayYear);
}
