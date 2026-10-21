package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * The liquid-glass slab of the top-right widgets, drawn like the dock band: a faint halo, a translucent fill tinted
 * by the theme accent, a sheen over the upper part and a lit edge. Light mode swaps to pale glass.
 */
final class GlassPanel {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sheen = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edge = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint halo = new Paint(Paint.ANTI_ALIAS_FLAG);

    GlassPanel() {
        edge.setStyle(Paint.Style.STROKE);
        halo.setStyle(Paint.Style.STROKE);
    }

    /** r is the slab; the halo is drawn outside it, so leave room (about 8dp) inside the view. */
    void draw(Canvas c, RectF r, float radius, float d, boolean light, int accent, boolean solid, boolean lit) {
        for (int i = 0; i < 6; i++) {
            float o = (i + 0.5f) * 1.3f * d;
            halo.setStrokeWidth(1.3f * d + 0.5f);
            halo.setColor(Math.round((light ? 0x22 : 0x30) * (1f - i / 6f)) << 24 | (light ? 0x1F2A44 : 0x000000));
            c.drawRoundRect(r.left - o, r.top - o, r.right + o, r.bottom + o, radius + o, radius + o, halo);
        }
        int top = light ? Tint.mix(0xFFFFFFFF, accent, 0.10f) : Tint.mix(0xFF20242C, accent, 0.22f);
        int bottom = light ? Tint.mix(0xFFF4F7FB, accent, 0.14f) : Tint.mix(0xFF101216, accent, 0.16f);
        fill.setShader(new LinearGradient(0, r.top, 0, r.bottom,
                Tint.withAlpha(top, solid ? 0xFF : light ? 0xC8 : 0x84),
                Tint.withAlpha(bottom, solid ? 0xFF : light ? 0x90 : 0x66), Shader.TileMode.CLAMP));
        c.drawRoundRect(r, radius, radius, fill);
        sheen.setShader(new LinearGradient(0, r.top, 0, r.top + r.height() * 0.4f,
                light ? 0x99FFFFFF : 0x2EFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
        c.save();
        c.clipRect(r.left, r.top, r.right, r.top + r.height() * 0.4f);
        c.drawRoundRect(r, radius, radius, sheen);
        c.restore();
        if (lit) {
            edge.setShader(null);
            edge.setColor(light ? 0xFF14171C : 0xFFFFFFFF);
            edge.setStrokeWidth(2.2f * d);
        } else {
            edge.setShader(new LinearGradient(0, r.top, 0, r.bottom,
                    light ? 0x66FFFFFF : 0x80FFFFFF, light ? 0x26000000 : 0x14FFFFFF, Shader.TileMode.CLAMP));
            edge.setStrokeWidth(1.2f * d);
        }
        c.drawRoundRect(r, radius, radius, edge);
    }
}
