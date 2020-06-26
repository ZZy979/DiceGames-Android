package com.zzy.dicegames.fragment.game.yahtzee;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.ScoreDatabase;
import com.zzy.dicegames.database.dao.SixYahtzeeScoreDao;
import com.zzy.dicegames.database.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.database.entity.SixYahtzeeScore;

/**
 * 6骰Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public class SixYahtzeeFragment extends AbstractYahtzeeFragment {

	public SixYahtzeeFragment() {}

	@Override
	public AbstractYahtzeeScoreBoardFragment createScoreBoardFragment() {
		return new SixYahtzeeScoreBoardFragment();
	}

	@Override
	public String getTitle() {
		return getString(R.string.sixYahtzee);
	}

	@Override
	public int getDiceCount() {
		return 6;
	}

	@Override
	public int getRollTimes() {
		return 2;
	}

	@Override
	protected int saveScore(AbstractYahtzeeScore score) {
		SixYahtzeeScoreDao sixYahtzeeScoreDao = ScoreDatabase.getInstance(getContext()).sixYahtzeeScoreDao();
		sixYahtzeeScoreDao.insert((SixYahtzeeScore) score);
		return sixYahtzeeScoreDao.findTop10Score().indexOf(score.getScore()) + 1;
	}

}
