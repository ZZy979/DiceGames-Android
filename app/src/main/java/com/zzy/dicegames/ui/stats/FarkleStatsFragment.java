package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.FarkleStatistics;

import androidx.lifecycle.LifecycleOwner;

/**
 * Farkle统计数据Fragment
 *
 * @author 赵正阳
 */
public class FarkleStatsFragment extends BaseStatsFragment {
    /** 局数标签 */
    private TextView mGamesPlayedTextView;

    /** 获胜局数标签 */
    private TextView mGamesWonTextView;

    /** 胜率标签 */
    private TextView mWinRateTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_farkle_stats, container, false);
    }

    @Override
    protected void initViews(View view) {
        mGamesPlayedTextView = view.findViewById(R.id.tvGamesPlayed);
        mGamesWonTextView = view.findViewById(R.id.tvGamesWon);
        mWinRateTextView = view.findViewById(R.id.tvWinRate);
    }

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        var dao = mScoreDatabase.farkleScoreDao();
        dao.statistics().observe(owner, this::onStatisticsChanged);
    }

    /** 统计数据更新时的回调 */
    private void onStatisticsChanged(FarkleStatistics stats) {
        mGamesPlayedTextView.setText(Integer.toString(stats.count));
        mGamesWonTextView.setText(Integer.toString(stats.winCount));
        mWinRateTextView.setText(stats.count == 0 ? "-" : String.format(
                "%.2f%%", (double) stats.winCount / stats.count * 100));
    }
}
