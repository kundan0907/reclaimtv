package com.gathin.tvhome;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * A tile that shows a View (the calendar or the year card) on a glass base: the view is drawn into a bitmap when
 * refresh() is called, so the grid never lays out or redraws the view itself.
 */
final class ViewArt extends Drawable {
    private final View view;
    private final Drawable base;
    private final Runnable prepare;
    private final Bitmap bitmap;
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final int w, h;

    ViewArt(View view, Drawable base, int w, int h, Runnable prepare) {
        this.view = view;
        this.base = base;
        this.prepare = prepare;
        this.w = w;
        this.h = h;
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        refresh();
    }

    void refresh() {
        prepare.run();
        view.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, w, h);
        bitmap.eraseColor(0);
        view.draw(new Canvas(bitmap));
        invalidateSelf();
    }

    @Override public void draw(Canvas c) {
        base.setBounds(getBounds());
        base.draw(c);
        c.drawBitmap(bitmap, null, getBounds(), paint);
    }

    @Override public int getIntrinsicWidth() { return 320; }
    @Override public int getIntrinsicHeight() { return 180; }
    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
