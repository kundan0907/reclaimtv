package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The current temperature (°F) and conditions under the clock (config.xml, weather). The location comes from the
 * TV's public IP through ipwho.is, once a day and rounded to 2 decimals (about 1 km); the reading comes from
 * Open-Meteo, at most every 30 minutes. Both are free and keyless. A failed fetch keeps the last reading.
 */
final class Weather {
    private static final String TAG = "TvHome.Weather";
    private static final String PREFS = "weather";
    private static final long READING_MS = 30 * 60_000L;
    private static final long LOCATION_MS = 24 * 3_600_000L;
    /** A reading older than this is hidden rather than shown as current. */
    private static final long STALE_MS = 3 * 3_600_000L;
    private static final AtomicBoolean busy = new AtomicBoolean();

    interface Listener { void onReading(int tempF, int code); }

    private Weather() {}

    /** The text for a WMO weather code, as Open-Meteo reports it. */
    static String describe(int code) {
        if (code == 0) return "Clear";
        if (code == 1) return "Mostly clear";
        if (code == 2) return "Partly cloudy";
        if (code == 3) return "Overcast";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 57) return "Drizzle";
        if (code >= 61 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Showers";
        if (code == 85 || code == 86) return "Snow showers";
        if (code >= 95) return "Thunderstorm";
        return "";
    }

    /** The icon for a WMO weather code: one of the five ic_wx_* drawables. */
    static int icon(int code) {
        if (code <= 1) return R.drawable.ic_wx_sun;
        if (code == 2 || code == 3 || code == 45 || code == 48) return R.drawable.ic_wx_cloud;
        if (code >= 95) return R.drawable.ic_wx_bolt;
        if ((code >= 71 && code <= 77) || code == 85 || code == 86) return R.drawable.ic_wx_snow;
        return R.drawable.ic_wx_rain;
    }

    /** "62°F · Partly cloudy". */
    static String line(int tempF, int code) {
        String text = describe(code);
        return text.isEmpty() ? tempF + "°F" : tempF + "°F · " + text;
    }

    /** {tempF, code, highF, lowF} from the last fetch, or null when there is none or it is stale. */
    static int[] last(Context ctx) {
        SharedPreferences p = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!p.contains("temp") || System.currentTimeMillis() - p.getLong("at", 0) >= STALE_MS) return null;
        return new int[]{p.getInt("temp", 0), p.getInt("code", 0), p.getInt("hi", p.getInt("temp", 0)), p.getInt("lo", p.getInt("temp", 0))};
    }

    /** Fetches now, ignoring the 30 minute cache. */
    static void refreshNow(Context ctx, Listener listener) {
        ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong("at", 0).apply();
        refresh(ctx, listener);
    }

    /** Hands the listener the stored reading if it is fresh enough, then fetches a new one when it is due. */
    static void refresh(Context ctx, Listener listener) {
        Context app = ctx.getApplicationContext();
        SharedPreferences prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        long at = prefs.getLong("at", 0);
        if (prefs.contains("temp") && now - at < STALE_MS) {
            listener.onReading(prefs.getInt("temp", 0), prefs.getInt("code", 0));
        }
        if ((now - at < READING_MS && prefs.contains("hi")) || !busy.compareAndSet(false, true)) return;
        Handler main = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                if (now - prefs.getLong("locAt", 0) >= LOCATION_MS || !prefs.contains("lat")) locate(prefs);
                JSONObject all = new JSONObject(get(String.format(Locale.US,
                        "https://api.open-meteo.com/v1/forecast?latitude=%.2f&longitude=%.2f"
                                + "&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min"
                                + "&forecast_days=1&temperature_unit=fahrenheit&timezone=auto",
                        Double.longBitsToDouble(prefs.getLong("lat", 0)),
                        Double.longBitsToDouble(prefs.getLong("lon", 0)))));
                JSONObject current = all.getJSONObject("current");
                JSONObject daily = all.getJSONObject("daily");
                int temp = (int) Math.round(current.getDouble("temperature_2m"));
                int code = current.getInt("weather_code");
                prefs.edit().putInt("temp", temp).putInt("code", code)
                        .putInt("hi", (int) Math.round(daily.getJSONArray("temperature_2m_max").getDouble(0)))
                        .putInt("lo", (int) Math.round(daily.getJSONArray("temperature_2m_min").getDouble(0)))
                        .putLong("at", System.currentTimeMillis()).apply();
                main.post(() -> listener.onReading(temp, code));
            } catch (Exception e) {
                Log.w(TAG, "weather not updated: " + e.getMessage());
            } finally {
                busy.set(false);
            }
        }, "weather").start();
    }

    private static void locate(SharedPreferences prefs) throws Exception {
        JSONObject where = new JSONObject(get("https://ipwho.is/?fields=success,latitude,longitude"));
        if (!where.optBoolean("success", false)) throw new java.io.IOException("no location for this address");
        prefs.edit().putLong("lat", Double.doubleToLongBits(where.getDouble("latitude")))
                .putLong("lon", Double.doubleToLongBits(where.getDouble("longitude")))
                .putLong("locAt", System.currentTimeMillis()).apply();
    }

    private static String get(String address) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(8_000);
        connection.setReadTimeout(8_000);
        try (InputStream in = connection.getInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0 && out.size() < 64 * 1024) out.write(buf, 0, n);
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            connection.disconnect();
        }
    }
}
