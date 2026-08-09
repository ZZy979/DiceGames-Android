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
        assertArrayEquals(new int[NUM_CATEGORIES][MAX_SELECTIONS], viewModel.getScores().getValue());
        assertArrayEquals(new int[NUM_CATEGORIES], viewModel.getSelectCount().getValue());
        assertEquals(0, viewModel.getNumSelected());
        assertArrayEquals(new int[NUM_CATEGORIES], viewModel.getCategoryScores().getValue());
        assertArrayEquals(new int[NUM_CATEGORIES], viewModel.getCategoryPoints().getValue());
        assertEquals(0, viewModel.getTotalScore().getValue().intValue());
        assertEquals(0, viewModel.getTotalScorePoints().getValue().intValue());
        assertEquals(0, viewModel.getTotalPoints().getValue().intValue());
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
        viewModel.updateDiceNumbers(1, 4, 5, 6, 6);
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
            viewModel.updateDiceNumbers(6, 6, 6, 6, 6);
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
        viewModel.updateDiceNumbers(6, 6, 6, 6, 6);
        viewModel.select(balut);
        assertEquals(4, viewModel.getSelectCount().getValue()[balut]);
        assertEquals(1, viewModel.getNumSelected());
        assertEquals(212, viewModel.getTotalScore().getValue().intValue());
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
        viewModel.updateDiceNumbers(5, 5, 5, 5, 5);
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
            viewModel.updateDiceNumbers(6, 6, 6, 5, 5);
            viewModel.select(fullHouse);
            viewModel.updateDiceNumbers(5, 5, 5, 5, 5);
            viewModel.select(choice);
        }
        viewModel.updateDiceNumbers(6, 6, 5, 5, 5);
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
    public void testGameOver() {
        var score = new BalutScore("2025-01-01", 400, 10, 2);
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
                spyViewModel.updateDiceNumbers(6, 6, 6, 6, 6);
                spyViewModel.select(i);
            }
        }
        var score = spyViewModel.createScoreEntity();
        assertEquals(440, score.score);
        assertEquals(13, score.points);
        assertEquals(4, score.numBalut);
    }

    @Test
    public void testReset() {
        int balut = BALUT.ordinal();
        for (int i = 1; i <= 4; i++) {
            viewModel.updateDiceNumbers(6, 6, 6, 6, 6);
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
