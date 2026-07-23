package com.zzy.dicegames.ui.stats;

import com.zzy.dicegames.data.entity.FiveYahtzeeScore;
import com.zzy.dicegames.data.entity.YahtzeeStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;

/**
 * 5骰Yahtzee统计数据Fragment
 *
 * @author 赵正阳
 */
public class FiveYahtzeeStatsFragment extends BaseYahtzeeStatsFragment {
    @Override
    protected LiveData<List<FiveYahtzeeScore>> getHighScores() {
        return mScoreDatabase.fiveYahtzeeScoreDao().findTop(10);
    }

    @Override
    protected LiveData<YahtzeeStatistics> getStatistics() {
        return mScoreDatabase.fiveYahtzeeScoreDao().statistics();
    }
}
