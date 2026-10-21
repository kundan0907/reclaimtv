package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RecentArtTest {
    @Test public void minutesHoursAndDays() {
        assertEquals("1m ago", RecentArt.shortAgo(10_000L));
        assertEquals("5m ago", RecentArt.shortAgo(5 * 60_000L));
        assertEquals("3h ago", RecentArt.shortAgo(3 * 3_600_000L + 120_000L));
        assertEquals("2d ago", RecentArt.shortAgo(49L * 3_600_000L));
    }
}
