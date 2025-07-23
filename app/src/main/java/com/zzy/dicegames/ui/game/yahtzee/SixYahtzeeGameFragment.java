package com.zzy.dicegames.ui.game.yahtzee;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.dao.SixYahtzeeScoreDao;
import com.zzy.dicegames.data.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.data.entity.SixYahtzeeScore;

/**
 * 6骰Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public class SixYahtzeeGameFragment extends AbstractYahtzeeGameFragment {
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
        return 3;
    }

    @Override
    protected int saveScore(AbstractYahtzeeScore score) {
        SixYahtzeeScoreDao sixYahtzeeScoreDao = ScoreDatabase.getInstance(getContext()).sixYahtzeeScoreDao();
        sixYahtzeeScoreDao.insert((SixYahtzeeScore) score);
        return sixYahtzeeScoreDao.findTop10Score().indexOf(score.getScore()) + 1;
    }

}
