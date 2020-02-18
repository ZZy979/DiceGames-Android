package com.zzy.dicegames.gamefragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;

import java.util.Arrays;

/**
 * Balut游戏Fragment
 *
 * @author 赵正阳
 */
public class BalutFragment extends GameFragment {
	/** 计分板 */
	private BalutScoreBoardFragment scoreBoard;

	public BalutFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState == null) {
			scoreBoard = new BalutScoreBoardFragment();
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
		scoreBoard = new BalutScoreBoardFragment();
		scoreBoard.setGameOverAction(this::startNewGame);
		getChildFragmentManager().beginTransaction()
				.replace(R.id.gameFragment, scoreBoard)
				.commit();
		mDiceFragment.setRollListener(scoreBoard::updateScores);
		mDiceFragment.activateRollButton();
	}

	@Override
	public String getTitle() {
		return getContext().getString(R.string.balut);
	}

	@Override
	public String getGameTypeCode() {
		return "balut";
	}

	@Override
	public int getDiceCount() {
		return 5;
	}

	@Override
	public int getRollTimes() {
		return 2;
	}

	/** 返回得分项数量 */
	public int getCategoryCount() {
		return 7;
	}

	public int calcScore(int[] d, int index) {
		int sum = Arrays.stream(d).sum();
		switch (index) {
		case 0: case 1: case 2:     // 4~6
			return Arrays.stream(d).filter(x -> x == index + 4).sum();
		case 3:     // 连顺
			if (d[0] == 1 && d[1] == 2 && d[2] == 3 && d[3] == 4 && d[4] == 5)
				return 15;
			else if (d[0] == 2 && d[1] == 3 && d[2] == 4 && d[3] == 5 && d[4] == 6)
				return 20;
			else
				return 0;
		case 4:     // 葫芦
			if ((d[0] == d[1] && d[1] == d[2] && d[3] == d[4] && d[0] != d[3])
					|| (d[0] == d[1] && d[2] == d[3] && d[3] == d[4] && d[0] != d[2]))
				return sum;
			else
				return 0;
		case 5:     // 选择
			return sum;
		case 6:     // Balut
			return Arrays.stream(d).allMatch(x -> x == d[0]) ? 20 + 5 * d[0] : 0;
		default:
			return 0;
		}
	}

}
