package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * The picture and title of what an app last played, for its tile in the recents row. MediaWatcher saves them from
 * the app's media session; nothing leaves the TV. One small JPEG and one title per package under filesDir/thumbs.
 */
final class Thumbs {
    private static final int W = 320, H = 180;

    private Thumbs() {}

    private static File dir(Context ctx) {
        File d = new File(ctx.getFilesDir(), "thumbs");
        if (!d.isDirectory()) d.mkdirs();
        return d;
    }

    static void save(Context ctx, String pkg, Bitmap art, String title) {
        if (art == null || art.getWidth() < 2 || art.getHeight() < 2) return;
        Bitmap out = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        // Centre-crop to 16:9.
        float s = Math.max(W / (float) art.getWidth(), H / (float) art.getHeight());
        int cw = Math.round(W / s), ch = Math.round(H / s);
        Rect src = new Rect((art.getWidth() - cw) / 2, (art.getHeight() - ch) / 2,
                (art.getWidth() + cw) / 2, (art.getHeight() + ch) / 2);
        new Canvas(out).drawBitmap(art, src, new Rect(0, 0, W, H), null);
        try (FileOutputStream f = new FileOutputStream(new File(dir(ctx), pkg + ".jpg"))) {
            out.compress(Bitmap.CompressFormat.JPEG, 82, f);
            if (title != null) {
                ctx.getSharedPreferences("thumbs", Context.MODE_PRIVATE).edit().putString(pkg, title).apply();
            }
        } catch (IOException ignored) {
            // the tile keeps the app's own banner
        }
    }

    static long savedAt(Context ctx, String pkg) {
        return new File(dir(ctx), pkg + ".jpg").lastModified();
    }

    /** Packages with a saved picture, the one played most recently first. */
    static java.util.List<String> packages(Context ctx) {
        File[] files = dir(ctx).listFiles((d, n) -> n.endsWith(".jpg"));
        java.util.List<String> out = new java.util.ArrayList<>();
        if (files == null) return out;
        java.util.Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        for (File f : files) out.add(f.getName().substring(0, f.getName().length() - 4));
        return out;
    }

    /** The saved picture as a drawable, or null when none was saved for this app. */
    static Drawable load(Context ctx, String pkg) {
        File f = new File(dir(ctx), pkg + ".jpg");
        if (!f.isFile()) return null;
        Bitmap b = BitmapFactory.decodeFile(f.getPath());
        return b == null ? null : new BitmapDrawable(ctx.getResources(), b);
    }

    static String title(Context ctx, String pkg) {
        return ctx.getSharedPreferences("thumbs", Context.MODE_PRIVATE).getString(pkg, null);
    }
}
