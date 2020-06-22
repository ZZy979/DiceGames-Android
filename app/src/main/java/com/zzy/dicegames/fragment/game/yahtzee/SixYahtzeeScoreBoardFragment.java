package com.zzy.dicegames.fragment.game.yahtzee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.database.entity.SixYahtzeeScore;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 6骰Yahtzee计分板Fragment
 *
 * @author 赵正阳
 */
public class SixYahtzeeScoreBoardFragment extends AbstractYahtzeeScoreBoardFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Bundle bundle = new Bundle();
		bundle.putInt(LAYOUT_ID, R.layout.fragment_six_yahtzee_score_board);
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
		mScoreButtons.add(rootView.findViewById(R.id.btn1p));
		mScoreButtons.add(rootView.findViewById(R.id.btn2p));
		mScoreButtons.add(rootView.findViewById(R.id.btn3p));
		mScoreButtons.add(rootView.findViewById(R.id.btn3e));
		mScoreButtons.add(rootView.findViewById(R.id.btn4e));
		mScoreButtons.add(rootView.findViewById(R.id.btn5e));
		mScoreButtons.add(rootView.findViewById(R.id.btnSmallStraight));
		mScoreButtons.add(rootView.findViewById(R.id.btnLargeStraight));
		mScoreButtons.add(rootView.findViewById(R.id.btnFullStraight));
		mScoreButtons.add(rootView.findViewById(R.id.btnHut));
		mScoreButtons.add(rootView.findViewById(R.id.btnHouse));
		mScoreButtons.add(rootView.findViewById(R.id.btnTower));
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
		mScoreTextViews.add(rootView.findViewById(R.id.tv1p));
		mScoreTextViews.add(rootView.findViewById(R.id.tv2p));
		mScoreTextViews.add(rootView.findViewById(R.id.tv3p));
		mScoreTextViews.add(rootView.findViewById(R.id.tv3e));
		mScoreTextViews.add(rootView.findViewById(R.id.tv4e));
		mScoreTextViews.add(rootView.findViewById(R.id.tv5e));
		mScoreTextViews.add(rootView.findViewById(R.id.tvSmallStraight));
		mScoreTextViews.add(rootView.findViewById(R.id.tvLargeStraight));
		mScoreTextViews.add(rootView.findViewById(R.id.tvFullStraight));
		mScoreTextViews.add(rootView.findViewById(R.id.tvHut));
		mScoreTextViews.add(rootView.findViewById(R.id.tvHouse));
		mScoreTextViews.add(rootView.findViewById(R.id.tvTower));
		mScoreTextViews.add(rootView.findViewById(R.id.tvChance));
		mScoreTextViews.add(rootView.findViewById(R.id.tvYahtzee));

		mUpperTotalTextView = rootView.findViewById(R.id.tvUpperTotal);
		mBonusTextView = rootView.findViewById(R.id.tvBonus);
		mGameTotalTextView = rootView.findViewById(R.id.tvGameTotal);
	}

	@Override
	public int getGameBonusCondition() {
		return 84;
	}

	@Override
	public int getGameBonus() {
		return 100;
	}

	@Override
	public int[] calcScores(int[] d) {
		Arrays.sort(d);
		int sum = Arrays.stream(d).sum();
		String s = Arrays.stream(d).distinct().mapToObj(String::valueOf).collect(Collectors.joining());
		boolean yahtzee = Arrays.stream(d).allMatch(x -> x == d[0]);
		boolean joker = yahtzee && isSelected(d[0] - 1) && isSelected(19);
		int[] numCount = new int[7];    // numCount[i] -> 数字i出现的次数
		for (int k : d)
			numCount[k] += 1;

		int[] result = new int[mScoreButtons.size()];
		// 1~6
		for (int k : d)
			result[k - 1] += k;
		// 1对
		for (int i = 6; i >= 1; --i)
			if (numCount[i] >= 2) {
				result[6] = 2 * i;
				break;
			}
		// 2对
		int pairs = 0, pairScore = 0;
		if (joker)
			result[7] = sum;
		else
			for (int i = 6; i >= 1; --i) {
				if (numCount[i] >= 2) {
					++pairs;
					pairScore += 2 * i;
				}
				if (pairs == 2) {
					result[7] = pairScore;
					break;
				}
			}
		// 3对
		pairs = pairScore = 0;
		if (joker)
			result[8] = sum;
		else
			for (int i = 6; i >= 1; --i) {
				if (numCount[i] >= 2) {
					++pairs;
					pairScore += 2 * i;
				}
				if (pairs == 3) {
					result[8] = pairScore;
					break;
				}
			}
		// 3个同点
		if (joker)
			result[9] = sum;
		else
			for (int i = 6; i >= 1; --i)
				if (numCount[i] >= 3) {
					result[9] = 3 * i;
					break;
				}
		// 4个同点
		if (joker)
			result[10] = sum;
		else
			for (int i = 6; i >= 1; --i)
				if (numCount[i] >= 4) {
					result[10] = 4 * i;
					break;
				}
		// 5个同点
		if (joker)
			result[11] = sum;
		else
			for (int i = 6; i >= 1; --i)
				if (numCount[i] >= 5) {
					result[11] = 5 * i;
					break;
				}
		// 小顺
		if (s.length() >= 5 && s.substring(0, 5).equals("12345") || joker)
			result[12] = 15;
		// 大顺
		if (s.length() >= 5 && s.substring(s.length() - 5).equals("23456") || joker)
			result[13] = 20;
		// 全连顺
		if (s.equals("123456") || joker)
			result[14] = 21;
		// 小屋
		if (joker)
			result[15] = sum;
		else {
			int s3 = 0, s2 = 0;
			// 先排除AAABBB的情况
			if (d[0] == d[1] && d[1] == d[2] && d[3] == d[4] && d[4] == d[5] && d[0] != d[3])
				result[15] = 2 * d[0] + 3 * d[3];
			else
				for (int i = 1; i <= 6; ++i)
					if (numCount[i] >= 3)
						s3 = i;
					else if (numCount[i] == 2)
						s2 = i;
			if (s3 != 0 && s2 != 0)
				result[15] = 3 * s3 + 2 * s2;
		}
		// 房子
		if (d[0] == d[1] && d[1] == d[2] && d[3] == d[4] && d[4] == d[5] && d[0] != d[3]
				|| joker)
			result[16] = sum;
		// 高楼
		if ((d[0] == d[1] && d[1] == d[2] && d[2] == d[3] && d[4] == d[5] && d[0] != d[4])
				|| (d[0] == d[1] && d[2] == d[3] && d[3] == d[4] && d[4] == d[5] && d[0] != d[2])
				|| joker)
			result[17] = sum;
		// 点数全加
		result[18] = sum;
		// Yahtzee
		if (yahtzee)
			result[19] = 100;
		return result;
	}

	@Override
	protected AbstractYahtzeeScore getScore() {
		return new SixYahtzeeScore(LocalDate.now().toString(), mGameTotal,
				mBonus == 0 ? 0 : 1,
				mScoreTextViews.get(19).getText().toString().equals("0") ? 0 : 1);
	}

}
