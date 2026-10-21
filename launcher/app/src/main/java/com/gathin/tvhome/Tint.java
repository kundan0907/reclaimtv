package com.gathin.tvhome;

/** Colour helpers for the widgets' theme accent (config.json, widget_accent). */
final class Tint {
    /** The accent when a theme sets none: a calm slate blue. */
    static final int NEUTRAL = 0xFF7F93B8;

    private Tint() {}

    static int accent(int accent) {
        return accent == 0 ? NEUTRAL : accent | 0xFF000000;
    }

    /** Brand light comes from identity, never a white banner or the selected icon material. */
    static int appAccent(String pkg, int fallback) {
        switch (pkg) {
            case "com.google.android.youtube.tv":
            case "org.smarttube.stable": return 0xFFFF2438;
            case "com.netflix.ninja": return 0xFFE50914;
            case "com.spotify.tv.android": return 0xFF1DB954;
            case "com.amazon.amazonvideo.livingroom": return 0xFF009FE3;
            default: return accent(fallback);
        }
    }

    /** a*(1-t) + b*t, opaque. */
    static int mix(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
        int g = Math.round(((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
        int bl = Math.round((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    /** Text colour for a fill: dark on a bright fill, white on a dark one. */
    static int on(int fill) {
        double l = (0.299 * ((fill >> 16) & 0xFF) + 0.587 * ((fill >> 8) & 0xFF) + 0.114 * (fill & 0xFF)) / 255.0;
        return l > 0.62 ? 0xFF14171C : 0xFFFFFFFF;
    }

    static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }
}
