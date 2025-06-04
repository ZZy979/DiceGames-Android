package com.zzy.dicegames.ui.dice;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DiceViewUnitTest {
    private Context context;
    private DiceView diceView;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
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
    public void setNumberWhenLocked() {
        diceView.setLocked(true);
        diceView.setNumber(4);
        assertEquals(6, diceView.getNumber());
    }

    @Test
    public void changeLocked() {
        assertFalse(diceView.isLocked());
        diceView.callOnClick();
        assertTrue(diceView.isLocked());
        diceView.callOnClick();
        assertFalse(diceView.isLocked());
    }

    @Test
    public void changeLockedWhenDisabled() {
        assertFalse(diceView.isLocked());
        diceView.setEnabled(false);
        diceView.callOnClick();
        assertFalse(diceView.isLocked());
    }
}
