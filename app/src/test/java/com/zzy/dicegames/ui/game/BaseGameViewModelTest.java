package com.zzy.dicegames.ui.game;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import static org.junit.Assert.*;

public class BaseGameViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private BaseGameViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new BaseGameViewModel() {};
    }

    @Test
    public void testSetDiceNumbers() {
        int[] diceNumbers = {2, 6, 4, 1, 6, 4, 3, 1, 4, 5};
        viewModel.setDiceNumbers(diceNumbers);
        assertArrayEquals(new int[] {1, 1, 2, 3, 4, 4, 4, 5, 6, 6}, viewModel.diceNumbers);
        assertArrayEquals(new int[] {0, 2, 1, 1, 3, 1, 2}, viewModel.diceCounts);
        assertEquals(36, viewModel.sumOfDice);
        assertArrayEquals(new int[] {2, 6, 4, 1, 6, 4, 3, 1, 4, 5}, diceNumbers);  // 不影响原数组
    }
}
