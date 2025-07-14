package com.zzy.dicegames.ui.dice;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RollDiceViewModelUnitTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private RollDiceViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new RollDiceViewModel();
    }

    @Test
    public void testInitialization() {
        viewModel.init(4, 3);
        assertEquals(4, viewModel.getDiceCount());
        assertEquals(3, viewModel.getMaxRolls());
        assertEquals(3, viewModel.getRemainingRolls().getValue().intValue());

        int[] diceNumbers = viewModel.getDiceNumbers().getValue();
        boolean[] diceLocked = viewModel.getDiceLocked().getValue();
        assertNotNull(diceNumbers);
        assertNotNull(diceLocked);
        assertEquals(4, diceNumbers.length);
        assertEquals(4, diceLocked.length);
        for (int i = 0; i < diceNumbers.length; i++) {
            assertEquals(DiceView.MAX_NUMBER, diceNumbers[i]);
            assertFalse(diceLocked[i]);
        }
    }

    @Test
    public void testToggleLocked() {
        viewModel.init(5, 2);
        viewModel.toggleLocked(2);
        assertTrue(viewModel.getDiceLocked().getValue()[2]);
        viewModel.toggleLocked(2);
        assertFalse(viewModel.getDiceLocked().getValue()[2]);
    }

    @Test
    public void testRollDice() {
        viewModel.init(5, 2);
        Observer<Integer> remainingRollsObserver = mock(Observer.class);
        Observer<int[]> diceNumbersObserver = mock(Observer.class);
        viewModel.getRemainingRolls().observeForever(remainingRollsObserver);
        viewModel.getDiceNumbers().observeForever(diceNumbersObserver);

        viewModel.rollDice();
        assertEquals(1, viewModel.getRemainingRolls().getValue().intValue());

        viewModel.rollDice();
        assertEquals(0, viewModel.getRemainingRolls().getValue().intValue());

        viewModel.rollDice();  // 无效
        assertEquals(0, viewModel.getRemainingRolls().getValue().intValue());

        InOrder inOrder = inOrder(remainingRollsObserver);
        inOrder.verify(remainingRollsObserver).onChanged(2);
        inOrder.verify(remainingRollsObserver).onChanged(1);
        inOrder.verify(remainingRollsObserver).onChanged(0);
        verify(diceNumbersObserver, times(3)).onChanged(any());
    }

    @Test
    public void testRollDiceDoesNotChangeLockedDice() {
        viewModel.init(5, 2);

        viewModel.rollDice();
        int firstDictNumber = viewModel.getDiceNumbers().getValue()[0];
        viewModel.toggleLocked(0);

        viewModel.rollDice();
        assertEquals(firstDictNumber, viewModel.getDiceNumbers().getValue()[0]);
    }
}
