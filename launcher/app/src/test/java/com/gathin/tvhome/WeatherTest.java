package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WeatherTest {
    @Test public void describesTheCommonCodes() {
        assertEquals("Clear", Weather.describe(0));
        assertEquals("Partly cloudy", Weather.describe(2));
        assertEquals("Rain", Weather.describe(63));
        assertEquals("Snow", Weather.describe(73));
        assertEquals("Thunderstorm", Weather.describe(96));
    }

    @Test public void lineJoinsTemperatureAndConditions() {
        assertEquals("62°F · Partly cloudy", Weather.line(62, 2));
        assertEquals("-3°F", Weather.line(-3, 4));   // an unknown code still shows the temperature
    }
}
