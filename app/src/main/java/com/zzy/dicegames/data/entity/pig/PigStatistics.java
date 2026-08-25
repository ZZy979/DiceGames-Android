package com.zzy.dicegames.data.entity.pig;

import com.zzy.dicegames.data.entity.BaseStatistics;

/** Pig统计数据结果类 */
public class PigStatistics extends BaseStatistics {
    public int winCount;

    public PigStatistics(int count, int maxScore, int minScore, double avgScore, int winCount) {
        super(count, maxScore, minScore, avgScore);
        this.winCount = winCount;
    }
}
