package com.zzy.dicegames.gamefragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zzy.dicegames.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Yahtzee计分板Fragment，嵌套于一个{@link AbstractYahtzeeFragment}<br>
 * 通过{@link #setArguments(Bundle)}传入的参数：
 * <ul>
 *     <li>layoutId：布局资源id</li>
 * </ul>
 *
 * @author 赵正阳
 */
public abstract class AbstractYahtzeeScoreBoardFragment extends Fragment {
	/** 所属的游戏Fragment */
	private AbstractYahtzeeFragment mGameFragment;

	/** 得分项按钮 */
	protected List<Button> mScoreButtons = new ArrayList<>();

	/** 得分标签 */
	protected List<TextView> mScoreTextViews = new ArrayList<>();

	/** 上区总分 */
	private int mUpperTotal = 0;

	/** 上区总分标签 */
	protected TextView mUpperTotalTextView;

	/** 奖励分 */
	private int mBonus = 0;

	/** 奖励分标签 */
	protected TextView mBonusTextView;

	/** 游戏总分 */
	private int mGameTotal = 0;

	/** 游戏总分标签 */
	protected TextView mGameTotalTextView;

	/** 已选择得分项的个数 */
	private int mSelected = 0;

	/** 选择一个得分项时执行的动作 */
	protected View.OnClickListener mChooseAction = v -> choose(mScoreButtons.indexOf(v));

	/** 游戏结束时执行的动作 */
	private Runnable mGameOverAction;

	/** 用于保存和恢复状态：每个得分项是否已选择 */
	private static final String CATEGORY_SELECTED = "categorySelected";

	/** 用于保存和恢复状态：每个得分项的得分 */
	private static final String CATEGORY_SCORE = "categoryScore";

	public AbstractYahtzeeScoreBoardFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mGameFragment = (AbstractYahtzeeFragment) getParentFragment();
		View rootView = inflater.inflate(getArguments().getInt("layoutId"), container, false);
		initViews(rootView);
		return rootView;
	}

	/** 获取得分按钮和标签 */
	protected abstract void initViews(View rootView);

	@Override
	public void onSaveInstanceState(Bundle outState) {
		List<Boolean> categorySelected = IntStream.range(0, mGameFragment.getCategoryCount())
				.mapToObj(this::isSelected)
				.collect(Collectors.toList());
		outState.putSerializable(CATEGORY_SELECTED, (Serializable) categorySelected);

		List<Integer> categoryScore = mScoreTextViews.stream()
				.map(t -> Integer.parseInt(t.getText().toString()))
				.collect(Collectors.toList());
		outState.putSerializable(CATEGORY_SCORE, (Serializable) categoryScore);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
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
				if (mUpperTotal >= mGameFragment.getBonusCondition()) {
					mBonus = mGameFragment.getBonus();
					mGameTotal += mBonus;
				}
				mUpperTotalTextView.setText(String.valueOf(mUpperTotal));
				mBonusTextView.setText(String.valueOf(mBonus));
				mGameTotalTextView.setText(String.valueOf(mGameTotal));
			}
		}
	}

	/** 选择第{@code index}项，更新得分并激活"Roll"按钮 */
	private void choose(int index) {
		int score = Integer.parseInt(mScoreTextViews.get(index).getText().toString());
		if (index <= 5) {
			mUpperTotal += score;
			mUpperTotalTextView.setText(String.valueOf(mUpperTotal));
			if (mUpperTotal >= mGameFragment.getBonusCondition() && mBonus == 0) {
				mBonus = mGameFragment.getBonus();
				mBonusTextView.setText(String.valueOf(mBonus));
				mGameTotal += mBonus;
			}
		}
		mGameTotal += score;
		mGameTotalTextView.setText(String.valueOf(mGameTotal));

		mScoreButtons.get(index).setEnabled(false);
		mScoreTextViews.get(index).setTextColor(Color.RED);

		++mSelected;
		if (mSelected == mGameFragment.getCategoryCount())
			new AlertDialog.Builder(getContext())
					.setTitle(getContext().getString(R.string.gameOver))
					.setMessage(String.format("%s: %d", getContext().getString(R.string.score), mGameTotal))
					.setPositiveButton(R.string.ok, (dialog, which) -> {
						if (mGameOverAction != null)
							mGameOverAction.run();
					})
					.show();
		else
			mGameFragment.activateRollButton();
	}

	public void setGameOverAction(Runnable gameOverAction) {
		mGameOverAction = gameOverAction;
	}

	/** 根据骰子点数更新分数 */
	public void updateScores(int[] diceNumbers) {
		Arrays.sort(diceNumbers);
		for (int i = 0; i < mScoreButtons.size(); ++i)
			if (mScoreButtons.get(i).isEnabled())
				mScoreTextViews.get(i).setText(
						String.valueOf(mGameFragment.calcScore(diceNumbers, i)));
	}

	public boolean isSelected(int index) {
		if (index < 0 || index >= mScoreButtons.size())
			return false;
		else
			return !mScoreButtons.get(index).isEnabled();
	}

}
