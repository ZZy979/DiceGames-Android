package com.zzy.dicegames.ui.game.yahtzee;

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

import static com.zzy.dicegames.ui.game.yahtzee.SixYahtzeeViewModel.*;
import static org.junit.Assert.*;

public class SixYahtzeeScoreBoardViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private SixYahtzeeViewModel viewModel;

    @Mock
    private Handler mockHandler;

    @Before
    public void setUp() {
        viewModel = new SixYahtzeeViewModel();
        viewModel.setHandler(mockHandler);
    }

    private static int[] arr(int... a) {
        return a;
    }

    @Test
    public void testInitialization() {
        assertEquals(6, viewModel.getNumDice());
        assertEquals(3, viewModel.getMaxRolls());
        assertEquals(20, viewModel.getNumCategories());
        assertEquals(84, viewModel.getBonusThreshold());
        assertEquals(100, viewModel.getBonusValue());
    }

    @Test
    public void testSetDiceNumbers() {
        viewModel.updateDiceNumbers(3, 4, 5, 5, 3, 5);
        int[] expected = {0, 0, 6, 4, 15, 0, 10, 16, 0, 15, 0, 0, 0, 0, 0, 21, 0, 0, 25, 0};
        assertArrayEquals(expected, viewModel.getScores().getValue());
    }

    @Test
    public void testUpperSection() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(1, 4, 5, 4, 1, 2), arr(2, 2, 0, 8, 5, 0)),
                Pair.create(arr(3, 2, 2, 2, 3, 2), arr(0, 8, 6, 0, 0, 0)),
                Pair.create(arr(6, 6, 6, 6, 6, 6), arr(0, 0, 0, 0, 0, 36)),
                Pair.create(arr(1, 2, 3, 4, 5, 6), arr(1, 2, 3, 4, 5, 6))
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
            viewModel.updateDiceNumbers(i, i, i, i, i - 1, i - 1);
            viewModel.select(i - 1);
        }
        assertEquals(80, viewModel.getUpperTotalScore().getValue().intValue());
        assertEquals(0, viewModel.getBonusScore().getValue().intValue());

        viewModel.updateDiceNumbers(1, 1, 1, 1, 2, 2);
        viewModel.select(ONES);
        assertEquals(84, viewModel.getUpperTotalScore().getValue().intValue());
        assertEquals(100, viewModel.getBonusScore().getValue().intValue());
    }

    @Test
    public void testPairs() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(1, 2, 3, 4, 5, 6), arr(0, 0, 0)),
                Pair.create(arr(2, 2, 3, 4, 5, 6), arr(4, 0, 0)),
                Pair.create(arr(2, 2, 3, 3, 5, 6), arr(6, 10, 0)),
                Pair.create(arr(2, 2, 3, 3, 6, 6), arr(12, 18, 22)),
                Pair.create(arr(3, 4, 4, 5, 5, 5), arr(10, 18, 0)),
                Pair.create(arr(4, 4, 4, 4, 4, 5), arr(8, 0, 0))
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second[0], viewModel.calculateScore(ONE_PAIR));
            assertEquals(t.second[1], viewModel.calculateScore(TWO_PAIRS));
            assertEquals(t.second[2], viewModel.calculateScore(THREE_PAIRS));
        }
    }

    @Test
    public void testOfAKind() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(1, 2, 2, 3, 4, 5), arr(0, 0, 0)),
                Pair.create(arr(1, 2, 4, 4, 4, 5), arr(12, 0, 0)),
                Pair.create(arr(1, 3, 5, 5, 5, 5), arr(15, 20, 0)),
                Pair.create(arr(4, 4, 4, 4, 4, 5), arr(12, 16, 20)),
                Pair.create(arr(6, 6, 6, 6, 6, 6), arr(18, 24, 30))
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second[0], viewModel.calculateScore(THREE_OF_A_KIND));
            assertEquals(t.second[1], viewModel.calculateScore(FOUR_OF_A_KIND));
            assertEquals(t.second[2], viewModel.calculateScore(FIVE_OF_A_KIND));
        }
    }

    @Test
    public void testStraight() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(1, 2, 3, 5, 5, 6), arr(0, 0, 0)),
                Pair.create(arr(1, 2, 3, 3, 4, 4), arr(0, 0, 0)),
                Pair.create(arr(1, 2, 3, 4, 4, 5), arr(15, 0, 0)),
                Pair.create(arr(2, 2, 3, 4, 5, 6), arr(0, 20, 0)),
                Pair.create(arr(1, 2, 3, 4, 5, 6), arr(15, 20, 21))
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second[0], viewModel.calculateScore(SMALL_STRAIGHT));
            assertEquals(t.second[1], viewModel.calculateScore(LARGE_STRAIGHT));
            assertEquals(t.second[2], viewModel.calculateScore(FULL_STRAIGHT));
        }
    }

    @Test
    public void testHutHouseTower() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(2, 2, 3, 5, 5, 6), arr(0, 0, 0)),
                Pair.create(arr(2, 2, 5, 6, 6, 6), arr(22, 0, 0)),
                Pair.create(arr(3, 3, 3, 5, 5, 5), arr(21, 24, 0)),
                Pair.create(arr(5, 5, 6, 6, 6, 6), arr(28, 0, 34)),
                Pair.create(arr(1, 1, 1, 1, 1, 3), arr(0, 0, 0)),
                Pair.create(arr(4, 4, 4, 4, 4, 4), arr(0, 0, 0))
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second[0], viewModel.calculateScore(HUT));
            assertEquals(t.second[1], viewModel.calculateScore(HOUSE));
            assertEquals(t.second[2], viewModel.calculateScore(TOWER));
        }
    }

    @Test
    public void testChance() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(6, 1, 2, 4, 5, 1), 19),
                Pair.create(arr(2, 2, 5, 5, 5, 6), 25),
                Pair.create(arr(6, 6, 6, 6, 6, 6), 36)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(CHANCE));
        }
    }

    @Test
    public void testYahtzee() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(1, 1, 1, 1, 1, 1), 100),
                Pair.create(arr(6, 6, 6, 6, 6, 6), 100),
                Pair.create(arr(5, 5, 5, 5, 5, 6), 0)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(YAHTZEE));
        }
    }

    @Test
    public void testJoker() {
        viewModel.updateDiceNumbers(1, 2, 3, 5, 6, 6);
        viewModel.select(SIXES);
        viewModel.select(YAHTZEE);

        viewModel.updateDiceNumbers(6, 6, 6, 6, 6, 6);
        assertEquals(36, viewModel.calculateScore(ONE_PAIR));
        assertEquals(36, viewModel.calculateScore(TWO_PAIRS));
        assertEquals(36, viewModel.calculateScore(THREE_PAIRS));
        assertEquals(36, viewModel.calculateScore(THREE_OF_A_KIND));
        assertEquals(36, viewModel.calculateScore(FOUR_OF_A_KIND));
        assertEquals(36, viewModel.calculateScore(FIVE_OF_A_KIND));
        assertEquals(15, viewModel.calculateScore(SMALL_STRAIGHT));
        assertEquals(20, viewModel.calculateScore(LARGE_STRAIGHT));
        assertEquals(21, viewModel.calculateScore(FULL_STRAIGHT));
        assertEquals(36, viewModel.calculateScore(HUT));
        assertEquals(36, viewModel.calculateScore(HOUSE));
        assertEquals(36, viewModel.calculateScore(TOWER));
    }
}
