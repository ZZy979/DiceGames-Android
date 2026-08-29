package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BaseStatistics;
import com.zzy.dicegames.data.entity.crag.CragScore;
import com.zzy.dicegames.data.entity.crag.CragStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;

/**
 * Crag统计数据Fragment
 */
public class CragStatsFragment extends BaseStatsFragment {
    /** 得到Crag次数标签 */
    private TextView mGotCragTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crag_stats, container, false);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        mGotCragTextView = view.findViewById(R.id.tvGotCrag);
    }

    @Override
    protected LiveData<List<CragScore>> getHighScores() {
        return mScoreDatabase.cragScoreDao().findTop(10);
    }

    @Override
    protected LiveData<CragStatistics> getStatistics() {
        return mScoreDatabase.cragScoreDao().statistics();
    }

    @Override
    protected void onStatisticsChanged(BaseStatistics stats) {
        super.onStatisticsChanged(stats);
        var s = (CragStatistics) stats;
        mGotCragTextView.setText(formatPercent(s.numCrag, s.count));
    }
}
