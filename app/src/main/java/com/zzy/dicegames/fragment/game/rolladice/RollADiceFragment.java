package com.zzy.dicegames.fragment.game.rolladice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.fragment.game.GameFragment;

/**
 * 掷骰子游戏Fragment
 *
 * @author 赵正阳
 */
public class RollADiceFragment extends GameFragment {
	/** 计分板 */
	private RollADiceScoreBoardFragment mScoreBoardFragment;

	public RollADiceFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState == null) {
			mScoreBoardFragment = new RollADiceScoreBoardFragment();
			getChildFragmentManager().beginTransaction()
					.add(R.id.scoreBoardFragment, mScoreBoardFragment)
					.commit();
		}
		else
			mScoreBoardFragment = (RollADiceScoreBoardFragment) getChildFragmentManager().findFragmentById(R.id.scoreBoardFragment);

		mScoreBoardFragment.setActionOnChangingDiceCount(mDiceFragment::setDiceCount);

		return rootView;
	}

	@Override
	public String getTitle() {
		return getString(R.string.rollADice);
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
		super.startNewGame();
		mScoreBoardFragment = new RollADiceScoreBoardFragment();
		getChildFragmentManager().beginTransaction()
				.replace(R.id.scoreBoardFragment, mScoreBoardFragment)
				.commit();

		mScoreBoardFragment.setActionOnChangingDiceCount(mDiceFragment::setDiceCount);
		mDiceFragment.activateRollButton();
	}

}
