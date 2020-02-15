package com.zzy.dicegames.gamefragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.zzy.dicegames.R;

/**
 * 掷骰子计分板Fragment，嵌套于一个{@link RollADiceFragment}
 *
 * @author 赵正阳
 */
public class RollADiceScoreBoardFragment extends Fragment {
	/** 所属的游戏Fragment */
	RollADiceFragment mGameFragment;

	/** 骰子个数选择器 */
	NumberPicker mDiceCountPicker;

	public RollADiceScoreBoardFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mGameFragment = (RollADiceFragment) getParentFragment();
		View rootView = inflater.inflate(R.layout.fragment_roll_a_dice_score_board, container, false);

		mDiceCountPicker = rootView.findViewById(R.id.diceCountPicker);
		mDiceCountPicker.setMinValue(1);
		mDiceCountPicker.setMaxValue(6);
		mDiceCountPicker.setValue(6);
		mDiceCountPicker.setOnValueChangedListener((picker, oldVal, newVal) -> mGameFragment.setDiceCount(newVal));

		return rootView;
	}

}
