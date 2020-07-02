package com.zzy.dicegames.game.rolladice;

import com.zzy.dicegames.R;
import com.zzy.dicegames.game.GameFragment;

/**
 * 掷骰子游戏Fragment
 *
 * @author 赵正阳
 */
public class RollADiceFragment extends GameFragment<RollADiceScoreBoardFragment> {

	public RollADiceFragment() {}

	@Override
	public RollADiceScoreBoardFragment createScoreBoardFragment() {
		return new RollADiceScoreBoardFragment();
	}

	@Override
	protected void setListeners() {
		mScoreBoardFragment.setActionOnChangingDiceCount(mDiceFragment::setDiceCount);
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
	public boolean rollOnStart() {
		return false;
	}

	@Override
	public void startNewGame() {
		super.startNewGame();
		mDiceFragment.activate();
	}

}
