package com.zzy.dicegames.ui.game.crag;

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

import static com.zzy.dicegames.ui.game.crag.CragGameViewModel.Category.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CragGameViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private CragGameViewModel viewModel;
    private CragGameViewModel spyViewModel;

    @Mock
    private Handler mockHandler;

    @Before
    public void setUp() {
        viewModel = new CragGameViewModel();
        viewModel.setHandler(mockHandler);
        spyViewModel = spy(viewModel);
    }

    private static int[] arr(int... a) {
        return a;
    }

    @Test
    public void testInitialization() {
        assertEquals(3, viewModel.getNumDice());
        assertEquals(2, viewModel.getMaxRolls());
        assertEquals(13, viewModel.getNumCategories());
        assertEquals(Integer.MAX_VALUE, viewModel.getBonusThreshold());
        assertEquals(0, viewModel.getBonusValue());
    }

    @Test
    public void testUpperSection() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(1, 4, 5), arr(1, 0, 0, 4, 5, 0)),
                Pair.create(arr(3, 3, 3), arr(0, 0, 9, 0, 0, 0)),
                Pair.create(arr(6, 6, 6), arr(0, 0, 0, 0, 0, 18)),
                Pair.create(arr(1, 2, 3), arr(1, 2, 3, 0, 0, 0))
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            for (int i = 0; i < 6; i++)
                assertEquals(t.second[i], viewModel.calculateScore(i));
        }
    }

    @Test
    public void testStraight() {
        List<Pair<int[], int[]>> testCases = List.of(
                Pair.create(arr(1, 2, 3), arr(20, 0, 0, 0)),
                Pair.create(arr(4, 5, 6), arr(0, 20, 0, 0)),
                Pair.create(arr(1, 3, 5), arr(0, 0, 20, 0)),
                Pair.create(arr(2, 4, 6), arr(0, 0, 0, 20)),
                Pair.create(arr(1, 2, 4), arr(0, 0, 0, 0)),
                Pair.create(arr(1, 1, 2), arr(0, 0, 0, 0))
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second[0], viewModel.calculateScore(LOW_STRAIGHT.ordinal()));
            assertEquals(t.second[1], viewModel.calculateScore(HIGH_STRAIGHT.ordinal()));
            assertEquals(t.second[2], viewModel.calculateScore(ODD_STRAIGHT.ordinal()));
            assertEquals(t.second[3], viewModel.calculateScore(EVEN_STRAIGHT.ordinal()));
        }
    }

    @Test
    public void testThreeOfAKind() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(1, 1, 1), 25),
                Pair.create(arr(6, 6, 6), 25),
                Pair.create(arr(2, 3, 4), 0)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(THREE_OF_A_KIND.ordinal()));
        }
    }

    @Test
    public void testThirteen() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(5, 4, 4), 26),
                Pair.create(arr(3, 4, 6), 26),
                Pair.create(arr(6, 5, 4), 0)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(THIRTEEN.ordinal()));
        }
    }

    @Test
    public void testCrag() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(4, 4, 5), 50),
                Pair.create(arr(1, 6, 6), 50),
                Pair.create(arr(5, 5, 3), 50),
                Pair.create(arr(6, 5, 2), 0),
                Pair.create(arr(5, 4, 3), 0),
                Pair.create(arr(3, 3, 3), 0)
        );
        for (var t : testCases) {
            viewModel.updateDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(CRAG.ordinal()));
        }
    }

    @Test
    public void testCreateScoreEntity() {
        doNothing().when(spyViewModel).gameOver();
        for (int i = 0; i < viewModel.getNumCategories(); i++) {
            spyViewModel.updateDiceNumbers(4, 4, 5);
            spyViewModel.select(i);
        }
        var score = spyViewModel.createScoreEntity();
        assertEquals(89, score.score);
        assertTrue(score.hasCrag);
    }
}
