package com.holidayaware.scheduler.holiday;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class WorkingDayCalculatorTest {

    private static final Set<LocalDate> NO_HOLIDAYS = Set.of();

    @Test
    @DisplayName("counts a plain Monday to Friday as five working days")
    void countsFullWeekdayRun() {
        int workingDays = WorkingDayCalculator.countWorkingDays(
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 9), NO_HOLIDAYS);

        assertThat(workingDays).isEqualTo(5);
    }

    @Test
    @DisplayName("ignores the weekend inside a full Monday to Sunday week")
    void excludesWeekends() {
        int workingDays = WorkingDayCalculator.countWorkingDays(
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 11), NO_HOLIDAYS);

        assertThat(workingDays).isEqualTo(5);
    }

    @Test
    @DisplayName("counts a weekend-only window as zero")
    void weekendOnlyWindowHasNoWorkingDays() {
        int workingDays = WorkingDayCalculator.countWorkingDays(
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 11), NO_HOLIDAYS);

        assertThat(workingDays).isZero();
    }

    @Test
    @DisplayName("subtracts a holiday that falls on a weekday")
    void subtractsWeekdayHoliday() {
        int workingDays = WorkingDayCalculator.countWorkingDays(
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 9),
                Set.of(LocalDate.of(2026, 1, 7)));

        assertThat(workingDays).isEqualTo(4);
    }

    @Test
    @DisplayName("does not subtract a holiday that falls on a Saturday")
    void doesNotDoubleCountWeekendHoliday() {
        int workingDays = WorkingDayCalculator.countWorkingDays(
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 11),
                Set.of(LocalDate.of(2026, 1, 10)));

        assertThat(workingDays).isEqualTo(5);
    }

    @Test
    @DisplayName("ignores holidays that fall outside the window")
    void ignoresHolidaysOutsideWindow() {
        int workingDays = WorkingDayCalculator.countWorkingDays(
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 9),
                Set.of(LocalDate.of(2026, 2, 2)));

        assertThat(workingDays).isEqualTo(5);
    }

    @Test
    @DisplayName("counts a single weekday as one and a single Sunday as zero")
    void handlesSingleDayWindows() {
        assertThat(WorkingDayCalculator.countWorkingDays(
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 5), NO_HOLIDAYS)).isEqualTo(1);

        assertThat(WorkingDayCalculator.countWorkingDays(
                LocalDate.of(2026, 1, 11), LocalDate.of(2026, 1, 11), NO_HOLIDAYS)).isZero();
    }

    @Test
    @DisplayName("returns zero rather than a negative count for an inverted window")
    void invertedWindowIsZero() {
        int workingDays = WorkingDayCalculator.countWorkingDays(
                LocalDate.of(2026, 1, 31), LocalDate.of(2026, 1, 1), NO_HOLIDAYS);

        assertThat(workingDays).isZero();
    }

    @Test
    @DisplayName("spans a year boundary")
    void spansYearBoundary() {
        int workingDays = WorkingDayCalculator.countWorkingDays(
                LocalDate.of(2026, 12, 28), LocalDate.of(2027, 1, 1),
                Set.of(LocalDate.of(2027, 1, 1)));

        assertThat(workingDays).isEqualTo(4);
    }

    @Test
    @DisplayName("suggests the day the required working days are reached")
    void suggestsEarliestDueDate() {
        Optional<LocalDate> suggestion = WorkingDayCalculator.earliestDueDate(
                LocalDate.of(2026, 1, 5), 5, NO_HOLIDAYS, LocalDate.of(2026, 3, 1));

        assertThat(suggestion).contains(LocalDate.of(2026, 1, 9));
    }

    @Test
    @DisplayName("pushes the suggestion past a weekend")
    void suggestionSkipsWeekend() {
        Optional<LocalDate> suggestion = WorkingDayCalculator.earliestDueDate(
                LocalDate.of(2026, 1, 5), 6, NO_HOLIDAYS, LocalDate.of(2026, 3, 1));

        assertThat(suggestion).contains(LocalDate.of(2026, 1, 12));
    }

    @Test
    @DisplayName("pushes the suggestion past a holiday")
    void suggestionSkipsHoliday() {
        Optional<LocalDate> suggestion = WorkingDayCalculator.earliestDueDate(
                LocalDate.of(2026, 1, 5), 5, Set.of(LocalDate.of(2026, 1, 7)),
                LocalDate.of(2026, 3, 1));

        assertThat(suggestion).contains(LocalDate.of(2026, 1, 12));
    }

    @Test
    @DisplayName("lands on a working day, never a weekend")
    void suggestionIsAlwaysAWorkingDay() {
        Optional<LocalDate> suggestion = WorkingDayCalculator.earliestDueDate(
                LocalDate.of(2026, 1, 5), 10, NO_HOLIDAYS, LocalDate.of(2026, 3, 1));

        assertThat(suggestion).isPresent();
        assertThat(WorkingDayCalculator.isWorkingDay(suggestion.get(), NO_HOLIDAYS)).isTrue();
    }

    @Test
    @DisplayName("gives up when the horizon is too short")
    void suggestionRespectsHorizon() {
        Optional<LocalDate> suggestion = WorkingDayCalculator.earliestDueDate(
                LocalDate.of(2026, 1, 5), 20, NO_HOLIDAYS, LocalDate.of(2026, 1, 9));

        assertThat(suggestion).isEmpty();
    }
}
