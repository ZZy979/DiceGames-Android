package com.zzy.dicegames.ui.game.yahtzee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;

import androidx.lifecycle.ViewModelProvider;

/**
 * 5骰Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public class FiveYahtzeeFragment extends BaseYahtzeeFragment {
    @Override
    public GameType getGameType() {
        return GameType.FIVE_YAHTZEE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_five_yahtzee, container, false);
    }

    @Override
    protected BaseYahtzeeViewModel createViewModel() {
        return new ViewModelProvider(this).get(FiveYahtzeeViewModel.class);
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

}
