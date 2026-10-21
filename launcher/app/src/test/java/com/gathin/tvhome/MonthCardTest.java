package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.Test;

public class MonthCardTest {
    @Test public void october2026StartsOnThursday() {
        LocalDate first = LocalDate.of(2026, 10, 1);
        assertEquals(4, MonthCardView.leadingBlanks(first, DayOfWeek.SUNDAY));
        assertEquals(3, MonthCardView.leadingBlanks(first, DayOfWeek.MONDAY));
    }

    @Test public void aMonthStartingOnTheFirstWeekdayHasNoBlanks() {
        assertEquals(0, MonthCardView.leadingBlanks(LocalDate.of(2026, 11, 1), DayOfWeek.SUNDAY));
    }
}
