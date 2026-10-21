package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.time.LocalDate;

/**
 * The days-left-in-the-year widget (config.xml, month_card): "2026 · 80%" and "71 days left" over a dot for every
 * day, past days dim, days to come bright in the theme accent, today in ink. Compact in the top right (a weekly
 * grid, 7 rows); full size in its dialog (31 columns).
 */
public final class YearCardView extends View {
    private final GlassPanel glass = new GlassPanel();
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private boolean light, solid, big, picked;
    private int accent = Tint.NEUTRAL;
    private final float density = getResources().getDisplayMetrics().density;

    public YearCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        text.setTypeface(getResources().getFont(R.font.outfit_medium));
        text.setFontFeatureSettings("tnum");
    }

    void setAccent(int argb) { accent = Tint.accent(argb); invalidate(); }
    void setLight(boolean light) { this.light = light; invalidate(); }
    void setSolid(boolean solid) { this.solid = solid; invalidate(); }
    void setBig(boolean big) { this.big = big; invalidate(); }
    void setPicked(boolean picked) { this.picked = picked; invalidate(); }
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

    @Override
    protected void onDraw(Canvas c) {
        float inset = bare ? 0 : (big ? 12 : 8) * density;
        float k = (getWidth() - 2 * inset) / ((big ? 300 : 146) * density), d = density * k;
        float w = getWidth() - 2 * inset, h = getHeight() - 2 * inset;
        c.save();
        c.translate(inset, inset);
        rect.set(0, 0, w, h);
        if (!bare) glass.draw(c, rect, (big ? 22 : 16) * d, d, light, accent, solid, picked || isFocused());

        LocalDate today = LocalDate.now();
        int[] info = WidgetArt.yearInfo(today);
        int bright = light ? Tint.mix(accent, 0xFF14171C, 0.25f) : Tint.mix(accent, 0xFFFFFFFF, 0.30f);
        float pad = (big ? 14 : 9) * d;
        float head = (big ? 30 : 12) * d, sub = (big ? 17 : 9.5f) * d;

        text.setTextAlign(Paint.Align.LEFT);
        text.setFakeBoldText(false);
        text.setTextSize(head);
        text.setColor(ink(0xFF));
        c.drawText(String.valueOf(today.getYear()), pad, pad + head * 0.86f, text);
        text.setTextSize(sub);
        text.setColor(ink(0xC0));
        c.drawText(getContext().getString(R.string.widget_year_day, info[0], info[3]), pad, pad + head * 0.86f + sub * 1.5f, text);
        text.setTextAlign(Paint.Align.RIGHT);
        if (big) {
            text.setTextSize(head * 1.25f);
            text.setFakeBoldText(true);
            text.setColor(bright);
            c.drawText(String.valueOf(info[2]), w - pad, pad + head * 0.86f, text);
            text.setFakeBoldText(false);
            text.setTextSize(sub);
            text.setColor(ink(0xA6));
            c.drawText(getContext().getString(R.string.widget_year_left_label), w - pad, pad + head * 0.86f + sub * 1.5f, text);
        } else {
            text.setTextSize(sub);
            text.setColor(ink(0xB0));
            String label = getContext().getString(R.string.widget_year_left_label);
            c.drawText(label, w - pad, pad + head * 0.86f, text);
            float lw = text.measureText(label + " ");
            text.setTextSize(head);
            text.setFakeBoldText(true);
            text.setColor(bright);
            c.drawText(String.valueOf(info[2]), w - pad - lw, pad + head * 0.86f, text);
            text.setFakeBoldText(false);
        }

        // One dot per day. Compact: 7 rows by weeks; full size: 31 columns by days.
        float top = pad + head * 0.86f + sub * (big ? 2.4f : 2.1f), bottom = h - pad;
        int cols = big ? 31 : (info[1] + 6) / 7, rows = big ? (info[1] + 30) / 31 : 7;
        float px = (w - 2 * pad) / cols, py = (bottom - top) / rows;
        float r = Math.min(px, py) * (big ? 0.34f : 0.32f);
        for (int i = 0; i < info[1]; i++) {
            float cx = pad + px * ((big ? i % 31 : i / 7) + 0.5f), cy = top + py * ((big ? i / 31 : i % 7) + 0.5f);
            if (i + 1 < info[0]) {
                dot.setColor(Tint.withAlpha(bright, light ? 0x44 : 0x55));
                c.drawCircle(cx, cy, r, dot);
            } else if (i + 1 == info[0]) {
                dot.setColor(Tint.withAlpha(bright, 0x66));
                c.drawCircle(cx, cy, r * 2.0f, dot);
                dot.setColor(ink(0xFF));
                c.drawCircle(cx, cy, r * 1.25f, dot);
            } else {
                dot.setColor(bright);
                c.drawCircle(cx, cy, r, dot);
            }
        }
        c.restore();
    }
}
