package com.zzy.dicegames.fragment.game.yahtzee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.fragment.game.GameFragment;

/**
 * Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public abstract class AbstractYahtzeeFragment extends GameFragment {
	/** 计分板 */
	protected AbstractYahtzeeScoreBoardFragment mScoreBoardFragment;

	public AbstractYahtzeeFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState == null) {
			mScoreBoardFragment = createScoreBoardFragment();
			getChildFragmentManager().beginTransaction()
					.add(R.id.scoreBoardFragment, mScoreBoardFragment)
					.commit();
		}
		else
			mScoreBoardFragment = (AbstractYahtzeeScoreBoardFragment) getChildFragmentManager().findFragmentById(R.id.scoreBoardFragment);

		mDiceFragment.setRollListener(mScoreBoardFragment::updateScores);
		mScoreBoardFragment.setActionAfterChoosing(mDiceFragment::activate);
		mScoreBoardFragment.setGameOverAction(this::onGameOver);

		return rootView;
	}

	@Override
	public void startNewGame() {
		super.startNewGame();
		mScoreBoardFragment = createScoreBoardFragment();
		getChildFragmentManager().beginTransaction()
				.replace(R.id.scoreBoardFragment, mScoreBoardFragment)
				.commit();

		mDiceFragment.setRollListener(mScoreBoardFragment::updateScores);
		mScoreBoardFragment.setActionAfterChoosing(mDiceFragment::activate);
		mScoreBoardFragment.setGameOverAction(this::onGameOver);
		mDiceFragment.activate();
	}

	/** 返回一个新的计分板Fragment */
	public abstract AbstractYahtzeeScoreBoardFragment createScoreBoardFragment();

	/**
	 * 游戏结束时的回调函数，保存得分并开始新游戏（如果作弊则不保存得分）<br>
	 * 将该方法设置为计分板的监听器，游戏结束时计分板将以本局得分为参数调用该监听器
	 */
	protected void onGameOver(AbstractYahtzeeScore score) {
		int rank = mCheated ? 0 : saveScore(score);
		showScore(score.getScore(), rank);
	}

	/** 保存得分，返回该得分在前10名中的名次，0表示不在前10名中 */
	protected abstract int saveScore(AbstractYahtzeeScore score);

}
