package com.zzy.dicegames.ui.game.yatzy;

import android.os.Handler;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.core.util.Pair;

import static com.zzy.dicegames.ui.game.yatzy.YatzyGameViewModel.Category.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class YatzyGameViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private YatzyGameViewModel viewModel;
    private YatzyGameViewModel spyViewModel;

    @Mock
    private Handler mockHandler;

    @Before
    public void setUp() {
        viewModel = new YatzyGameViewModel();
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
        assertEquals(15, viewModel.getNumCategories());
        assertEquals(63, viewModel.getBonusThreshold());
        assertEquals(50, viewModel.getBonusValue());
    }

    @Test
    public void testSetDiceNumbers() {
        viewModel.updateDiceNumbers(3, 4, 5, 5, 3);
        int[] expected = {0, 0, 6, 4, 10, 0, 10, 16, 0, 0, 0, 0, 0, 20, 0};
        assertArrayEquals(expected, viewModel.getScores().getValue());
    }

    @Test
    public void testUpperSection() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(1, 4, 5, 4, 1), arr(2, 0, 0, 8, 5, 0)),
                Pair.create(arr(3, 2, 2, 2, 3), arr(0, 6, 6, 0, 0, 0)),
                Pair.create(arr(6, 6, 6, 6, 6), arr(0, 0, 0, 0, 0, 30)),
                Pair.create(arr(1, 2, 3, 4, 5), arr(1, 2, 3, 4, 5, 0))
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            for (int i = 0; i < 6; i++)
                assertEquals(t.second[i], viewModel.calculateScore(i));
        }
    }

    @Test
    public void testBonus() {
        for (int i = 6; i >= 2; i--) {
            viewModel.updateDiceNumbers(i, i, i, i - 1, i - 1);
            viewModel.select(i - 1);
        }
        assertEquals(60, viewModel.getUpperTotalScore().getValue().intValue());
        assertEquals(0, viewModel.getBonusScore().getValue().intValue());

        viewModel.updateDiceNumbers(1, 1, 1, 2, 2);
        viewModel.select(ONES.ordinal());
        assertEquals(63, viewModel.getUpperTotalScore().getValue().intValue());
        assertEquals(50, viewModel.getBonusScore().getValue().intValue());
    }

    @Test
    public void testPairs() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(1, 2, 3, 4, 5), arr(0, 0)),
                Pair.create(arr(2, 2, 3, 4, 5), arr(4, 0)),
                Pair.create(arr(2, 2, 3, 3, 5), arr(6, 10)),
                Pair.create(arr(3, 3, 4, 4, 6), arr(8, 14)),
                Pair.create(arr(3, 4, 4, 5, 5), arr(10, 18)),
                Pair.create(arr(4, 4, 4, 4, 5), arr(8, 0))
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second[0], viewModel.calculateScore(ONE_PAIR.ordinal()));
            assertEquals(t.second[1], viewModel.calculateScore(TWO_PAIRS.ordinal()));
        }
    }

    @Test
    public void testOfAKind() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(1, 2, 2, 3, 4), arr(0, 0)),
                Pair.create(arr(2, 2, 2, 3, 4), arr(6, 0)),
                Pair.create(arr(3, 3, 3, 3, 5), arr(9, 12)),
                Pair.create(arr(6, 6, 6, 6, 6), arr(18, 24))
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second[0], viewModel.calculateScore(THREE_OF_A_KIND.ordinal()));
            assertEquals(t.second[1], viewModel.calculateScore(FOUR_OF_A_KIND.ordinal()));
        }
    }

    @Test
    public void testStraight() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(1, 2, 3, 5, 6), arr(0, 0)),
                Pair.create(arr(1, 2, 3, 4, 4), arr(0, 0)),
                Pair.create(arr(2, 2, 3, 4, 5), arr(0, 0)),
                Pair.create(arr(1, 2, 3, 4, 5), arr(15, 0)),
                Pair.create(arr(2, 3, 4, 5, 6), arr(0, 20))
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second[0], viewModel.calculateScore(SMALL_STRAIGHT.ordinal()));
            assertEquals(t.second[1], viewModel.calculateScore(LARGE_STRAIGHT.ordinal()));
        }
    }

    @Test
    public void testFullHouse() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(4, 4, 4, 5, 5), 22),
                Pair.create(arr(1, 1, 2, 2, 3), 0),
                Pair.create(arr(2, 2, 3, 5, 5), 0),
                Pair.create(arr(5, 5, 5, 5, 5), 0)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(FULL_HOUSE.ordinal()));
        }
    }

    @Test
    public void testChance() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(6, 1, 2, 4, 5), 18),
                Pair.create(arr(2, 2, 5, 5, 5), 19),
                Pair.create(arr(6, 6, 6, 6, 6), 30)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(CHANCE.ordinal()));
        }
    }

    @Test
    public void testYatzy() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(1, 1, 1, 1, 1), 50),
                Pair.create(arr(6, 6, 6, 6, 6), 50),
                Pair.create(arr(5, 5, 5, 5, 6), 0)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(YATZY.ordinal()));
        }
    }

    @Test
    public void testCreateScoreEntity() {
        doNothing().when(spyViewModel).gameOver();
        for (int i = 0; i < viewModel.getNumCategories(); i++) {
            spyViewModel.updateDiceNumbers(5, 5, 5, 5, 5);
            spyViewModel.select(i);
        }
        var score = spyViewModel.createScoreEntity();
        assertEquals(145, score.score);
        assertFalse(score.hasBonus);
        assertTrue(score.hasYatzy);
    }
}
