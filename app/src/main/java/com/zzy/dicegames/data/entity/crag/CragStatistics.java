package com.zzy.dicegames.data.entity.crag;

import com.zzy.dicegames.data.entity.BaseStatistics;

/** Crag统计数据结果类 */
public class CragStatistics extends BaseStatistics {
    public int numCrag;

    /** 双人（对计算机）游戏局数 */
    public int numMultiplayer;

    /** 双人游戏中人类玩家获胜局数（平局不计入胜局） */
    public int winCount;

    public CragStatistics(
            int count, int maxScore, int minScore, double avgScore,
            int numCrag, int numMultiplayer, int winCount) {
        super(count, maxScore, minScore, avgScore);
        this.numCrag = numCrag;
        this.numMultiplayer = numMultiplayer;
        this.winCount = winCount;
    }
}
