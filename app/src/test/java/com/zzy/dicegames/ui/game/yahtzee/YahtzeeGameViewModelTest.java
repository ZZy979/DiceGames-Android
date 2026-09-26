package com.zzy.dicegames.ui.game.yahtzee;

import android.os.Handler;

import com.zzy.dicegames.ui.game.yahtzee.BaseYahtzeeGameViewModel.YahtzeeGameData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.core.util.Pair;

import static com.zzy.dicegames.ui.game.BaseGameViewModel.PLAYER_COMPUTER;
import static com.zzy.dicegames.ui.game.BaseGameViewModel.PLAYER_HUMAN;
import static com.zzy.dicegames.ui.game.yahtzee.YahtzeeGameViewModel.Category.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class YahtzeeGameViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private YahtzeeGameViewModel viewModel;
    private YahtzeeGameViewModel spyViewModel;

    @Mock
    private Handler mockHandler;

    @Before
    public void setUp() {
        viewModel = new YahtzeeGameViewModel();
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
        assertEquals(13, viewModel.getNumCategories());
        assertEquals(63, viewModel.getBonusThreshold());
        assertEquals(35, viewModel.getBonusValue());
    }

    @Test
    public void testUpdateDiceNumbers() {
        viewModel.updateDiceNumbers(4, 1, 3, 2, 4);
        int[] expected = {1, 2, 3, 8, 0, 0, 0, 0, 0, 30, 0, 14, 0};
        assertArrayEquals(expected, viewModel.data(PLAYER_HUMAN).scores.getValue());
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
        YahtzeeGameData data = viewModel.data(PLAYER_HUMAN);
        for (int i = 6; i >= 2; i--) {
            viewModel.updateDiceNumbers(i, i, i, i - 1, i - 1);
            viewModel.select(i - 1);
        }
        assertEquals(60, data.upperTotalScore.getValue().intValue());
        assertEquals(0, data.bonusScore.getValue().intValue());

        viewModel.updateDiceNumbers(1, 1, 1, 2, 2);
        viewModel.select(ONES.ordinal());
        assertEquals(63, data.upperTotalScore.getValue().intValue());
        assertEquals(35, data.bonusScore.getValue().intValue());
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
            assertEquals(t.second[0], viewModel.calculateScore(THREE_OF_A_KIND.ordinal()));
            assertEquals(t.second[1], viewModel.calculateScore(FOUR_OF_A_KIND.ordinal()));
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
            assertEquals(t.second.intValue(), viewModel.calculateScore(FULL_HOUSE.ordinal()));
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
            assertEquals(t.second[0], viewModel.calculateScore(SMALL_STRAIGHT.ordinal()));
            assertEquals(t.second[1], viewModel.calculateScore(LARGE_STRAIGHT.ordinal()));
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
            assertEquals(t.second.intValue(), viewModel.calculateScore(CHANCE.ordinal()));
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
            assertEquals(t.second.intValue(), viewModel.calculateScore(YAHTZEE.ordinal()));
        }
    }

    @Test
    public void testJoker() {
        viewModel.updateDiceNumbers(5, 2, 5, 4, 6);
        viewModel.select(FIVES.ordinal());
        viewModel.select(YAHTZEE.ordinal());

        viewModel.updateDiceNumbers(5, 5, 5, 5, 5);
        assertEquals(25, viewModel.calculateScore(THREE_OF_A_KIND.ordinal()));
        assertEquals(25, viewModel.calculateScore(FOUR_OF_A_KIND.ordinal()));
        assertEquals(25, viewModel.calculateScore(FULL_HOUSE.ordinal()));
        assertEquals(30, viewModel.calculateScore(SMALL_STRAIGHT.ordinal()));
        assertEquals(40, viewModel.calculateScore(LARGE_STRAIGHT.ordinal()));
    }

    @Test
    public void testCreateScoreEntity() {
        doNothing().when(spyViewModel).gameOver();
        for (int i = 0; i < viewModel.getNumCategories(); i++) {
            spyViewModel.rollDice(5, 5, 5, 5, 5);
            spyViewModel.select(i);
        }
        var score = spyViewModel.createScoreEntity();
        assertEquals(150, score.score);
        assertFalse(score.hasBonus);
        assertTrue(score.hasYahtzee);
    }

    @Test
    public void testPlayerCount() {
        assertTrue(viewModel.supportsPlayerCountSelection());
        assertEquals(List.of(1, 2), viewModel.getSupportedPlayerCounts());
        assertThrows(IllegalArgumentException.class, () -> viewModel.setNumPlayers(3));
    }

    @Test
    public void testTwoPlayersTurnFlow() {
        viewModel.setNumPlayers(2);
        assertEquals(2, viewModel.getNumPlayersValue());
        assertTrue(viewModel.isMultiplayer());
        assertEquals(PLAYER_HUMAN, viewModel.getCurrentPlayerValue());

        viewModel.updateDiceNumbers(5, 5, 5, 5, 5);
        viewModel.select(YAHTZEE.ordinal());
        assertEquals(50, viewModel.getPlayerScoreValue(PLAYER_HUMAN));
        assertEquals(PLAYER_COMPUTER, viewModel.getCurrentPlayerValue());

        // 计算机回合时人类不能替计算机选择得分项
        viewModel.select(ONES.ordinal());
        assertFalse(viewModel.data(PLAYER_COMPUTER).selected.getValue()[ONES.ordinal()]);
    }

    @Test
    public void testComputerChooseCategory() {
        // 能得Yahtzee时优先选择Yahtzee
        viewModel.updateDiceNumbers(5, 5, 5, 5, 5);
        assertEquals(YAHTZEE.ordinal(), viewModel.computerChooseCategory());

        // 得分相同时选择优先级更高的得分项
        viewModel.updateDiceNumbers(3, 3, 3, 3, 5);
        assertEquals(FOUR_OF_A_KIND.ordinal(), viewModel.computerChooseCategory());

        // 只有CHANCE能得分时选择CHANCE
        viewModel.updateDiceNumbers(1, 1, 2, 3, 6);
        assertEquals(CHANCE.ordinal(), viewModel.computerChooseCategory());
    }

    @Test
    public void testComputerChooseCategory_NoScore() {
        // 上区和CHANCE已选完且其余项都不得分时，放弃优先级最低的THREE_OF_A_KIND
        for (int c = 0; c <= SIXES.ordinal(); c++)
            viewModel.select(c);
        viewModel.select(CHANCE.ordinal());

        viewModel.updateDiceNumbers(2, 2, 3, 4, 6);
        assertEquals(THREE_OF_A_KIND.ordinal(), viewModel.computerChooseCategory());
    }

    @Test
    public void testComputerChooseKeep() {
        viewModel.updateDiceNumbers(3, 1, 3, 5, 2);
        assertArrayEquals(new boolean[] {true, false, true, false, false},
                viewModel.computerChooseKeep(THREES.ordinal()));

        viewModel.updateDiceNumbers(3, 3, 5, 1, 3);
        assertArrayEquals(new boolean[] {true, true, false, false, true},
                viewModel.computerChooseKeep(THREE_OF_A_KIND.ordinal()));

        viewModel.updateDiceNumbers(1, 2, 3, 4, 6);
        assertArrayEquals(new boolean[] {true, true, true, true, false},
                viewModel.computerChooseKeep(SMALL_STRAIGHT.ordinal()));

        // 连顺中重复的点数只保留一颗，其余重掷（保留2、3、4、5，重掷重复的2）
        viewModel.updateDiceNumbers(2, 3, 4, 5, 2);
        assertArrayEquals(new boolean[] {true, true, true, true, false},
                viewModel.computerChooseKeep(SMALL_STRAIGHT.ordinal()));

        viewModel.updateDiceNumbers(1, 5, 6, 2, 6);
        assertArrayEquals(new boolean[] {false, true, true, false, true},
                viewModel.computerChooseKeep(CHANCE.ordinal()));
    }

    @Test
    public void testComputerShouldRollAgain() {
        // 上区：未集满5个时继续掷骰子（掷出3个3点也不应浪费剩下的掷骰子机会）
        viewModel.updateDiceNumbers(3, 3, 4, 5, 6);
        assertTrue(viewModel.computerShouldRollAgain(THREES.ordinal()));
        viewModel.updateDiceNumbers(3, 3, 3, 5, 6);
        assertTrue(viewModel.computerShouldRollAgain(THREES.ordinal()));
        viewModel.updateDiceNumbers(3, 3, 3, 3, 3);
        assertFalse(viewModel.computerShouldRollAgain(THREES.ordinal()));

        // 连顺：小顺已得分但保留的骰子是4连顺时继续掷骰子，争取大顺
        viewModel.updateDiceNumbers(1, 2, 3, 5, 6);
        assertTrue(viewModel.computerShouldRollAgain(SMALL_STRAIGHT.ordinal()));
        viewModel.updateDiceNumbers(1, 2, 3, 4, 6);
        assertTrue(viewModel.computerShouldRollAgain(SMALL_STRAIGHT.ordinal()));
        assertTrue(viewModel.computerShouldRollAgain(LARGE_STRAIGHT.ordinal()));
        viewModel.updateDiceNumbers(1, 2, 3, 4, 5);
        assertFalse(viewModel.computerShouldRollAgain(LARGE_STRAIGHT.ordinal()));

        // 葫芦：已成葫芦时不再掷骰子
        viewModel.updateDiceNumbers(4, 4, 5, 5, 6);
        assertTrue(viewModel.computerShouldRollAgain(FULL_HOUSE.ordinal()));
        viewModel.updateDiceNumbers(4, 4, 5, 5, 5);
        assertFalse(viewModel.computerShouldRollAgain(FULL_HOUSE.ordinal()));

        // 四条：已有4个同点时继续掷骰子，争取Yahtzee
        viewModel.updateDiceNumbers(6, 6, 6, 6, 5);
        assertTrue(viewModel.computerShouldRollAgain(FOUR_OF_A_KIND.ordinal()));
        viewModel.updateDiceNumbers(6, 6, 6, 6, 6);
        assertFalse(viewModel.computerShouldRollAgain(YAHTZEE.ordinal()));
    }

    @Test
    public void testComputerKeepsRollingForBetterScore() {
        // 3个6点：不直接选6，而是保留3个6点继续掷骰子
        viewModel.updateDiceNumbers(6, 6, 6, 2, 3);
        assertEquals(THREE_OF_A_KIND.ordinal(), viewModel.computerChooseCategory());
        assertTrue(viewModel.computerShouldRollAgain(THREE_OF_A_KIND.ordinal()));
        assertArrayEquals(new boolean[] {true, true, true, false, false},
                viewModel.computerChooseKeep(THREE_OF_A_KIND.ordinal()));

        // 2、3、4、5：保留4连顺并重掷多余的一颗，争取大顺而非直接选小顺
        viewModel.updateDiceNumbers(2, 3, 4, 5, 2);
        assertEquals(SMALL_STRAIGHT.ordinal(), viewModel.computerChooseCategory());
        assertTrue(viewModel.computerShouldRollAgain(SMALL_STRAIGHT.ordinal()));
        assertArrayEquals(new boolean[] {true, true, true, true, false},
                viewModel.computerChooseKeep(SMALL_STRAIGHT.ordinal()));

        // 4个6点：保留4个6点继续掷骰子，争取Yahtzee
        viewModel.updateDiceNumbers(6, 6, 6, 6, 3);
        assertEquals(FOUR_OF_A_KIND.ordinal(), viewModel.computerChooseCategory());
        assertTrue(viewModel.computerShouldRollAgain(FOUR_OF_A_KIND.ordinal()));
        assertArrayEquals(new boolean[] {true, true, true, true, false},
                viewModel.computerChooseKeep(FOUR_OF_A_KIND.ordinal()));
    }
}
