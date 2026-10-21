package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * The band behind the first (dock) row. In glass mode it is the same liquid glass as the widgets, with coloured
 * light behind it: a bright blob under the focused tile and a slower one trailing it, so the colour flows along the
 * dock as focus moves, and shows through the glass and the translucent icons.
 */
public class DockDecoration extends RecyclerView.ItemDecoration {

    private final int dockSize;
    private final float density;
    private final Paint band = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edge = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blob = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path clip = new Path();
    private final boolean glass;
    private final boolean light;
    private final int accent;
    private final GlassPanel panel = new GlassPanel();
    /** Extra band below the tiles for their names (always_show_names), in px. */
    private final float namesRoom;

    // Light behind the dock, driven by DockMagnifier: fractional dock positions, strength 0..1, colour per tile.
    private float glowFocus = -1f, glowLag = -1f, glowAmp;
    private int[] glowColors = new int[0];
    private final float[] centers = new float[16];

    /**
     * @param translucent the cinematic shelf's band: the artwork and the glow show through it.
     * @param names       the dock tiles show their names, so the band reaches down over them.
     */
    DockDecoration(int dockSize, float density, boolean translucent, boolean names, boolean light, boolean glass,
                   int accent) {
        this.glass = glass;
        this.light = light;
        this.accent = Tint.accent(accent);
        this.dockSize = dockSize;
        this.density = density;
        this.namesRoom = names ? 22 * density : 0;
        band.setColor(light ? (translucent ? 0x8CFFFFFF : 0xFFE9ECF1) : translucent ? 0x8C121014 : 0xFF141416);
        edge.setStyle(Paint.Style.STROKE);
        edge.setStrokeWidth(density);
        edge.setColor(light ? 0x26000000 : translucent ? 0x26FFFFFF : 0x1FFFFFFF);
    }

    void setGlow(float focus, float lag, float amp, int[] colors) {
        glowFocus = focus;
        glowLag = lag;
        glowAmp = amp;
        glowColors = colors;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        int pos = parent.getChildAdapterPosition(view);
        if (pos >= 0 && pos < dockSize) {
            outRect.top = (int) (6 * density);
            outRect.bottom = (int) (12 * density);
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        boolean any = false;
        float cy = 0, tileW = 0;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int pos = parent.getChildAdapterPosition(child);
            if (pos < 0 || pos >= dockSize) continue;
            RecyclerView.ViewHolder vh = parent.getChildViewHolder(child);
            View card = vh instanceof AppAdapter.Holder ? ((AppAdapter.Holder) vh).card : child.findViewById(R.id.card);
            if (card == null) continue;
            float l = child.getLeft() + card.getLeft();
            float t = child.getTop() + card.getTop() + child.getTranslationY();
            float r = l + card.getWidth();
            float b = t + card.getHeight();
            if (pos < centers.length) centers[pos] = (l + r) / 2f;
            cy = (t + b) / 2f;
            tileW = card.getWidth();
            if (!any) {
                rect.set(l, t, r, b);
                any = true;
            } else {
                rect.union(l, t, r, b);
            }
        }
        if (!any) return;
        float pad = 14 * density;
        // The band can only draw inside the grid, and the first and last dock tiles sit 12dp in from its
        // edges: a full 14dp at the sides sliced the rounded ends flat (#454). Keep the sides, with the
        // 1dp edge line, inside the grid; the top and bottom have room.
        float sidePad = glass ? 24 * density : pad;   // the magnified end tiles grow about 20dp sideways: keep them inside the glass
        float side = Math.max(0f, Math.min(sidePad, Math.min(rect.left, parent.getWidth() - rect.right) - density));
        rect.inset(-side, -pad);
        rect.bottom += namesRoom;
        float radius = 26 * density;
        if (glass) {
            drawGlow(c, cy, tileW, radius);
            panel.draw(c, rect, radius, density, light, accent, false, false);
            return;
        }
        c.drawRoundRect(rect, radius, radius, band);
        c.drawRoundRect(rect, radius, radius, edge);
    }

    /** Two soft blobs of the focused app's colour behind the glass: the near one leads, the far one trails. */
    private void drawGlow(Canvas c, float cy, float tileW, float radius) {
        if (glowAmp < 0.01f || glowFocus < 0f || glowColors.length == 0) return;
        float strength = glowAmp * (light ? 0.7f : 1f);
        // Inside the band only: the grid view ends at the screen margin and would slice a wider glow flat.
        clip.rewind();
        clip.addRoundRect(rect, radius, radius, Path.Direction.CW);
        c.save();
        c.clipPath(clip);
        blob(c, at(glowLag), cy, tileW * 2.0f, colorAt(glowLag), (int) (0x58 * strength));
        blob(c, at(glowFocus), cy, tileW * 1.25f, colorAt(glowFocus), (int) (0x90 * strength));
        c.restore();
    }

    private float at(float f) {
        int lo = Math.max(0, Math.min(dockSize - 1, (int) Math.floor(f)));
        int hi = Math.min(dockSize - 1, lo + 1);
        float t = Math.max(0f, Math.min(1f, f - lo));
        return centers[lo] + (centers[hi] - centers[lo]) * t;
    }

    private int colorAt(float f) {
        int lo = Math.max(0, Math.min(glowColors.length - 1, (int) Math.floor(f)));
        int hi = Math.min(glowColors.length - 1, lo + 1);
        return Tint.mix(glowColors[lo], glowColors[hi], Math.max(0f, Math.min(1f, f - lo)));
    }

    private void blob(Canvas c, float cx, float cy, float radius, int rgb, int alpha) {
        blob.setShader(new RadialGradient(cx, cy, radius,
                new int[]{Tint.withAlpha(rgb, alpha), Tint.withAlpha(rgb, alpha / 3), 0},
                new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
        c.save();
        c.scale(1f, 0.72f, cx, cy);
        c.drawCircle(cx, cy, radius, blob);
        c.restore();
    }
}
