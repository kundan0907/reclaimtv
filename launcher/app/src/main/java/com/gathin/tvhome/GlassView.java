package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * A faint local glow for original artwork and widget tiles.
 * One instance follows the focused tile with spring animations.
 */
public class GlassView extends View {

    /** Room around the tile for the halo, in dp: MainActivity sizes this view as the tile plus this on every side. */
    static final float PAD_DP = 16f;
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean suppressed;
    private final RectF rect = new RectF();
    private final float density;
    private float radius;
    /** Glow colour as 0xRRGGBB: white normally, a festival's accent (diya gold) while a festive skin is on. */
    private int glowRgb = 0xFFFFFF;
    /** Glow strength: the white glow stays faint, a coloured one needs more alpha to read as warm light. */
    private int glowAlpha = 0x30;
    /** Dark light on a pale surface keeps focus visible. */
    private boolean light;

    public GlassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        radius = 20f * density;
        glow.setStyle(Paint.Style.STROKE);
        // Cache the glow: springs change translation without replaying its rings every frame.
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    /** Switches the neutral glow for a pale surface. */
    void setLight(boolean light) {
        this.light = light;
        setAccent(0);
    }

    void setAccent(int argb) {
        if (argb == 0) {
            glowRgb = light ? 0x1F2A44 : 0xFFFFFF;
            glowAlpha = 0x30;
        } else {
            glowRgb = argb & 0xFFFFFF;
            glowAlpha = 0x78;
        }
        invalidate();
    }

    /** Hides the halo while another light (the dock's) is on. */
    void setSuppressed(boolean suppressed) {
        if (this.suppressed == suppressed) return;
        this.suppressed = suppressed;
        invalidate();
    }

    void setRadius(float px) {
        radius = px;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (suppressed) return;
        float inset = PAD_DP * density;   // room for the glow
        rect.set(inset, inset, getWidth() - inset, getHeight() - inset);

        // Outer glow: thin rings across the inset, each a little fainter, so it fades out smoothly. Three wide
        // strokes left visible bands once the glow took an app's colour at a stronger alpha. The hardware layer
        // caches the result, so the rings cost one draw, not one per frame of the spring.
        int rings = 24;
        float step = inset / rings;
        glow.setStrokeWidth(step + 0.5f);
        for (int i = 0; i < rings; i++) {
            float o = step * (i + 0.5f);
            float t = 1f - (float) i / rings;
            glow.setColor(Math.round(glowAlpha * t) << 24 | glowRgb);
            canvas.drawRoundRect(rect.left - o, rect.top - o, rect.right + o, rect.bottom + o, radius + o, radius + o, glow);
        }
    }
}
