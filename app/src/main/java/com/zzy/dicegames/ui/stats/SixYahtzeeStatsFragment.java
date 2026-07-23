package com.zzy.dicegames.ui.stats;

import com.zzy.dicegames.data.entity.SixYahtzeeScore;
import com.zzy.dicegames.data.entity.YahtzeeStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;

/**
 * 6骰Yahtzee统计数据Fragment
 *
 * @author 赵正阳
 */
public class SixYahtzeeStatsFragment extends BaseYahtzeeStatsFragment {
   @Override
   protected LiveData<List<SixYahtzeeScore>> getHighScores() {
      return mScoreDatabase.sixYahtzeeScoreDao().findTop(10);
   }

   @Override
   protected LiveData<YahtzeeStatistics> getStatistics() {
      return mScoreDatabase.sixYahtzeeScoreDao().statistics();
   }
}
