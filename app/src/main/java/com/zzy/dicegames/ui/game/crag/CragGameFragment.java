package com.zzy.dicegames.ui.game.crag;

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
 * Crag游戏Fragment
 */
public class CragGameFragment extends BaseYahtzeeGameFragment {
    @Override
    public GameType getGameType() {
        return GameType.CRAG;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crag_game, container, false);
    }

    @Override
    protected BaseYahtzeeGameViewModel createViewModel() {
        return new ViewModelProvider(this).get(CragGameViewModel.class);
    }

    @Override
    protected int[] getScoreTextViewIds() {
        return new int[] {
                R.id.tvOnes, R.id.tvTwos, R.id.tvThrees, R.id.tvFours, R.id.tvFives, R.id.tvSixes,
                R.id.tvLowStraight, R.id.tvHighStraight, R.id.tvOddStraight, R.id.tvEvenStraight,
                R.id.tvThreeOfAKind, R.id.tvThirteen, R.id.tvCrag
        };
    }

    @Override
    protected void onUpperTotalScoreChanged(int upperTotalScore) {}

    @Override
    protected void onBonusScoreChanged(int bonusScore) {}

}
