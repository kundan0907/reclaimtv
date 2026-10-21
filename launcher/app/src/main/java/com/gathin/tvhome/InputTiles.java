package com.gathin.tvhome;

import java.util.List;
import java.util.Locale;

/**
 * Quick-start tiles for gear on an HDMI input (config.xml, input_tiles), e.g. "PlayStation|PlayStation": the label
 * on the tile, then a case-insensitive piece of the CEC device name (or, failing that, the input's own label). A
 * tile exists only while a matching input is there, so a TV without the console shows nothing in its place.
 */
final class InputTiles {
    /** One HDMI input the TV reports: its id, the CEC device name (may be empty) and its own label. */
    static final class Input {
        final String id, deviceName, label;

        Input(String id, String deviceName, String label) {
            this.id = id;
            this.deviceName = deviceName == null ? "" : deviceName;
            this.label = label == null ? "" : label;
        }
    }

    /** What an entry resolved to: the tile's label and the input to open. */
    static final class Tile {
        final String label, inputId;

        Tile(String label, String inputId) {
            this.label = label;
            this.inputId = inputId;
        }
    }

    private InputTiles() {}

    /** {label, match} for "Label|match"; a bare "Name" is both. Null for a blank entry. */
    static String[] parse(String entry) {
        if (entry == null || entry.trim().isEmpty()) return null;
        int bar = entry.indexOf('|');
        String label = (bar < 0 ? entry : entry.substring(0, bar)).trim();
        String match = (bar < 0 ? entry : entry.substring(bar + 1)).trim();
        if (label.isEmpty()) label = match;
        if (match.isEmpty()) match = label;
        return label.isEmpty() ? null : new String[]{label, match};
    }

    /** The first input whose device name contains the match, else the first whose label does; null for none. */
    static Tile resolve(String entry, List<Input> inputs) {
        String[] parts = parse(entry);
        if (parts == null) return null;
        String needle = parts[1].toLowerCase(Locale.ROOT);
        for (Input in : inputs) {
            if (in.deviceName.toLowerCase(Locale.ROOT).contains(needle)) return new Tile(parts[0], in.id);
        }
        for (Input in : inputs) {
            if (in.label.toLowerCase(Locale.ROOT).contains(needle)) return new Tile(parts[0], in.id);
        }
        return null;
    }
}
