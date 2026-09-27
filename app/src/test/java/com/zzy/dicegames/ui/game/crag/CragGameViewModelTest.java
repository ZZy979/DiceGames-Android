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

import static com.zzy.dicegames.ui.game.BaseGameViewModel.PLAYER_COMPUTER;
import static com.zzy.dicegames.ui.game.BaseGameViewModel.PLAYER_HUMAN;
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
            spyViewModel.rollDice(4, 4, 5);
            spyViewModel.select(i);
        }
        var score = spyViewModel.createScoreEntity();
        assertEquals(89, score.score);
        assertTrue(score.hasCrag);
    }

    @Test
    public void testPlayerCount() {
        assertTrue(viewModel.supportsPlayerCountSelection());
        assertEquals(List.of(1, 2), viewModel.getSupportedPlayerCounts());
        assertEquals(1, viewModel.getNumPlayersValue());
        assertFalse(viewModel.isMultiplayer());

        viewModel.setNumPlayers(2);
        assertEquals(2, viewModel.getNumPlayersValue());
        assertTrue(viewModel.isMultiplayer());
        assertEquals(PLAYER_HUMAN, viewModel.getCurrentPlayerValue());
        assertThrows(IllegalArgumentException.class, () -> viewModel.setNumPlayers(3));
    }

    @Test
    public void testTwoPlayersTurnFlow() {
        viewModel.setNumPlayers(2);

        viewModel.updateDiceNumbers(4, 5, 4);
        viewModel.select(CRAG.ordinal());
        assertEquals(50, viewModel.getPlayerScoreValue(PLAYER_HUMAN));
        assertEquals(PLAYER_COMPUTER, viewModel.getCurrentPlayerValue());

        // 计算机回合时人类不能替计算机选择得分项
        viewModel.select(ONES.ordinal());
        assertFalse(viewModel.data(PLAYER_COMPUTER).selected.getValue()[ONES.ordinal()]);
    }

    @Test
    public void testCreateScoreEntity_Multiplayer() {
        // 单人局：不记录计算机得分
        assertEquals(1, viewModel.createScoreEntity().numPlayers);
        assertEquals(0, viewModel.createScoreEntity().computerScore);

        // 双人局：记录玩家数量和计算机得分
        viewModel.setNumPlayers(2);
        viewModel.updateDiceNumbers(6, 6, 6);
        viewModel.select(THREE_OF_A_KIND.ordinal());
        viewModel.data(PLAYER_COMPUTER).score.setValue(100);

        var score = viewModel.createScoreEntity();
        assertEquals(2, score.numPlayers);
        assertEquals(25, score.score);
        assertEquals(100, score.computerScore);
    }

    @Test
    public void testComputerChooseCategory() {
        // 能得分的高价值得分项优先选择，不再掷骰子
        viewModel.updateDiceNumbers(6, 6, 6);
        assertEquals(THREE_OF_A_KIND.ordinal(), viewModel.computerChooseCategory());

        viewModel.updateDiceNumbers(1, 2, 3);
        assertEquals(LOW_STRAIGHT.ordinal(), viewModel.computerChooseCategory());

        viewModel.updateDiceNumbers(1, 3, 5);
        assertEquals(ODD_STRAIGHT.ordinal(), viewModel.computerChooseCategory());

        viewModel.updateDiceNumbers(2, 4, 6);
        assertEquals(EVEN_STRAIGHT.ordinal(), viewModel.computerChooseCategory());

        viewModel.updateDiceNumbers(4, 5, 6);
        assertEquals(HIGH_STRAIGHT.ordinal(), viewModel.computerChooseCategory());

        // 都不得分时选择优先级最高的上区得分项
        viewModel.updateDiceNumbers(4, 4, 6);
        assertEquals(FOURS.ordinal(), viewModel.computerChooseCategory());
    }

    @Test
    public void testChooseDiceToKeep() {
        // 1、2：只差一颗就能得到小顺，保留1、2
        viewModel.updateDiceNumbers(1, 2, 4);
        assertArrayEquals(new boolean[] {true, true, false}, viewModel.chooseDiceToKeep(1));

        // 3、3、5：只差一颗就能得到奇顺，保留一颗3和5
        viewModel.updateDiceNumbers(3, 3, 5);
        assertArrayEquals(new boolean[] {true, false, true}, viewModel.chooseDiceToKeep(1));

        // 1、1、4：两颗骰子凑不出9点，改为保留点数最大的4点去凑13
        viewModel.updateDiceNumbers(1, 1, 4);
        assertArrayEquals(new boolean[] {false, false, true}, viewModel.chooseDiceToKeep(1));
    }

    @Test
    public void testAnalyzeDiceToKeep_ChooseCategory() {
        // 能直接得到偶顺时选择偶顺，不再保留骰子
        viewModel.rollDice(2, 4, 6);
        viewModel.analyzeDiceToKeep();

        assertTrue(viewModel.data(PLAYER_HUMAN).selected.getValue()[EVEN_STRAIGHT.ordinal()]);
    }

    @Test
    public void testAnalyzeDiceToKeep_KeepDice() {
        // 不能直接得分时锁定要保留的骰子并继续掷骰子
        viewModel.rollDice(1, 2, 4);
        viewModel.analyzeDiceToKeep();

        assertArrayEquals(new boolean[] {true, true, false},
                viewModel.getDiceLocked().getValue());
        verify(mockHandler).postDelayed(any(Runnable.class), anyLong());
    }
}
