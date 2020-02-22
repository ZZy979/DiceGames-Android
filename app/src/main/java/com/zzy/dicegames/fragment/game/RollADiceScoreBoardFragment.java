package com.zzy.dicegames.fragment.game;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.zzy.dicegames.R;
import com.zzy.dicegames.fragment.dice.DiceFragment;

import java.util.function.IntConsumer;

/**
 * 掷骰子计分板Fragment，嵌套于一个{@link RollADiceFragment}
 *
 * @author 赵正阳
 */
public class RollADiceScoreBoardFragment extends Fragment {
	// ----------游戏状态数据----------
	/** 骰子个数选择器 */
	private NumberPicker mDiceCountPicker;

	/** 改变骰子个数时执行的动作 */
	private IntConsumer mActionOnChangingDiceCount;

	// ----------保存和恢复状态----------
	/** 用于保存和恢复状态：骰子个数 */
	private static final String DICE_COUNT = "diceCount";

	public RollADiceScoreBoardFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_roll_a_dice_score_board, container, false);

		mDiceCountPicker = rootView.findViewById(R.id.diceCountPicker);
		mDiceCountPicker.setMinValue(DiceFragment.MIN_DICE_NUM);
		mDiceCountPicker.setMaxValue(DiceFragment.MAX_DICE_NUM);
		if (savedInstanceState == null)
			mDiceCountPicker.setValue(DiceFragment.MAX_DICE_NUM);
		else
			mDiceCountPicker.setValue(savedInstanceState.getInt(DICE_COUNT));
		mDiceCountPicker.setOnValueChangedListener((picker, oldVal, newVal) -> mActionOnChangingDiceCount.accept(newVal));

		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(DICE_COUNT, mDiceCountPicker.getValue());
		super.onSaveInstanceState(outState);
	}

	public void setActionOnChangingDiceCount(IntConsumer actionOnChangingDiceCount) {
		mActionOnChangingDiceCount = actionOnChangingDiceCount;
	}

}
