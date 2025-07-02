package com.zzy.dicegames.ui.dice;

import android.content.Context;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DiceViewUnitTest {
    private DiceView diceView;

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        diceView = new DiceView(context);
    }

    @Test
    public void testInitialState() {
        assertEquals(6, diceView.getNumber());
        assertFalse(diceView.isLocked());
    }

    @Test
    public void testSetNumber() {
        diceView.setNumber(1);
        assertEquals(1, diceView.getNumber());

        diceView.setNumber(3);
        assertEquals(3, diceView.getNumber());

        diceView.setNumber(6);
        assertEquals(6, diceView.getNumber());

        assertThrows(IllegalArgumentException.class, () -> diceView.setNumber(0));
        assertThrows(IllegalArgumentException.class, () -> diceView.setNumber(7));
    }

    @Test
    public void testSetNumberWhenLocked() {
        diceView.setLocked(true);
        diceView.setNumber(4);
        assertEquals(6, diceView.getNumber());
    }

    @Test
    public void testToggleLocked() {
        assertFalse(diceView.isLocked());
        diceView.toggleLocked();
        assertTrue(diceView.isLocked());
        diceView.toggleLocked();
        assertFalse(diceView.isLocked());
    }

    @Test
    public void testSaveInstanceState() {
        diceView.setNumber(4);
        diceView.setLocked(true);
        Bundle state = (Bundle) diceView.onSaveInstanceState();
        assertTrue(state.containsKey(DiceView.SUPER_STATE));
        assertEquals(4, state.getInt(DiceView.NUMBER));
        assertTrue(state.getBoolean(DiceView.LOCKED));
    }

    @Test
    public void testRestoreInstanceState() {
        Bundle state = new Bundle();
        state.putParcelable(DiceView.SUPER_STATE, null);
        state.putInt(DiceView.NUMBER, 4);
        state.putBoolean(DiceView.LOCKED, true);
        diceView.onRestoreInstanceState(state);
        assertEquals(4, diceView.getNumber());
        assertTrue(diceView.isLocked());
    }
}
