package com.holidayaware.scheduler.holiday;

import org.springframework.data.jpa.repository.JpaRepository;

interface HolidaySyncRepository extends JpaRepository<HolidaySync, HolidaySync.Key> {
}
