package com.zzy.dicegames.ui.game.yahtzee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;

import androidx.lifecycle.ViewModelProvider;

/**
 * 6骰Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public class SixYahtzeeGameFragment extends BaseYahtzeeGameFragment {
    @Override
    public GameType getGameType() {
        return GameType.SIX_YAHTZEE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_six_yahtzee_game, container, false);
    }

    @Override
    protected BaseYahtzeeGameViewModel createViewModel() {
        return new ViewModelProvider(this).get(SixYahtzeeGameViewModel.class);
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

}
