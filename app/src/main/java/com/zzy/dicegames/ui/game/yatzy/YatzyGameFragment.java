package com.zzy.dicegames.ui.game.yatzy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;
import com.zzy.dicegames.ui.game.yahtzee.BaseYahtzeeGameFragment;
import com.zzy.dicegames.ui.game.yahtzee.BaseYahtzeeGameViewModel;

import androidx.lifecycle.ViewModelProvider;

/**
 * Yatzy游戏Fragment、
 */
public class YatzyGameFragment extends BaseYahtzeeGameFragment {
    @Override
    public GameType getGameType() {
        return GameType.YATZY;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_yatzy_game, container, false);
    }

    @Override
    protected BaseYahtzeeGameViewModel createViewModel() {
        return new ViewModelProvider(this).get(YatzyGameViewModel.class);
    }

    @Override
    protected int[] getScoreTextViewIds() {
        return new int[] {
                R.id.tvOnes, R.id.tvTwos, R.id.tvThrees, R.id.tvFours, R.id.tvFives, R.id.tvSixes,
                R.id.tvOnePair, R.id.tvTwoPairs,
                R.id.tvThreeOfAKind, R.id.tvFourOfAKind,
                R.id.tvSmallStraight, R.id.tvLargeStraight,
                R.id.tvFullHouse, R.id.tvChance, R.id.tvYatzy
        };
    }

}
