package com.gathin.tvhome;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class InputTilesTest {
    private final List<InputTiles.Input> inputs = Arrays.asList(
            new InputTiles.Input("HW1", "", "HDMI1"),
            new InputTiles.Input("HW2", "", "HDMI2"),
            new InputTiles.Input("HDMI300004", "PlayStation 5", "PlayStation 5(HDMI3)"));

    @Test public void parsesLabelAndMatch() {
        assertArrayEquals(new String[]{"PlayStation", "PS5"}, InputTiles.parse("PlayStation|PS5"));
        assertArrayEquals(new String[]{"Xbox", "Xbox"}, InputTiles.parse(" Xbox "));
        assertArrayEquals(new String[]{"Switch", "Switch"}, InputTiles.parse("|Switch"));
        assertNull(InputTiles.parse("  "));
        assertNull(InputTiles.parse(null));
    }

    @Test public void matchesTheCecDeviceNameIgnoringCase() {
        InputTiles.Tile t = InputTiles.resolve("PlayStation|playstation", inputs);
        assertEquals("PlayStation", t.label);
        assertEquals("HDMI300004", t.inputId);
    }

    @Test public void fallsBackToTheInputLabel() {
        assertEquals("HW2", InputTiles.resolve("Game|hdmi2", inputs).inputId);
    }

    @Test public void noMatchMeansNoTile() {
        assertNull(InputTiles.resolve("Xbox|Xbox", inputs));
        assertNull(InputTiles.resolve("PlayStation|PlayStation", Collections.<InputTiles.Input>emptyList()));
        assertNull(InputTiles.resolve("", inputs));
    }
}
