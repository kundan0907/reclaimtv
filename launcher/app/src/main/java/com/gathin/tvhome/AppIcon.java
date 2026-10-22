package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;

/** Theme-tinted transparent material with a monochrome app mark, on the banner's 320 × 180 canvas. */
final class AppIcon extends Drawable {
    private final Paint material = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mark = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint aura = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean focused;
    private final RectF panel = new RectF(1, 1, 319, 179);
    private final Drawable glyph;
    private final boolean prime;

    AppIcon(Context ctx, String pkg, Drawable fallback, boolean light, int accent) {
        int id = glyph(pkg);
        prime = pkg.equals("com.amazon.amazonvideo.livingroom");
        Drawable source = id != 0 ? ctx.getDrawable(id) : fallback;
        if (source instanceof AdaptiveIconDrawable) source = ((AdaptiveIconDrawable) source).getForeground();
        glyph = source == null ? null : source.getConstantState() != null
                ? source.getConstantState().newDrawable().mutate() : source.mutate();
        int ink = light ? Tint.mix(0xFF14171C, accent, 0.18f) : Tint.mix(0xFFF5F7FF, accent, 0.14f);
        if (glyph != null) {
            glyph.setTint(ink);
            glyph.setBounds(116, 46, 204, 134);
        }
        int brand = Tint.appAccent(pkg, accent);
        aura.setShader(new RadialGradient(160, 90, 76,
                new int[]{Tint.withAlpha(brand, 0x60), Tint.withAlpha(brand, 0x20), 0},
                new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
        mark.setColor(ink);
        mark.setTypeface(ctx.getResources().getFont(R.font.outfit_medium));
        mark.setTextSize(54);
        mark.setTextAlign(Paint.Align.CENTER);
        material.setShader(new LinearGradient(0, 0, 0, 180,
                Tint.withAlpha(Tint.mix(light ? 0xFFFFFFFF : 0xFF29303B, accent, 0.16f), light ? 0x90 : 0x70),
                Tint.withAlpha(Tint.mix(light ? 0xFFE9EDF6 : 0xFF11151D, accent, 0.12f), light ? 0x66 : 0x40),
                Shader.TileMode.CLAMP));
        rim.setStyle(Paint.Style.STROKE);
        rim.setStrokeWidth(1.6f);
        rim.setShader(new LinearGradient(0, 0, 320, 180,
                light ? 0x99FFFFFF : Tint.withAlpha(ink, 0x80),
                Tint.withAlpha(accent, 0x35), Shader.TileMode.CLAMP));
    }

    void setFocused(boolean focused) {
        if (this.focused == focused) return;
        this.focused = focused;
        invalidateSelf();
    }

    private static int glyph(String pkg) {
        switch (pkg) {
            case "com.google.android.youtube.tv": return R.drawable.ic_brand_youtube;
            case "com.netflix.ninja": return R.drawable.ic_brand_netflix;
            case "com.spotify.tv.android": return R.drawable.ic_brand_spotify;
            case "com.apple.atve.androidtv.appletv": return R.drawable.ic_brand_appletv;
            case "org.smarttube.stable": return R.drawable.ic_glyph_smarttube;
            case "com.community.mbox.tv": return R.drawable.ic_glyph_moviebox;
            case "tv.own.owntv": return R.drawable.ic_glyph_owntv;
            case "ar.tvplayer.tv": return R.drawable.ic_glyph_tivimate;
            case "com.esaba.downloader": return R.drawable.ic_glyph_downloader;
            case "com.android.vending": return R.drawable.ic_glyph_playstore;
            default: return pkg.startsWith("input:") ? R.drawable.ic_glyph_console : 0;
        }
    }

    @Override public void draw(Canvas c) {
        Rect b = getBounds();
        c.save();
        c.translate(b.left, b.top);
        c.scale(b.width() / 320f, b.height() / 180f);
        c.drawRoundRect(panel, 28, 28, material);
        c.drawRoundRect(panel, 28, 28, rim);
        if (prime) {
            c.drawText("prime", 160, 99, mark);
            mark.setStyle(Paint.Style.STROKE);
            mark.setStrokeWidth(3);
            mark.setStrokeCap(Paint.Cap.ROUND);
            c.drawArc(100, 82, 220, 123, 20, 138, false, mark);
            c.drawLine(213, 108, 218, 108, mark);
            c.drawLine(218, 108, 217, 114, mark);
            mark.setStyle(Paint.Style.FILL);
        } else if (glyph != null) glyph.draw(c);
        c.restore();
    }

    @Override public int getIntrinsicWidth() { return 320; }
    @Override public int getIntrinsicHeight() { return 180; }
    @Override public void setAlpha(int alpha) {
        material.setAlpha(alpha);
        rim.setAlpha(alpha);
        mark.setAlpha(alpha);
        if (glyph != null) glyph.setAlpha(alpha);
    }
    @Override public void setColorFilter(ColorFilter filter) {
        material.setColorFilter(filter);
        rim.setColorFilter(filter);
        mark.setColorFilter(filter);
        if (glyph != null) glyph.setColorFilter(filter);
    }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
