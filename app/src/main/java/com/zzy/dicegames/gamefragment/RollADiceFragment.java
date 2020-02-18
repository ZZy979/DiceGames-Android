package com.zzy.dicegames.gamefragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;

/**
 * 掷骰子游戏Fragment
 *
 * @author 赵正阳
 */
public class RollADiceFragment extends GameFragment {

	public RollADiceFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState == null) {
			RollADiceScoreBoardFragment scoreBoard = new RollADiceScoreBoardFragment();
			getChildFragmentManager().beginTransaction()
					.add(R.id.gameFragment, scoreBoard)
					.commit();
		}

		return rootView;
	}

	@Override
	public String getTitle() {
		return getContext().getString(R.string.rollADice);
	}

	@Override
	public String getGameTypeCode() {
		return "roll_a_dice";
	}

	@Override
	public int getDiceCount() {
		return 6;
	}

	@Override
	public int getRollTimes() {
		return 0;
	}

	@Override
	public void startNewGame() {
		mDiceFragment.activateRollButton();
	}

}
