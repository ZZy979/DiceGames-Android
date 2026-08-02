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

import static com.zzy.dicegames.ui.game.yahtzee.FiveYahtzeeGameViewModel.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FiveYahtzeeGameViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private FiveYahtzeeGameViewModel viewModel;
    private FiveYahtzeeGameViewModel spyViewModel;

    @Mock
    private Handler mockHandler;

    @Before
    public void setUp() {
        viewModel = new FiveYahtzeeGameViewModel();
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
        assertEquals(14, viewModel.getNumCategories());
        assertEquals(63, viewModel.getBonusThreshold());
        assertEquals(50, viewModel.getBonusValue());
    }

    @Test
    public void testSetDiceNumbers() {
        viewModel.updateDiceNumbers(4, 1, 3, 2, 4);
        int[] expected = {1, 2, 3, 8, 0, 0, 0, 0, 0, 0, 30, 0, 14, 0};
        assertArrayEquals(expected, viewModel.getScores().getValue());
    }

    @Test
    public void testUpperSection() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(4, 3, 1, 4, 6), arr(1, 0, 3, 8, 0, 6)),
                Pair.create(arr(5, 2, 2, 6, 2), arr(0, 6, 0, 0, 5, 6)),
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
        viewModel.select(ONES);
        assertEquals(63, viewModel.getUpperTotalScore().getValue().intValue());
        assertEquals(50, viewModel.getBonusScore().getValue().intValue());
    }

    @Test
    public void testPairs() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(1, 1, 2, 2, 3), 9),
                Pair.create(arr(1, 1, 2, 2, 2), 8),
                Pair.create(arr(1, 1, 1, 2, 2), 7),
                Pair.create(arr(1, 1, 2, 3, 4), 0)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(TWO_PAIRS));
        }
    }

    @Test
    public void testOfAKind() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(1, 2, 2, 3, 4), arr(0, 0)),
                Pair.create(arr(2, 2, 2, 3, 4), arr(13, 0)),
                Pair.create(arr(3, 3, 3, 3, 5), arr(17, 17)),
                Pair.create(arr(6, 6, 6, 6, 6), arr(30, 30))
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second[0], viewModel.calculateScore(THREE_OF_A_KIND));
            assertEquals(t.second[1], viewModel.calculateScore(FOUR_OF_A_KIND));
        }
    }

    @Test
    public void testFullHouse() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(4, 4, 4, 5, 5), 25),
                Pair.create(arr(1, 1, 2, 2, 3), 0),
                Pair.create(arr(5, 5, 5, 5, 5), 0)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(FULL_HOUSE));
        }
    }

    @Test
    public void testStraight() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(1, 2, 3, 5, 6), arr(0, 0)),
                Pair.create(arr(1, 2, 2, 3, 4), arr(30, 0)),
                Pair.create(arr(2, 3, 4, 5, 5), arr(30, 0)),
                Pair.create(arr(1, 3, 4, 5, 6), arr(30, 0)),
                Pair.create(arr(1, 2, 3, 4, 5), arr(30, 40)),
                Pair.create(arr(2, 3, 4, 5, 6), arr(30, 40))
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second[0], viewModel.calculateScore(SMALL_STRAIGHT));
            assertEquals(t.second[1], viewModel.calculateScore(LARGE_STRAIGHT));
        }
    }

    @Test
    public void testChance() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(2, 4, 5, 5, 6), 22),
                Pair.create(arr(6, 6, 6, 6, 6), 30)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(CHANCE));
        }
    }

    @Test
    public void testYahtzee() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(1, 1, 1, 1, 1), 50),
                Pair.create(arr(6, 6, 6, 6, 6), 50),
                Pair.create(arr(6, 6, 6, 5, 6), 0)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(YAHTZEE));
        }
    }

    @Test
    public void testJoker() {
        viewModel.updateDiceNumbers(5, 2, 5, 4, 6);
        viewModel.select(FIVES);
        viewModel.select(YAHTZEE);

        viewModel.updateDiceNumbers(5, 5, 5, 5, 5);
        assertEquals(25, viewModel.calculateScore(TWO_PAIRS));
        assertEquals(25, viewModel.calculateScore(THREE_OF_A_KIND));
        assertEquals(25, viewModel.calculateScore(FOUR_OF_A_KIND));
        assertEquals(25, viewModel.calculateScore(FULL_HOUSE));
        assertEquals(30, viewModel.calculateScore(SMALL_STRAIGHT));
        assertEquals(40, viewModel.calculateScore(LARGE_STRAIGHT));
    }

    @Test
    public void testCreateScoreEntity() {
        doNothing().when(spyViewModel).gameOver();
        for (int i = 0; i < viewModel.getNumCategories(); i++) {
            spyViewModel.updateDiceNumbers(5, 5, 5, 5, 5);
            spyViewModel.select(i);
        }
        var score = spyViewModel.createScoreEntity();
        assertEquals(150, score.score);
        assertFalse(score.hasBonus);
        assertTrue(score.hasYahtzee);
    }
}
