package com.zzy.dicegames.fragment.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.entity.AbstractYahtzeeScore;

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
			Bundle bundle = new Bundle();
			bundle.putInt(AbstractYahtzeeScoreBoardFragment.CATEGORY_COUNT, getCategoryCount());
			bundle.putInt(AbstractYahtzeeScoreBoardFragment.GAME_BONUS_CONDITION, getGameBonusCondition());
			bundle.putInt(AbstractYahtzeeScoreBoardFragment.GAME_BONUS, getGameBonus());
			mScoreBoardFragment.setArguments(bundle);
			getChildFragmentManager().beginTransaction()
					.add(R.id.scoreBoardFragment, mScoreBoardFragment)
					.commit();
		}
		else
			mScoreBoardFragment = (AbstractYahtzeeScoreBoardFragment) getChildFragmentManager().findFragmentById(R.id.scoreBoardFragment);

		mDiceFragment.setRollListener(mScoreBoardFragment::updateScores);
		mScoreBoardFragment.setActionAfterChoosing(mDiceFragment::activateRollButton);
		mScoreBoardFragment.setCalcScoreFunc(this::calcScore);
		mScoreBoardFragment.setGameOverAction(this::onGameOver);

		return rootView;
	}

	@Override
	public void startNewGame() {
		super.startNewGame();
		mScoreBoardFragment = createScoreBoardFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(AbstractYahtzeeScoreBoardFragment.CATEGORY_COUNT, getCategoryCount());
		bundle.putInt(AbstractYahtzeeScoreBoardFragment.GAME_BONUS_CONDITION, getGameBonusCondition());
		bundle.putInt(AbstractYahtzeeScoreBoardFragment.GAME_BONUS, getGameBonus());
		mScoreBoardFragment.setArguments(bundle);
		getChildFragmentManager().beginTransaction()
				.replace(R.id.scoreBoardFragment, mScoreBoardFragment)
				.commit();

		mDiceFragment.setRollListener(mScoreBoardFragment::updateScores);
		mScoreBoardFragment.setActionAfterChoosing(mDiceFragment::activateRollButton);
		mScoreBoardFragment.setCalcScoreFunc(this::calcScore);
		mScoreBoardFragment.setGameOverAction(this::onGameOver);
		mDiceFragment.activateRollButton();
	}

	/** 返回一个新的计分板Fragment */
	public abstract AbstractYahtzeeScoreBoardFragment createScoreBoardFragment();

	/** 返回得分项数量 */
	public abstract int getCategoryCount();

	/** 上区总分大于等于多少时获得奖励分 */
	public abstract int getGameBonusCondition();

	/** 奖励分 */
	public abstract int getGameBonus();

	/**
	 * 根据骰子点数计算得分项的得分
	 *
	 * @param d 排序后的骰子点数
	 * @param index 得分项索引
	 */
	public abstract int calcScore(int[] d, int index);

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
