package com.zzy.dicegames.game.balut;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.ScoreDatabase;
import com.zzy.dicegames.database.dao.BalutScoreDao;
import com.zzy.dicegames.database.entity.BalutScore;
import com.zzy.dicegames.game.GameFragment;

/**
 * Balut游戏Fragment
 *
 * @author 赵正阳
 */
public class BalutFragment extends GameFragment<BalutScoreBoardFragment> {

	public BalutFragment() {}

	@Override
	public BalutScoreBoardFragment createScoreBoardFragment() {
		return new BalutScoreBoardFragment();
	}

	@Override
	protected void setListeners() {
		mDiceFragment.setRollListener(mScoreBoardFragment::updateScores);
		mScoreBoardFragment.setActionAfterChoosing(mDiceFragment::activate);
		mScoreBoardFragment.setGameOverAction(this::onGameOver);
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
		mDiceFragment.activate();
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
