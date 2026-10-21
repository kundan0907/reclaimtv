package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.time.LocalDate;
import org.junit.Test;

public class CalendarLogicTest {
    @Test public void weekStripCentresOnToday() {
        LocalDate today = LocalDate.of(2026, 10, 21);
        LocalDate[] days = MonthCardView.weekStrip(today);
        assertEquals(7, days.length);
        assertEquals(LocalDate.of(2026, 10, 18), days[0]);
        assertEquals(today, days[3]);
        assertEquals(LocalDate.of(2026, 10, 24), days[6]);
    }

    @Test public void weekStripCrossesMonthEnds() {
        assertEquals(LocalDate.of(2026, 12, 29), MonthCardView.weekStrip(LocalDate.of(2027, 1, 1))[0]);
    }

    @Test public void arcIsLowestAtTheEnds() {
        assertEquals(0f, MonthCardView.arcDrop(3, 10f), 0f);
        assertEquals(10f, MonthCardView.arcDrop(0, 10f), 0.001f);
        assertEquals(10f, MonthCardView.arcDrop(6, 10f), 0.001f);
        assertEquals(MonthCardView.arcDrop(1, 10f), MonthCardView.arcDrop(5, 10f), 0.001f);
    }

    @Test public void yearInfoForAnOrdinaryYear() {
        int[] i = WidgetArt.yearInfo(LocalDate.of(2026, 1, 18));
        assertEquals(18, i[0]);
        assertEquals(365, i[1]);
        assertEquals(347, i[2]);
        assertEquals(4, i[3]);
    }

    @Test public void yearInfoForALeapYearsLastDay() {
        int[] i = WidgetArt.yearInfo(LocalDate.of(2028, 12, 31));
        assertEquals(366, i[1]);
        assertEquals(0, i[2]);
        assertEquals(100, i[3]);
    }

    @Test public void onPicksAReadableTextColour() {
        assertEquals(0xFFFFFFFF, Tint.on(0xFF1C2230));
        assertEquals(0xFF14171C, Tint.on(0xFFF2C96B));
        assertNotEquals(Tint.NEUTRAL, Tint.accent(0xFF3FD0C0));
        assertEquals(Tint.NEUTRAL, Tint.accent(0));
    }
}
