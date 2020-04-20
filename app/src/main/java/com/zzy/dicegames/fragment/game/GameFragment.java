package com.zzy.dicegames.fragment.game;


import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.fragment.dice.DiceFragment;

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
		else {
			mDiceFragment = (DiceFragment) getChildFragmentManager().findFragmentById(R.id.diceFragment);
			mCheated = savedInstanceState.getBoolean(CHEATED);
		}

		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(CHEATED, mCheated);
		super.onSaveInstanceState(outState);
	}

	/** 返回游戏标题 */
	public abstract String getTitle();

	/** 返回游戏使用的骰子个数 */
	public abstract int getDiceCount();

	/** 返回游戏每轮掷骰子次数 */
	public abstract int getRollTimes();

	/** 开始一次新游戏 */
	public void startNewGame() {
		mDiceFragment.setDiceCount(getDiceCount());
		mDiceFragment.setRollTimes(getRollTimes());
		mCheated = false;
	}

	/**
	 * 根据名次返回对应的文字“第rank名”
	 *
	 * @throws IllegalArgumentException 如果rank<=0
	 */
	public String getRankText(int rank) {
		if (rank <= 0)
			throw new IllegalArgumentException("名次必须大于0");
		String lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
		String arg;
		if (lang.equals("en")) {
			if (rank % 10 == 1 && rank != 11)
				arg = String.format("%dst", rank);
			else if (rank % 10 == 2 && rank != 12)
				arg = String.format("%dnd", rank);
			else if (rank % 10 == 3 && rank != 13)
				arg = String.format("%drd", rank);
			else
				arg = String.format("%dth", rank);
		}
		else
			arg = String.valueOf(rank);
		return String.format(getString(R.string.nthPlace), arg);
	}

	/**
	 * 弹出窗口，显示得分
	 *
	 * @param score 得分
	 * @param rank 排名
	 */
	public void showScore(int score, int rank) {
		String honor;
		if (rank == 1)
			honor = getString(R.string.newHighScore);
		else if (rank >= 2 && rank <= 10)
			honor = getRankText(rank);
		else
			honor = getString(R.string.score);
		new AlertDialog.Builder(getContext())
				.setTitle(getContext().getString(R.string.gameOver))
				.setMessage(String.format("%s: %d", honor, score))
				.setPositiveButton(R.string.ok, (dialog, which) -> startNewGame())
				.show();
	}

	/**
	 * 执行命令行。命令列表：
	 * <table border="1">
	 * <tr><td>{@code ZZyCgDice [-t|--trust-me] <nums>}</td><td>设置骰子点数，若指定了参数-t则不算作弊</td></tr>
	 * <tr><td>{@code SetRollTimes [-t|--trust-me] <n>}</td><td>设置掷骰子次数，若指定了参数-t则不算作弊</td></tr>
	 * </table>
	 */
	public void executeCmd(String cmd, String[] args) {
		if (cmd.equals("ZZyCgDice") && (args.length == 1 || args.length == 2)) {
			try {
				mDiceFragment.setDiceNumbers(Arrays.stream(args[args.length - 1].split(","))
						.mapToInt(Integer::parseInt)
						.toArray());
				mCheated = !(args.length == 2 && (args[0].equals("-t") || args[0].equals("--trust-me")));
			}
			catch (IllegalArgumentException ignored) {
			}
		}
		else if (cmd.equals("SetRollTimes") && (args.length == 1 || args.length == 2)) {
			try {
				mDiceFragment.setRollTimes(Integer.parseInt(args[args.length - 1]));
				mDiceFragment.activateRollButton();
				mCheated = !(args.length == 2 && (args[0].equals("-t") || args[0].equals("--trust-me")));
			}
			catch (NumberFormatException ignored) {
			}
		}
	}

}
