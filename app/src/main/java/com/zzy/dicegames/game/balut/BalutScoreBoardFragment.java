package com.zzy.dicegames.game.balut;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.entity.BalutScore;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Balut计分板Fragment，嵌套于一个{@link BalutFragment}
 *
 * @author 赵正阳
 */
public class BalutScoreBoardFragment extends Fragment {
	// ----------游戏参数----------
	/** 每个得分项可选择的次数 */
	private static final int TIMES_PER_ITEM = 4;

	// ----------游戏状态数据----------
	/** 得分项按钮 */
	private List<Button> mScoreButtons = new ArrayList<>();

	/** 得分标签 */
	private List<List<TextView>> mScoreTextViews = new ArrayList<>();

	/** 游戏总分 */
	private int mGameTotal = 0;

	/** 游戏总分标签 */
	private TextView mGameTotalTextView;

	/** 已选择得分项的个数 */
	private int mSelected = 0;

	/** 每个得分项已选择的次数 */
	private List<Integer> mItemSelected = new ArrayList<>();

	/** 每次选择一项后执行的动作 */
	private Runnable mActionAfterChoosing;

	/** 游戏结束时执行的动作 */
	private Consumer<BalutScore> mGameOverAction;

	// ----------保存和恢复状态----------
	/** 用于保存和恢复状态：每个得分项是否已选择 */
	private static final String CATEGORY_SELECTED = "categorySelected";

	/** 用于保存和恢复状态：每个得分项的得分 */
	private static final String CATEGORY_SCORE = "categoryScore";

	public BalutScoreBoardFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_balut_score_board, container, false);
		initViews(rootView);
		for (int i = 0; i < mScoreButtons.size(); ++i)
			mItemSelected.add(0);

		if (savedInstanceState != null)
			restoreState(savedInstanceState);

		return rootView;
	}

	/** 获取得分按钮和标签 */
	private void initViews(View rootView) {
		mScoreButtons.add(rootView.findViewById(R.id.btn4));
		mScoreButtons.add(rootView.findViewById(R.id.btn5));
		mScoreButtons.add(rootView.findViewById(R.id.btn6));
		mScoreButtons.add(rootView.findViewById(R.id.btnStraight));
		mScoreButtons.add(rootView.findViewById(R.id.btnFullHouse));
		mScoreButtons.add(rootView.findViewById(R.id.btnChoice));
		mScoreButtons.add(rootView.findViewById(R.id.btnBalut));
		for (int i = 0; i < mScoreButtons.size(); ++i) {
			int finalI = i;
			mScoreButtons.get(i).setOnClickListener(v -> choose(finalI));
		}

		mScoreTextViews.add(Arrays.asList(
				rootView.findViewById(R.id.tv41),
				rootView.findViewById(R.id.tv42),
				rootView.findViewById(R.id.tv43),
				rootView.findViewById(R.id.tv44)
		));
		mScoreTextViews.add(Arrays.asList(
				rootView.findViewById(R.id.tv51),
				rootView.findViewById(R.id.tv52),
				rootView.findViewById(R.id.tv53),
				rootView.findViewById(R.id.tv54)
		));
		mScoreTextViews.add(Arrays.asList(
				rootView.findViewById(R.id.tv61),
				rootView.findViewById(R.id.tv62),
				rootView.findViewById(R.id.tv63),
				rootView.findViewById(R.id.tv64)
		));
		mScoreTextViews.add(Arrays.asList(
				rootView.findViewById(R.id.tvStraight1),
				rootView.findViewById(R.id.tvStraight2),
				rootView.findViewById(R.id.tvStraight3),
				rootView.findViewById(R.id.tvStraight4)
		));
		mScoreTextViews.add(Arrays.asList(
				rootView.findViewById(R.id.tvFullHouse1),
				rootView.findViewById(R.id.tvFullHouse2),
				rootView.findViewById(R.id.tvFullHouse3),
				rootView.findViewById(R.id.tvFullHouse4)
		));
		mScoreTextViews.add(Arrays.asList(
				rootView.findViewById(R.id.tvChoice1),
				rootView.findViewById(R.id.tvChoice2),
				rootView.findViewById(R.id.tvChoice3),
				rootView.findViewById(R.id.tvChoice4)
		));
		mScoreTextViews.add(Arrays.asList(
				rootView.findViewById(R.id.tvBalut1),
				rootView.findViewById(R.id.tvBalut2),
				rootView.findViewById(R.id.tvBalut3),
				rootView.findViewById(R.id.tvBalut4)
		));

		mGameTotalTextView = rootView.findViewById(R.id.tvGameTotal);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		List<List<Boolean>> categorySelected = new ArrayList<>();
		for (int i = 0; i < mItemSelected.size(); ++i) {
			int finalI = i;
			categorySelected.add(IntStream.range(0, TIMES_PER_ITEM)
					.mapToObj(x -> x < mItemSelected.get(finalI))
					.collect(Collectors.toList())
			);
		}
		outState.putSerializable(CATEGORY_SELECTED, (Serializable) categorySelected);

		List<List<Integer>> categoryScore = new ArrayList<>();
		for (int i = 0; i < mScoreTextViews.size(); ++i)
			categoryScore.add(mScoreTextViews.get(i).stream()
					.map(t -> Integer.parseInt(t.getText().toString()))
					.collect(Collectors.toList())
			);
		outState.putSerializable(CATEGORY_SCORE, (Serializable) categoryScore);

		super.onSaveInstanceState(outState);
	}

	private void restoreState(Bundle savedInstanceState) {
		List<List<Boolean>> categorySelected = (List<List<Boolean>>) savedInstanceState.getSerializable(CATEGORY_SELECTED);
		List<List<Integer>> categoryScore = (List<List<Integer>>) savedInstanceState.getSerializable(CATEGORY_SCORE);
		if (categorySelected != null && categoryScore != null) {
			for (int i = 0; i < categorySelected.size(); ++i) {
				for (int j = 0; j < TIMES_PER_ITEM; ++j) {
					mScoreTextViews.get(i).get(j).setText(String.valueOf(categoryScore.get(i).get(j)));
					if (categorySelected.get(i).get(j)) {
						mScoreTextViews.get(i).get(j).setTextColor(Color.RED);
						mItemSelected.set(i, mItemSelected.get(i) + 1);
						mGameTotal += categoryScore.get(i).get(j);
					}
				}
				if (mItemSelected.get(i) == TIMES_PER_ITEM) {
					mScoreButtons.get(i).setEnabled(false);
					++mSelected;
				}
			}
			mGameTotalTextView.setText(String.valueOf(mGameTotal));
		}
	}

	/** 选择第{@code index}项，更新得分并激活"Roll"按钮 */
	private void choose(int index) {
		int score = Integer.parseInt(mScoreTextViews.get(index).get(mItemSelected.get(index)).getText().toString());
		mGameTotal += score;
		mGameTotalTextView.setText(String.valueOf(mGameTotal));

		mScoreTextViews.get(index).get(mItemSelected.get(index)).setTextColor(Color.RED);
		mItemSelected.set(index, mItemSelected.get(index) + 1);
		if (mItemSelected.get(index) == TIMES_PER_ITEM) {
			mScoreButtons.get(index).setEnabled(false);
			++mSelected;
		}

		if (mSelected == mScoreButtons.size()) {
			if (mGameOverAction != null) {
				int gotBalut = (int) mScoreTextViews.get(6).stream()
						.filter(t -> !t.getText().toString().equals("0"))
						.count();
				mGameOverAction.accept(new BalutScore(LocalDate.now().toString(), mGameTotal, gotBalut));
			}
		}
		else if (mActionAfterChoosing != null)
			mActionAfterChoosing.run();
	}

	public void setActionAfterChoosing(Runnable actionAfterChoosing) {
		mActionAfterChoosing = actionAfterChoosing;
	}

	public void setGameOverAction(Consumer<BalutScore> gameOverAction) {
		mGameOverAction = gameOverAction;
	}

	/**
	 * 根据骰子点数计算所有得分项的得分
	 *
	 * @param d 骰子点数
	 * @return 长度为{@code mScoreButtons.size()}的数组，表示d在每个得分项的得分
	 */
	public int[] calcScores(int[] d) {
		Arrays.sort(d);
		int sum = Arrays.stream(d).sum();

		int[] result = new int[mScoreButtons.size()];
		// 4~6
		for (int k : d)
			if (k >= 4 && k <= 6)
				result[k - 4] += k;
		// 连顺
		if (d[0] == 1 && d[1] == 2 && d[2] == 3 && d[3] == 4 && d[4] == 5)
			result[3] = 15;
		else if (d[0] == 2 && d[1] == 3 && d[2] == 4 && d[3] == 5 && d[4] == 6)
			result[3] = 20;
		// 葫芦
		if ((d[0] == d[1] && d[1] == d[2] && d[3] == d[4] && d[0] != d[3])
				|| (d[0] == d[1] && d[2] == d[3] && d[3] == d[4] && d[0] != d[2]))
			result[4] = sum;
		// 选择
		result[5] = sum;
		// Balut
		if (Arrays.stream(d).allMatch(x -> x == d[0]))
			result[6] = 20 + 5 * d[0];
		return result;
	}

	/** 根据骰子点数更新得分 */
	public void updateScores(int[] diceNumbers) {
		int[] scores = calcScores(diceNumbers);
		for (int i = 0; i < mScoreButtons.size(); ++i)
			if (mItemSelected.get(i) < TIMES_PER_ITEM)
				mScoreTextViews.get(i).get(mItemSelected.get(i)).setText(String.valueOf(scores[i]));
	}

}
