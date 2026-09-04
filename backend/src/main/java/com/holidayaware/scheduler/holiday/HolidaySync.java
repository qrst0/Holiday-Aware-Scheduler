package com.holidayaware.scheduler.holiday;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "holiday_sync")
@IdClass(HolidaySync.Key.class)
class HolidaySync {

    @Id
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Id
    @Column(name = "holiday_year", nullable = false)
    private int holidayYear;

    @Column(name = "last_attempt_at", nullable = false)
    private LocalDateTime lastAttemptAt;

    @Column(name = "last_success_at")
    private LocalDateTime lastSuccessAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_outcome", nullable = false, length = 10)
    private SyncOutcome lastOutcome;

    protected HolidaySync() {
    }

    HolidaySync(String countryCode, int holidayYear) {
        this.countryCode = countryCode;
        this.holidayYear = holidayYear;
    }

    LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    LocalDateTime getLastSuccessAt() {
        return lastSuccessAt;
    }

    SyncOutcome getLastOutcome() {
        return lastOutcome;
    }

    void recordAnswer(LocalDateTime at, SyncOutcome outcome) {
        this.lastAttemptAt = at;
        this.lastSuccessAt = at;
        this.lastOutcome = outcome;
    }

    void recordFailure(LocalDateTime at) {
        this.lastAttemptAt = at;
        this.lastOutcome = SyncOutcome.FAILED;
    }

    public static class Key implements Serializable {

        private String countryCode;
        private int holidayYear;

        public Key() {
        }

        public Key(String countryCode, int holidayYear) {
            this.countryCode = countryCode;
            this.holidayYear = holidayYear;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key key)) {
                return false;
            }
            return holidayYear == key.holidayYear && Objects.equals(countryCode, key.countryCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(countryCode, holidayYear);
        }
    }
}
