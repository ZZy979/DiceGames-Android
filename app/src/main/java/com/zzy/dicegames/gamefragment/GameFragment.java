package com.zzy.dicegames.gamefragment;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

	/** 是否作弊 */
	protected boolean mCheated = false;

	// ----------保存和恢复状态----------
	/** 用于保存和恢复状态：{@link #mCheated} */
	private static final String CHEATED = "cheated";

	public GameFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_game, container, false);

		((TextView) rootView.findViewById(R.id.tvTitle)).setText(getTitle());

		if (savedInstanceState == null) {
			mDiceFragment = new DiceFragment();
			Bundle bundle = new Bundle();
			bundle.putInt(DiceFragment.DICE_COUNT, getDiceCount());
			bundle.putInt(DiceFragment.ROLL_TIMES, getRollTimes());
			mDiceFragment.setArguments(bundle);
			getChildFragmentManager().beginTransaction()
					.add(R.id.diceFragment, mDiceFragment)
					.commit();
		}
		else
			mDiceFragment = (DiceFragment) getChildFragmentManager().findFragmentById(R.id.diceFragment);

		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(CHEATED, mCheated);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null)
			mCheated = savedInstanceState.getBoolean(CHEATED);
	}

	/** 返回游戏标题 */
	public abstract String getTitle();

	/** 返回游戏使用的骰子个数 */
	public abstract int getDiceCount();

	/** 返回游戏每轮掷骰子次数 */
	public abstract int getRollTimes();

	/** 开始一次新游戏 */
	public abstract void startNewGame();

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
				mCheated = true;
			}
			catch (IllegalArgumentException ignored) {
			}
		}
		else if (parsedCmd.equals("SetRollTimes") && args.length == 1) {
			try {
				mDiceFragment.setRollTimes(Integer.parseInt(args[0]));
				mDiceFragment.activateRollButton();
				mCheated = true;
			}
			catch (NumberFormatException ignored) {
			}
		}
	}

}
