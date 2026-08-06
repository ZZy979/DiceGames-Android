package com.zzy.dicegames.ui.game.yahtzee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;

import androidx.lifecycle.ViewModelProvider;

/**
 * Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public class YahtzeeGameFragment extends BaseYahtzeeGameFragment {
    @Override
    public GameType getGameType() {
        return GameType.YAHTZEE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_yahtzee_game, container, false);
    }

    @Override
    protected BaseYahtzeeGameViewModel createViewModel() {
        return new ViewModelProvider(this).get(YahtzeeGameViewModel.class);
    }

    @Override
    protected int[] getScoreTextViewIds() {
        return new int[] {
                R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4, R.id.tv5, R.id.tv6,
                R.id.tv3e, R.id.tv4e, R.id.tvFullHouse,
                R.id.tvSmallStraight, R.id.tvLargeStraight, R.id.tvChance, R.id.tvYahtzee
        };
    }

}
