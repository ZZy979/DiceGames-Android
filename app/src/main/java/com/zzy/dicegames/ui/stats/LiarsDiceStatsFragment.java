package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.BaseStatistics;
import com.zzy.dicegames.data.entity.liarsdice.LiarsDiceStatistics;

import java.util.List;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

/**
 * 大话骰统计数据Fragment
 *
 * @author 赵正阳
 */
public class LiarsDiceStatsFragment extends BaseStatsFragment {
    /** 指定人数游戏的统计标签 */
    private TextView[] mStatsPlayersTextViews;

    /** 总计统计标签 */
    private TextView mStatsTotalTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liars_dice_stats, container, false);
    }

    @Override
    protected void initViews(View view) {
        int[] statsPlayersTextViewIds = {R.id.tvStats2Players, R.id.tvStats3Players, R.id.tvStats4Players};
        mStatsPlayersTextViews = new TextView[statsPlayersTextViewIds.length];
        for (int i = 0; i < statsPlayersTextViewIds.length; i++)
            mStatsPlayersTextViews[i] = view.findViewById(statsPlayersTextViewIds[i]);
        mStatsTotalTextView = view.findViewById(R.id.tvStatsTotal);
    }

    @Override
    protected LiveData<? extends List<? extends BaseScore>> getHighScores() {
        return null;
    }

    @Override
    protected LiveData<? extends BaseStatistics> getStatistics() {
        return mScoreDatabase.liarsDiceScoreDao().totalStatistics();
    }

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        var dao = mScoreDatabase.liarsDiceScoreDao();
        for (int n = 2; n <= 4; n++)
            dao.statistics(n).observe(owner, this::updatePlayerStats);
        dao.totalStatistics().observe(owner, this::updateTotalStats);
    }

    /** 更新指定人数游戏的统计标签 */
    private void updatePlayerStats(LiarsDiceStatistics stats) {
        mStatsPlayersTextViews[stats.numPlayers - 2].setText(getString(R.string.liarsDiceStatsFormat,
                stats.numPlayers, stats.wins, stats.losses, stats.getWinRate() * 100));
    }

    /** 更新总计统计标签 */
    private void updateTotalStats(LiarsDiceStatistics stats) {
        mStatsTotalTextView.setText(getString(R.string.liarsDiceTotalStatsFormat,
                stats.wins, stats.losses, stats.getWinRate() * 100));
    }
}
