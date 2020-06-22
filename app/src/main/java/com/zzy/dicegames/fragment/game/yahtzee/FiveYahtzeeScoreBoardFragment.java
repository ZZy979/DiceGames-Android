package com.zzy.dicegames.fragment.game.yahtzee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.database.entity.FiveYahtzeeScore;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 5骰Yahtzee计分板Fragment
 *
 * @author 赵正阳
 */
public class FiveYahtzeeScoreBoardFragment extends AbstractYahtzeeScoreBoardFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Bundle bundle = new Bundle();
		bundle.putInt(LAYOUT_ID, R.layout.fragment_five_yahtzee_score_board);
		setArguments(bundle);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	protected void initViews(View rootView) {
		mScoreButtons.add(rootView.findViewById(R.id.btn1));
		mScoreButtons.add(rootView.findViewById(R.id.btn2));
		mScoreButtons.add(rootView.findViewById(R.id.btn3));
		mScoreButtons.add(rootView.findViewById(R.id.btn4));
		mScoreButtons.add(rootView.findViewById(R.id.btn5));
		mScoreButtons.add(rootView.findViewById(R.id.btn6));
		mScoreButtons.add(rootView.findViewById(R.id.btn2p));
		mScoreButtons.add(rootView.findViewById(R.id.btn3e));
		mScoreButtons.add(rootView.findViewById(R.id.btn4e));
		mScoreButtons.add(rootView.findViewById(R.id.btnFullHouse));
		mScoreButtons.add(rootView.findViewById(R.id.btnSmallStraight));
		mScoreButtons.add(rootView.findViewById(R.id.btnLargeStraight));
		mScoreButtons.add(rootView.findViewById(R.id.btnChance));
		mScoreButtons.add(rootView.findViewById(R.id.btnYahtzee));
		for (int i = 0; i < mScoreButtons.size(); ++i) {
			int finalI = i;
			mScoreButtons.get(i).setOnClickListener(v -> choose(finalI));
		}

		mScoreTextViews.add(rootView.findViewById(R.id.tv1));
		mScoreTextViews.add(rootView.findViewById(R.id.tv2));
		mScoreTextViews.add(rootView.findViewById(R.id.tv3));
		mScoreTextViews.add(rootView.findViewById(R.id.tv4));
		mScoreTextViews.add(rootView.findViewById(R.id.tv5));
		mScoreTextViews.add(rootView.findViewById(R.id.tv6));
		mScoreTextViews.add(rootView.findViewById(R.id.tv2p));
		mScoreTextViews.add(rootView.findViewById(R.id.tv3e));
		mScoreTextViews.add(rootView.findViewById(R.id.tv4e));
		mScoreTextViews.add(rootView.findViewById(R.id.tvFullHouse));
		mScoreTextViews.add(rootView.findViewById(R.id.tvSmallStraight));
		mScoreTextViews.add(rootView.findViewById(R.id.tvLargeStraight));
		mScoreTextViews.add(rootView.findViewById(R.id.tvChance));
		mScoreTextViews.add(rootView.findViewById(R.id.tvYahtzee));

		mUpperTotalTextView = rootView.findViewById(R.id.tvUpperTotal);
		mBonusTextView = rootView.findViewById(R.id.tvBonus);
		mGameTotalTextView = rootView.findViewById(R.id.tvGameTotal);
	}

	@Override
	public int getGameBonusCondition() {
		return 63;
	}

	@Override
	public int getGameBonus() {
		return 50;
	}

	@Override
	public int[] calcScores(int[] d) {
		Arrays.sort(d);
		int sum = Arrays.stream(d).sum();
		String s = Arrays.stream(d).distinct().mapToObj(String::valueOf).collect(Collectors.joining());
		boolean yahtzee = Arrays.stream(d).allMatch(x -> x == d[0]);
		boolean joker = yahtzee && isSelected(d[0] - 1) && isSelected(13);

		int[] result = new int[mScoreButtons.size()];
		// 1~6
		for (int k : d)
			result[k - 1] += k;
		// 2对
		if ((d[0] == d[1] && d[2] == d[3] && d[0] != d[2])
				|| (d[0] == d[1] && d[3] == d[4] && d[0] != d[3])
				|| (d[1] == d[2] && d[3] == d[4] && d[1] != d[3])
				|| joker)
			result[6] = sum;
		// 3个同点
		if ((d[0] == d[1] && d[1] == d[2])
				|| (d[1] == d[2] && d[2] == d[3])
				|| (d[2] == d[3] && d[3] == d[4])
				|| joker)
			result[7] = sum;
		// 4个同点
		if ((d[0] == d[1] && d[1] == d[2] && d[2] == d[3])
				|| (d[1] == d[2] && d[2] == d[3] && d[3] == d[4])
				|| joker)
			result[8] = sum;
		// 葫芦
		if ((d[0] == d[1] && d[1] == d[2] && d[3] == d[4] && d[0] != d[3])
				|| (d[0] == d[1] && d[2] == d[3] && d[3] == d[4] && d[0] != d[2])
				|| joker)
			result[9] = 25;
		// 小顺
		if (s.length() >= 4
				&& (s.substring(0, 4).equals("1234")
				|| s.substring(0, 4).equals("2345")
				|| s.substring(s.length() - 4).equals("2345")
				|| s.substring(s.length() - 4).equals("3456"))
				|| joker)
			result[10] = 30;
		// 大顺
		if (s.equals("12345") || s.equals("23456") || joker)
			result[11] = 40;
		// 点数全加
		result[12] = sum;
		// Yahtzee
		if (yahtzee)
			result[13] = 50;
		return result;
	}

	@Override
	protected AbstractYahtzeeScore getScore() {
		return new FiveYahtzeeScore(LocalDate.now().toString(), mGameTotal,
				mBonus == 0 ? 0 : 1,
				mScoreTextViews.get(13).getText().toString().equals("0") ? 0 : 1);
	}

}
