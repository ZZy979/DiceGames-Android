package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BaseStatistics;
import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.data.entity.balut.BalutStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;

/**
 * Balut统计数据Fragment
 *
 * @author 赵正阳
 */
public class BalutStatsFragment extends BaseStatsFragment {
    /** 得到Balut次数标签 */
    private TextView mGotBalutTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_balut_stats, container, false);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        mGotBalutTextView = view.findViewById(R.id.tvGotBalut);
    }

    @Override
    protected LiveData<List<BalutScore>> getHighScores() {
        return mScoreDatabase.balutScoreDao().findTop(10);
    }

    @Override
    protected LiveData<BalutStatistics> getStatistics() {
        return mScoreDatabase.balutScoreDao().statistics();
    }

    @Override
    protected void onStatisticsChanged(BaseStatistics stats) {
        super.onStatisticsChanged(stats);
        var s = (BalutStatistics) stats;
        mGotBalutTextView.setText(stats.count == 0 ? "-" : String.format(
                "%.2f%% (%d/%d)", (double) s.numBalut / (s.count * 4) * 100, s.numBalut, s.count * 4));
    }
}
