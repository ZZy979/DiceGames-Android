package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BaseStatistics;
import com.zzy.dicegames.data.entity.yatzy.MaxiYatzyScore;
import com.zzy.dicegames.data.entity.yatzy.MaxiYatzyStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;

/**
 * Maxi Yatzy统计数据Fragment
 *
 * @author 赵正阳
 */
public class MaxiYatzyStatsFragment extends BaseStatsFragment {
    /** 得到奖励分次数标签 */
    private TextView mGotBonusTextView;

    /** 得到Yatzy次数标签 */
    private TextView mGotYatzyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maxi_yatzy_stats, container, false);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        mGotBonusTextView = view.findViewById(R.id.tvGotBonus);
        mGotYatzyTextView = view.findViewById(R.id.tvGotYatzy);
    }

    @Override
    protected LiveData<List<MaxiYatzyScore>> getHighScores() {
        return mScoreDatabase.maxiYatzyScoreDao().findTop(10);
    }

    @Override
    protected LiveData<MaxiYatzyStatistics> getStatistics() {
        return mScoreDatabase.maxiYatzyScoreDao().statistics();
    }

    @Override
    protected void onStatisticsChanged(BaseStatistics stats) {
        super.onStatisticsChanged(stats);
        var s = (MaxiYatzyStatistics) stats;
        mGotBonusTextView.setText(stats.count == 0 ? "-" : String.format(
                "%.2f%% (%d/%d)", (double) s.numBonus / s.count * 100, s.numBonus, s.count));
        mGotYatzyTextView.setText(stats.count == 0 ? "-" : String.format(
                "%.2f%% (%d/%d)", (double) s.numYatzy / s.count * 100, s.numYatzy, s.count));
    }
}
