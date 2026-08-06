package com.zzy.dicegames.data.entity.balut;

import com.zzy.dicegames.data.entity.BaseStatistics;

/** Balut统计数据结果类 */
public class BalutStatistics extends BaseStatistics {
    public int numBalut;

    public BalutStatistics(int count, int maxScore, int minScore, double avgScore, int numBalut) {
        super(count, maxScore, minScore, avgScore);
        this.numBalut = numBalut;
    }
}
