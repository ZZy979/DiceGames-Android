package com.zzy.dicegames.ui.game.liarsdice;

import android.os.Handler;

import com.zzy.dicegames.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import static com.zzy.dicegames.ui.game.liarsdice.LiarsDiceGameViewModel.*;
import static org.junit.Assert.*;

public class LiarsDiceGameViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private LiarsDiceGameViewModel viewModel;

    @Mock
    private Handler mockHandler;

    @Before
    public void setUp() {
        // ViewModel初始化必须放在setUp()方法中
        viewModel = new LiarsDiceGameViewModel();
        viewModel.setHandler(mockHandler);
    }

    private static Bid bid(int quantity, int face, boolean zhai) {
        return new Bid(quantity, face, zhai);
    }

    @Test
    public void testInitialization() {
        assertEquals(NUM_DICE_PER_PLAYER, viewModel.getNumDice());
        assertEquals(DEFAULT_PLAYERS, viewModel.getNumPlayers());
        assertEquals(DEFAULT_PLAYERS * NUM_DICE_PER_PLAYER, viewModel.getTotalDice());
        assertEquals(1, viewModel.getCurrentRound().getValue().intValue());
        assertEquals(PLAYER_HUMAN, viewModel.getCurrentPlayer().getValue().intValue());
        assertTrue(viewModel.isHumanTurn());
        assertArrayEquals(new int[] {0, 0}, viewModel.getWinLossRecords().getValue()[0]);
        assertArrayEquals(new int[] {0, 0}, viewModel.getWinLossRecords().getValue()[1]);
        assertNull(viewModel.getCurrentBid().getValue());
        assertNull(viewModel.getRanking().getValue());
        assertNull(viewModel.getRevealResult().getValue());
        assertTrue(viewModel.getBidControlsEnabled().getValue());
        var gameLog = viewModel.getGameLog().getValue();
        assertEquals(3, gameLog.size());
        assertEquals(R.string.logGameBegins, gameLog.get(0).first.intValue());
        assertEquals(R.string.logRoundBegins, gameLog.get(1).first.intValue());
        assertEquals(R.string.logPlayerTurn, gameLog.get(2).first.intValue());
    }

    @Test
    public void testNewGame() {
        viewModel.newGame(MIN_PLAYERS);
        assertEquals(MIN_PLAYERS, viewModel.getNumPlayers());
        assertEquals(2, viewModel.getWinLossRecords().getValue().length);
        viewModel.newGame(MAX_PLAYERS);
        assertEquals(MAX_PLAYERS, viewModel.getNumPlayers());
        assertEquals(4, viewModel.getWinLossRecords().getValue().length);
        assertThrows(IllegalArgumentException.class, () -> viewModel.newGame(MIN_PLAYERS - 1));
        assertThrows(IllegalArgumentException.class, () -> viewModel.newGame(MAX_PLAYERS + 1));
    }

    @Test
    public void testIsBidValid() {
        // 默认2人：喊1点/斋/飞 起叫个数为 2/2/3
        assertTrue(viewModel.isBidValid(bid(3, 5, false)));
        assertTrue(viewModel.isBidValid(bid(2, 1, true)));   // 喊1点只需2个
        assertTrue(viewModel.isBidValid(bid(2, 2, true)));   // 喊斋只需2个
        assertFalse(viewModel.isBidValid(bid(3, 1, false))); // 叫1点必须是斋
        assertFalse(viewModel.isBidValid(bid(2, 5, false))); // 低于飞起叫个数3
        assertFalse(viewModel.isBidValid(bid(0, 5, false)));
        assertFalse(viewModel.isBidValid(bid(3, 7, false)));
        // 数量上限为场上骰子总数（默认2人：10）
        assertFalse(viewModel.isBidValid(bid(viewModel.getTotalDice() + 1, 5, false)));
        assertFalse(viewModel.isBidValid(null));
    }

    @Test
    public void testMinQuantity() {
        // 2人：喊1点2、斋2、飞3
        assertEquals(2, viewModel.getMinQuantity(1, true));
        assertEquals(2, viewModel.getMinQuantity(2, true));
        assertEquals(3, viewModel.getMinQuantity(2, false));
        // 3人：喊1点3、斋4、飞5
        viewModel.newGame(3);
        assertEquals(3, viewModel.getMinQuantity(1, true));
        assertEquals(4, viewModel.getMinQuantity(2, true));
        assertEquals(5, viewModel.getMinQuantity(2, false));
        // 喊1点比喊斋的起叫个数更少
        assertTrue(viewModel.isBidValid(bid(3, 1, true)));
        assertFalse(viewModel.isBidValid(bid(3, 5, true)));
        assertTrue(viewModel.isBidValid(bid(4, 5, true)));
        // 4人：喊1点4、斋5、飞6
        viewModel.newGame(4);
        assertEquals(4, viewModel.getMinQuantity(1, true));
        assertEquals(5, viewModel.getMinQuantity(2, true));
        assertEquals(6, viewModel.getMinQuantity(2, false));
    }

    @Test
    public void testQuantityLimit() {
        // 默认2人：数量范围为[3, 10]
        viewModel.setSelectedZhai(false);
        viewModel.setSelectedQuantity(3);
        assertEquals(3, viewModel.getSelectedQuantity().getValue().intValue());
        viewModel.setSelectedQuantity(10);
        assertEquals(10, viewModel.getSelectedQuantity().getValue().intValue());
        viewModel.setSelectedQuantity(11);  // 超过上限，无效
        assertEquals(10, viewModel.getSelectedQuantity().getValue().intValue());
        viewModel.setSelectedQuantity(1);   // 低于起叫个数，无效
        assertEquals(10, viewModel.getSelectedQuantity().getValue().intValue());
    }

    @Test
    public void testIsBidRaiseValid() {
        // 开叫（prev为null），数量不能低于起叫个数
        assertTrue(viewModel.isBidRaiseValid(null, bid(3, 2, false)));
        // 同模式：数量更大（点数不限）
        assertTrue(viewModel.isBidRaiseValid(bid(3, 5, false), bid(4, 5, false)));
        assertTrue(viewModel.isBidRaiseValid(bid(3, 5, true), bid(4, 5, true)));
        assertTrue(viewModel.isBidRaiseValid(bid(3, 5, false), bid(4, 6, false)));  // 数量和点数都变
        // 同模式：数量不变点数更大（1为最高）
        assertTrue(viewModel.isBidRaiseValid(bid(3, 5, false), bid(3, 6, false)));
        assertTrue(viewModel.isBidRaiseValid(bid(3, 6, true), bid(3, 1, true)));
        // 同模式：无效情况
        assertFalse(viewModel.isBidRaiseValid(bid(3, 5, false), bid(3, 5, false)));
        assertFalse(viewModel.isBidRaiseValid(bid(3, 5, false), bid(2, 5, false)));
        // 斋→飞：数量至少+2
        assertTrue(viewModel.isBidRaiseValid(bid(3, 3, true), bid(5, 2, false)));
        assertTrue(viewModel.isBidRaiseValid(bid(3, 3, true), bid(5, 5, false)));
        assertFalse(viewModel.isBidRaiseValid(bid(3, 3, true), bid(4, 5, false)));
        // 飞→斋：数量至少-1
        assertTrue(viewModel.isBidRaiseValid(bid(6, 3, false), bid(5, 2, true)));
        assertTrue(viewModel.isBidRaiseValid(bid(6, 3, false), bid(6, 3, true)));
        assertFalse(viewModel.isBidRaiseValid(bid(6, 3, false), bid(4, 3, true)));
        // 开叫不能低于起叫个数（2人飞起叫3）
        assertFalse(viewModel.isBidRaiseValid(null, bid(2, 5, false)));
    }

    @Test
    public void testCountBid() {
        viewModel.setDiceForTest(new int[][] {
                {5, 1, 1, 3, 4},  // 玩家0
                {1, 2, 3, 4, 6}   // 玩家1
        });
        // 飞：5的个数 = 5本身的个数 + 万能1的个数
        assertEquals(4, viewModel.countBid(5, false));
        // 斋：不计万能1
        assertEquals(1, viewModel.countBid(5, true));
        // 点数1：只计1本身
        assertEquals(3, viewModel.countBid(1, false));
    }

    @Test
    public void testShouldLock() {
        assertTrue(shouldLock(5, 5, false));
        assertTrue(shouldLock(1, 5, false));   // 万能1
        assertFalse(shouldLock(3, 5, false));
        assertTrue(shouldLock(5, 5, true));
        assertFalse(shouldLock(1, 5, true));   // 斋不计万能1
        assertTrue(shouldLock(1, 1, false));
        assertFalse(shouldLock(5, 1, false));
    }

    @Test
    public void testSelectedFaceOneForcesZhai() {
        viewModel.setSelectedFace(1);
        assertTrue(viewModel.getSelectedZhai().getValue());
        viewModel.setSelectedZhai(false);  // 叫1点时不能飞
        assertTrue(viewModel.getSelectedZhai().getValue());
    }

    @Test
    public void testBidInvalidRejected() {
        viewModel.doBid(bid(3, 5, false));
        assertEquals(bid(3, 5, false), viewModel.getCurrentBid().getValue());
        assertEquals(1, viewModel.getCurrentPlayer().getValue().intValue());

        // 同模式数量变小：非法
        viewModel.doBid(bid(2, 5, false));
        assertEquals(bid(3, 5, false), viewModel.getCurrentBid().getValue());
        assertEquals(1, viewModel.getCurrentPlayer().getValue().intValue());

        // 数量更小且点数更高：非法
        viewModel.doBid(bid(2, 6, false));
        assertEquals(bid(3, 5, false), viewModel.getCurrentBid().getValue());
        assertEquals(1, viewModel.getCurrentPlayer().getValue().intValue());
    }

    @Test
    public void testChallengeBidTrue() {
        viewModel.setDiceForTest(new int[][] {
                {5, 5, 1, 3, 4},  // 玩家0：3个有效5
                {2, 2, 3, 4, 6}   // 玩家1：0个
        });
        viewModel.doBid(bid(3, 5, false));  // 玩家0开叫"3个5飞"
        viewModel.doChallenge(1);           // 玩家1质疑

        RevealResult result = viewModel.getRevealResult().getValue();
        assertNotNull(result);
        assertEquals(3, result.actualCount);
        assertTrue(result.bidTrue);
        assertEquals(1, result.loser);      // 叫数属实，质疑者输
        // 只对开骰双方计分：质疑者输，上家赢
        assertArrayEquals(new int[] {1, 0}, viewModel.getWinLossRecords().getValue()[0]);
        assertArrayEquals(new int[] {0, 1}, viewModel.getWinLossRecords().getValue()[1]);
        assertFalse(viewModel.getBidButtonEnabled().getValue());
        // 日志中增加“玩家1开了玩家0”
        var gameLog = viewModel.getGameLog().getValue();
        var openLog = gameLog.get(gameLog.size() - 3);
        assertEquals(R.string.logOpen, openLog.first.intValue());
        assertArrayEquals(new Object[] {1, 0}, openLog.second);
    }

    @Test
    public void testChallengeBidFalse() {
        viewModel.setDiceForTest(new int[][] {
                {5, 1, 1, 1, 1},  // 玩家0：5个有效5
                {2, 2, 3, 4, 6}   // 玩家1：0个
        });
        viewModel.doBid(bid(6, 5, false));  // 玩家0开叫"6个5飞"，实际只有5个
        viewModel.doChallenge(1);           // 玩家1质疑

        RevealResult result = viewModel.getRevealResult().getValue();
        assertNotNull(result);
        assertEquals(5, result.actualCount);
        assertFalse(result.bidTrue);
        assertEquals(0, result.loser);      // 上家吹牛，上家输
        assertArrayEquals(new int[] {0, 1}, viewModel.getWinLossRecords().getValue()[0]);
        assertArrayEquals(new int[] {1, 0}, viewModel.getWinLossRecords().getValue()[1]);
    }

    @Test
    public void testZhaiChallenge() {
        viewModel.setDiceForTest(new int[][] {
                {5, 1, 1, 1, 1},  // 玩家0：1个5 + 4个万能1
                {2, 2, 3, 4, 6}
        });
        // 喊斋"4个5"：1不作为万能，实际只有1个5
        viewModel.doBid(bid(4, 5, true));
        viewModel.doChallenge(1);

        RevealResult result = viewModel.getRevealResult().getValue();
        assertNotNull(result);
        assertEquals(1, result.actualCount);
        assertFalse(result.bidTrue);
        assertEquals(0, result.loser);
    }

    @Test
    public void testWinLossOnlyForInvolvedPlayers() {
        viewModel.newGame(3);
        viewModel.setDiceForTest(new int[][] {
                {5, 5, 1, 5, 5},  // 玩家0：5个有效5
                {5, 5, 5, 2, 3},  // 玩家1：3个5
                {2, 3, 4, 4, 6}   // 玩家2：0个5
        });
        viewModel.doBid(bid(5, 5, false));  // 玩家0开叫“5个5飞”
        viewModel.doBid(bid(6, 5, false));  // 玩家1加码“6个5飞”
        viewModel.doChallenge(2);           // 玩家2质疑（被开玩家1）

        RevealResult result = viewModel.getRevealResult().getValue();
        assertNotNull(result);
        assertTrue(result.bidTrue);          // 实际8个5 >= 6，叫数属实
        assertEquals(2, result.loser);       // 质疑者输
        // 仅开骰双方计分：玩家1胜+1、玩家2负+1，玩家0不变
        assertArrayEquals(new int[] {0, 0}, viewModel.getWinLossRecords().getValue()[0]);
        assertArrayEquals(new int[] {1, 0}, viewModel.getWinLossRecords().getValue()[1]);
        assertArrayEquals(new int[] {0, 1}, viewModel.getWinLossRecords().getValue()[2]);
    }

    @Test
    public void testContinueAfterRevealStartsNextRound() {
        viewModel.setDiceForTest(new int[][] {
                {5, 5, 1, 3, 4},
                {2, 2, 3, 4, 6}
        });
        viewModel.doBid(bid(3, 5, false));
        viewModel.doChallenge(1);
        assertNotNull(viewModel.getRevealResult().getValue());

        viewModel.continueAfterReveal();
        assertNull(viewModel.getRevealResult().getValue());
        assertNull(viewModel.getCurrentBid().getValue());
        // 下一局由输家（玩家1）先叫
        assertEquals(1, viewModel.getCurrentPlayer().getValue().intValue());
        assertEquals(2, viewModel.getCurrentRound().getValue().intValue());
        assertNull(viewModel.getRanking().getValue());
    }

    @Test
    public void testTurnOrder() {
        viewModel.doBid(bid(3, 2, false));  // 玩家0开叫
        assertEquals(1, viewModel.getCurrentPlayer().getValue().intValue());
        viewModel.doBid(bid(4, 2, false));  // 玩家1加码
        assertEquals(0, viewModel.getCurrentPlayer().getValue().intValue());
    }

    @Test
    public void testGameOverAfterTenRounds() {
        // 进行10局
        for (int i = 0; i < TOTAL_ROUNDS; i++) {
            viewModel.doBid(bid(3, 2, false));
            viewModel.doChallenge(0);
            viewModel.continueAfterReveal();
            if (i < TOTAL_ROUNDS - 1)
                assertNull(viewModel.getRanking().getValue());
        }

        var ranking = viewModel.getRanking().getValue();
        assertNotNull(ranking);
        assertEquals(2, ranking.size());
        // 每个玩家的胜局数+负局数 = 总局数
        int[][] records = viewModel.getWinLossRecords().getValue();
        for (int p = 0; p < viewModel.getNumPlayers(); p++)
            assertEquals(TOTAL_ROUNDS, records[p][0] + records[p][1]);
    }
}
