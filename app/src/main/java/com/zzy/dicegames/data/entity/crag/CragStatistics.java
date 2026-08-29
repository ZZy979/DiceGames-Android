package com.zzy.dicegames.data.entity.crag;

import com.zzy.dicegames.data.entity.BaseStatistics;

/** Crag统计数据结果类 */
public class CragStatistics extends BaseStatistics {
    public int numCrag;

    public CragStatistics(int count, int maxScore, int minScore, double avgScore, int numCrag) {
        super(count, maxScore, minScore, avgScore);
        this.numCrag = numCrag;
    }
}
