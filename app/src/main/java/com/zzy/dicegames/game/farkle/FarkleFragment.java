package com.zzy.dicegames.game.farkle;

import android.os.Bundle;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.ScoreDatabase;
import com.zzy.dicegames.database.entity.FarkleScore;
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
		mScoreBoardFragment.setGameOverAction(this::onGameOver);
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

	/** 游戏结束时的回调函数 */
	private void onGameOver(FarkleScore score) {
		if (!mCheated)
			ScoreDatabase.getInstance(getContext()).farkleScoreDao().insert(score);
	}

}
