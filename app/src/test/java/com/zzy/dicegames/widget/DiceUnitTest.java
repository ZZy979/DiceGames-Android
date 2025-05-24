package com.zzy.dicegames.widget;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DiceUnitTest {
    private Context context;
    private Dice dice;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        dice = new Dice(context);
    }

    @Test
    public void testInitialState() {
        assertEquals(6, dice.getNumber());
        assertFalse(dice.isLocked());
    }

    @Test
    public void testSetNumber() {
        dice.setNumber(1);
        assertEquals(1, dice.getNumber());

        dice.setNumber(3);
        assertEquals(3, dice.getNumber());

        dice.setNumber(6);
        assertEquals(6, dice.getNumber());

        assertThrows(IllegalArgumentException.class, () -> dice.setNumber(0));
        assertThrows(IllegalArgumentException.class, () -> dice.setNumber(7));
    }

    @Test
    public void setNumberWhenLocked() {
        dice.setLocked(true);
        dice.setNumber(4);
        assertEquals(6, dice.getNumber());
    }

    @Test
    public void forceSetNumber() {
        dice.setLocked(true);
        dice.forceSetNumber(4);
        assertEquals(4, dice.getNumber());
    }

    @Test
    public void changeLocked() {
        assertFalse(dice.isLocked());
        dice.callOnClick();
        assertTrue(dice.isLocked());
        dice.callOnClick();
        assertFalse(dice.isLocked());
    }

    @Test
    public void changeLockedWhenDisabled() {
        assertFalse(dice.isLocked());
        dice.setEnabled(false);
        dice.callOnClick();
        assertFalse(dice.isLocked());
    }
}
