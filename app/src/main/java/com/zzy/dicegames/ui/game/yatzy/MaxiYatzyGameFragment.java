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
 * Maxi Yatzy游戏Fragment
 *
 * @author 赵正阳
 */
public class MaxiYatzyGameFragment extends BaseYahtzeeGameFragment {
    @Override
    public GameType getGameType() {
        return GameType.MAXI_YATZY;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maxi_yatzy_game, container, false);
    }

    @Override
    protected BaseYahtzeeGameViewModel createViewModel() {
        return new ViewModelProvider(this).get(MaxiYatzyGameViewModel.class);
    }

    @Override
    protected int[] getScoreTextViewIds() {
        return new int[] {
                R.id.tvOnes, R.id.tvTwos, R.id.tvThrees, R.id.tvFours, R.id.tvFives, R.id.tvSixes,
                R.id.tvOnePair, R.id.tvTwoPairs, R.id.tvThreePairs, R.id.tvThreeOfAKind, R.id.tvFourOfAKind, R.id.tvFiveOfAKind,
                R.id.tvSmallStraight, R.id.tvLargeStraight, R.id.tvFullStraight,
                R.id.tvFullHouse, R.id.tvCastle, R.id.tvTower,
                R.id.tvChance, R.id.tvYatzy
        };
    }

}
