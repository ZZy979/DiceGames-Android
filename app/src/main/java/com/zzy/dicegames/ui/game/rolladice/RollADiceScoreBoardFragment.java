package com.zzy.dicegames.ui.game.rolladice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.ui.game.BaseScoreBoardFragment;

import androidx.lifecycle.ViewModelProvider;

/**
 * 掷骰子计分板Fragment，嵌套于一个{@link RollADiceGameFragment}
 *
 * @author 赵正阳
 */
public class RollADiceScoreBoardFragment extends BaseScoreBoardFragment<RollADiceScoreBoardViewModel> {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_roll_a_dice_score_board, container, false);
    }

    @Override
    protected RollADiceScoreBoardViewModel createViewModel() {
        return new ViewModelProvider(this).get(RollADiceScoreBoardViewModel.class);
    }

    @Override
    protected void setObservers() {}

    @Override
    protected void initViews(View rootView) {}
}
