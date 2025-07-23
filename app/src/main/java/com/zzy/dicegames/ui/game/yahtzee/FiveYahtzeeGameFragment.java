package com.zzy.dicegames.ui.game.yahtzee;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.dao.FiveYahtzeeScoreDao;
import com.zzy.dicegames.data.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.data.entity.FiveYahtzeeScore;

/**
 * 5骰Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public class FiveYahtzeeGameFragment extends AbstractYahtzeeGameFragment {
    @Override
    public AbstractYahtzeeScoreBoardFragment createScoreBoardFragment() {
        return new FiveYahtzeeScoreBoardFragment();
    }

    @Override
    public String getTitle() {
        return getString(R.string.fiveYahtzee);
    }

    @Override
    public int getDiceCount() {
        return 5;
    }

    @Override
    public int getRollTimes() {
        return 3;
    }

    @Override
    protected int saveScore(AbstractYahtzeeScore score) {
        FiveYahtzeeScoreDao fiveYahtzeeScoreDao = ScoreDatabase.getInstance(getContext()).fiveYahtzeeScoreDao();
        fiveYahtzeeScoreDao.insert((FiveYahtzeeScore) score);
        return fiveYahtzeeScoreDao.findTop10Score().indexOf(score.getScore()) + 1;
    }

}
