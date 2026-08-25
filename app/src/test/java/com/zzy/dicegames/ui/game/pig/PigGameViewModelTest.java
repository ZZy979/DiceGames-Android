package com.zzy.dicegames.ui.game.pig;

import android.os.Handler;

import com.zzy.dicegames.data.entity.pig.PigScore;
import com.zzy.dicegames.utils.score.ScoreUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import static com.zzy.dicegames.ui.game.pig.PigGameViewModel.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PigGameViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private PigGameViewModel viewModel;
    private PigGameViewModel spyViewModel;

    @Mock
    private Handler mockHandler;

    @Before
    public void setUp() {
        viewModel = new PigGameViewModel();
        viewModel.setHandler(mockHandler);
        spyViewModel = spy(viewModel);
    }

    @Test
    public void testInitialization() {
        assertEquals(1, viewModel.getNumDice());
        assertEquals(UNLIMITED_ROLLS, viewModel.getMaxRolls());
        assertTrue(viewModel.getRollButtonEnabled().getValue());
        assertFalse(viewModel.getHoldButtonEnabled().getValue());
        assertFalse(viewModel.getNewGameButtonVisible().getValue());
        assertEquals(PLAYER_HUMAN, viewModel.getCurrentPlayer().getValue().intValue());
        assertArrayEquals(new int[NUM_PLAYERS], viewModel.getPlayerScores().getValue());
        assertEquals(0, viewModel.getTurnScore().getValue().intValue());
    }

    @Test
    public void testRollDice() {
        when(spyViewModel.generateRandomDiceNumbers()).thenReturn(new int[] {4});
        spyViewModel.rollDice();
        assertEquals(4, spyViewModel.getTurnScore().getValue().intValue());
        assertTrue(spyViewModel.getHoldButtonEnabled().getValue());
        assertTrue(spyViewModel.getRollButtonEnabled().getValue());
        assertEquals(0, spyViewModel.getCurrentPlayerScore());
    }

    @Test
    public void testRollDice_Pig() {
        doNothing().when(spyViewModel).pig();
        when(spyViewModel.generateRandomDiceNumbers()).thenReturn(new int[] {1});
        spyViewModel.rollDice();
        verify(spyViewModel).pig();
        assertEquals(0, spyViewModel.getTurnScore().getValue().intValue());
    }

    @Test
    public void testUpdateDiceNumbers_Pig() {
        doNothing().when(spyViewModel).pig();
        spyViewModel.updateDiceNumbers(1);
        verify(spyViewModel).pig();
    }

    @Test
    public void testUpdateDiceNumbers_Human() {
        viewModel.updateDiceNumbers(4);
        assertEquals(4, viewModel.getTurnScore().getValue().intValue());
        assertTrue(viewModel.getHoldButtonEnabled().getValue());
        assertTrue(viewModel.getRollButtonEnabled().getValue());
        assertEquals(0, viewModel.getCurrentPlayerScore());

        viewModel.updateDiceNumbers(3);
        assertEquals(7, viewModel.getTurnScore().getValue().intValue());
    }

    @Test
    public void testUpdateDiceNumbers_Computer() {
        viewModel.nextPlayer();
        viewModel.updateDiceNumbers(3);
        assertEquals(3, viewModel.getTurnScore().getValue().intValue());
        assertFalse(viewModel.getHoldButtonEnabled().getValue());
        assertFalse(viewModel.getRollButtonEnabled().getValue());
        verify(mockHandler, times(2)).postDelayed(any(), anyLong());
    }

    @Test
    public void testUpdateDiceNumbers_Win() {
        doNothing().when(spyViewModel).win();
        spyViewModel.addCurrentPlayerScore(95);
        spyViewModel.updateDiceNumbers(4);
        verify(spyViewModel, never()).win();

        spyViewModel.updateDiceNumbers(5);
        verify(spyViewModel).win();
        assertEquals(104, spyViewModel.getPlayerScores().getValue()[PLAYER_HUMAN]
                + spyViewModel.getTurnScore().getValue());

    }

    @Test
    public void testPig() {
        viewModel.updateDiceNumbers(4);
        viewModel.pig();
        assertEquals(0, viewModel.getTurnScore().getValue().intValue());
        assertFalse(viewModel.getHoldButtonEnabled().getValue());
        verify(mockHandler).postDelayed(any(), anyLong());
    }

    @Test
    public void testWin() {
        doNothing().when(spyViewModel).gameOver();
        spyViewModel.updateDiceNumbers(4);
        spyViewModel.win();
        assertEquals(4, spyViewModel.getCurrentPlayerScore());
        verify(spyViewModel).gameOver();
    }

    @Test
    public void testComputerShouldRoll() {
        // 电脑总分 + 本轮得分 >= 100 → 保存
        assertFalse(viewModel.computerShouldRoll(0, 95, 10));
        assertFalse(viewModel.computerShouldRoll(80, 95, 10));
        // 用户得分 >= 71（最后冲刺）→ 继续掷骰子
        assertTrue(viewModel.computerShouldRoll(71, 50, 10));
        assertTrue(viewModel.computerShouldRoll(80, 50, 30));
        // 电脑得分 >= 71（最后冲刺）→ 继续掷骰子
        assertTrue(viewModel.computerShouldRoll(50, 71, 10));
        assertTrue(viewModel.computerShouldRoll(50, 80, 10));
        // 本轮得分未达到追赶目标 → 继续掷骰子
        // 追赶目标 = 21 + round((用户得分 - 电脑得分) / 8)
        assertTrue(viewModel.computerShouldRoll(50, 40, 21));   // 目标22，21 < 22
        assertFalse(viewModel.computerShouldRoll(50, 40, 22));  // 22 < 22 不成立
        assertTrue(viewModel.computerShouldRoll(50, 50, 20));   // 目标21，20 < 21
        assertFalse(viewModel.computerShouldRoll(50, 50, 21));  // 21 < 21 不成立
        assertTrue(viewModel.computerShouldRoll(50, 58, 19));   // 目标20，19 < 20
        assertFalse(viewModel.computerShouldRoll(50, 58, 20));  // 20 < 20 不成立
    }

    @Test
    public void testComputerTurn_RollAgain() {
        // 电脑回合，双方得分均为0，本轮得分5 < 追赶目标21 → 继续掷骰子
        viewModel.nextPlayer();
        viewModel.updateDiceNumbers(5);
        viewModel.computerTurn();

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(mockHandler, atLeast(1)).postDelayed(captor.capture(), anyLong());
        // 执行 computerTurn 调度的动作：应继续掷骰子，不会改变得分和当前玩家
        captor.getValue().run();
        assertEquals(0, viewModel.getPlayerScores().getValue()[PLAYER_COMPUTER]);
        assertEquals(PLAYER_COMPUTER, viewModel.getCurrentPlayer().getValue().intValue());
    }

    @Test
    public void testComputerTurn_Hold() {
        // 电脑回合，双方得分均为0，本轮得分25 >= 追赶目标21 → 保存
        viewModel.nextPlayer();
        viewModel.updateDiceNumbers(5);
        viewModel.updateDiceNumbers(5);
        viewModel.updateDiceNumbers(5);
        viewModel.updateDiceNumbers(5);
        viewModel.updateDiceNumbers(5);
        viewModel.computerTurn();

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(mockHandler, atLeast(1)).postDelayed(captor.capture(), anyLong());
        // 执行 computerTurn 调度的动作：应保存本轮得分并切换到人类玩家
        captor.getValue().run();
        assertEquals(25, viewModel.getPlayerScores().getValue()[PLAYER_COMPUTER]);
        assertEquals(PLAYER_HUMAN, viewModel.getCurrentPlayer().getValue().intValue());
    }

    @Test
    public void testHold() {
        doNothing().when(spyViewModel).nextPlayer();
        spyViewModel.updateDiceNumbers(4);
        spyViewModel.hold();
        assertEquals(4, spyViewModel.getCurrentPlayerScore());
        verify(spyViewModel).nextPlayer();
    }

    @Test
    public void testNextPlayer_Human() {
        viewModel.nextPlayer();
        assertEquals(PLAYER_COMPUTER, viewModel.getCurrentPlayer().getValue().intValue());
        assertEquals(0, viewModel.getTurnScore().getValue().intValue());
        assertFalse(viewModel.getHoldButtonEnabled().getValue());
        assertFalse(viewModel.getRollButtonEnabled().getValue());
        verify(mockHandler).postDelayed(any(), anyLong());
    }

    @Test
    public void testNextPlayer_Computer() {
        viewModel.nextPlayer();
        viewModel.nextPlayer();
        assertEquals(PLAYER_HUMAN, viewModel.getCurrentPlayer().getValue().intValue());
        assertTrue(viewModel.getRollButtonEnabled().getValue());
        verify(mockHandler).postDelayed(any(), anyLong());
    }

    @Test
    public void testGameOver() {
        var score = new PigScore("2025-01-01", 100, 85);
        doReturn(score).when(spyViewModel).createScoreEntity();
        doNothing().when(spyViewModel).saveScoreToDatabase(any());

        spyViewModel.gameOver();
        assertFalse(spyViewModel.getRollButtonEnabled().getValue());
        assertFalse(spyViewModel.getHoldButtonEnabled().getValue());
        assertTrue(spyViewModel.getNewGameButtonVisible().getValue());
        verify(spyViewModel).createScoreEntity();
        verify(spyViewModel).saveScoreToDatabase(argThat(s -> ScoreUtil.isEqual(score, s)));
    }

    @Test
    public void testCreateScoreEntity() {
        viewModel.addCurrentPlayerScore(100);
        viewModel.nextPlayer();
        viewModel.addCurrentPlayerScore(85);
        var score = viewModel.createScoreEntity();
        assertEquals(100, score.score);
        assertEquals(85, score.computerScore);
    }

    @Test
    public void testReset() {
        viewModel.reset();
        assertEquals(PLAYER_HUMAN, viewModel.getCurrentPlayer().getValue().intValue());
        assertTrue(viewModel.getRollButtonEnabled().getValue());
        assertFalse(viewModel.getHoldButtonEnabled().getValue());
        assertFalse(viewModel.getNewGameButtonVisible().getValue());
        assertArrayEquals(new int[NUM_PLAYERS], viewModel.getPlayerScores().getValue());
        assertEquals(0, viewModel.getTurnScore().getValue().intValue());
    }
}
