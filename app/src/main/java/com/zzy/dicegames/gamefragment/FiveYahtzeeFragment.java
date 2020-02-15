package com.zzy.dicegames.gamefragment;

import com.zzy.dicegames.R;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 5骰Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public class FiveYahtzeeFragment extends AbstractYahtzeeFragment {

	public FiveYahtzeeFragment() {}

	@Override
	public AbstractYahtzeeScoreBoardFragment createScoreBoardFragment() {
		return new FiveYahtzeeScoreBoardFragment();
	}

	@Override
	public String getTitle() {
		return getContext().getString(R.string.fiveYahtzee);
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
	public int getCategoryCount() {
		return 14;
	}

	@Override
	public int getBonusCondition() {
		return 63;
	}

	@Override
	public int getBonus() {
		return 50;
	}

	@Override
	public int calcScore(int[] d, int index) {
		int sum = Arrays.stream(d).sum();
		String s = Arrays.stream(d).distinct().mapToObj(String::valueOf).collect(Collectors.joining());
		boolean yahtzee = Arrays.stream(d).allMatch(x -> x == d[0]);
		boolean joker = yahtzee && scoreBoard.isSelected(d[0] - 1) && scoreBoard.isSelected(13);

		switch (index) {
		case 0:	case 1:	case 2:	case 3:	case 4:	case 5:     // 1~6
			return Arrays.stream(d).filter(x -> x == index + 1).sum();
		case 6:     // 2对
			if ((d[0] == d[1] && d[2] == d[3] && d[0] != d[2])
					|| (d[0] == d[1] && d[3] == d[4] && d[0] != d[3])
					|| (d[1] == d[2] && d[3] == d[4] && d[1] != d[3])
					|| joker)
				return sum;
			else
				return 0;
		case 7:     // 3个同点
			if ((d[0] == d[1] && d[1] == d[2])
					|| (d[1] == d[2] && d[2] == d[3])
					|| (d[2] == d[3] && d[3] == d[4])
					|| joker)
				return sum;
			else
				return 0;
		case 8:     // 4个同点
			if ((d[0] == d[1] && d[1] == d[2] && d[2] == d[3])
					|| (d[1] == d[2] && d[2] == d[3] && d[3] == d[4])
					|| joker)
				return sum;
			else
				return 0;
		case 9:     // 葫芦
			if ((d[0] == d[1] && d[1] == d[2] && d[3] == d[4] && d[0] != d[3])
					|| (d[0] == d[1] && d[2] == d[3] && d[3] == d[4] && d[0] != d[2])
					|| joker)
				return 25;
			else
				return 0;
		case 10:    // 小顺
			if (s.length() >= 4
					&& (s.substring(0, 4).equals("1234")
					|| s.substring(0, 4).equals("2345")
					|| s.substring(s.length() - 4).equals("2345")
					|| s.substring(s.length() - 4).equals("3456"))
					|| joker)
				return 30;
			else
				return 0;
		case 11:    // 大顺
			if (s.equals("12345") || s.equals("23456") || joker)
				return 40;
			else
				return 0;
		case 12:    // 点数全加
			return sum;
		case 13:    // Yahtzee
			return yahtzee ? 50 : 0;
		default:
			return 0;
		}
	}

}
