package com.zzy.dicegames.game.yahtzee;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zzy.dicegames.database.entity.AbstractYahtzeeScore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Yahtzee计分板Fragment，嵌套于一个{@link AbstractYahtzeeFragment}<br>
 * 传入的参数：
 * <ul>
 *     <li>{@link #LAYOUT_ID}：布局资源id</li>
 * </ul>
 *
 * @author 赵正阳
 */
public abstract class AbstractYahtzeeScoreBoardFragment extends Fragment {
	// ----------传入参数----------
	/** 传入参数：布局资源id */
	public static final String LAYOUT_ID = "layoutId";

	// ----------游戏状态数据----------
	/** 得分项按钮 */
	protected List<Button> mScoreButtons = new ArrayList<>();

	/** 得分标签 */
	protected List<TextView> mScoreTextViews = new ArrayList<>();

	/** 上区总分 */
	protected int mUpperTotal = 0;

	/** 上区总分标签 */
	protected TextView mUpperTotalTextView;

	/** 奖励分 */
	protected int mBonus = 0;

	/** 奖励分标签 */
	protected TextView mBonusTextView;

	/** 游戏总分 */
	protected int mGameTotal = 0;

	/** 游戏总分标签 */
	protected TextView mGameTotalTextView;

	/** 已选择得分项的个数 */
	protected int mSelected = 0;

	/** 每次选择一项后执行的动作 */
	private Runnable mActionAfterChoosing;

	/** 游戏结束时执行的动作 */
	private Consumer<AbstractYahtzeeScore> mGameOverAction;

	// ----------保存和恢复状态----------
	/** 用于保存和恢复状态：每个得分项是否已选择 */
	private static final String CATEGORY_SELECTED = "categorySelected";

	/** 用于保存和恢复状态：每个得分项的得分 */
	private static final String CATEGORY_SCORE = "categoryScore";

	public AbstractYahtzeeScoreBoardFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		View rootView = inflater.inflate(bundle.getInt(LAYOUT_ID), container, false);
		initViews(rootView);

		if (savedInstanceState != null)
			restoreState(savedInstanceState);

		return rootView;
	}

	/** 获取得分按钮和标签 */
	protected abstract void initViews(View rootView);

	@Override
	public void onSaveInstanceState(Bundle outState) {
		List<Boolean> categorySelected = IntStream.range(0, mScoreButtons.size())
				.mapToObj(this::isSelected)
				.collect(Collectors.toList());
		outState.putSerializable(CATEGORY_SELECTED, (Serializable) categorySelected);

		List<Integer> categoryScore = mScoreTextViews.stream()
				.map(t -> Integer.parseInt(t.getText().toString()))
				.collect(Collectors.toList());
		outState.putSerializable(CATEGORY_SCORE, (Serializable) categoryScore);

		super.onSaveInstanceState(outState);
	}

	private void restoreState(Bundle savedInstanceState) {
		List<Boolean> categorySelected = (List<Boolean>) savedInstanceState.getSerializable(CATEGORY_SELECTED);
		List<Integer> categoryScore = (List<Integer>) savedInstanceState.getSerializable(CATEGORY_SCORE);
		if (categorySelected != null && categoryScore != null) {
			for (int i = 0; i < categorySelected.size(); ++i) {
				mScoreTextViews.get(i).setText(String.valueOf(categoryScore.get(i)));
				if (categorySelected.get(i)) {
					mScoreButtons.get(i).setEnabled(false);
					mScoreTextViews.get(i).setTextColor(Color.RED);
					++mSelected;
					if (i <= 5)
						mUpperTotal += categoryScore.get(i);
					mGameTotal += categoryScore.get(i);
				}
			}
			if (mUpperTotal >= getGameBonusCondition()) {
				mBonus = getGameBonus();
				mGameTotal += mBonus;
			}
			mUpperTotalTextView.setText(String.valueOf(mUpperTotal));
			mBonusTextView.setText(String.valueOf(mBonus));
			mGameTotalTextView.setText(String.valueOf(mGameTotal));
		}
	}

	/** 上区总分大于等于多少时获得奖励分 */
	public abstract int getGameBonusCondition();

	/** 奖励分数量 */
	public abstract int getGameBonus();

	/** 选择第{@code index}项，更新得分并激活"Roll"按钮 */
	protected void choose(int index) {
		int score = Integer.parseInt(mScoreTextViews.get(index).getText().toString());
		if (index <= 5) {
			mUpperTotal += score;
			mUpperTotalTextView.setText(String.valueOf(mUpperTotal));
			if (mUpperTotal >= getGameBonusCondition() && mBonus == 0) {
				mBonus = getGameBonus();
				mBonusTextView.setText(String.valueOf(mBonus));
				mGameTotal += mBonus;
			}
		}
		mGameTotal += score;
		mGameTotalTextView.setText(String.valueOf(mGameTotal));

		mScoreButtons.get(index).setEnabled(false);
		mScoreTextViews.get(index).setTextColor(Color.RED);

		++mSelected;
		if (mSelected == mScoreButtons.size()) {
			if (mGameOverAction != null)
				mGameOverAction.accept(getScore());
		}
		else if (mActionAfterChoosing != null)
			mActionAfterChoosing.run();
	}

	public void setActionAfterChoosing(Runnable actionAfterChoosing) {
		mActionAfterChoosing = actionAfterChoosing;
	}

	public void setGameOverAction(Consumer<AbstractYahtzeeScore> gameOverAction) {
		mGameOverAction = gameOverAction;
	}

	public boolean isSelected(int index) {
		if (index < 0 || index >= mScoreButtons.size())
			return false;
		else
			return !mScoreButtons.get(index).isEnabled();
	}

	/**
	 * 根据骰子点数计算所有得分项的得分
	 *
	 * @param d 骰子点数
	 * @return 长度为{@code mScoreButtons.size()}的数组，表示d在每个得分项的得分
	 */
	public abstract int[] calcScores(int[] d);

	/** 根据骰子点数更新得分 */
	public void updateScores(int[] diceNumbers) {
		int[] scores = calcScores(diceNumbers);
		for (int i = 0; i < mScoreButtons.size(); ++i)
			if (!isSelected(i))
				mScoreTextViews.get(i).setText(String.valueOf(scores[i]));
	}

	/** 游戏结束时获取得分 */
	protected abstract AbstractYahtzeeScore getScore();

}
