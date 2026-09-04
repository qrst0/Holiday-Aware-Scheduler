package com.holidayaware.scheduler.holiday;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "holiday_cache")
class HolidayCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "holiday_year", nullable = false)
    private int holidayYear;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "holiday_name", length = 150)
    private String holidayName;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    protected HolidayCache() {
    }

    HolidayCache(String countryCode, int holidayYear, LocalDate holidayDate, String holidayName,
                 LocalDateTime fetchedAt) {
        this.countryCode = countryCode;
        this.holidayYear = holidayYear;
        this.holidayDate = holidayDate;
        this.holidayName = holidayName;
        this.fetchedAt = fetchedAt;
    }

    Long getId() {
        return id;
    }

    String getCountryCode() {
        return countryCode;
    }

    int getHolidayYear() {
        return holidayYear;
    }

    LocalDate getHolidayDate() {
        return holidayDate;
    }

    String getHolidayName() {
        return holidayName;
    }

    LocalDateTime getFetchedAt() {
        return fetchedAt;
    }
}
