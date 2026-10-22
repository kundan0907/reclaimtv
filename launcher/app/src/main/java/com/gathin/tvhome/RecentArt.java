package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

/**
 * A recent-apps tile on the 320 x 180 design canvas: the app's tile or the picture of what it last played, a pill
 * that says how long ago ("Continue · 2h ago", "Recent · 3d ago") and, over a picture, the title of what was playing.
 */
final class RecentArt extends Drawable {
    private final Drawable base;
    private final String pill;
    private final String title;
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint text = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF box = new RectF();
    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF panel = new RectF(1, 1, 319, 179);
    private StaticLayout titleLayout;

    RecentArt(Context ctx, Drawable base, String pill, String title, int accent) {
        this.base = base;
        this.pill = pill;
        this.title = title;
        text.setTypeface(ctx.getResources().getFont(R.font.outfit_medium));
        // The same lit edge the other row-two and widget tiles have, so a picture tile sits in the row.
        rim.setStyle(Paint.Style.STROKE);
        rim.setStrokeWidth(2.6f);
        rim.setShader(new LinearGradient(0, 0, 320, 180, 0xE0FFFFFF, Tint.withAlpha(Tint.accent(accent), 0xC0), Shader.TileMode.CLAMP));
    }

    @Override public void draw(Canvas c) {
        Rect b = getBounds();
        c.save();
        c.translate(b.left, b.top);
        c.scale(b.width() / 320f, b.height() / 180f);
        if (base != null) {
            Rect saved = base.copyBounds();   // the base may be the very drawable another tile shows
            base.setBounds(0, 0, 320, 180);
            base.draw(c);
            base.setBounds(saved);
        }
        if (title != null) {
            fill.setShader(new LinearGradient(0, 70, 0, 180, 0x00000000, 0xD0000000, Shader.TileMode.CLAMP));
            c.drawRect(0, 70, 320, 180, fill);
            fill.setShader(null);
            if (titleLayout == null) {
                text.setTextSize(27);
                text.setColor(0xFFFFFFFF);
                titleLayout = StaticLayout.Builder.obtain(title, 0, title.length(), text, 288)
                        .setMaxLines(2).setEllipsize(TextUtils.TruncateAt.END).setAlignment(Layout.Alignment.ALIGN_NORMAL).build();
            }
            c.save();
            c.translate(16, 172 - titleLayout.getHeight());
            titleLayout.draw(c);
            c.restore();
        }
        if (!(base instanceof AppIcon)) c.drawRoundRect(panel, 28, 28, rim);
        text.setTextSize(21);
        text.setTypeface(Typeface.create(text.getTypeface(), Typeface.NORMAL));
        float pw = text.measureText(pill) + 28;
        box.set(14, 14, 14 + pw, 14 + 32);
        fill.setColor(0xA6000000);
        c.drawRoundRect(box, 16, 16, fill);
        text.setColor(0xFFFFFFFF);
        c.drawText(pill, 28, 36.5f, text);
        c.restore();
    }

    /** "5m ago", "3h ago", "2d ago" for something that happened {@code ms} ago. */
    static String shortAgo(long ms) {
        long m = Math.max(1, ms / 60_000L);
        if (m < 60) return m + "m ago";
        long h = m / 60;
        if (h < 24) return h + "h ago";
        return (h / 24) + "d ago";
    }

    @Override public int getIntrinsicWidth() { return 320; }
    @Override public int getIntrinsicHeight() { return 180; }
    @Override public void setAlpha(int alpha) { }
    @Override public void setColorFilter(ColorFilter filter) { }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
