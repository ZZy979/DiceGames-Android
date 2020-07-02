package com.zzy.dicegames.game.yahtzee;

import com.zzy.dicegames.database.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.game.GameFragment;

/**
 * Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public abstract class AbstractYahtzeeFragment extends GameFragment<AbstractYahtzeeScoreBoardFragment> {

	public AbstractYahtzeeFragment() {}

	@Override
	protected void setListeners() {
		mDiceFragment.setRollListener(mScoreBoardFragment::updateScores);
		mScoreBoardFragment.setActionAfterChoosing(mDiceFragment::activate);
		mScoreBoardFragment.setGameOverAction(this::onGameOver);
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
	protected void onGameOver(AbstractYahtzeeScore score) {
		int rank = mCheated ? 0 : saveScore(score);
		showScore(score.getScore(), rank);
	}

	/** 保存得分，返回该得分在前10名中的名次，0表示不在前10名中 */
	protected abstract int saveScore(AbstractYahtzeeScore score);

}
