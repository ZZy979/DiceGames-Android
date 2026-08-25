package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.BaseStatistics;
import com.zzy.dicegames.data.entity.pig.PigStatistics;

import java.util.List;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

/**
 * Pig统计数据Fragment
 */
public class PigStatsFragment extends BaseStatsFragment {
    /** 获胜局数标签 */
    private TextView mGamesWonTextView;

    /** 胜率标签 */
    private TextView mWinRateTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pig_stats, container, false);
    }

    @Override
    protected void initViews(View view) {
        mGamesPlayedTextView = view.findViewById(R.id.tvGamesPlayed);
        mHighestScoreTextView = view.findViewById(R.id.tvHighestScore);
        mLowestScoreTextView = view.findViewById(R.id.tvLowestScore);
        mAverageScoreTextView = view.findViewById(R.id.tvAverageScore);
        mGamesWonTextView = view.findViewById(R.id.tvGamesWon);
        mWinRateTextView = view.findViewById(R.id.tvWinRate);
    }

    @Override
    protected LiveData<? extends List<? extends BaseScore>> getHighScores() {
        return null;
    }

    @Override
    protected LiveData<? extends BaseStatistics> getStatistics() {
        return mScoreDatabase.pigScoreDao().statistics();
    }

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        getStatistics().observe(owner, this::onStatisticsChanged);
    }

    @Override
    protected void onStatisticsChanged(BaseStatistics stats) {
        super.onStatisticsChanged(stats);
        var s = (PigStatistics) stats;
        mGamesWonTextView.setText(Integer.toString(s.winCount));
        mWinRateTextView.setText(s.count == 0 ? "-" : String.format(
                "%.2f%%", (double) s.winCount / s.count * 100));
    }
}
