package com.holidayaware.scheduler.holiday;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

final class WorkingDayCalculator {

    private WorkingDayCalculator() {
    }

    static int countWorkingDays(LocalDate start, LocalDate end, Set<LocalDate> holidays) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0;
        }
        int workingDays = 0;
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            if (isWorkingDay(day, holidays)) {
                workingDays++;
            }
        }
        return workingDays;
    }

    static boolean isWorkingDay(LocalDate day, Set<LocalDate> holidays) {
        return !isWeekend(day) && !holidays.contains(day);
    }

    private static boolean isWeekend(LocalDate day) {
        DayOfWeek dayOfWeek = day.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
