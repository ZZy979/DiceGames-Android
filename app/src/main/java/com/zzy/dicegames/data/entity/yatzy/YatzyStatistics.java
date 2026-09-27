package com.zzy.dicegames.data.entity.yatzy;

import com.zzy.dicegames.data.entity.BaseStatistics;

/** Yatzy统计数据结果类 */
public class YatzyStatistics extends BaseStatistics {
    public int numBonus;
    public int numYatzy;

    /** 双人（对计算机）游戏局数 */
    public int numMultiplayer;

    /** 双人游戏中人类玩家获胜局数（平局不计入胜局） */
    public int winCount;

    public YatzyStatistics(
            int count, int maxScore, int minScore, double avgScore,
            int numBonus, int numYatzy, int numMultiplayer, int winCount) {
        super(count, maxScore, minScore, avgScore);
        this.numBonus = numBonus;
        this.numYatzy = numYatzy;
        this.numMultiplayer = numMultiplayer;
        this.winCount = winCount;
    }
}
