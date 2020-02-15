package com.zzy.dicegames.gamefragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;

/**
 * Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public abstract class AbstractYahtzeeFragment extends GameFragment {
	/** 计分板 */
	protected AbstractYahtzeeScoreBoardFragment scoreBoard;

	public AbstractYahtzeeFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState == null) {
			scoreBoard = createScoreBoardFragment();
			scoreBoard.setGameOverAction(this::startNewGame);
			getChildFragmentManager().beginTransaction()
					.add(R.id.gameFragment, scoreBoard)
					.commit();
			mDiceFragment.setRollListener(scoreBoard::updateScores);
		}

		return rootView;
	}

	@Override
	public void startNewGame() {
		scoreBoard = createScoreBoardFragment();
		scoreBoard.setGameOverAction(this::startNewGame);
		getChildFragmentManager().beginTransaction()
				.replace(R.id.gameFragment, scoreBoard)
				.commit();
		mDiceFragment.setRollListener(scoreBoard::updateScores);
		mDiceFragment.activateRollButton();
	}

	/** 返回一个新的计分板Fragment */
	public abstract AbstractYahtzeeScoreBoardFragment createScoreBoardFragment();

	/** 返回得分项数量 */
	public abstract int getCategoryCount();

	/** 上区总分大于等于多少时获得奖励分 */
	public abstract int getBonusCondition();

	/** 奖励分 */
	public abstract int getBonus();

	/**
	 * 根据骰子点数计算得分项的得分
	 *
	 * @param d 排序后的骰子点数
	 * @param index 得分项索引
	 */
	public abstract int calcScore(int[] d, int index);

}
