package com.zzy.dicegames.ui.game.rolladice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.ui.dice.RollDiceFragment;
import com.zzy.dicegames.ui.dice.RollDiceViewModel;
import com.zzy.dicegames.ui.game.BaseGameFragment;

import androidx.lifecycle.ViewModelProvider;

/**
 * 掷骰子游戏Fragment
 *
 * @author 赵正阳
 */
public class RollADiceFragment extends BaseGameFragment<RollADiceViewModel> {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_roll_a_dice, container, false);
    }

    @Override
    protected RollDiceFragment createRollDiceFragment() {
        return RollDiceFragment.newInstance(6, RollDiceViewModel.UNLIMITED_ROLLS, false);
    }

    @Override
    protected RollADiceViewModel createViewModel() {
        return new ViewModelProvider(this).get(RollADiceViewModel.class);
    }
}
