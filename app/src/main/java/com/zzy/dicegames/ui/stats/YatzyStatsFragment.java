package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BaseStatistics;
import com.zzy.dicegames.data.entity.yatzy.YatzyScore;
import com.zzy.dicegames.data.entity.yatzy.YatzyStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;

/**
 * Yatzy统计数据Fragment
 */
public class YatzyStatsFragment extends BaseStatsFragment {
    /** 得到奖励分次数标签 */
    private TextView mGotBonusTextView;

    /** 得到Yatzy次数标签 */
    private TextView mGotYatzyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_yatzy_stats, container, false);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        mGotBonusTextView = view.findViewById(R.id.tvGotBonus);
        mGotYatzyTextView = view.findViewById(R.id.tvGotYatzy);
    }

    @Override
    protected LiveData<List<YatzyScore>> getHighScores() {
        return mScoreDatabase.yatzyScoreDao().findTop(10);
    }

    @Override
    protected LiveData<YatzyStatistics> getStatistics() {
        return mScoreDatabase.yatzyScoreDao().statistics();
    }

    @Override
    protected void onStatisticsChanged(BaseStatistics stats) {
        super.onStatisticsChanged(stats);
        var s = (YatzyStatistics) stats;
        mGotBonusTextView.setText(formatPercent(s.numBonus, s.count));
        mGotYatzyTextView.setText(formatPercent(s.numYatzy, s.count));
    }
}
