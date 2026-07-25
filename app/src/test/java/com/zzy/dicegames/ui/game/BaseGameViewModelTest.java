package com.zzy.dicegames.ui.game;

import android.os.Handler;

import com.zzy.dicegames.utils.ArrayUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import static com.zzy.dicegames.ui.game.BaseGameViewModel.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BaseGameViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private BaseGameViewModel viewModel;

    @Mock
    private Handler mockHandler;

    @Before
    public void setUp() {
        // ViewModel初始化必须放在setUp()方法中，否则会报错 Method xxx not mocked
        viewModel = new BaseGameViewModel(5, 2);
        viewModel.setHandler(mockHandler);
    }

    @Test
    public void testInitialization() {
        viewModel = new BaseGameViewModel(4, 3);
        assertEquals(4, viewModel.getNumDice());
        assertEquals(3, viewModel.getMaxRolls());
        assertEquals(3, viewModel.getRemainingRolls().getValue().intValue());
        assertArrayEquals(ArrayUtil.create(4, 6), viewModel.getDiceNumbers().getValue());
        assertArrayEquals(ArrayUtil.create(4, false), viewModel.getDiceLocked().getValue());
        assertArrayEquals(ArrayUtil.create(4, true), viewModel.getDiceEnabled().getValue());
        assertTrue(viewModel.getRollButtonEnabled().getValue());
    }

    @Test
    public void testIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new BaseGameViewModel(0, 3));
        assertThrows(IllegalArgumentException.class, () -> new BaseGameViewModel(7, 3));
        assertThrows(IllegalArgumentException.class, () -> new BaseGameViewModel(4, 0));
        assertThrows(IllegalArgumentException.class, () -> new BaseGameViewModel(5, -1));
    }

    @Test
    public void testToggleLocked() {
        viewModel.toggleLocked(2);
        assertTrue(viewModel.getDiceLocked().getValue()[2]);
        viewModel.toggleLocked(2);
        assertFalse(viewModel.getDiceLocked().getValue()[2]);
    }

    @Test
    public void testRollDice() {
        Observer<Integer> remainingRollsObserver = mock(Observer.class);
        Observer<int[]> diceNumbersObserver = mock(Observer.class);
        viewModel.getRemainingRolls().observeForever(remainingRollsObserver);
        viewModel.getDiceNumbers().observeForever(diceNumbersObserver);

        viewModel.rollDice();
        assertEquals(1, viewModel.getRemainingRolls().getValue().intValue());
        for (int n : viewModel.getDiceNumbers().getValue())
            assertTrue(n >= 1 && n <= 6);
        assertTrue(ArrayUtil.all(viewModel.getDiceEnabled().getValue(), true));
        assertTrue(viewModel.getRollButtonEnabled().getValue());

        viewModel.rollDice();
        assertEquals(0, viewModel.getRemainingRolls().getValue().intValue());
        assertTrue(ArrayUtil.all(viewModel.getDiceEnabled().getValue(), false));
        assertFalse(viewModel.getRollButtonEnabled().getValue());

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
        viewModel.rollDice();
        int firstDictNumber = viewModel.getDiceNumbers().getValue()[0];
        viewModel.toggleLocked(0);

        viewModel.rollDice();
        assertEquals(firstDictNumber, viewModel.getDiceNumbers().getValue()[0]);
    }

    @Test
    public void testUnlimitedRolls() {
        viewModel = new BaseGameViewModel(5, UNLIMITED_ROLLS);
        assertEquals(UNLIMITED_ROLLS, viewModel.getRemainingRolls().getValue().intValue());

        viewModel.rollDice();
        assertEquals(UNLIMITED_ROLLS, viewModel.getRemainingRolls().getValue().intValue());
    }

    @Test
    public void testRollDiceAnimation() {
        // 第一帧
        viewModel.rollDiceWithAnimation();
        verify(mockHandler).postDelayed(any(), eq(ROLL_DICE_ANIMATION_INTERVAL));

        // 最后一帧
        viewModel.rollDiceAnimation(ROLL_DICE_ANIMATION_FRAMES);
        assertEquals(1, viewModel.getRemainingRolls().getValue().intValue());
        assertTrue(viewModel.getRollButtonEnabled().getValue());
    }

    @Test
    public void testUpdateDiceNumbers() {
        viewModel.updateDiceNumbers(4, 3, 1, 4, 6);
        assertArrayEquals(new int[] {0, 1, 0, 1, 2, 0, 1}, viewModel.diceCounts);
        assertEquals(18, viewModel.sumOfDice);

        viewModel.updateDiceNumbers(5, 5, 5, 5, 5);
        assertArrayEquals(new int[] {0, 0, 0, 0, 0, 5, 0}, viewModel.diceCounts);
        assertEquals(25, viewModel.sumOfDice);
    }

    @Test
    public void testResetDiceWindow() {
        viewModel.toggleLocked(3);
        viewModel.rollDice();
        assertEquals(1, viewModel.getRemainingRolls().getValue().intValue());
        assertTrue(viewModel.getDiceLocked().getValue()[3]);
        assertArrayEquals(ArrayUtil.create(5, true), viewModel.getDiceEnabled().getValue());
        assertTrue(viewModel.getRollButtonEnabled().getValue());

        viewModel.rollDice();
        assertEquals(0, viewModel.getRemainingRolls().getValue().intValue());
        assertArrayEquals(ArrayUtil.create(5, false), viewModel.getDiceEnabled().getValue());
        assertFalse(viewModel.getRollButtonEnabled().getValue());

        viewModel.resetDiceWindow();
        assertEquals(2, viewModel.getRemainingRolls().getValue().intValue());
        assertFalse(viewModel.getDiceLocked().getValue()[3]);
        assertArrayEquals(ArrayUtil.create(5, true), viewModel.getDiceEnabled().getValue());
        assertTrue(viewModel.getRollButtonEnabled().getValue());
    }
}
