package com.zzy.dicegames.ui.game.yahtzee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.dao.SixYahtzeeScoreDao;
import com.zzy.dicegames.data.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.data.entity.SixYahtzeeScore;

import java.time.LocalDate;

import androidx.lifecycle.ViewModelProvider;

/**
 * 6骰Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public class SixYahtzeeFragment extends BaseYahtzeeFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_six_yahtzee, container, false);
    }

    @Override
    protected BaseYahtzeeViewModel createViewModel() {
        return new ViewModelProvider(this).get(SixYahtzeeViewModel.class);
    }

    @Override
    public int getDiceCount() {
        return 6;
    }

    @Override
    public int getMaxRolls() {
        return 3;
    }

    @Override
    protected int[] getScoreButtonIds() {
        return new int[] {
                R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6,
                R.id.btn1p, R.id.btn2p, R.id.btn3p, R.id.btn3e, R.id.btn4e, R.id.btn5e,
                R.id.btnSmallStraight, R.id.btnLargeStraight, R.id.btnFullStraight,
                R.id.btnHut, R.id.btnHouse, R.id.btnTower,
                R.id.btnChance, R.id.btnYahtzee
        };
    }

    @Override
    protected int[] getScoreTextViewIds() {
        return new int[] {
                R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4, R.id.tv5, R.id.tv6,
                R.id.tv1p, R.id.tv2p, R.id.tv3p, R.id.tv3e, R.id.tv4e, R.id.tv5e,
                R.id.tvSmallStraight, R.id.tvLargeStraight, R.id.tvFullStraight,
                R.id.tvHut, R.id.tvHouse, R.id.tvTower,
                R.id.tvChance, R.id.tvYahtzee
        };
    }

    @Override
    protected AbstractYahtzeeScore getScore() {
        int[] scores = mViewModel.getScores().getValue();
        if (mViewModel.getTotalScore().getValue() == null
                || mViewModel.getBonusScore().getValue() == null
                || scores == null)
            return null;

        return new SixYahtzeeScore(LocalDate.now().toString(),
                mViewModel.getTotalScore().getValue(),
                mViewModel.getBonusScore().getValue() == 0 ? 0 : 1,
                scores[scores.length - 1] == 0 ? 0 : 1);
    }

    @Override
    protected int saveScore(AbstractYahtzeeScore score) {
        SixYahtzeeScoreDao sixYahtzeeScoreDao = ScoreDatabase.getInstance(getContext()).sixYahtzeeScoreDao();
        sixYahtzeeScoreDao.insert((SixYahtzeeScore) score);
        return sixYahtzeeScoreDao.findTop10Score().indexOf(score.getScore()) + 1;
    }

}
