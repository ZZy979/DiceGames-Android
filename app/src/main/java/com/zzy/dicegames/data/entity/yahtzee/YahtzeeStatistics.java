package com.zzy.dicegames.data.entity.yahtzee;

import com.zzy.dicegames.data.entity.BaseStatistics;

/** Yahtzee统计数据结果类 */
public class YahtzeeStatistics extends BaseStatistics {
    public int numBonus;
    public int numYahtzee;

    public YahtzeeStatistics(int count, int maxScore, int minScore, double avgScore, int numBonus, int numYahtzee) {
        super(count, maxScore, minScore, avgScore);
        this.numBonus = numBonus;
        this.numYahtzee = numYahtzee;
    }
}
