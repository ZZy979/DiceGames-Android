package com.zzy.dicegames.data.entity;

/** 统计数据结果基类 */
public class BaseStatistics {
    public int count;
    public int maxScore;
    public int minScore;
    public double avgScore;

    public BaseStatistics(int count, int maxScore, int minScore, double avgScore) {
        this.count = count;
        this.maxScore = maxScore;
        this.minScore = minScore;
        this.avgScore = avgScore;
    }
}
