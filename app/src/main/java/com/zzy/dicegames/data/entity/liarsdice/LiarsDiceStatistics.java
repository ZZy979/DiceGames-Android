package com.zzy.dicegames.data.entity.liarsdice;

import com.zzy.dicegames.data.entity.BaseStatistics;

/**
 * 大话骰统计数据结果类
 *
 * @author 赵正阳
 */
public class LiarsDiceStatistics extends BaseStatistics {
    /** 游戏人数 */
    public int numPlayers;

    /** 胜局数 */
    public int wins;

    /** 负局数 */
    public int losses;

    public LiarsDiceStatistics(int numPlayers, int wins, int losses) {
        super(0, 0, 0, 0);
        this.numPlayers = numPlayers;
        this.wins = wins;
        this.losses = losses;
    }

    /** 总局数 */
    public int getTotalGames() {
        return wins + losses;
    }

    /** 胜率（0~1），无记录时返回0 */
    public double getWinRate() {
        int total = getTotalGames();
        return total == 0 ? 0 : (double) wins / total;
    }
}
