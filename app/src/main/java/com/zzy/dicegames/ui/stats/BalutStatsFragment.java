package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BalutScore;
import com.zzy.dicegames.data.entity.BalutStatistics;

import java.util.List;

import androidx.lifecycle.LifecycleOwner;

/**
 * Balut统计数据Fragment
 *
 * @author 赵正阳
 */
public class BalutStatsFragment extends BaseStatsFragment {
    /** 最高分列表 */
    private ListView mHighScoresListView;

    /** 最高分为空时的占位符标签 */
    private TextView mNothingTextView;

    /** 局数标签 */
    private TextView mGamesPlayedTextView;

    /** 最高分标签 */
    private TextView mMaxScoreTextView;

    /** 最低分标签 */
    private TextView mMinScoreTextView;

    /** 平均分标签 */
    private TextView mAverageScoreTextView;

    /** 得到Balut次数标签 */
    private TextView mGotBalutTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_balut_stats, container, false);
    }

    @Override
    protected void initViews(View view) {
        mHighScoresListView = view.findViewById(R.id.lvHighScores);
        mNothingTextView = view.findViewById(R.id.tvNothing);
        mGamesPlayedTextView = view.findViewById(R.id.tvGamesPlayed);
        mMaxScoreTextView = view.findViewById(R.id.tvMaxScore);
        mMinScoreTextView = view.findViewById(R.id.tvMinScore);
        mAverageScoreTextView = view.findViewById(R.id.tvAverageScore);
        mGotBalutTextView = view.findViewById(R.id.tvGotBalut);
    }

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        var dao = mScoreDatabase.balutScoreDao();
        dao.findTop(10).observe(owner, this::onHighScoresChanged);
        dao.statistics().observe(owner, this::onStatisticsChanged);
    }

    /** 最高分列表更新时的回调 */
    private void onHighScoresChanged(List<BalutScore> highScores) {
        if (highScores.isEmpty()) {
            mHighScoresListView.setVisibility(View.GONE);
            mNothingTextView.setVisibility(View.VISIBLE);
        }
        else {
            mHighScoresListView.setVisibility(View.VISIBLE);
            mNothingTextView.setVisibility(View.GONE);
            mHighScoresListView.setAdapter(createHighScoresListAdapter(highScores));
        }
    }

    /** 统计数据更新时的回调 */
    private void onStatisticsChanged(BalutStatistics stats) {
        mGamesPlayedTextView.setText(Integer.toString(stats.count));
        mMaxScoreTextView.setText(Integer.toString(stats.maxScore));
        mMinScoreTextView.setText(Integer.toString(stats.minScore));
        mAverageScoreTextView.setText(String.format("%.2f", stats.avgScore));
        mGotBalutTextView.setText(stats.count == 0 ? "-" : String.format(
                "%.2f%% (%d/%d)", (double) stats.numBalut / (stats.count * 4) * 100, stats.numBalut, stats.count * 4));
    }
}
