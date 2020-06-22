package com.zzy.dicegames.fragment.game.yahtzee;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.ScoreDatabase;
import com.zzy.dicegames.database.dao.FiveYahtzeeScoreDao;
import com.zzy.dicegames.database.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.database.entity.FiveYahtzeeScore;

/**
 * 5骰Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public class FiveYahtzeeFragment extends AbstractYahtzeeFragment {

	public FiveYahtzeeFragment() {}

	@Override
	public AbstractYahtzeeScoreBoardFragment createScoreBoardFragment() {
		return new FiveYahtzeeScoreBoardFragment();
	}

	@Override
	public String getTitle() {
		return getContext().getString(R.string.fiveYahtzee);
	}

	@Override
	public int getDiceCount() {
		return 5;
	}

	@Override
	public int getRollTimes() {
		return 2;
	}

	@Override
	protected int saveScore(AbstractYahtzeeScore score) {
		FiveYahtzeeScoreDao fiveYahtzeeScoreDao = ScoreDatabase.getInstance(getContext()).fiveYahtzeeScoreDao();
		fiveYahtzeeScoreDao.insert((FiveYahtzeeScore) score);
		return fiveYahtzeeScoreDao.findTop10Score().indexOf(score.getScore()) + 1;
	}

}
