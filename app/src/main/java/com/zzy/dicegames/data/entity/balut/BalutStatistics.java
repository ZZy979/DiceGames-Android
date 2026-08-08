package com.zzy.dicegames.data.entity.balut;

import com.zzy.dicegames.data.entity.BaseStatistics;

/** Balut统计数据结果类 */
public class BalutStatistics extends BaseStatistics {
    public int maxPoints;
    public int minPoints;
    public double avgPoints;
    public int numBalut;

    public BalutStatistics(
            int count, int maxScore, int minScore, double avgScore,
            int maxPoints, int minPoints, double avgPoints, int numBalut) {
        super(count, maxScore, minScore, avgScore);
        this.maxPoints = maxPoints;
        this.minPoints = minPoints;
        this.avgPoints = avgPoints;
        this.numBalut = numBalut;
    }
}
