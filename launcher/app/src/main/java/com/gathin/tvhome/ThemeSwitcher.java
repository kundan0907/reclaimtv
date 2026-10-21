package com.gathin.tvhome;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Themes bundled in assets/themes/&lt;id&gt;/ (config.json, optional wallpaper.jpg), applied from Launcher
 * settings the way tools/theme.sh pushes one. A bundled config.json holds only look keys: they are merged
 * over the config.json already on the TV, so the dock order, hidden apps, labels and banners stay put.
 * A key a theme must clear when another one is chosen (race_calendar) is listed in every theme.
 */
final class ThemeSwitcher {
    private static final String ROOT = "themes";

    private ThemeSwitcher() {}

    static List<String> ids(Context ctx) {
        try {
            String[] names = ctx.getAssets().list(ROOT);
            if (names == null) return new ArrayList<>();
            Arrays.sort(names);
            return new ArrayList<>(Arrays.asList(names));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    static String label(Context ctx, String id) {
        try {
            return bundled(ctx, id).optString("theme_label", id);
        } catch (IOException | JSONException e) {
            return id;
        }
    }

    /** The id of the theme last applied here, or null when the TV's config.json came from somewhere else. */
    static String current(Context ctx) {
        File dir = Config.dir(ctx);
        if (dir == null) return null;
        try {
            return read(new File(dir, Config.FILE_NAME)).optString("theme", null);
        } catch (IOException | JSONException e) {
            return null;
        }
    }

    static void apply(Context ctx, String id) throws IOException, JSONException {
        File dir = Config.dir(ctx);
        if (dir == null) throw new IOException("external storage unavailable");
        JSONObject merged = new File(dir, Config.FILE_NAME).isFile() ? read(new File(dir, Config.FILE_NAME)) : new JSONObject();
        JSONObject theme = bundled(ctx, id);
        for (java.util.Iterator<String> keys = theme.keys(); keys.hasNext(); ) {
            String key = keys.next();
            merged.put(key, theme.get(key));
        }
        File wallpaper = new File(dir, Config.WALLPAPER_NAME);
        try (InputStream in = ctx.getAssets().open(ROOT + "/" + id + "/" + Config.WALLPAPER_NAME);
             FileOutputStream out = new FileOutputStream(wallpaper)) {
            copy(in, out);
        } catch (java.io.FileNotFoundException e) {
            wallpaper.delete();   // a theme without a picture shows its background colour
        }
        write(new File(dir, Config.FILE_NAME), merged.toString(2));
        // A new revision token makes Config drop the on-TV column/motion/wallpaper choices, as a pushed theme does.
        write(new File(dir, "theme_revision"), UUID.randomUUID().toString().replace("-", ""));
    }

    private static JSONObject bundled(Context ctx, String id) throws IOException, JSONException {
        try (InputStream in = ctx.getAssets().open(ROOT + "/" + id + "/" + Config.FILE_NAME)) {
            return new JSONObject(new String(readAll(in), StandardCharsets.UTF_8));
        }
    }

    private static JSONObject read(File file) throws IOException, JSONException {
        try (InputStream in = new java.io.FileInputStream(file)) {
            return new JSONObject(new String(readAll(in), StandardCharsets.UTF_8));
        }
    }

    private static void write(File file, String text) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    private static void copy(InputStream in, java.io.OutputStream out) throws IOException {
        byte[] buf = new byte[16 * 1024];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
    }
}
