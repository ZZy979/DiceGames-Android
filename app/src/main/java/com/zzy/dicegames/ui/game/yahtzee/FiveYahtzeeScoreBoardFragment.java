package com.zzy.dicegames.ui.game.yahtzee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.data.entity.FiveYahtzeeScore;

import java.time.LocalDate;

import androidx.lifecycle.ViewModelProvider;

/**
 * 5骰Yahtzee计分板Fragment
 *
 * @author 赵正阳
 */
public class FiveYahtzeeScoreBoardFragment extends AbstractYahtzeeScoreBoardFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_five_yahtzee_score_board, container, false);
    }

    @Override
    protected AbstractYahtzeeScoreBoardViewModel createViewModel() {
        return new ViewModelProvider(this).get(FiveYahtzeeScoreBoardViewModel.class);
    }

    @Override
    protected int[] getScoreButtonIds() {
        return new int[] {
                R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6,
                R.id.btn2p, R.id.btn3e, R.id.btn4e, R.id.btnFullHouse,
                R.id.btnSmallStraight, R.id.btnLargeStraight, R.id.btnChance, R.id.btnYahtzee
        };
    }

    @Override
    protected int[] getScoreTextViewIds() {
        return new int[] {
                R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4, R.id.tv5, R.id.tv6,
                R.id.tv2p, R.id.tv3e, R.id.tv4e, R.id.tvFullHouse,
                R.id.tvSmallStraight, R.id.tvLargeStraight, R.id.tvChance, R.id.tvYahtzee
        };
    }

    @Override
    protected AbstractYahtzeeScore getScore() {
        int[] scores = mViewModel.getScores().getValue();
        if (mViewModel.getTotalScore().getValue() == null
                || mViewModel.getBonusScore().getValue() == null
                || scores == null)
            return null;

        return new FiveYahtzeeScore(LocalDate.now().toString(),
                mViewModel.getTotalScore().getValue(),
                mViewModel.getBonusScore().getValue() == 0 ? 0 : 1,
                scores[scores.length - 1] == 0 ? 0 : 1);
    }

}
