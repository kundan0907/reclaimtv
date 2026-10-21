package com.gathin.tvhome;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.service.notification.NotificationListenerService;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the picture and title of whatever a media app is playing, for the recents tile (Thumbs). It is a
 * notification listener only because Android hands media sessions to that kind of service; it reads no
 * notifications. The access is granted on the TV (Settings > Notification access, or
 * adb shell cmd notification allow_listener com.gathin.tvhome/com.gathin.tvhome.MediaWatcher). Local only.
 */
public final class MediaWatcher extends NotificationListenerService {
    private MediaSessionManager sessions;
    private final List<MediaController> watched = new ArrayList<>();

    private final MediaSessionManager.OnActiveSessionsChangedListener onSessions = this::watch;

    @Override
    public void onListenerConnected() {
        sessions = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
        ComponentName me = new ComponentName(this, MediaWatcher.class);
        try {
            sessions.addOnActiveSessionsChangedListener(onSessions, me);
            watch(sessions.getActiveSessions(me));
        } catch (SecurityException ignored) {
            // access not granted yet
        }
    }

    @Override
    public void onListenerDisconnected() {
        if (sessions != null) sessions.removeOnActiveSessionsChangedListener(onSessions);
        watched.clear();
    }

    private void watch(List<MediaController> controllers) {
        if (controllers == null) return;
        for (MediaController c : controllers) {
            if (c.getPackageName().equals(getPackageName())) continue;
            keep(c.getPackageName(), c.getMetadata());
            watched.add(c);
            c.registerCallback(new MediaController.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadata m) {
                    keep(c.getPackageName(), m);
                }
            });
        }
    }

    private void keep(String pkg, MediaMetadata m) {
        if (m == null) return;
        Bitmap art = m.getBitmap(MediaMetadata.METADATA_KEY_ART);
        if (art == null) art = m.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
        if (art == null) art = m.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
        CharSequence title = m.getText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
        if (title == null) title = m.getText(MediaMetadata.METADATA_KEY_TITLE);
        final String name = title == null ? null : title.toString();
        java.util.List<String> have = new java.util.ArrayList<>();
        for (String k : m.keySet()) have.add(k + "=" + (m.getText(k) != null ? m.getText(k) : m.getBitmap(k) != null ? "<bitmap>" : "-"));
        android.util.Log.d("TvHomeMedia", pkg + " " + have);
        if (art != null) {
            Thumbs.save(this, pkg, art, name);
            return;
        }
        // Many apps publish a picture address instead of a bitmap: fetch that one small picture, off the main thread.
        String uri = m.getString(MediaMetadata.METADATA_KEY_ART_URI);
        if (uri == null) uri = m.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI);
        if (uri == null) uri = m.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI);
        // YouTube clients (SmartTube) publish only the video's id: YouTube's public thumbnail and title for it.
        final String id = m.getString(MediaMetadata.METADATA_KEY_MEDIA_ID);
        final boolean youtube = (uri == null || !uri.startsWith("http")) && id != null && isYoutubeClient(pkg)
                && id.matches("[A-Za-z0-9_-]{11}");
        if (youtube) uri = "https://i.ytimg.com/vi/" + id + "/mqdefault.jpg";
        if (uri == null || !uri.startsWith("http")) return;
        final String address = uri;
        if (youtube && id.equals(getSharedPreferences("thumbs", MODE_PRIVATE).getString(pkg + ".id", null))
                && Thumbs.load(this, pkg) != null) return;   // already have this video
        new Thread(() -> {
            try {
                Bitmap b;
                String shown = name;
                if (youtube && shown == null) shown = youtubeTitle(id);
                java.net.HttpURLConnection c = (java.net.HttpURLConnection) new java.net.URL(address).openConnection();
                c.setConnectTimeout(6000);
                c.setReadTimeout(6000);
                try (java.io.InputStream in = c.getInputStream()) {
                    b = android.graphics.BitmapFactory.decodeStream(in);
                } finally {
                    c.disconnect();
                }
                if (b != null) {
                    Thumbs.save(this, pkg, b, shown);
                    if (youtube) getSharedPreferences("thumbs", MODE_PRIVATE).edit().putString(pkg + ".id", id).apply();
                }
            } catch (Exception e) {
                android.util.Log.d("TvHomeMedia", "no picture for " + pkg + ": " + e.getMessage());
            }
        }, "media-art").start();
    }

    private static boolean isYoutubeClient(String pkg) {
        return "org.smarttube.stable".equals(pkg) || "com.liskovsoft.smarttubetv.beta".equals(pkg)
                || "com.google.android.youtube.tv".equals(pkg);
    }

    /** The video's title from YouTube's public oEmbed answer, or null. */
    private static String youtubeTitle(String id) {
        try {
            java.net.HttpURLConnection c = (java.net.HttpURLConnection) new java.net.URL(
                    "https://www.youtube.com/oembed?format=json&url=https://www.youtube.com/watch?v=" + id).openConnection();
            c.setConnectTimeout(6000);
            c.setReadTimeout(6000);
            try (java.io.InputStream in = c.getInputStream()) {
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) > 0 && out.size() < 32 * 1024) out.write(buf, 0, n);
                return new org.json.JSONObject(out.toString("UTF-8")).optString("title", null);
            } finally {
                c.disconnect();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
