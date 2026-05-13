package com.rextart.sys.sistemainformativo.util;

import com.rextart.sys.sistemainformativo.model.DayType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Set;

public class ItalianHolidayUtil {

    private static final Set<MonthDay> FIXED_HOLIDAYS = Set.of(
            MonthDay.of(1, 1),
            MonthDay.of(1, 6),
            MonthDay.of(4, 25),
            MonthDay.of(5, 1),
            MonthDay.of(6, 2),
            MonthDay.of(8, 15),
            MonthDay.of(11, 1),
            MonthDay.of(12, 8),
            MonthDay.of(12, 25),
            MonthDay.of(12, 26)
    );

    private ItalianHolidayUtil() {}

    public static DayType getDayType(LocalDate date) {
        if (isHoliday(date)) return DayType.HOLIDAY;
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) return DayType.SATURDAY;
        return DayType.WORKING;
    }

    public static boolean isHoliday(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) return true;
        if (FIXED_HOLIDAYS.contains(MonthDay.from(date))) return true;
        LocalDate easter = computeEaster(date.getYear());
        return date.equals(easter) || date.equals(easter.plusDays(1));
    }

    // Anonymous Gregorian algorithm
    private static LocalDate computeEaster(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}