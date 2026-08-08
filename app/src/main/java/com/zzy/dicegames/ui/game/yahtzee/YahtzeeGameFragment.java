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
                R.id.tvOnes, R.id.tvTwos, R.id.tvThrees, R.id.tvFours, R.id.tvFives, R.id.tvSixes,
                R.id.tvThreeOfAKind, R.id.tvFourOfAKind, R.id.tvFullHouse,
                R.id.tvSmallStraight, R.id.tvLargeStraight, R.id.tvChance, R.id.tvYahtzee
        };
    }

}
