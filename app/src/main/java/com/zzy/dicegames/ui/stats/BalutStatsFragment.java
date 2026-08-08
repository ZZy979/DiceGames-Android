package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.BaseStatistics;
import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.data.entity.balut.BalutStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.LiveData;

/**
 * Balut统计数据Fragment
 *
 * @author 赵正阳
 */
public class BalutStatsFragment extends BaseStatsFragment {
    /** 最高点数标签 */
    protected TextView mHighestPointsTextView;

    /** 最低点数标签 */
    protected TextView mLowestPointsTextView;

    /** 平均点数标签 */
    protected TextView mAveragePointsTextView;

    /** 得到Balut次数标签 */
    private TextView mGotBalutTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_balut_stats, container, false);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        mHighestPointsTextView = view.findViewById(R.id.tvHighestPoints);
        mLowestPointsTextView = view.findViewById(R.id.tvLowestPoints);
        mAveragePointsTextView = view.findViewById(R.id.tvAveragePoints);
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
        mHighestPointsTextView.setText(Integer.toString(s.maxPoints));
        mLowestPointsTextView.setText(Integer.toString(s.minPoints));
        mAveragePointsTextView.setText(String.format("%.2f", s.avgPoints));
        mGotBalutTextView.setText(stats.count == 0 ? "-" : String.format(
                "%.2f%% (%d/%d)", (double) s.numBalut / (s.count * 4) * 100, s.numBalut, s.count * 4));
    }

    @Override
    protected ListAdapter createHighScoresListAdapter(List<? extends BaseScore> highScores) {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(Map.of(
                "rank", getString(R.string.rank),
                "points", getString(R.string.points),
                "score", getString(R.string.score),
                "date", getString(R.string.date)));
        for (int i = 0; i < highScores.size(); ++i) {
            var score = (BalutScore) highScores.get(i);
            data.add(Map.of(
                    "rank", i + 1,
                    "points", score.points,
                    "score", score.score,
                    "date", score.date));
        }
        return new SimpleAdapter(
                getContext(), data, R.layout.high_score_item_balut,
                new String[] {"rank", "points", "score", "date"},
                new int[] {R.id.tvRank, R.id.tvPoints, R.id.tvScore, R.id.tvDate}
        );
    }
}
