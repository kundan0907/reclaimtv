package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ThemeCycleTest {
    @Test public void optionsStepThroughAndWrapToOff() {
        assertEquals(15, ThemeCycle.next(0));
        assertEquals(60, ThemeCycle.next(15));
        assertEquals(1440, ThemeCycle.next(60));
        assertEquals(0, ThemeCycle.next(1440));
    }

    @Test public void anUnknownValueFallsBackToOff() {
        assertEquals(0, ThemeCycle.next(7));
    }
}
