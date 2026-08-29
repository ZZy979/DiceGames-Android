package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BaseStatistics;
import com.zzy.dicegames.data.entity.yahtzee.YahtzeeScore;
import com.zzy.dicegames.data.entity.yahtzee.YahtzeeStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;

/**
 * Yahtzee统计数据Fragment
 *
 * @author 赵正阳
 */
public class YahtzeeStatsFragment extends BaseStatsFragment {
    /** 得到奖励分次数标签 */
    private TextView mGotBonusTextView;

    /** 得到Yahtzee次数标签 */
    private TextView mGotYahtzeeTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_yahtzee_stats, container, false);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        mGotBonusTextView = view.findViewById(R.id.tvGotBonus);
        mGotYahtzeeTextView = view.findViewById(R.id.tvGotYahtzee);
    }

    @Override
    protected LiveData<List<YahtzeeScore>> getHighScores() {
        return mScoreDatabase.yahtzeeScoreDao().findTop(10);
    }

    @Override
    protected LiveData<YahtzeeStatistics> getStatistics() {
        return mScoreDatabase.yahtzeeScoreDao().statistics();
    }

    @Override
    protected void onStatisticsChanged(BaseStatistics stats) {
        super.onStatisticsChanged(stats);
        var s = (YahtzeeStatistics) stats;
        mGotBonusTextView.setText(formatPercent(s.numBonus, s.count));
        mGotYahtzeeTextView.setText(formatPercent(s.numYahtzee, s.count));
    }
}
