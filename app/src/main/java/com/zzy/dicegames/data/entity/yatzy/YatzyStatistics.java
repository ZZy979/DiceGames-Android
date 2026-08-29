package com.zzy.dicegames.data.entity.yatzy;

import com.zzy.dicegames.data.entity.BaseStatistics;

/** Yatzy统计数据结果类 */
public class YatzyStatistics extends BaseStatistics {
    public int numBonus;
    public int numYatzy;

    public YatzyStatistics(int count, int maxScore, int minScore, double avgScore, int numBonus, int numYatzy) {
        super(count, maxScore, minScore, avgScore);
        this.numBonus = numBonus;
        this.numYatzy = numYatzy;
    }
}
