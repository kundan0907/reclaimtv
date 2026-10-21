package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class AppAccentTest {
    @Test public void majorBrandsNeverUseTheWhiteArtworkAverage() {
        assertEquals(0xFFFF2438, Tint.appAccent("com.google.android.youtube.tv", 0xFFFFFFFF));
        assertEquals(0xFFE50914, Tint.appAccent("com.netflix.ninja", 0xFFFFFFFF));
        assertEquals(0xFF1DB954, Tint.appAccent("com.spotify.tv.android", 0xFFFFFFFF));
        assertEquals(0xFF009FE3, Tint.appAccent("com.amazon.amazonvideo.livingroom", 0xFFFFFFFF));
        assertEquals(0xFFFF2438, Tint.appAccent("org.smarttube.stable", 0xFFFFFFFF));
    }

    @Test public void unknownBrandsKeepTheirSampleOrTheThemeFallback() {
        assertEquals(0xFF9B6BFF, Tint.appAccent("input:PlayStation", 0xFF9B6BFF));
        assertEquals(0xFF12765A, Tint.appAccent("other.app", 0xFF12765A));
        assertEquals(Tint.NEUTRAL, Tint.appAccent("other.app", 0));
    }
}
