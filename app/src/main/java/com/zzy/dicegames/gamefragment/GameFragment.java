package com.zzy.dicegames.gamefragment;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.dice.DiceFragment;

import java.util.Arrays;

/**
 * 游戏Fragment基类
 *
 * @author 赵正阳
 */
public abstract class GameFragment extends Fragment {
	/** 骰子窗口 */
	protected DiceFragment mDiceFragment;

	public GameFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_game, container, false);

		((TextView) rootView.findViewById(R.id.tvTitle)).setText(getTitle());

		if (savedInstanceState == null) {
			mDiceFragment = new DiceFragment();
			Bundle bundle = new Bundle();
			bundle.putInt("diceCount", getDiceCount());
			bundle.putInt("rollTimes", getRollTimes());
			mDiceFragment.setArguments(bundle);
			getChildFragmentManager().beginTransaction()
					.add(R.id.diceFragment, mDiceFragment)
					.commit();
		}

		return rootView;
	}

	/** 返回游戏标题（与系统语言相关，用于界面展示） */
	public abstract String getTitle();

	/** 返回游戏类型代码（与系统语言无关，代码内部使用） */
	public abstract String getGameTypeCode();

	/** 返回游戏使用的骰子个数 */
	public abstract int getDiceCount();

	/** 返回游戏每轮掷骰子次数 */
	public abstract int getRollTimes();

	/** 开始一次新游戏 */
	public abstract void startNewGame();

	/** 重新激活骰子窗口的"Roll"按钮、解锁骰子并掷骰子 */
	public void activateRollButton() {
		mDiceFragment.activateRollButton();
	}

	/** 设置骰子窗口的骰子个数 */
	public void setDiceCount(int diceCount) {
		mDiceFragment.setDiceCount(diceCount);
	}

	/**
	 * 解析命令行并执行相应操作。命令列表：
	 * <table border="1">
	 * <tr><td>{@code ZZyCgDice <diceNumbers>}</td><td>设置骰子点数</td></tr>
	 * <tr><td>{@code SetRollTimes <n>}</td><td>设置掷骰子次数</td></tr>
	 * </table>
	 */
	public void executeCmd(String cmd) {
		String parsedCmd = cmd.trim();
		if (parsedCmd.isEmpty())
			return;
		String[] words = parsedCmd.split("\\s+", 2);
		parsedCmd = words[0];
		String[] args = words.length > 1 ? words[1].split("\\s+") : new String[0];
		if (parsedCmd.equals("ZZyCgDice") && args.length == 1) {
			try {
				mDiceFragment.setDiceNumbers(Arrays.stream(args[0].split(","))
						.mapToInt(Integer::parseInt)
						.toArray());
			}
			catch (IllegalArgumentException ignored) {
			}
		}
		else if (parsedCmd.equals("SetRollTimes") && args.length == 1) {
			try {
				mDiceFragment.setRollTimes(Integer.parseInt(args[0]));
			}
			catch (NumberFormatException ignored) {
			}
		}
	}

}
