package com.zzy.dicegames.ui.game.farkle;

import android.os.Handler;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.farkle.FarkleScore;
import com.zzy.dicegames.utils.ArrayUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.core.util.Pair;

import static com.zzy.dicegames.ui.game.farkle.FarkleGameViewModel.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FarkleGameViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private FarkleGameViewModel viewModel;
    private FarkleGameViewModel spyViewModel;

    @Mock
    private Handler mockHandler;

    @Before
    public void setUp() {
        viewModel = new FarkleGameViewModel();
        viewModel.setHandler(mockHandler);
        spyViewModel = spy(viewModel);
    }

    private static int[] arr(int... a) {
        return a;
    }

    private static void assertGameLogEquals(
            Pair<Integer, Object[]> actual, int expectedId, Object... expectedArgs) {
        assertEquals(expectedId, actual.first.intValue());
        assertArrayEquals(expectedArgs, actual.second);
    }

    @Test
    public void testInitialization() {
        assertEquals(6, viewModel.getNumDice());
        assertEquals(UNLIMITED_ROLLS, viewModel.getMaxRolls());
        assertArrayEquals(new boolean[NUM_DICE], viewModel.getDiceEnabled().getValue());
        assertTrue(viewModel.getRollButtonEnabled().getValue());
        assertFalse(viewModel.getBankButtonEnabled().getValue());
        assertFalse(viewModel.getNewGameButtonVisible().getValue());
        assertEquals(PLAYER_HUMAN, viewModel.getCurrentPlayer().getValue().intValue());
        assertArrayEquals(new int[NUM_PLAYERS], viewModel.getPlayerScores().getValue());
        assertEquals(0, viewModel.getEstimatedTurnScore().getValue().intValue());
        var gameLog = viewModel.getGameLog().getValue();
        assertEquals(3, gameLog.size());
        assertGameLogEquals(gameLog.get(0), R.string.logGameBegins);
        assertGameLogEquals(gameLog.get(1), R.string.logYourTurn);
        assertGameLogEquals(gameLog.get(2), R.string.logStartingScore, 0);
    }

    @Test
    public void testAddLog() {
        viewModel.addLog(123, "foo", "bar");
        var gameLog = viewModel.getGameLog().getValue();
        assertGameLogEquals(gameLog.get(gameLog.size() - 1), 123, "foo", "bar");
    }

    @Test
    public void testAddDiceNumbersLog() {
        viewModel.addDiceNumbersLog(456, new int[] {1, 2, 3, 4, 5, 6});
        var gameLog = viewModel.getGameLog().getValue();
        assertGameLogEquals(gameLog.get(gameLog.size() - 1), 456, "1,2,3,4,5,6");
    }

    @Test
    public void testToggleLocked() {
        viewModel.beforeRollDice();
        viewModel.updateDiceNumbers(2, 3, 3, 3, 5, 6);
        assertArrayEquals(new boolean[NUM_DICE], viewModel.getDiceLocked().getValue());
        assertEquals(0, viewModel.getEstimatedTurnScore().getValue().intValue());

        // [2] [3] [3] 3 5 6
        for (int i = 0; i < 3; i++)
            viewModel.toggleLocked(i);
        assertEquals(0, viewModel.getEstimatedTurnScore().getValue().intValue());
        assertFalse(viewModel.getRollButtonEnabled().getValue());

        // [2] [3] [3] [3] 5 6
        viewModel.toggleLocked(3);
        assertEquals(300, viewModel.getEstimatedTurnScore().getValue().intValue());
        assertTrue(viewModel.getRollButtonEnabled().getValue());

        // [2] [3] [3] [3] [5] 6
        viewModel.toggleLocked(4);
        assertEquals(350, viewModel.getEstimatedTurnScore().getValue().intValue());
        assertTrue(viewModel.getRollButtonEnabled().getValue());

        // [2] [3] [3] [3] [5] [6]
        viewModel.toggleLocked(5);
        assertEquals(350, viewModel.getEstimatedTurnScore().getValue().intValue());
        assertFalse(viewModel.getRollButtonEnabled().getValue());

        // [2] [3] [3] 3 [5] [6]
        viewModel.toggleLocked(3);
        assertEquals(50, viewModel.getEstimatedTurnScore().getValue().intValue());
        assertTrue(viewModel.getRollButtonEnabled().getValue());

        // [2] [3] [3] 3 5 [6]
        viewModel.toggleLocked(4);
        assertEquals(0, viewModel.getEstimatedTurnScore().getValue().intValue());
        assertFalse(viewModel.getRollButtonEnabled().getValue());
    }

    @Test
    public void testRollDice() {
        int[] numbers1 = {4, 1, 3, 5, 4, 6}, numbers2 = {5, 1, 6, 5, 2, 3};
        when(spyViewModel.generateRandomDiceNumbers()).thenReturn(numbers1, numbers2);

        // 初始状态
        assertTrue(ArrayUtil.all(spyViewModel.getDiceEnabled().getValue(), false));
        assertFalse(spyViewModel.getBankButtonEnabled().getValue());

        // 本轮第一次掷骰子
        spyViewModel.rollDice();
        assertTrue(ArrayUtil.all(spyViewModel.getDiceEnabled().getValue(), true));
        assertArrayEquals(numbers1, spyViewModel.getDiceNumbers().getValue());
        var gameLog = viewModel.getGameLog().getValue();
        assertGameLogEquals(gameLog.get(gameLog.size() - 1), R.string.logDiceRolled, "4,1,3,5,4,6");
        assertTrue(viewModel.getBankButtonEnabled().getValue());

        // 后续掷骰子
        spyViewModel.toggleLocked(1);
        spyViewModel.toggleLocked(3);
        spyViewModel.rollDice();
        assertArrayEquals(new boolean[] {true, false, true, false, true, true},
                spyViewModel.getDiceEnabled().getValue());
        assertArrayEquals(numbers2, spyViewModel.getDiceNumbers().getValue());
        assertGameLogEquals(gameLog.get(gameLog.size() - 2), R.string.logDiceKept, "1,5");
        assertGameLogEquals(gameLog.get(gameLog.size() - 1), R.string.logDiceRolled, "5,6,2,3");
    }

    @Test
    public void testUpdateDiceNumbers_Farkle() {
        doNothing().when(spyViewModel).farkle();
        spyViewModel.beforeRollDice();
        spyViewModel.updateDiceNumbers(4, 2, 4, 3, 2, 6);
        verify(spyViewModel).farkle();

        spyViewModel = spy(new FarkleGameViewModel());
        doNothing().when(spyViewModel).farkle();
        spyViewModel.beforeRollDice();
        spyViewModel.updateDiceNumbers(3, 2, 5, 1, 3, 6);
        verify(spyViewModel, never()).farkle();

        spyViewModel.toggleLocked(2);
        spyViewModel.toggleLocked(3);
        spyViewModel.beforeRollDice();
        spyViewModel.updateDiceNumbers(6, 6, 5, 1, 4, 3);
        verify(spyViewModel).farkle();
    }

    @Test
    public void testUpdateDiceNumbers_Win() {
        doNothing().when(spyViewModel).win(anyInt());
        spyViewModel.addCurrentPlayerScore(9800);
        spyViewModel.beforeRollDice();
        spyViewModel.updateDiceNumbers(5, 4, 4, 1, 2, 6);
        spyViewModel.toggleLocked(0);
        spyViewModel.toggleLocked(3);
        assertEquals(150, spyViewModel.getEstimatedTurnScore().getValue().intValue());
        verify(spyViewModel, never()).win(anyInt());

        spyViewModel.beforeRollDice();
        spyViewModel.updateDiceNumbers(5, 1, 3, 1, 6, 6);
        verify(spyViewModel).win(100);
    }

    @Test
    public void testUpdateDiceNumbers_HotDice() {
        doNothing().when(spyViewModel).hotDice(anyInt());
        spyViewModel.beforeRollDice();
        spyViewModel.updateDiceNumbers(1, 1, 2, 2, 2, 5);
        verify(spyViewModel).hotDice(450);

        spyViewModel = spy(new FarkleGameViewModel());
        doNothing().when(spyViewModel).hotDice(anyInt());
        spyViewModel.beforeRollDice();
        spyViewModel.updateDiceNumbers(3, 3, 3, 5, 6, 6);
        verify(spyViewModel, never()).hotDice(anyInt());

        for (int i = 0; i < 4; i++)
            spyViewModel.toggleLocked(i);
        spyViewModel.beforeRollDice();
        spyViewModel.updateDiceNumbers(3, 3, 3, 5, 1, 1);
        verify(spyViewModel).hotDice(200);
    }

    @Test
    public void testUpdateDiceNumbers_Human() {
        viewModel.beforeRollDice();
        viewModel.updateDiceNumbers(1, 1, 2, 3, 5, 6);
        assertTrue(viewModel.getBankButtonEnabled().getValue());
    }

    @Test
    public void testUpdateDiceNumbers_Computer() {
        viewModel.nextPlayer();
        viewModel.beforeRollDice();
        viewModel.updateDiceNumbers(2, 3, 3, 3, 4, 6);
        assertFalse(viewModel.getBankButtonEnabled().getValue());
        verify(mockHandler, times(2)).postDelayed(any(), anyLong());
    }

    @Test
    public void testStraight() {
        List<Pair<int[], Pair<Integer, Integer>>> testCases = List.of(
                Pair.create(arr(1, 2, 3, 4, 5, 6), Pair.create(1500, 6)),
                Pair.create(arr(2, 2, 3, 4, 5, 6), Pair.create(50, 1))
        );
        for (var t : testCases)
            assertEquals(t.second, viewModel.calculateScore(t.first));
    }

    @Test
    public void testThreePairs() {
        List<Pair<int[], Pair<Integer, Integer>>> testCases = List.of(
                Pair.create(arr(1, 1, 3, 3, 5, 5), Pair.create(750, 6)),
                Pair.create(arr(1, 2, 3, 3, 5, 5), Pair.create(200, 3))
        );
        for (var t : testCases)
            assertEquals(t.second, viewModel.calculateScore(t.first));
    }

    @Test
    public void testOfAKind() {
        List<Pair<int[], Pair<Integer, Integer>>> testCases = List.of(
                Pair.create(arr(1, 1, 1, 1, 1, 1), Pair.create(4000, 6)),
                Pair.create(arr(6, 6, 6, 6, 6, 6), Pair.create(2400, 6)),
                Pair.create(arr(6, 6, 6, 6, 6, 2), Pair.create(1800, 5)),
                Pair.create(arr(4, 4, 4, 4, 4, 5), Pair.create(1250, 6)),
                Pair.create(arr(2, 3, 3, 3, 3, 4), Pair.create(600, 4)),
                Pair.create(arr(1, 4, 5, 5, 5, 5), Pair.create(1100, 5)),
                Pair.create(arr(1, 1, 1, 2, 3, 6), Pair.create(1000, 3)),
                Pair.create(arr(2, 2, 2, 4, 5, 5), Pair.create(300, 5)),
                Pair.create(arr(3, 3, 3, 6, 6, 6), Pair.create(900, 6)),
                Pair.create(arr(1, 2, 2, 2, 5, 5), Pair.create(400, 6))
        );
        for (var t : testCases)
            assertEquals(t.second, viewModel.calculateScore(t.first));
    }

    @Test
    public void testGeneralSituation() {
        List<Pair<int[], Pair<Integer, Integer>>> testCases = List.of(
                Pair.create(arr(1, 2, 2, 3, 6, 6), Pair.create(100, 1)),
                Pair.create(arr(1, 1, 3, 4, 4, 6), Pair.create(200, 2)),
                Pair.create(arr(2, 2, 3, 4, 5, 6), Pair.create(50, 1)),
                Pair.create(arr(2, 3, 3, 4, 5, 5), Pair.create(100, 2)),
                Pair.create(arr(1, 2, 3, 5, 6, 6), Pair.create(150, 2)),
                Pair.create(arr(1, 3, 4, 4, 5, 5), Pair.create(200, 3)),
                Pair.create(arr(1, 1, 3, 4, 5, 6), Pair.create(250, 3)),
                Pair.create(arr(1, 1, 2, 3, 5, 5), Pair.create(300, 4)),
                Pair.create(arr(2, 3, 3, 4, 4, 6), Pair.create(0, 0)),
                // 排除已禁用或未锁定的骰子
                Pair.create(arr(2, 0, 0, 0, 5, 6), Pair.create(50, 1)),
                Pair.create(arr(1, 0, 0, 0, 0, 4), Pair.create(100, 1)),
                Pair.create(arr(0, 0, 0, 0, 0, 3), Pair.create(0, 0))
        );
        for (var t : testCases)
            assertEquals(t.second, viewModel.calculateScore(t.first));
    }

    @Test
    public void testGetScoringDice() {
        List<Pair<int[], Pair<int[], int[]>>> testCases = List.of(
                Pair.create(arr(6, 5, 4, 3, 2, 1), Pair.create(arr(0, 1, 2, 3, 4, 5), arr(6, 5, 4, 3, 2, 1))),
                Pair.create(arr(2, 2, 4, 4, 6, 6), Pair.create(arr(0, 1, 2, 3, 4, 5), arr(2, 2, 4, 4, 6, 6))),
                Pair.create(arr(3, 2, 3, 3, 4, 3), Pair.create(arr(0, 2, 3, 5), arr(3, 3, 3, 3))),
                Pair.create(arr(2, 2, 5, 3, 1, 2), Pair.create(arr(0, 1, 2, 4, 5), arr(2, 2, 5, 1, 2))),
                Pair.create(arr(1, 1, 3, 4, 5, 6), Pair.create(arr(0, 1, 4), arr(1, 1, 5))),
                Pair.create(arr(5, 0, 1, 0, 0, 4), Pair.create(arr(0, 2), arr(5, 1))),
                Pair.create(arr(0, 0, 0, 0, 0, 3), Pair.create(arr(), arr()))
        );
        for (var t : testCases) {
            assertArrayEquals(t.second.first, viewModel.getScoringDice(true, t.first));
            assertArrayEquals(t.second.second, viewModel.getScoringDice(false, t.first));
        }
    }

    @Test
    public void testFarkle() {
        viewModel.beforeRollDice();
        viewModel.farkle();
        var gameLog = viewModel.getGameLog().getValue();
        assertGameLogEquals(gameLog.get(gameLog.size() - 1), R.string.logFarkle);
        assertTrue(viewModel.isAllDiceDisabled());
        assertFalse(viewModel.getBankButtonEnabled().getValue());
        verify(mockHandler).postDelayed(any(), anyLong());
    }

    @Test
    public void testWin() {
        doNothing().when(spyViewModel).gameOver();
        spyViewModel.addCurrentPlayerScore(9900);
        spyViewModel.beforeRollDice();
        spyViewModel.win(600);
        var gameLog = spyViewModel.getGameLog().getValue();
        assertGameLogEquals(gameLog.get(gameLog.size() - 1), R.string.logYouWin);
        assertEquals(600, spyViewModel.getEstimatedTurnScore().getValue().intValue());
        assertEquals(10500, spyViewModel.getCurrentPlayerScore());
        verify(spyViewModel).gameOver();
    }

    @Test
    public void testHotDice() {
        viewModel.beforeRollDice();
        viewModel.hotDice(1500);
        var gameLog = viewModel.getGameLog().getValue();
        assertGameLogEquals(gameLog.get(gameLog.size() - 1), R.string.logHotDice, 1500);
        assertEquals(1500, viewModel.getEstimatedTurnScore().getValue().intValue());
        assertFalse(viewModel.getBankButtonEnabled().getValue());
        assertTrue(ArrayUtil.all(viewModel.getDiceLocked().getValue(), false));
        assertTrue(ArrayUtil.all(viewModel.getDiceEnabled().getValue(), false));
        assertTrue(viewModel.getRollButtonEnabled().getValue());
    }

    @Test
    public void testHotDice_Computer() {
        viewModel.nextPlayer();
        viewModel.beforeRollDice();
        viewModel.hotDice(750);
        assertFalse(viewModel.getRollButtonEnabled().getValue());
        verify(mockHandler, times(2)).postDelayed(any(), anyLong());
    }

    @Test
    public void testBank() {
        doNothing().when(spyViewModel).nextPlayer();
        spyViewModel.beforeRollDice();
        spyViewModel.updateDiceNumbers(1, 3, 4, 6, 6, 6);
        spyViewModel.bank();
        assertEquals(700, spyViewModel.getCurrentPlayerScore());
        var gameLog = viewModel.getGameLog().getValue();
        assertGameLogEquals(gameLog.get(gameLog.size() - 2), R.string.logDiceKept, "1,6,6,6");
        assertGameLogEquals(gameLog.get(gameLog.size() - 1), R.string.logFinishTurn, 700);
        verify(spyViewModel).nextPlayer();
    }

    @Test
    public void testNextPlayer_Human() {
        viewModel.nextPlayer();
        assertEquals(PLAYER_COMPUTER, viewModel.getCurrentPlayer().getValue().intValue());
        assertEquals(0, viewModel.getEstimatedTurnScore().getValue().intValue());
        assertFalse(viewModel.getBankButtonEnabled().getValue());
        assertTrue(ArrayUtil.all(viewModel.getDiceLocked().getValue(), false));
        assertTrue(ArrayUtil.all(viewModel.getDiceEnabled().getValue(), false));
        assertFalse(viewModel.getRollButtonEnabled().getValue());
        var gameLog = viewModel.getGameLog().getValue();
        assertGameLogEquals(gameLog.get(gameLog.size() - 3), R.string.logSeparator);
        assertGameLogEquals(gameLog.get(gameLog.size() - 2), R.string.logComputerTurn);
        assertGameLogEquals(gameLog.get(gameLog.size() - 1), R.string.logStartingScore, 0);
        verify(mockHandler).postDelayed(any(), anyLong());
    }

    @Test
    public void testNextPlayer_Computer() {
        viewModel.nextPlayer();
        viewModel.nextPlayer();
        assertEquals(PLAYER_HUMAN, viewModel.getCurrentPlayer().getValue().intValue());
        assertTrue(viewModel.getRollButtonEnabled().getValue());
        var gameLog = viewModel.getGameLog().getValue();
        assertGameLogEquals(gameLog.get(gameLog.size() - 2), R.string.logYourTurn);
    }

    @Test
    public void testGameOver() {
        doReturn(new FarkleScore("2025-01-01", 10000, 8000)).when(spyViewModel).createScoreEntity();
        doNothing().when(spyViewModel).saveScoreToDatabase(any());

        spyViewModel.gameOver();
        assertTrue(spyViewModel.isAllDiceDisabled());
        assertFalse(spyViewModel.getRollButtonEnabled().getValue());
        assertFalse(spyViewModel.getBankButtonEnabled().getValue());
        assertTrue(spyViewModel.getNewGameButtonVisible().getValue());
        verify(spyViewModel).createScoreEntity();
        verify(spyViewModel).saveScoreToDatabase(
                argThat(s -> s.score == 10000 && s.computerScore == 8000));
    }

    @Test
    public void testCreateScoreEntity() {
        viewModel.addCurrentPlayerScore(7500);
        viewModel.nextPlayer();
        viewModel.addCurrentPlayerScore(10500);
        var score = viewModel.createScoreEntity();
        assertEquals(7500, score.score);
        assertEquals(10500, score.computerScore);
    }

    @Test
    public void testResetDiceWindow() {
        viewModel.beforeRollDice();
        viewModel.updateDiceNumbers(1, 3, 3, 3, 4, 5);
        viewModel.toggleLocked(0);

        assertTrue(viewModel.getDiceLocked().getValue()[0]);
        assertTrue(viewModel.getDiceEnabled().getValue()[0]);

        viewModel.resetDiceWindow();
        assertFalse(viewModel.getDiceLocked().getValue()[0]);
        assertFalse(viewModel.getDiceEnabled().getValue()[0]);
        assertTrue(viewModel.getRollButtonEnabled().getValue());
    }

    @Test
    public void testReset() {
        viewModel.reset();
        assertEquals(PLAYER_HUMAN, viewModel.getCurrentPlayer().getValue().intValue());
        assertTrue(ArrayUtil.all(viewModel.getDiceLocked().getValue(), false));
        assertTrue(ArrayUtil.all(viewModel.getDiceEnabled().getValue(), false));
        assertTrue(viewModel.getRollButtonEnabled().getValue());
        assertFalse(viewModel.getBankButtonEnabled().getValue());
        assertFalse(viewModel.getNewGameButtonVisible().getValue());
        assertArrayEquals(new int[NUM_PLAYERS], viewModel.getPlayerScores().getValue());
        assertEquals(0, viewModel.getEstimatedTurnScore().getValue().intValue());
        var gameLog = viewModel.getGameLog().getValue();
        assertEquals(3, gameLog.size());
        assertGameLogEquals(gameLog.get(0), R.string.logGameBegins);
        assertGameLogEquals(gameLog.get(1), R.string.logYourTurn);
        assertGameLogEquals(gameLog.get(2), R.string.logStartingScore, 0);
    }
}
