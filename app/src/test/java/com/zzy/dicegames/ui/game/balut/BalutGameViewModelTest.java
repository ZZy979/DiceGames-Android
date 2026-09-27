package com.zzy.dicegames.ui.game.balut;

import android.os.Handler;

import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.utils.ArrayUtil;
import com.zzy.dicegames.utils.score.ScoreUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;
import java.util.function.Consumer;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;

import static com.zzy.dicegames.ui.game.balut.BalutGameViewModel.*;
import static com.zzy.dicegames.ui.game.balut.BalutGameViewModel.Category.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BalutGameViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private BalutGameViewModel viewModel;
    private BalutGameViewModel spyViewModel;

    @Mock
    private Handler mockHandler;

    @Before
    public void setUp() {
        viewModel = new BalutGameViewModel();
        viewModel.setHandler(mockHandler);
        spyViewModel = spy(viewModel);
    }

    private static int[] arr(int... a) {
        return a;
    }

    @Test
    public void testInitialization() {
        assertEquals(5, viewModel.getNumDice());
        assertEquals(3, viewModel.getMaxRolls());
        assertFalse(viewModel.getClickable().getValue());
        assertArrayEquals(new int[NUM_CATEGORIES][MAX_SELECTIONS], viewModel.getScores().getValue());
        assertArrayEquals(new int[NUM_CATEGORIES], viewModel.getSelectCount().getValue());
        assertEquals(0, viewModel.getNumSelected());
        assertArrayEquals(new int[NUM_CATEGORIES], viewModel.getCategoryScores().getValue());
        assertArrayEquals(new int[NUM_CATEGORIES], viewModel.getCategoryPoints().getValue());
        assertEquals(0, viewModel.getTotalScore().getValue().intValue());
        assertEquals(0, viewModel.getTotalScorePoints().getValue().intValue());
        assertEquals(0, viewModel.getTotalPoints().getValue().intValue());
        assertFalse(viewModel.hasRolled());
        assertFalse(viewModel.isDiceRolling());
        assertTrue(ArrayUtil.all(viewModel.getDiceEnabled().getValue(), false));
        assertTrue(viewModel.getRollButtonEnabled().getValue());
    }

    @Test
    public void testUpdateDiceNumbers() {
        viewModel.updateDiceNumbers(2, 1, 5, 4, 4);
        int[] expected = {8, 5, 0, 0, 0, 16, 0};
        int[][] scores = viewModel.getScores().getValue();
        for (int i = 0; i < scores.length; i++) {
            for (int j = 0; j < scores[i].length; j++)
                assertEquals(j == 0 ? expected[i] : 0, scores[i][j]);
        }
    }

    @Test
    public void testFourFiveSix() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(4, 3, 1, 4, 6), arr(8, 0, 6)),
                Pair.create(arr(5, 2, 2, 6, 2), arr(0, 5, 6)),
                Pair.create(arr(6, 6, 6, 6, 6), arr(0, 0, 30)),
                Pair.create(arr(1, 2, 3, 4, 5), arr(4, 5, 0))
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            for (int i = 0; i < 3; i++)
                assertEquals(t.second[i], viewModel.calculateScore(i));
        }
    }

    @Test
    public void testStraight() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(1, 2, 3, 5, 6), 0),
                Pair.create(arr(1, 2, 2, 3, 4), 0),
                Pair.create(arr(2, 3, 4, 5, 5), 0),
                Pair.create(arr(1, 3, 4, 5, 6), 0),
                Pair.create(arr(1, 2, 3, 4, 5), 15),
                Pair.create(arr(2, 3, 4, 5, 6), 20)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(STRAIGHT.ordinal()));
        }
    }

    @Test
    public void testFullHouse() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(1, 1, 1, 5, 5), 13),
                Pair.create(arr(2, 2, 6, 6, 6), 22),
                Pair.create(arr(1, 1, 2, 2, 3), 0),
                Pair.create(arr(5, 5, 5, 5, 5), 0)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(FULL_HOUSE.ordinal()));
        }
    }

    @Test
    public void testChoice() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(2, 4, 5, 5, 6), 22),
                Pair.create(arr(6, 6, 6, 6, 6), 30)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(CHOICE.ordinal()));
        }
    }

    @Test
    public void testBalut() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(1, 1, 1, 1, 1), 25),
                Pair.create(arr(2, 2, 2, 2, 2), 30),
                Pair.create(arr(3, 3, 3, 3, 3), 35),
                Pair.create(arr(4, 4, 4, 4, 4), 40),
                Pair.create(arr(5, 5, 5, 5, 5), 45),
                Pair.create(arr(6, 6, 6, 6, 6), 50),
                Pair.create(arr(6, 6, 6, 5, 6), 0)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(BALUT.ordinal()));
        }
    }

    @Test
    public void testSelect() {
        int six = SIXES.ordinal();
        viewModel.rollDice(1, 4, 5, 6, 6);
        viewModel.select(six);
        assertArrayEquals(new int[] {12, 0, 0, 0}, viewModel.getScores().getValue()[six]);
        assertEquals(1, viewModel.getSelectCount().getValue()[six]);
        assertEquals(0, viewModel.getNumSelected());
        assertEquals(12, viewModel.getCategoryScores().getValue()[six]);
        assertEquals(12, viewModel.getTotalScore().getValue().intValue());
        assertEquals(0, viewModel.getCategoryPoints().getValue()[six]);
        assertEquals(0, viewModel.getTotalPoints().getValue().intValue());

        int balut = BALUT.ordinal();
        for (int i = 1; i <= 4; i++) {
            viewModel.rollDice(6, 6, 6, 6, 6);
            viewModel.select(balut);
        }
        assertArrayEquals(new int[] {50, 50, 50, 50}, viewModel.getScores().getValue()[balut]);
        assertEquals(4, viewModel.getSelectCount().getValue()[balut]);
        assertEquals(1, viewModel.getNumSelected());
        assertEquals(200, viewModel.getCategoryScores().getValue()[balut]);
        assertEquals(212, viewModel.getTotalScore().getValue().intValue());
        assertEquals(8, viewModel.getCategoryPoints().getValue()[balut]);
        assertEquals(8, viewModel.getTotalPoints().getValue().intValue());

        // 已达到最大次数
        viewModel.rollDice(6, 6, 6, 6, 6);
        viewModel.select(balut);
        assertEquals(4, viewModel.getSelectCount().getValue()[balut]);
        assertEquals(1, viewModel.getNumSelected());
        assertEquals(212, viewModel.getTotalScore().getValue().intValue());
    }

    @Test
    public void testManualRollFlow() {
        // 初始：等待手动掷骰子
        assertEquals(3, viewModel.getRemainingRolls().getValue().intValue());
        assertFalse(viewModel.hasRolled());
        assertFalse(viewModel.isClickable());
        assertTrue(ArrayUtil.all(viewModel.getDiceEnabled().getValue(), false));
        assertTrue(viewModel.getRollButtonEnabled().getValue());

        // 第一次掷骰子后：骰子可用
        viewModel.rollDice();
        assertEquals(2, viewModel.getRemainingRolls().getValue().intValue());
        assertTrue(viewModel.hasRolled());
        assertTrue(viewModel.isClickable());
        assertTrue(ArrayUtil.all(viewModel.getDiceEnabled().getValue(), true));
        assertTrue(viewModel.getRollButtonEnabled().getValue());

        // 掷满次数后：骰子和Roll按钮不可用
        while (viewModel.getRemainingRolls().getValue() > 0)
            viewModel.rollDice();
        assertTrue(viewModel.hasRolled());
        assertTrue(viewModel.isClickable());
        assertTrue(ArrayUtil.all(viewModel.getDiceEnabled().getValue(), false));
        assertFalse(viewModel.getRollButtonEnabled().getValue());

        // 选择得分项后：恢复初始状态
        viewModel.select(SIXES.ordinal());
        assertEquals(3, viewModel.getRemainingRolls().getValue().intValue());
        assertFalse(viewModel.hasRolled());
        assertFalse(viewModel.isClickable());
        assertTrue(ArrayUtil.all(viewModel.getDiceEnabled().getValue(), false));
        assertTrue(viewModel.getRollButtonEnabled().getValue());
    }

    @Test
    public void testSelectObserver() {
        Observer<int[][]> scoresObserver = mock(Observer.class);
        Observer<int[]> selectCountObserver = mock(Observer.class);
        Observer<int[]> categoryScoresObserver = mock(Observer.class);
        Observer<int[]> categoryPointsObserver = mock(Observer.class);
        Observer<Integer> totalScoreObserver = mock(Observer.class);
        Observer<Integer> totalPointsObserver = mock(Observer.class);

        viewModel.getScores().observeForever(scoresObserver);
        viewModel.getSelectCount().observeForever(selectCountObserver);
        viewModel.getCategoryScores().observeForever(categoryScoresObserver);
        viewModel.getCategoryPoints().observeForever(categoryPointsObserver);
        viewModel.getTotalScore().observeForever(totalScoreObserver);
        viewModel.getTotalPoints().observeForever(totalPointsObserver);

        int balut = BALUT.ordinal();
        viewModel.rollDice(5, 5, 5, 5, 5);
        viewModel.select(balut);

        verify(scoresObserver, atLeastOnce()).onChanged(argThat(a -> a[balut][0] == 45));
        verify(selectCountObserver, atLeastOnce()).onChanged(argThat(a -> a[balut] == 1));
        verify(categoryScoresObserver, atLeastOnce()).onChanged(argThat(a -> a[balut] == 45));
        verify(categoryPointsObserver, atLeastOnce()).onChanged(argThat(a -> a[balut] == 2));
        verify(totalScoreObserver).onChanged(45);
        verify(totalPointsObserver).onChanged(2);
    }

    @Test
    public void testSelectAll() {
        doNothing().when(spyViewModel).gameOver();
        for (int i = 0; i < NUM_CATEGORIES; i++) {
            for (int j = 0; j < MAX_SELECTIONS; j++)
                spyViewModel.select(i);
        }
        verify(spyViewModel).gameOver();
    }

    @Test
    public void testCalculatePoints() {
        record TestCase(int category, int selectCount, int[] scores, int expected) {}
        TestCase[] testCases = {
                new TestCase(FOURS.ordinal(), 4, arr(12, 8, 16, 12), 0),
                new TestCase(FOURS.ordinal(), 4, arr(12, 12, 16, 12), 2),
                new TestCase(FOURS.ordinal(), 3, arr(12, 12, 16, 12), 0),
                new TestCase(FIVES.ordinal(), 4, arr(10, 5, 25, 20), 0),
                new TestCase(FIVES.ordinal(), 4, arr(15, 10, 25, 20), 2),
                new TestCase(SIXES.ordinal(), 4, arr(18, 12, 6, 0), 0),
                new TestCase(SIXES.ordinal(), 4, arr(18, 18, 18, 24), 2),
                new TestCase(STRAIGHT.ordinal(), 4, arr(15, 20, 0, 15), 0),
                new TestCase(STRAIGHT.ordinal(), 4, arr(15, 20, 20, 15), 4),
                new TestCase(FULL_HOUSE.ordinal(), 4, arr(0, 13, 0, 28), 0),
                new TestCase(FULL_HOUSE.ordinal(), 4, arr(7, 13, 22, 28), 3),
                new TestCase(CHOICE.ordinal(), 4, arr(20, 24, 18, 29), 0),
                new TestCase(CHOICE.ordinal(), 4, arr(24, 25, 26, 27), 2),
                new TestCase(BALUT.ordinal(), 4, arr(0, 0, 0, 0), 0),
                new TestCase(BALUT.ordinal(), 4, arr(0, 40, 0, 0), 2),
                new TestCase(BALUT.ordinal(), 4, arr(35, 40, 45, 50), 8)
        };
        for (var t : testCases)
            assertEquals(t.expected(), viewModel.calculatePoints(t.category(), t.selectCount(), t.scores()));
    }

    @Test
    public void testCalculatePointsOnlyForObtainedScore() {
        int fullHouse = FULL_HOUSE.ordinal(), choice = CHOICE.ordinal();
        for (int i = 1; i <= 3; i++) {
            viewModel.rollDice(6, 6, 6, 5, 5);
            viewModel.select(fullHouse);
            viewModel.rollDice(5, 5, 5, 5, 5);
            viewModel.select(choice);
        }
        viewModel.rollDice(6, 6, 5, 5, 5);
        assertArrayEquals(new int[] {28, 28, 28, 27}, viewModel.getScores().getValue()[fullHouse]);  // 最后一个是预估得分
        assertEquals(84, viewModel.getCategoryScores().getValue()[fullHouse]);
        assertEquals(0, viewModel.getCategoryPoints().getValue()[fullHouse]);
        assertArrayEquals(new int[] {25, 25, 25, 27}, viewModel.getScores().getValue()[choice]);  // 最后一个是预估得分
        assertEquals(75, viewModel.getCategoryScores().getValue()[choice]);
        assertEquals(0, viewModel.getCategoryPoints().getValue()[choice]);

        // 验证预估得分不参与计算点数
        viewModel.select(FIVES.ordinal());
        assertEquals(0, viewModel.getCategoryPoints().getValue()[fullHouse]);
        assertEquals(0, viewModel.getCategoryPoints().getValue()[choice]);
        assertEquals(0, viewModel.getTotalPoints().getValue().intValue());
    }

    @Test
    public void testCalculateTotalScorePoints() {
        List<Pair<Integer, Integer>> testCases = List.of(
                Pair.create(100, -2),
                Pair.create(299, -2),
                Pair.create(321, -1),
                Pair.create(369, 0),
                Pair.create(444, 1),
                Pair.create(482, 2),
                Pair.create(520, 3),
                Pair.create(575, 4),
                Pair.create(649, 5),
                Pair.create(650, 6),
                Pair.create(800, 6)
        );
        for (var t : testCases)
            assertEquals(t.second.intValue(), viewModel.calculateTotalScorePoints(t.first));
    }

    @Test
    public void testSupportedPlayerCounts() {
        assertEquals(List.of(1, 2), viewModel.getSupportedPlayerCounts());
        assertTrue(viewModel.supportsPlayerCountSelection());
        assertFalse(viewModel.isMultiplayer());
        assertEquals(1, viewModel.getNumPlayersValue());

        viewModel.setNumPlayers(2);
        assertEquals(2, viewModel.getNumPlayersValue());
        assertTrue(viewModel.isMultiplayer());
        assertNotSame(viewModel.data(PLAYER_HUMAN), viewModel.data(PLAYER_COMPUTER));
        assertEquals(PLAYER_HUMAN, viewModel.getCurrentPlayerValue());
        assertArrayEquals(new int[NUM_CATEGORIES], viewModel.getSelectCount().getValue());
    }

    /** 人类玩家选择一个得分项后轮到计算机玩家 */
    private void passToComputer(BalutGameViewModel vm) {
        vm.select(FOURS.ordinal());
    }

    @Test
    public void testTurnSwitchToComputer() {
        spyViewModel.setNumPlayers(2);
        passToComputer(spyViewModel);

        // 每位玩家只选择一个得分项就轮到对方
        assertEquals(PLAYER_COMPUTER, spyViewModel.getCurrentPlayerValue());
        assertFalse(spyViewModel.isHumanTurn());
        assertFalse(spyViewModel.isClickable());
        // 人类玩家的回合已结束，计算机玩家的回合由Handler延迟执行
        assertFalse(spyViewModel.getRollButtonEnabled().getValue());
        assertTrue(ArrayUtil.all(spyViewModel.getDiceEnabled().getValue(), false));
        verify(mockHandler).postDelayed(any(Runnable.class), anyLong());

        // 人类玩家不能再选择得分项
        spyViewModel.select(FOURS.ordinal());
        assertEquals(1, spyViewModel.data(PLAYER_HUMAN).selectCount.getValue()[FOURS.ordinal()]);
        // 当前玩家是计算机玩家，其数据仍为空
        assertArrayEquals(new int[NUM_CATEGORIES], spyViewModel.getSelectCount().getValue());
    }

    @Test
    public void testAnalyzeDiceToKeep() {
        spyViewModel.setNumPlayers(2);
        passToComputer(spyViewModel);

        spyViewModel.rollDice(6, 6, 6, 2, 3);
        spyViewModel.analyzeDiceToKeep();

        // 上区得分项优先级最高，保留3颗6点
        assertArrayEquals(new boolean[] {true, true, true, false, false},
                spyViewModel.getDiceLocked().getValue());
        verify(mockHandler, atLeastOnce()).postDelayed(any(Runnable.class), anyLong());
    }

    @Test
    public void testAnalyzeDiceToKeepForBalut() {
        spyViewModel.setNumPlayers(2);
        passToComputer(spyViewModel);

        // 5颗骰子点数相同时可直接得到Balut，无需再掷骰子
        spyViewModel.rollDice(6, 6, 6, 6, 6);
        spyViewModel.analyzeDiceToKeep();

        var computerData = spyViewModel.data(PLAYER_COMPUTER);
        assertEquals(1, computerData.selectCount.getValue()[BALUT.ordinal()]);
        assertEquals(50, computerData.score.getValue().intValue());
        // 计算机玩家选择后轮到人类玩家
        assertEquals(PLAYER_HUMAN, spyViewModel.getCurrentPlayerValue());
        assertTrue(spyViewModel.getRollButtonEnabled().getValue());
    }

    @Test
    public void testComputerChooseCategory() {
        spyViewModel.setNumPlayers(2);
        passToComputer(spyViewModel);

        spyViewModel.rollDice(6, 6, 6, 1, 2);
        assertEquals(SIXES.ordinal(), spyViewModel.computerChooseCategory());

        // 能立即得到高价值得分项时优先选择该得分项
        spyViewModel.rollDice(2, 3, 4, 5, 6);
        assertEquals(STRAIGHT.ordinal(), spyViewModel.computerChooseCategory());
    }

    @Test
    public void testGameOver() {
        var score = new BalutScore("2025-01-01", 400, 1, 0, 10, 0, 2);
        doReturn(score).when(spyViewModel).createScoreEntity();
        doReturn(6).when(spyViewModel).saveScoreToDatabase(any());
        Consumer<Object[]> gameOverAction = mock(Consumer.class);
        spyViewModel.setGameOverAction(gameOverAction);

        spyViewModel.gameOver();
        assertTrue(ArrayUtil.all(spyViewModel.getDiceEnabled().getValue(), false));
        assertFalse(spyViewModel.getRollButtonEnabled().getValue());
        verify(spyViewModel).createScoreEntity();
        verify(spyViewModel).saveScoreToDatabase(argThat(s -> ScoreUtil.isEqual(score, s)));
        verify(gameOverAction).accept(argThat(args ->
            ScoreUtil.isEqual(score, (BalutScore) args[0]) && (int) args[1] == 6));
    }

    @Test
    public void testCreateScoreEntity() {
        doNothing().when(spyViewModel).gameOver();
        for (int i = 0; i < NUM_CATEGORIES; i++) {
            for (int j = 0; j < MAX_SELECTIONS; j++) {
                spyViewModel.rollDice(6, 6, 6, 6, 6);
                spyViewModel.select(i);
            }
        }
        var score = spyViewModel.createScoreEntity();
        assertEquals(440, score.score);
        assertEquals(13, score.points);
        assertEquals(4, score.numBalut);
        assertEquals(1, score.numPlayers);
        assertEquals(0, score.computerScore);
    }

    @Test
    public void testCreateScoreEntityMultiplayer() {
        spyViewModel.setNumPlayers(2);
        doNothing().when(spyViewModel).gameOver();

        // 人类玩家和计算机玩家交替掷骰子、选择得分项，直到双方都选完所有格子
        for (int i = 0; i < NUM_CATEGORIES * MAX_SELECTIONS; i++) {
            spyViewModel.rollDice(6, 6, 6, 6, 6);
            spyViewModel.select(i % NUM_CATEGORIES);
            spyViewModel.rollDice(6, 6, 6, 6, 6);
            spyViewModel.doSelect(i % NUM_CATEGORIES);
        }

        var score = spyViewModel.createScoreEntity();
        assertEquals(2, score.numPlayers);
        assertEquals(440, score.score);
        assertEquals(13, score.points);
        assertEquals(4, score.numBalut);
        assertEquals(440, score.computerScore);
        assertEquals(13, score.computerPoints);
    }

    @Test
    public void testReset() {
        int balut = BALUT.ordinal();
        for (int i = 1; i <= 4; i++) {
            viewModel.rollDice(6, 6, 6, 6, 6);
            viewModel.select(balut);
        }
        assertArrayEquals(new int[] {50, 50, 50, 50}, viewModel.getScores().getValue()[balut]);
        assertEquals(4, viewModel.getSelectCount().getValue()[balut]);
        assertEquals(1, viewModel.getNumSelected());
        assertEquals(200, viewModel.getCategoryScores().getValue()[balut]);
        assertEquals(8, viewModel.getCategoryPoints().getValue()[balut]);
        assertEquals(200, viewModel.getTotalScore().getValue().intValue());
        assertEquals(8, viewModel.getTotalPoints().getValue().intValue());

        viewModel.reset();
        assertArrayEquals(new int[NUM_CATEGORIES][MAX_SELECTIONS], viewModel.getScores().getValue());
        assertArrayEquals(new int[NUM_CATEGORIES], viewModel.getSelectCount().getValue());
        assertEquals(0, viewModel.getNumSelected());
        assertArrayEquals(new int[NUM_CATEGORIES], viewModel.getCategoryScores().getValue());
        assertArrayEquals(new int[NUM_CATEGORIES], viewModel.getCategoryPoints().getValue());
        assertEquals(0, viewModel.getTotalScore().getValue().intValue());
        assertEquals(0, viewModel.getTotalScorePoints().getValue().intValue());
        assertEquals(0, viewModel.getTotalPoints().getValue().intValue());
    }
}
