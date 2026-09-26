package com.zzy.dicegames.data.entity.yahtzee;

import com.zzy.dicegames.data.entity.BaseStatistics;

/** Yahtzee统计数据结果类 */
public class YahtzeeStatistics extends BaseStatistics {
    public int numBonus;
    public int numYahtzee;

    /** 双人（对计算机）游戏局数 */
    public int numMultiplayer;

    /** 双人游戏中人类玩家获胜局数（平局不计入胜局） */
    public int winCount;

    public YahtzeeStatistics(
            int count, int maxScore, int minScore, double avgScore,
            int numBonus, int numYahtzee, int numMultiplayer, int winCount) {
        super(count, maxScore, minScore, avgScore);
        this.numBonus = numBonus;
        this.numYahtzee = numYahtzee;
        this.numMultiplayer = numMultiplayer;
        this.winCount = winCount;
    }
}
