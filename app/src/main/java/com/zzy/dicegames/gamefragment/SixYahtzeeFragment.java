package com.zzy.dicegames.gamefragment;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.ScoreDatabase;
import com.zzy.dicegames.database.dao.SixYahtzeeScoreDao;
import com.zzy.dicegames.database.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.database.entity.SixYahtzeeScore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 6骰Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public class SixYahtzeeFragment extends AbstractYahtzeeFragment {

	public SixYahtzeeFragment() {}

	@Override
	public AbstractYahtzeeScoreBoardFragment createScoreBoardFragment() {
		return new SixYahtzeeScoreBoardFragment();
	}

	@Override
	public String getTitle() {
		return getContext().getString(R.string.sixYahtzee);
	}

	@Override
	public int getDiceCount() {
		return 6;
	}

	@Override
	public int getRollTimes() {
		return 2;
	}

	@Override
	public int getCategoryCount() {
		return 20;
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
	public int calcScore(int[] d, int index) {
		int sum = Arrays.stream(d).sum();
		String s = Arrays.stream(d).distinct().mapToObj(String::valueOf).collect(Collectors.joining());
		int pairs = 0, pairScore = 0;   // 用于2对、3对
		int s3 = 0, s2 = 0;             // 用于小屋
		boolean yahtzee = Arrays.stream(d).allMatch(x -> x == d[0]);
		boolean joker = yahtzee && mScoreBoardFragment.isSelected(d[0] - 1) && mScoreBoardFragment.isSelected(19);
		Map<Integer, Integer> numCount = new HashMap<>();
		for (int i = 1; i <= 6; ++i)
			numCount.put(i, 0);
		for (int k : d)
			numCount.put(k, numCount.get(k) + 1);

		if (joker) {
			if (index >= 6 && index <= 11 || index >= 15 && index <= 18)
				return sum;
			else if (index == 12)
				return 15;
			else if (index == 13)
				return 20;
			else if (index == 14)
				return 21;
		}

		switch (index) {
		case 0: case 1: case 2: case 3: case 4: case 5:     // 1~6
			return numCount.get(index + 1) * (index + 1);
		case 6:     // 1对
			for (int i = 6; i >= 1; --i)
				if (numCount.get(i) >= 2)
					return 2 * i;
			return 0;
		case 7:     // 2对
			for (int i = 6; i >= 1; --i) {
				if (numCount.get(i) >= 2) {
					++pairs;
					pairScore += 2 * i;
				}
				if (pairs == 2)
					return pairScore;
			}
			return 0;
		case 8:     // 3对
			for (int i = 6; i >= 1; --i) {
				if (numCount.get(i) >= 2) {
					++pairs;
					pairScore += 2 * i;
				}
				if (pairs == 3)
					return pairScore;
			}
			return 0;
		case 9:     // 3个同点
			for (int i = 6; i >= 1; --i)
				if (numCount.get(i) >= 3)
					return 3 * i;
			return 0;
		case 10:    // 4个同点
			for (int i = 6; i >= 1; --i)
				if (numCount.get(i) >= 4)
					return 4 * i;
			return 0;
		case 11:    // 5个同点
			for (int i = 6; i >= 1; --i)
				if (numCount.get(i) >= 5)
					return 5 * i;
			return 0;
		case 12:    // 小顺
			return s.length() >= 5 && s.substring(0, 5).equals("12345") ? 15 : 0;
		case 13:    // 大顺
			return s.length() >= 5 && s.substring(s.length() - 5).equals("23456") ? 20 : 0;
		case 14:    // 全连顺
			return s.equals("123456") ? 21 : 0;
		case 15:    // 小屋
			// 先排除AAABBB的情况
			if (d[0] == d[1] && d[1] == d[2] && d[3] == d[4] && d[4] == d[5] && d[0] != d[3])
				return 2 * d[0] + 3 * d[3];
			else
				for (int i = 1; i <= 6; ++i)
					if (numCount.get(i) >= 3)
						s3 = i;
					else if (numCount.get(i) == 2)
						s2 = i;
			if (s3 != 0 && s2 != 0)
				return 3 * s3 + 2 * s2;
			else
				return 0;
		case 16:    // 房子
			if (d[0] == d[1] && d[1] == d[2] && d[3] == d[4] && d[4] == d[5] && d[0] != d[3])
				return sum;
			else
				return 0;
		case 17:    // 高楼
			if ((d[0] == d[1] && d[1] == d[2] && d[2] == d[3] && d[4] == d[5] && d[0] != d[4])
					|| (d[0] == d[1] && d[2] == d[3] && d[3] == d[4] && d[4] == d[5] && d[0] != d[2]))
				return sum;
			else
				return 0;
		case 18:    // 点数全加
			return sum;
		case 19:    // Yahtzee
			return yahtzee ? 100 : 0;
		default:
			return 0;
		}
	}

	@Override
	protected int saveScore(AbstractYahtzeeScore score) {
		SixYahtzeeScoreDao sixYahtzeeScoreDao = ScoreDatabase.getInstance(getContext()).sixYahtzeeScoreDao();
		sixYahtzeeScoreDao.insert((SixYahtzeeScore) score);
		return sixYahtzeeScoreDao.findTop10Score().indexOf(score.getScore()) + 1;
	}

}
