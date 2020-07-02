package com.zzy.dicegames.game.farkle;

import android.os.Bundle;

import com.zzy.dicegames.R;
import com.zzy.dicegames.game.GameFragment;

/**
 * Farkle游戏Fragment
 *
 * @author 赵正阳
 */
public class FarkleFragment extends GameFragment<FarkleScoreBoardFragment> {

	public FarkleFragment() {}

	// 该方法被调用时DiceFragment.onCreateView()已被调用
	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState == null)
			mDiceFragment.setLeftRollTimes(0);
	}

	@Override
	public FarkleScoreBoardFragment createScoreBoardFragment() {
		return new FarkleScoreBoardFragment();
	}

	@Override
	protected void setListeners() {
		mScoreBoardFragment.setDiceFragment(mDiceFragment);
		mDiceFragment.setRollListener(mScoreBoardFragment::onDiceRolled);
	}

	@Override
	public String getTitle() {
		return getString(R.string.farkle);
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
		mDiceFragment.activate();
		mDiceFragment.setLeftRollTimes(0);
	}

}
