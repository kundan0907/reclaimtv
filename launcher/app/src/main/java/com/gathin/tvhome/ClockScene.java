package com.gathin.tvhome;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Random;

/**
 * The clock screensaver's scene (layout dream_clock): the thin clock and date on black over a slow ambient
 * glow, or the configured local wallpaper. The clock and configured widgets drift together every minute.
 * Shared by ClockDream (the system screensaver) and ClockActivity (Home left idle).
 */
final class ClockScene {

    private static final long MOVE_MS = 60_000L;
    /** Deep, low-brightness glow colours; the glow is drawn at partial alpha on black. */
    private static final int[] PALETTE = {0xFF2B5CFF, 0xFF00A39A, 0xFF8A3FFC, 0xFFE0661B, 0xFFD6336C};

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final View content;
    private final GradientDrawable glow;
    private final MonthCardView monthCard;
    private final YearCardView yearCard;
    private final TextView weatherLine;
    private final boolean light;
    private final boolean ambientShown;
    private boolean running;
    private Runnable onThemeChanged;
    private ValueAnimator glowAnimator;
    private int colorIndex;
    private int glowColor;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            if (onThemeChanged != null && ThemeCycle.advanceIfDue(content.getContext())) {
                onThemeChanged.run();
                return;
            }
            refreshWidgets();
            moveClock();
            if (ambientShown) shiftGlow();
            handler.postDelayed(this, MOVE_MS);
        }
    };

    private final Runnable enter = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            placeClock();
            content.animate().alpha(1f).setDuration(1200).start();
        }
    };

    /** root is the inflated dream_clock layout. */
    ClockScene(View root) {
        content = root.findViewById(R.id.dreamContent);
        ThemeCycle.advanceIfDue(root.getContext());   // theme rotation: show the next theme each time the screensaver starts
        Config config = Config.load(root.getContext());
        ((View) content.getParent()).setBackgroundColor(config.backgroundColor);
        light = config.lightMode;
        int ink = light ? 0x14171C : 0xFFFFFF;
        ((TextView) root.findViewById(R.id.dreamTime)).setTextColor(0xE6000000 | ink);
        ((TextView) root.findViewById(R.id.dreamAmPm)).setTextColor(0x99000000 | ink);
        ((TextView) root.findViewById(R.id.dreamDate)).setTextColor(0xB0000000 | ink);
        monthCard = config.monthCard ? root.findViewById(R.id.dreamMonthCard) : null;
        yearCard = config.monthCard ? root.findViewById(R.id.dreamYearCard) : null;
        if (monthCard != null) {
            root.findViewById(R.id.dreamWidgets).setVisibility(View.VISIBLE);
            monthCard.setAccent(config.widgetAccent);
            monthCard.setLight(light);
            monthCard.setWeekly(root.getContext().getSharedPreferences("widgets", Context.MODE_PRIVATE)
                    .getBoolean("calWeekly", true));
            yearCard.setAccent(config.widgetAccent);
            yearCard.setLight(light);
        }
        float widgetScale = WidgetScale.factor(root.getContext());
        if (monthCard != null) {
            WidgetScale.apply(monthCard, widgetScale);
            WidgetScale.apply(yearCard, widgetScale);
        }
        weatherLine = config.weather ? root.findViewById(R.id.dreamWeather) : null;
        if (weatherLine != null) weatherLine.setTextSize(18 * widgetScale);
        if (weatherLine != null) weatherLine.setTextColor(0xE6000000 | ink);
        // The screensaver holds the first wallpaper; closing stops its unused rotation worker.
        new WallpaperPlayer(root.getContext(), (FrameLayout) content.getParent(), config).close();
        View shade = root.findViewById(R.id.dreamShade);
        if (config.wallpaperEnabled && !config.wallpapers.isEmpty()) {
            shade.setVisibility(View.VISIBLE);
            // Dark (or pale) on the left where the clock and widgets sit, clear on the right where the picture shows.
            int veil = light ? 0xFFFFFF : 0x000000;
            shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{0xD8000000 | veil, 0x80000000 | veil, 0x14000000 | veil}));
        }
        View ambient = root.findViewById(R.id.dreamAmbient);
        ambientShown = !config.wallpaperEnabled || config.wallpapers.isEmpty();
        if (!ambientShown) ambient.setVisibility(View.GONE);
        glow = new GradientDrawable();
        glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        ambient.setBackground(glow);
        ambient.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) ->
                glow.setGradientRadius((r - l) * 0.55f));
        colorIndex = random.nextInt(PALETTE.length);
        setGlow(PALETTE[colorIndex]);
    }

    /** Called when a rotated theme replaces this one while the screensaver runs: the owner rebuilds the scene. */
    void setOnThemeChanged(Runnable r) {
        onThemeChanged = r;
    }

    void start() {
        stop();
        running = true;
        refreshWidgets();
        content.setAlpha(0f);
        content.post(enter);
        handler.postDelayed(tick, MOVE_MS);
    }

    void stop() {
        running = false;
        handler.removeCallbacks(tick);
        content.removeCallbacks(enter);
        content.animate().cancel();
        if (glowAnimator != null) {
            glowAnimator.cancel();
            glowAnimator = null;
        }
    }

    private void refreshWidgets() {
        if (monthCard != null) {
            monthCard.refresh();
            yearCard.refresh();
        }
        if (weatherLine == null) return;
        // Reuse Home's fresh reading; the screensaver never starts a location or weather request.
        int[] reading = Weather.last(content.getContext());
        weatherLine.setVisibility(reading == null ? View.GONE : View.VISIBLE);
        if (reading == null) return;
        int icon = Weather.icon(reading[1]);
        weatherLine.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);
        if (light) weatherLine.setCompoundDrawableTintList(ColorStateList.valueOf(
                icon == R.drawable.ic_wx_sun ? 0xFFD18A00 : 0xFF2A3346));
        weatherLine.setText(Weather.line(reading[0], reading[1]));
    }

    private void moveClock() {
        content.animate().alpha(0f).setDuration(900).withEndAction(() -> {
            if (!running) return;
            placeClock();
            content.animate().alpha(1f).setDuration(1200).start();
        }).start();
    }

    /** The group stays at the top left; only a few dp of drift each minute protect the screen. */
    private void placeClock() {
        View parent = (View) content.getParent();
        float d = content.getResources().getDisplayMetrics().density;
        content.setTranslationX(random.nextFloat() * 18 * d);
        content.setTranslationY(random.nextFloat() * 14 * d);
        if (parent.getWidth() > 0 && parent.getHeight() > 0) glow.setGradientCenter(0.72f, 0.45f);
    }

    private void shiftGlow() {
        int from = glowColor;
        colorIndex = (colorIndex + 1) % PALETTE.length;
        if (glowAnimator != null) glowAnimator.cancel();
        glowAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), from, PALETTE[colorIndex]);
        glowAnimator.setDuration(8000);
        glowAnimator.addUpdateListener(a -> setGlow((int) a.getAnimatedValue()));
        glowAnimator.start();
    }

    private void setGlow(int color) {
        glowColor = color;
        int c = color & 0x00FFFFFF;
        glow.setColors(new int[]{0x40000000 | c, 0x14000000 | c, 0x00000000 | c});
    }
}
