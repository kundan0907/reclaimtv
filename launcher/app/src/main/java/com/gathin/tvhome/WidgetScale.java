package com.gathin.tvhome;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/** The widget size choice (Launcher settings, Widget size): small, medium or large cards, weather line and screensaver widgets. */
final class WidgetScale {
    static final float[] FACTORS = {0.85f, 1f, 1.2f};
    private static final String KEY = "widget_scale";

    private WidgetScale() {}

    static int index(Context ctx) {
        int i = ctx.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE).getInt(KEY, 1);
        return i < 0 || i >= FACTORS.length ? 1 : i;
    }

    static float factor(Context ctx) {
        return FACTORS[index(ctx)];
    }

    /** Medium -> large -> small -> medium; returns the new index. */
    static int next(Context ctx) {
        int next = (index(ctx) + 1) % FACTORS.length;
        ctx.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE).edit().putInt(KEY, next).apply();
        return next;
    }

    /** Scales a view's layout size from its XML size. */
    static void apply(View v, float factor) {
        if (v == null || factor == 1f) return;
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp == null) return;
        if (lp.width > 0) lp.width = Math.round(lp.width * factor);
        if (lp.height > 0) lp.height = Math.round(lp.height * factor);
        v.setLayoutParams(lp);
    }
}
