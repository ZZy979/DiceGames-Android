package com.zzy.dicegames.ui.game.balut;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;

import static com.zzy.dicegames.ui.game.balut.BalutViewModel.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BalutViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private BalutViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new BalutViewModel();
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
        assertEquals(0, viewModel.getTotalScore().getValue().intValue());
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
            viewModel.setDiceNumbers(t.first);
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
            viewModel.setDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(STRAIGHT));
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
            viewModel.setDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(FULL_HOUSE));
        }
    }

    @Test
    public void testChoice() {
        List<Pair<int[], Integer>> testCases = List.of(
                Pair.create(arr(2, 4, 5, 5, 6), 22),
                Pair.create(arr(6, 6, 6, 6, 6), 30)
        );
        for (var t : testCases) {
            viewModel.setDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(CHOICE));
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
            viewModel.setDiceNumbers(t.first);
            assertEquals(t.second.intValue(), viewModel.calculateScore(BALUT));
        }
    }

    @Test
    public void testSelect() {
        viewModel.setDiceNumbers(1, 4, 5, 6, 6);
        viewModel.select(SIXES);
        assertArrayEquals(new int[] {12, 0, 0, 0}, viewModel.getScores().getValue()[SIXES]);
        assertEquals(1, viewModel.getSelectCount().getValue()[SIXES]);
        assertEquals(0, viewModel.getNumSelected());
        assertEquals(12, viewModel.getTotalScore().getValue().intValue());

        for (int i = 1; i <= 3; i++)
            viewModel.select(SIXES);
        assertArrayEquals(new int[] {12, 12, 12, 12}, viewModel.getScores().getValue()[SIXES]);
        assertEquals(4, viewModel.getSelectCount().getValue()[SIXES]);
        assertEquals(1, viewModel.getNumSelected());
        assertEquals(48, viewModel.getTotalScore().getValue().intValue());

        // 已达到最大次数
        viewModel.setDiceNumbers(6, 6, 6, 6, 6);
        viewModel.select(SIXES);
        assertArrayEquals(new int[] {12, 12, 12, 12}, viewModel.getScores().getValue()[SIXES]);
        assertEquals(4, viewModel.getSelectCount().getValue()[SIXES]);
        assertEquals(1, viewModel.getNumSelected());
        assertEquals(48, viewModel.getTotalScore().getValue().intValue());
    }

    @Test
    public void testSelectObserver() {
        Observer<int[][]> scoresObserver = mock(Observer.class);
        Observer<int[]> selectCountObserver = mock(Observer.class);
        Observer<Integer> totalScoreObserver = mock(Observer.class);
        viewModel.getScores().observeForever(scoresObserver);
        viewModel.getSelectCount().observeForever(selectCountObserver);
        viewModel.getTotalScore().observeForever(totalScoreObserver);

        viewModel.setDiceNumbers(5, 5, 5, 5, 5);
        viewModel.select(BALUT);

        verify(scoresObserver, atLeastOnce()).onChanged(argThat(a -> a[BALUT][0] == 45));
        verify(selectCountObserver, atLeastOnce()).onChanged(argThat(a -> a[BALUT] == 1));
        verify(totalScoreObserver).onChanged(45);
    }

    @Test
    public void testReset() {
        viewModel.setDiceNumbers(6, 6, 6, 6, 6);
        for (int i = 1; i <= 4; i++)
            viewModel.select(BALUT);
        assertArrayEquals(new int[] {50, 50, 50, 50}, viewModel.getScores().getValue()[BALUT]);
        assertEquals(4, viewModel.getSelectCount().getValue()[BALUT]);
        assertEquals(1, viewModel.getNumSelected());
        assertEquals(200, viewModel.getTotalScore().getValue().intValue());

        viewModel.reset();
        assertArrayEquals(new int[NUM_CATEGORIES][MAX_SELECTIONS], viewModel.getScores().getValue());
        assertArrayEquals(new int[NUM_CATEGORIES], viewModel.getSelectCount().getValue());
        assertEquals(0, viewModel.getNumSelected());
        assertEquals(0, viewModel.getTotalScore().getValue().intValue());
    }
}
