package com.zzy.dicegames.fragment.game.balut;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.ScoreDatabase;
import com.zzy.dicegames.database.dao.BalutScoreDao;
import com.zzy.dicegames.database.entity.BalutScore;
import com.zzy.dicegames.fragment.game.GameFragment;

/**
 * Balut游戏Fragment
 *
 * @author 赵正阳
 */
public class BalutFragment extends GameFragment {
	/** 计分板 */
	private BalutScoreBoardFragment mScoreBoardFragment;

	public BalutFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState == null) {
			mScoreBoardFragment = new BalutScoreBoardFragment();
			getChildFragmentManager().beginTransaction()
					.add(R.id.scoreBoardFragment, mScoreBoardFragment)
					.commit();
		}
		else
			mScoreBoardFragment = (BalutScoreBoardFragment) getChildFragmentManager().findFragmentById(R.id.scoreBoardFragment);

		mDiceFragment.setRollListener(mScoreBoardFragment::updateScores);
		mScoreBoardFragment.setActionAfterChoosing(mDiceFragment::activateRollButton);
		mScoreBoardFragment.setGameOverAction(this::onGameOver);

		return rootView;
	}

	@Override
	public String getTitle() {
		return getString(R.string.balut);
	}

	@Override
	public int getDiceCount() {
		return 5;
	}

	@Override
	public int getRollTimes() {
		return 2;
	}

	@Override
	public void startNewGame() {
		super.startNewGame();
		mScoreBoardFragment = new BalutScoreBoardFragment();
		getChildFragmentManager().beginTransaction()
				.replace(R.id.scoreBoardFragment, mScoreBoardFragment)
				.commit();

		mDiceFragment.setRollListener(mScoreBoardFragment::updateScores);
		mScoreBoardFragment.setActionAfterChoosing(mDiceFragment::activateRollButton);
		mScoreBoardFragment.setGameOverAction(this::onGameOver);
		mDiceFragment.activateRollButton();
	}

	/**
	 * 游戏结束时的回调函数，保存得分并开始新游戏（如果作弊则不保存得分）<br>
	 * 将该方法设置为计分板的监听器，游戏结束时计分板将以本局得分为参数调用该监听器
	 */
	private void onGameOver(BalutScore score) {
		int rank = mCheated ? 0 : saveScore(score);
		showScore(score.getScore(), rank);
	}

	/** 保存得分，返回该得分在前10名中的名次，0表示不在前10名中 */
	private int saveScore(BalutScore score) {
		BalutScoreDao balutScoreDao = ScoreDatabase.getInstance(getContext()).balutScoreDao();
		balutScoreDao.insert(score);
		return balutScoreDao.findTop10Score().indexOf(score.getScore()) + 1;
	}

}
