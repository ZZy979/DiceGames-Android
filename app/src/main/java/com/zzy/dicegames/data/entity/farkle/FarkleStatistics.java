package com.zzy.dicegames.data.entity.farkle;

import com.zzy.dicegames.data.entity.BaseStatistics;

/** Farkle统计数据结果类 */
public class FarkleStatistics extends BaseStatistics {
    public int winCount;

    public FarkleStatistics(int count, int maxScore, int minScore, double avgScore, int winCount) {
        super(count, maxScore, minScore, avgScore);
        this.winCount = winCount;
    }
}
