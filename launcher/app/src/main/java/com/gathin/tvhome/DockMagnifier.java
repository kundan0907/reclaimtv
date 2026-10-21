package com.gathin.tvhome;

import android.animation.ValueAnimator;
import android.view.animation.PathInterpolator;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Dock magnification as one smooth wave: a single focus position glides between tiles and every dock tile is scaled
 * from its distance to it, so tiles never restart their own animations or fight each other. The same position drives
 * the coloured light behind the dock (DockDecoration), with a slower copy trailing it for a flowing look.
 */
final class DockMagnifier {
    private static final float SIGMA = 0.75f, AMOUNT = 0.20f;

    private final RecyclerView grid;
    private final AppAdapter adapter;
    private final DockDecoration decoration;
    private final int columns;
    private final float density;
    private final boolean reduced;
    private final PathInterpolator ease = new PathInterpolator(0.2f, 0.9f, 0.3f, 1f);
    private float focus = -1f, lag = -1f, amp;
    private ValueAnimator focusAnim, lagAnim, ampAnim;
    private final int[] colors;

    DockMagnifier(RecyclerView grid, AppAdapter adapter, DockDecoration decoration, int columns, float density,
                  boolean reduced) {
        this.grid = grid;
        this.adapter = adapter;
        this.decoration = decoration;
        this.columns = columns;
        this.density = density;
        this.reduced = reduced;
        this.colors = new int[columns];
    }

    /** The dock tile at this position took focus, or -1 when focus left the dock. */
    void focus(int pos) {
        if (pos < 0 || pos >= columns) {
            animateAmp(0f);
            return;
        }
        for (int i = 0; i < columns; i++) colors[i] = adapter.ambientColorAt(i);
        if (focus < 0f || amp < 0.02f || reduced) {
            cancel();
            focus = lag = pos;
            if (reduced) amp = 1f;
        } else {
            focusAnim = glide(focus, pos, 280, v -> focus = v, focusAnim);
            lagAnim = glide(lag, pos, 760, v -> lag = v, lagAnim);
        }
        animateAmp(1f);
        apply();
    }

    private interface Setter { void set(float v); }

    private ValueAnimator glide(float from, float to, long ms, Setter setter, ValueAnimator old) {
        if (old != null) old.cancel();
        ValueAnimator a = ValueAnimator.ofFloat(from, to);
        a.setDuration(ms);
        a.setInterpolator(ease);
        a.addUpdateListener(v -> {
            setter.set((float) v.getAnimatedValue());
            apply();
        });
        a.start();
        return a;
    }

    private void animateAmp(float to) {
        if (ampAnim != null) ampAnim.cancel();
        if (reduced) {
            amp = to;
            apply();
            return;
        }
        ampAnim = ValueAnimator.ofFloat(amp, to);
        ampAnim.setDuration(to > amp ? 260 : 220);
        ampAnim.addUpdateListener(v -> {
            amp = (float) v.getAnimatedValue();
            apply();
        });
        ampAnim.start();
    }

    private void cancel() {
        if (focusAnim != null) focusAnim.cancel();
        if (lagAnim != null) lagAnim.cancel();
    }

    private void apply() {
        for (int i = 0; i < columns; i++) {
            RecyclerView.ViewHolder vh = grid.findViewHolderForAdapterPosition(i);
            if (!(vh instanceof AppAdapter.Holder)) continue;
            AppAdapter.Holder h = (AppAdapter.Holder) vh;
            float d = i - focus;
            float g = focus < 0f ? 0f : (float) Math.exp(-d * d / (2f * SIGMA * SIGMA));
            float s = 1f + AMOUNT * amp * g;
            h.card.setScaleX(s);
            h.card.setScaleY(s);
            h.name.setTranslationY(density * (-6f + 6f * amp * g) + (s - 1f) * h.card.getHeight() / 2f);
        }
        decoration.setGlow(focus, lag, amp, colors);
        grid.invalidate();
    }
}
