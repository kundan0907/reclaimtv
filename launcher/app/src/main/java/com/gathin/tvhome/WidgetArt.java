package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * The widget row's tiles, drawn on a 320x180 design canvas like the app banners: Today (the date at a glance) and
 * Weather. Sizes keep the smallest text at about 12dp on the TV so it reads from the sofa.
 */
abstract class WidgetArt extends Drawable {
    static final float W = 320f, H = 180f;
    protected final boolean light;
    /** The theme accent (opaque), tinting the tile and its marks. */
    protected final int accent;
    protected final Typeface medium, regular;
    private final Drawable glassBase;
    protected final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);

    WidgetArt(Context ctx, boolean light, int accent) {
        this.light = light;
        this.accent = Tint.accent(accent);
        glassBase = new AppIcon(ctx, "widget", null, light, accent);
        medium = ctx.getResources().getFont(R.font.outfit_medium);
        regular = ctx.getResources().getFont(R.font.outfit_regular);
    }

    protected int ink(int alpha) {
        return (alpha << 24) | (light ? 0x14171C : 0xFFFFFF);
    }

    @Override
    public void draw(Canvas c) {
        float s = getBounds().width() / W;
        glassBase.setBounds(getBounds());
        glassBase.draw(c);
        c.save();
        c.translate(getBounds().left, getBounds().top);
        c.scale(s, s);
        paint(c);
        c.restore();
    }

    abstract void paint(Canvas c);

    @Override public int getIntrinsicWidth() { return (int) W; }
    @Override public int getIntrinsicHeight() { return (int) H; }
    @Override public void setAlpha(int alpha) { }
    @Override public void setColorFilter(ColorFilter colorFilter) { }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }

    /** The temperature, conditions and today's range; "Updating" until the first reading arrives. */
    static final class Weather extends WidgetArt {
        private final Context ctx;
        private int[] reading;   // {tempF, code, highF, lowF}

        Weather(Context ctx, boolean light, int accent) {
            super(ctx, light, accent);
            this.ctx = ctx;
        }

        void set(int[] reading) {
            this.reading = reading;
            invalidateSelf();
        }

        @Override
        void paint(Canvas c) {
            text.setTypeface(medium);
            if (reading == null) {
                text.setTextSize(26);
                text.setColor(ink(0xA6));
                c.drawText(ctx.getString(R.string.widget_weather_wait), 24, 100, text);
                return;
            }
            int code = reading[1];
            Drawable icon = ctx.getDrawable(com.gathin.tvhome.Weather.icon(code)).mutate();
            icon.setTint(code <= 1 ? (light ? 0xFFD18A00 : 0xFFF2C96B) : (light ? 0xFF2A3346 : 0xFFD6DEE8));
            icon.setBounds(22, 22, 58, 58);
            icon.draw(c);
            text.setTextSize(22);
            text.setColor(ink(0xA6));
            c.drawText(com.gathin.tvhome.Weather.describe(code), 68, 49, text);
            text.setTextSize(96);
            text.setColor(ink(0xFF));
            String t = String.valueOf(reading[0]);
            c.drawText(t, 20, 132, text);
            float w = text.measureText(t);
            text.setTextSize(34);
            c.drawText("°F", 24 + w, 98, text);
            text.setTypeface(regular);
            text.setTextSize(22);
            text.setColor(ink(0xA6));
            c.drawText(ctx.getString(R.string.widget_weather_range, reading[2], reading[3]), 24, 162, text);
        }
    }

    /** {day of year, days in the year, days left, percent done} for a date. */
    static int[] yearInfo(LocalDate d) {
        int len = d.lengthOfYear(), day = d.getDayOfYear();
        return new int[]{day, len, len - day, day * 100 / len};
    }
}
