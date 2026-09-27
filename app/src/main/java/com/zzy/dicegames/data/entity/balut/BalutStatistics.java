package com.zzy.dicegames.data.entity.balut;

import com.zzy.dicegames.data.entity.BaseStatistics;

/** Balut统计数据结果类 */
public class BalutStatistics extends BaseStatistics {
    public int maxPoints;
    public int minPoints;
    public double avgPoints;
    public int numBalut;

    /** 双人（对计算机）游戏局数 */
    public int numMultiplayer;

    /** 双人游戏中人类玩家获胜局数（平局不计入胜局） */
    public int winCount;

    public BalutStatistics(
            int count, int maxScore, int minScore, double avgScore,
            int maxPoints, int minPoints, double avgPoints, int numBalut,
            int numMultiplayer, int winCount) {
        super(count, maxScore, minScore, avgScore);
        this.maxPoints = maxPoints;
        this.minPoints = minPoints;
        this.avgPoints = avgPoints;
        this.numBalut = numBalut;
        this.numMultiplayer = numMultiplayer;
        this.winCount = winCount;
    }
}
