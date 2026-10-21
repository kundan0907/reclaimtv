package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

/**
 * Theme rotation (Launcher settings, Theme rotation): every N minutes the next bundled theme is applied, so Home and
 * the screensaver keep changing. It only advances when Home starts or resumes, or while the screensaver runs, never
 * while you are using Home.
 */
final class ThemeCycle {
    /** Off, every 15 minutes, hourly, daily. */
    static final int[] OPTIONS = {0, 15, 60, 1440};
    private static final String PREFS = "theme_cycle";

    private ThemeCycle() {}

    static int minutes(Context ctx) {
        return prefs(ctx).getInt("minutes", 0);
    }

    /** The option after the current one, wrapping to off. */
    static int next(int minutes) {
        for (int i = 0; i < OPTIONS.length; i++) if (OPTIONS[i] == minutes) return OPTIONS[(i + 1) % OPTIONS.length];
        return OPTIONS[0];
    }

    static void setMinutes(Context ctx, int minutes) {
        prefs(ctx).edit().putInt("minutes", minutes).putLong("last", System.currentTimeMillis()).apply();
    }

    /** A theme was just chosen by hand: restart the timer so rotation does not replace it at once. */
    static void touch(Context ctx) {
        prefs(ctx).edit().putLong("last", System.currentTimeMillis()).apply();
    }

    static boolean due(Context ctx, long now) {
        int m = minutes(ctx);
        return m > 0 && now - prefs(ctx).getLong("last", 0) >= m * 60_000L;
    }

    /** Applies the next theme when one is due. True when the theme changed. */
    static boolean advanceIfDue(Context ctx) {
        if (!due(ctx, System.currentTimeMillis())) return false;
        List<String> ids = ThemeSwitcher.ids(ctx);
        if (ids.size() < 2) return false;
        int at = ids.indexOf(ThemeSwitcher.current(ctx));
        try {
            ThemeSwitcher.apply(ctx, ids.get((at + 1) % ids.size()));
        } catch (Exception e) {
            return false;
        }
        touch(ctx);
        return true;
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
