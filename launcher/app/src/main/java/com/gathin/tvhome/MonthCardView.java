package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * The calendar widget (config.xml, month_card): a liquid-glass card, Weekly (seven days on a gentle arc, today
 * centred) or Monthly (the grid). Today is the accent colour in a heavier weight with a soft underline, no circle.
 * Compact in the top right; full size (with the Weekly / Monthly toggle) in the Calendar dialog.
 */
public final class MonthCardView extends View {
    private static final int ROWS = 6;
    private final GlassPanel glass = new GlassPanel();
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private boolean picked;
    private boolean light;
    private boolean solid;
    private boolean weekly = true;
    private boolean showToggle;
    private int accent = Tint.NEUTRAL;
    private final float density = getResources().getDisplayMetrics().density;

    public MonthCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        text.setTypeface(getResources().getFont(R.font.outfit_medium));
        text.setFontFeatureSettings("tnum");
        text.setTextAlign(Paint.Align.CENTER);
    }

    /** How many blank cells precede day 1 when weeks start on {@code first}. */
    static int leadingBlanks(LocalDate firstOfMonth, DayOfWeek first) {
        return Math.floorMod(firstOfMonth.getDayOfWeek().getValue() - first.getValue(), 7);
    }

    /** The seven days of the Weekly strip: three before today, today, three after. */
    static LocalDate[] weekStrip(LocalDate today) {
        LocalDate[] days = new LocalDate[7];
        for (int i = 0; i < 7; i++) days[i] = today.plusDays(i - 3);
        return days;
    }

    /** How far strip position i (0..6) sits below the middle one: 0 at the centre, amp at both ends. */
    static float arcDrop(int i, float amp) {
        float t = (i - 3) / 3f;
        return amp * t * t;
    }

    void setAccent(int argb) { accent = Tint.accent(argb); invalidate(); }
    void setWeekly(boolean weekly) { this.weekly = weekly; invalidate(); }
    boolean isWeekly() { return weekly; }
    /** The full-size card in the Calendar dialog shows the Weekly / Monthly toggle. */
    void setShowToggle(boolean show) { showToggle = show; invalidate(); }
    /** An opaque panel, for the card in the dialog. */
    void setSolid(boolean solid) { this.solid = solid; invalidate(); }
    /** Dark ink on a pale glass panel (light_mode). */
    void setLight(boolean light) { this.light = light; invalidate(); }
    /** Outlines the card while it is the widget being moved. */
    void setPicked(boolean picked) { this.picked = picked; invalidate(); }
    /** Redraws for the current date; call when Home comes back and on the minute tick. */
    /** No panel of its own: ViewArt draws the glass under it. */
    private boolean bare;
    void setBare(boolean bare) { this.bare = bare; invalidate(); }

    void refresh() { invalidate(); }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, android.graphics.Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        invalidate();
    }

    private int ink(int alpha) {
        return Tint.withAlpha(light ? 0x14171C : 0xFFFFFF, alpha);
    }

    private int todayColor() {
        return light ? Tint.mix(accent, 0xFF14171C, 0.15f) : Tint.mix(accent, 0xFFFFFFFF, 0.25f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float inset = bare ? 0 : (showToggle ? 12 : 8) * density;
        float k = (getWidth() - 2 * inset) / ((showToggle ? 300 : 146) * density), d = density * k;
        float w = getWidth() - 2 * inset, h = getHeight() - 2 * inset;
        canvas.save();
        canvas.translate(inset, inset);
        rect.set(0, 0, w, h);
        if (!bare) glass.draw(canvas, rect, (showToggle ? 22 : 16) * d, d, light, accent, solid, picked || isFocused());

        LocalDate now = LocalDate.now();
        Locale locale = Locale.getDefault();
        float pad = (showToggle ? 14 : 9) * d, y = pad;
        if (showToggle) {
            drawToggle(canvas, pad, y, w - 2 * pad, 34 * d, d);
            y += 34 * d + 10 * d;
        }
        if (weekly) drawWeek(canvas, now, locale, pad, y, w, h, d);
        else if (bare) drawMonthDots(canvas, now, locale, pad, y, w, h, d);   // too small for numbers: a dot per day
        else drawMonth(canvas, now, locale, pad, y, w, h, d);
        canvas.restore();
    }

    private void drawToggle(Canvas c, float x, float y, float width, float height, float d) {
        float trackW = Math.min(width, 300 * d);
        rect.set(x, y, x + trackW, y + height);
        fill.setColor(ink(light ? 0x14 : 0x1F));
        c.drawRoundRect(rect, height / 2, height / 2, fill);
        float seg = trackW / 2;
        float px = x + (weekly ? 0 : seg);
        rect.set(px + 3 * d, y + 3 * d, px + seg - 3 * d, y + height - 3 * d);
        int pill = light ? 0xFF14171C : 0xF2FFFFFF;
        fill.setColor(pill);
        c.drawRoundRect(rect, height / 2, height / 2, fill);
        text.setTextAlign(Paint.Align.CENTER);
        text.setFakeBoldText(false);
        text.setTextSize(15 * d);
        float base = y + height / 2 + 5.2f * d;
        text.setColor(weekly ? Tint.on(pill) : ink(0xCC));
        c.drawText(getContext().getString(R.string.widget_weekly), x + seg / 2, base, text);
        text.setColor(weekly ? ink(0xCC) : Tint.on(pill));
        c.drawText(getContext().getString(R.string.widget_monthly), x + seg * 1.5f, base, text);
    }

    private void head(Canvas c, String left, String right, float pad, float baseline, float size, float w) {
        text.setFakeBoldText(false);
        text.setTextSize(size);
        text.setColor(ink(0xFF));
        text.setTextAlign(Paint.Align.LEFT);
        c.drawText(left, pad, baseline, text);
        if (right != null) {
            text.setTextAlign(Paint.Align.RIGHT);
            c.drawText(right, w - pad, baseline, text);
        }
    }

    /** A day number: today in the accent, heavier, with a soft underline. */
    private void dayNumber(Canvas c, int day, boolean isToday, float cx, float baseline, float size, float fade, float d) {
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(size);
        text.setFakeBoldText(isToday);
        text.setColor(isToday ? todayColor() : ink((int) (0xE0 * fade)));
        c.drawText(String.valueOf(day), cx, baseline, text);
        if (isToday) {
            fill.setColor(Tint.withAlpha(todayColor(), 0xB0));
            float half = size * 0.42f;
            c.drawRoundRect(cx - half, baseline + 2.6f * d, cx + half, baseline + 4.2f * d, d, d, fill);
        }
        text.setFakeBoldText(false);
    }

    private void drawWeek(Canvas c, LocalDate now, Locale l, float pad, float y, float w, float h, float d) {
        float headSize = (showToggle ? 46 : 15) * d;
        head(c, now.getMonth().getDisplayName(TextStyle.FULL, l), String.valueOf(now.getDayOfMonth()), pad,
                y + headSize * 0.92f, headSize, w);
        float top = y + headSize * 1.25f, bottom = h - pad;
        float rows = bottom - top;
        float amp = showToggle ? rows * 0.10f : 3 * d;
        float labelY = top + rows * (showToggle ? 0.34f : 0.30f);
        float numY = top + rows * (showToggle ? 0.80f : 0.78f);
        float colW = (w - 2 * pad) / 7;
        LocalDate[] days = weekStrip(now);
        for (int i = 0; i < 7; i++) {
            float cx = pad + colW * (i + 0.5f), drop = arcDrop(i, amp);
            float fade = i == 0 || i == 6 ? 0.42f : i == 1 || i == 5 ? 0.72f : 1f;
            text.setTextAlign(Paint.Align.CENTER);
            text.setFakeBoldText(false);
            text.setTextSize((showToggle ? 13.5f : 9.5f) * d);
            text.setColor(ink((int) (0xB0 * fade)));
            c.drawText(days[i].getDayOfWeek().getDisplayName(showToggle ? TextStyle.SHORT : TextStyle.NARROW, l),
                    cx, labelY + drop, text);
            dayNumber(c, days[i].getDayOfMonth(), i == 3, cx, numY + drop, (showToggle ? 20 : 12.5f) * d, fade, d);
        }
    }

    /** The month as a tile: its name, the weekday letters and one dot per day, today lit. */
    private void drawMonthDots(Canvas c, LocalDate now, Locale l, float pad, float y, float w, float h, float d) {
        YearMonth month = YearMonth.from(now);
        DayOfWeek first = WeekFields.of(l).getFirstDayOfWeek();
        float headSize = 14 * d;
        head(c, month.getMonth().getDisplayName(TextStyle.FULL, l), String.valueOf(month.getYear()), pad,
                y + headSize * 0.92f, headSize, w);
        float labelY = y + headSize * 1.35f + 8 * d;
        float colW = (w - 2 * pad) / 7;
        text.setTextAlign(Paint.Align.CENTER);
        text.setFakeBoldText(false);
        text.setTextSize(9 * d);
        text.setColor(ink(0x90));
        for (int col = 0; col < 7; col++) {
            c.drawText(first.plus(col).getDisplayName(TextStyle.NARROW, l), pad + colW * (col + 0.5f), labelY, text);
        }
        float top = labelY + 5 * d, rowH = (h - pad - top) / ROWS;
        float r = Math.min(colW, rowH) * 0.30f;
        int cell = leadingBlanks(month.atDay(1), first);
        for (int day = 1; day <= month.lengthOfMonth(); day++, cell++) {
            float cx = pad + colW * (cell % 7 + 0.5f), cy = top + rowH * (cell / 7 + 0.5f);
            if (day == now.getDayOfMonth()) {
                fill.setColor(Tint.withAlpha(todayColor(), 0x66));
                c.drawCircle(cx, cy, r * 2.1f, fill);
                fill.setColor(todayColor());
                c.drawCircle(cx, cy, r * 1.35f, fill);
            } else {
                fill.setColor(ink(day < now.getDayOfMonth() ? 0x40 : 0xB0));
                c.drawCircle(cx, cy, r, fill);
            }
        }
    }

    private void drawMonth(Canvas c, LocalDate now, Locale l, float pad, float y, float w, float h, float d) {
        YearMonth month = YearMonth.from(now);
        DayOfWeek first = WeekFields.of(l).getFirstDayOfWeek();
        float headSize = (showToggle ? 28 : 13) * d;
        head(c, month.getMonth().getDisplayName(TextStyle.FULL, l), String.valueOf(month.getYear()), pad,
                y + headSize * 0.92f, headSize, w);
        float gridTop = y + headSize * (showToggle ? 1.3f : 1.35f);
        float weekH = (showToggle ? 24 : 11) * d;
        float rowH = (h - pad - gridTop - weekH) / ROWS;
        float colW = (w - 2 * pad) / 7;
        text.setTextAlign(Paint.Align.CENTER);
        text.setFakeBoldText(false);
        text.setTextSize((showToggle ? 13 : 9) * d);
        text.setColor(ink(0x80));
        for (int col = 0; col < 7; col++) {
            c.drawText(first.plus(col).getDisplayName(TextStyle.NARROW, l), pad + colW * (col + 0.5f),
                    gridTop + weekH * 0.72f, text);
        }
        int cell = leadingBlanks(month.atDay(1), first);
        for (int day = 1; day <= month.lengthOfMonth(); day++, cell++) {
            float cx = pad + colW * (cell % 7 + 0.5f);
            float cy = gridTop + weekH + rowH * (cell / 7) + rowH / 2;
            dayNumber(c, day, day == now.getDayOfMonth(), cx, cy + (showToggle ? 5.6f : 3.2f) * d,
                    (showToggle ? 16 : 10.5f) * d, 1f, d);
        }
    }
}
