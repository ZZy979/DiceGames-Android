package com.zzy.dicegames.ui.game.rolladice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;
import com.zzy.dicegames.ui.game.BaseGameFragment;

import androidx.lifecycle.ViewModelProvider;

/**
 * 掷骰子游戏Fragment
 *
 * @author 赵正阳
 */
public class RollADiceGameFragment extends BaseGameFragment<RollADiceGameViewModel> {
    @Override
    public GameType getGameType() {
        return GameType.ROLL_A_DICE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_roll_a_dice_game, container, false);
    }

    @Override
    protected RollADiceGameViewModel createViewModel() {
        return new ViewModelProvider(this).get(RollADiceGameViewModel.class);
    }
}
