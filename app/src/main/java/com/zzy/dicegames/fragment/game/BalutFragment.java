package com.zzy.dicegames.fragment.game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.ScoreDatabase;
import com.zzy.dicegames.database.dao.BalutScoreDao;
import com.zzy.dicegames.database.entity.BalutScore;

import java.util.Arrays;

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
			Bundle bundle = new Bundle();
			bundle.putInt(AbstractYahtzeeScoreBoardFragment.CATEGORY_COUNT, getCategoryCount());
			mScoreBoardFragment.setArguments(bundle);
			getChildFragmentManager().beginTransaction()
					.add(R.id.scoreBoardFragment, mScoreBoardFragment)
					.commit();
		}
		else
			mScoreBoardFragment = (BalutScoreBoardFragment) getChildFragmentManager().findFragmentById(R.id.scoreBoardFragment);

		mDiceFragment.setRollListener(mScoreBoardFragment::updateScores);
		mScoreBoardFragment.setActionAfterChoosing(mDiceFragment::activateRollButton);
		mScoreBoardFragment.setCalcScoreFunc(this::calcScore);
		mScoreBoardFragment.setGameOverAction(this::onGameOver);

		return rootView;
	}

	@Override
	public String getTitle() {
		return getContext().getString(R.string.balut);
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
		Bundle bundle = new Bundle();
		bundle.putInt(AbstractYahtzeeScoreBoardFragment.CATEGORY_COUNT, getCategoryCount());
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

	/**
	 * 游戏结束时的回调函数，保存得分并开始新游戏（如果作弊则不保存得分）<br>
	 * 将该方法设置为计分板的监听器，游戏结束时计分板将以本局得分为参数调用该监听器
	 */
	private void onGameOver(BalutScore score) {
		int rank = mCheated ? 0 : saveScore(score);
		String honor;
		if (rank == 1)
			honor = getString(R.string.newHighScore);
		else if (rank >= 2 && rank <= 10)
			honor = getString(R.string.top10);
		else
			honor = getString(R.string.score);
		new AlertDialog.Builder(getContext())
				.setTitle(getContext().getString(R.string.gameOver))
				.setMessage(String.format("%s: %d", honor, score.getScore()))
				.setPositiveButton(R.string.ok, (dialog, which) -> startNewGame())
				.show();
	}

	/** 保存得分，返回该得分在前10名中的名次，0表示不在前10名中 */
	private int saveScore(BalutScore score) {
		BalutScoreDao balutScoreDao = ScoreDatabase.getInstance(getContext()).balutScoreDao();
		balutScoreDao.insert(score);
		return balutScoreDao.findTop10Score().indexOf(score.getScore()) + 1;
	}

}
