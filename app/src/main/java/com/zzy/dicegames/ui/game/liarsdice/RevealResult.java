package com.zzy.dicegames.ui.game.liarsdice;

/**
 * 开骰结果
 */
public class RevealResult {
    /** 玩家数量 */
    public final int numPlayers;

    /** 所有玩家的骰子点数 */
    public final int[][] dice;

    /** 被质疑的叫数 */
    public final Bid bid;

    /** 实际个数 */
    public final int actualCount;

    /** 质疑的玩家 */
    public final int challenger;

    /** 输家（本局输家） */
    public final int loser;

    /** 叫数是否属实（属实则质疑者输，否则上家输） */
    public final boolean bidTrue;

    RevealResult(
            int numPlayers, int[][] dice, Bid bid, int actualCount,
            int challenger, int loser, boolean bidTrue) {
        this.numPlayers = numPlayers;
        this.dice = dice;
        this.bid = bid;
        this.actualCount = actualCount;
        this.challenger = challenger;
        this.loser = loser;
        this.bidTrue = bidTrue;
    }
}
