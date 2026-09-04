package com.holidayaware.scheduler.holiday;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

interface HolidaySyncRepository extends JpaRepository<HolidaySync, HolidaySync.Key> {

    @Modifying
    @Query("update HolidaySync s set s.lastAttemptAt = :backdatedTo where s.lastOutcome = com.holidayaware.scheduler.holiday.SyncOutcome.FAILED")
    void expireFailedAttempts(LocalDateTime backdatedTo);
}
