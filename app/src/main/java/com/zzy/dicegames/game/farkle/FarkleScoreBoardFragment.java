package com.zzy.dicegames.game.farkle;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.fragment.dice.DiceFragment;
import com.zzy.dicegames.widget.Dice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Farkle计分板Fragment，嵌套于一个{@link FarkleFragment}
 *
 * @author 赵正阳
 */
public class FarkleScoreBoardFragment extends Fragment {
	// ----------游戏参数----------
	/** 玩家数 */
	private static final int N_PLAYER = 2;

	/** 3个同点基本得分 */
	private static final int[] BASIC_SCORES = new int[] {0, 1000, 200, 300, 400, 500, 600};

	// ----------游戏状态数据----------
	/** 玩家 */
	private Player[] mPlayer = new Player[N_PLAYER];

	/** 当前玩家 */
	private int mCurrentPlayer = 0;

	/** 玩家得分 */
	private int[] mScore = new int[N_PLAYER];

	/** 玩家得分标签 */
	private TextView[] mScoreTextView = new TextView[N_PLAYER];

	/** “保存得分”按钮 */
	private Button mBankButton;

	/** “新游戏”按钮 */
	private Button mNewGameButton;

	/** 已积累的本轮得分 */
	private int mCurrentTurnScore;

	/** 预览本轮得分标签（已积累+本次掷骰子后选择的） */
	private TextView mCurrentTurnScoreTextView;

	/** 包含日志输出窗口的{@code ScrollView} */
	private ScrollView mLogScrollView;

	/** 日志输出窗口 */
	private TextView mLogText;

	/*
		该类中对"Roll"按钮和骰子的操作较多，
		对"Roll"按钮的操作包括改变点击监听器、改变可用状态及点击按钮；
		对骰子的操作包括改变点击监听器、查询和设置锁定状态及可用状态
		如果通过Lambda表达式间接操作需要在DiceFragment类中添加大量的getter/setter方法，
		并在该类中设置相同数量的监听器，非常麻烦，因此获取DiceFragment的引用，直接对其操作
	 */
	/** 骰子窗口 */
	private DiceFragment mDiceFragment;

	// ----------保存和恢复状态----------
	/** 用于保存和恢复状态：{@link #mCurrentPlayer} */
	private static final String CURRENT_PLAYER = "currentPlayer";

	/** 用于保存和恢复状态：{@link #mScore} */
	private static final String SCORE = "score";

	/** 用于保存和恢复状态：{@link #mCurrentTurnScore} */
	private static final String CURRENT_TURN_SCORE = "currentTurnScore";

	/** 用于保存和恢复状态：{@link #mBankButton}可用状态 */
	private static final String BANK_BUTTON_ENABLED = "bankButtonEnabled";

	/** 用于保存和恢复状态：{@link #mNewGameButton}可见状态 */
	private static final String NEW_GAME_BUTTON_VISIBILITY = "newGameButtonVisibility";

	public FarkleScoreBoardFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_farkle_score_board, container, false);

		mScoreTextView[0] = rootView.findViewById(R.id.tv1pScore);
		mScoreTextView[1] = rootView.findViewById(R.id.tvCPUScore);
		mCurrentTurnScoreTextView = rootView.findViewById(R.id.tvCurrentTurnScore);

		mBankButton = rootView.findViewById(R.id.btnBank);
		mBankButton.setOnClickListener(v -> getActivity().runOnUiThread(this::bank));

		mNewGameButton = rootView.findViewById(R.id.btnNewGame);
		mNewGameButton.setOnClickListener(v -> ((FarkleFragment) getParentFragment()).startNewGame());
		mNewGameButton.setVisibility(View.GONE);

		mPlayer[0] = new HumanPlayer();
		mPlayer[1] = createBotPlayer();

		mLogScrollView = rootView.findViewById(R.id.svLog);
		mLogText = rootView.findViewById(R.id.tvLog);

		if (savedInstanceState == null) {
			writeLog(getString(R.string.logYourTurn));
			writeLog(String.format(getString(R.string.logStartingScore), mScore[0]));
		}
		else
			restoreState(savedInstanceState);

		return rootView;
	}

	/*
		由于该Fragment在DiceFragment之前创建
		因此该Fragment的onActivityCreated()和onViewStateRestored()被调用时
		DiceFragment.onCreateView()还未被调用，其骰子数组和"Roll"按钮均为空指针
		而该方法被调用时DiceFragment.onCreateView()已被调用
	 */
	@Override
	public void onStart() {
		super.onStart();
		for (Dice d : mDiceFragment.getDice())
			d.setOnClickListener(this::onDiceClicked);
		mDiceFragment.getRollButton().setOnClickListener(v -> getActivity().runOnUiThread(this::onRollButtonClicked));
		changeButtonVisibility();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(CURRENT_PLAYER, mCurrentPlayer);
		outState.putIntArray(SCORE, mScore);
		outState.putInt(CURRENT_TURN_SCORE, mCurrentTurnScore);
		outState.putBoolean(BANK_BUTTON_ENABLED, mBankButton.isEnabled());
		outState.putInt(NEW_GAME_BUTTON_VISIBILITY, mNewGameButton.getVisibility());
		super.onSaveInstanceState(outState);
	}

	private void restoreState(Bundle savedInstanceState) {
		mCurrentPlayer = savedInstanceState.getInt(CURRENT_PLAYER);
		mScore = savedInstanceState.getIntArray(SCORE);
		mCurrentTurnScore = savedInstanceState.getInt(CURRENT_TURN_SCORE);
		mBankButton.setEnabled(savedInstanceState.getBoolean(BANK_BUTTON_ENABLED));
		mNewGameButton.setVisibility(savedInstanceState.getInt(NEW_GAME_BUTTON_VISIBILITY));
	}

	/** 返回一个新的电脑玩家，并设置相关动作 */
	private BotPlayer createBotPlayer() {
		BotPlayer botPlayer = new BotPlayer();
		botPlayer.setDiceFragment(mDiceFragment);
		botPlayer.setCurrentTurnScoreSupplier(() ->
				Integer.parseInt(mCurrentTurnScoreTextView.getText().toString()));
		botPlayer.setBankScoreAction(mBankButton::callOnClick);
		return botPlayer;
	}

	public void setDiceFragment(DiceFragment diceFragment) {
		mDiceFragment = diceFragment;
	}

	private void writeLog(String msg) {
		mLogText.setText(String.format("%s%s\n", mLogText.getText(), msg));
		mLogScrollView.post(() -> mLogScrollView.fullScroll(ScrollView.FOCUS_DOWN));
	}

	/** 骰子的点击事件监听器 */
	private void onDiceClicked(View v) {
		Dice dice = (Dice) v;
		if (dice.isEnabled())
			dice.setLocked(!dice.isLocked());
		updateCurrentTurnScore();
	}

	/** "Roll"按钮的点击事件监听器 */
	private void onRollButtonClicked() {
		mDiceFragment.setLeftRollTimes(0);
		if (Arrays.stream(mDiceFragment.getDice()).anyMatch(d -> !d.isLocked() && !d.isEnabled())) {
			// 上次掷骰子后Hot Dice
			for (Dice dice : mDiceFragment.getDice()) {
				dice.setEnabled(true);
				dice.setLocked(false);
			}
			mBankButton.setEnabled(true);
		}
		else {
			writeLog(String.format(getString(R.string.logDiceKept),
					Arrays.stream(mDiceFragment.getDice())
							.filter(this::isLockedAfterLastRoll)
							.map(d -> String.valueOf(d.getNumber()))
							.collect(Collectors.joining(","))
			));
			// 禁用上次掷骰子后锁定的骰子
			Arrays.stream(mDiceFragment.getDice())
					.filter(this::isLockedAfterLastRoll)
					.forEach(d -> d.setEnabled(false));
		}
		mDiceFragment.roll();
	}

	/**
	 * 根据骰子点数计算最大可能的得分及得分的骰子下标
	 *
	 * @param d 骰子点数，0表示不参与计算
	 * @see Result
	 */
	public static Result calcScore(int[] d) {
		Map<Integer, List<Integer>> indices = IntStream.range(0, d.length).boxed()
				.collect(Collectors.groupingBy(i -> d[i]));
		Arrays.sort(d);
		int[] numCount = new int[7];    // numCount[i] -> 数字i出现的次数
		for (int k : d)
			numCount[k] += 1;

		// 连顺
		if (d.length == 6 && IntStream.range(0, 6).allMatch(i -> d[i] == i + 1))
			return new Result(1500, Arrays.asList(0, 1, 2, 3, 4, 5));
		// 3对
		if (IntStream.rangeClosed(1, 6).filter(i -> numCount[i] >= 2).count() == 3)
			return new Result(750, Arrays.asList(0, 1, 2, 3, 4, 5));

		int score = 0;
		List<Integer> scoringDiceIndices = new ArrayList<>();
		// 3~6个同点
		for (int i = 1; i <= 6; ++i)
			for (int n = 6; n >= 3; --n)
				if (numCount[i] == n) {
					score += (n - 2) * BASIC_SCORES[i];
					scoringDiceIndices.addAll(Objects.requireNonNull(
							indices.getOrDefault(i, Collections.emptyList())));
					break;
				}
		// <=2个的1点和5点
		if (numCount[1] == 1 || numCount[1] == 2) {
			score += numCount[1] * 100;
			scoringDiceIndices.addAll(Objects.requireNonNull(indices.get(1)));
		}
		if (numCount[5] == 1 || numCount[5] == 2) {
			score += numCount[5] * 50;
			scoringDiceIndices.addAll(Objects.requireNonNull(indices.get(5)));
		}
		return new Result(score, scoringDiceIndices);
	}

	/** 上次掷骰子后锁定的骰子<=>已锁定且未禁用 */
	private boolean isLockedAfterLastRoll(Dice dice) {
		return dice.isLocked() && dice.isEnabled();
	}

	/** 设置所有骰子的可用状态 */
	private void disableAllDice() {
		Arrays.stream(mDiceFragment.getDice()).forEach(d -> d.setEnabled(false));
	}

	/** 掷骰子后的回调函数 */
	public void onDiceRolled(int[] diceNumbers) {
		for (int i = 0; i < mDiceFragment.getDice().length; ++i)
			if (mDiceFragment.getDice()[i].isLocked())
				diceNumbers[i] = 0;
		writeLog(String.format(getString(R.string.logDiceRolled), Arrays.stream(diceNumbers)
				.filter(x -> x != 0)
				.mapToObj(String::valueOf)
				.collect(Collectors.joining(","))));
		Result result = calcScore(diceNumbers);
		if (result.getScoringDiceIndices().size() == 0)
			onFarkle();
		else {
			// 将上次掷骰子的得分加到本轮得分
			mCurrentTurnScore = Integer.parseInt(mCurrentTurnScoreTextView.getText().toString());
			if (mScore[mCurrentPlayer] + mCurrentTurnScore + result.getScore() >= 10000)
				onWin(mScore[mCurrentPlayer] + mCurrentTurnScore + result.getScore());
			else if (result.getScoringDiceIndices().size()
					+ Arrays.stream(mDiceFragment.getDice()).filter(Dice::isLocked).count() == 6)
				onHotDice(result.getScore());
			else
				mPlayer[mCurrentPlayer].onDiceRolled(result);
		}
	}

	private void onWin(int finalScore) {
		mScore[mCurrentPlayer] = finalScore;
		mScoreTextView[mCurrentPlayer].setText(String.valueOf(finalScore));
		writeLog(getString(mCurrentPlayer == 0 ? R.string.logYouWin : R.string.logCPUWins));
		disableAllDice();
		mBankButton.setEnabled(false);
		mDiceFragment.setLeftRollTimes(0);
		mNewGameButton.setVisibility(View.VISIBLE);
	}

	private void onHotDice(int score) {
		mCurrentTurnScore += score;
		mCurrentTurnScoreTextView.setText(String.valueOf(mCurrentTurnScore));
		writeLog(String.format(getString(R.string.logHotDice), score));
		disableAllDice();
		mBankButton.setEnabled(false);
		mDiceFragment.setLeftRollTimes(1);
		mPlayer[mCurrentPlayer].onHotDice();
	}

	private void onFarkle() {
		writeLog(getString(R.string.logFarkled));
		disableAllDice();
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException ignored) {
		}
		nextPlayer();
	}

	/** 锁定/解锁骰子时更新本轮得分 */
	private void updateCurrentTurnScore() {
		Result result = calcScore(Arrays.stream(mDiceFragment.getDice())
				.filter(this::isLockedAfterLastRoll)
				.mapToInt(Dice::getNumber)
				.toArray());
		mCurrentTurnScoreTextView.setText(String.valueOf(mCurrentTurnScore + result.getScore()));
		boolean allLocked = Arrays.stream(mDiceFragment.getDice()).allMatch(Dice::isLocked);
		mDiceFragment.setLeftRollTimes(allLocked || result.getScoringDiceIndices().isEmpty() ? 0 : 1);
	}

	/**
	 * 保存本轮得分<br>
	 * 如果上次掷骰子后未锁定任何骰子则默认选择所有得分的骰子
	 */
	private void bank() {
		boolean anyLocked = Arrays.stream(mDiceFragment.getDice()).anyMatch(this::isLockedAfterLastRoll);
		int currentTurnScore;
		if (anyLocked) {
			writeLog(String.format(getString(R.string.logDiceKept),
					Arrays.stream(mDiceFragment.getDice())
							.filter(this::isLockedAfterLastRoll)
							.map(d -> String.valueOf(d.getNumber()))
							.collect(Collectors.joining(","))
			));
			currentTurnScore = Integer.parseInt(mCurrentTurnScoreTextView.getText().toString());
		}
		else {
			Result result = calcScore(Arrays.stream(mDiceFragment.getDice())
					.mapToInt(d -> d.isLocked() ? 0 : d.getNumber())
					.toArray());
			writeLog(String.format(getString(R.string.logDiceKept),
					result.getScoringDiceIndices().stream().sorted()
							.map(i -> String.valueOf(mDiceFragment.getDice()[i].getNumber()))
							.collect(Collectors.joining(","))
			));
			currentTurnScore = mCurrentTurnScore + result.getScore();   // 最大可能的值
		}
		mScore[mCurrentPlayer] += currentTurnScore;
		mScoreTextView[mCurrentPlayer].setText(String.valueOf(mScore[mCurrentPlayer]));
		// 此处mScore[mCurrentPlayer]不可能>=10000，否则在onDiceRolled中已调用onWin
		writeLog(String.format(getString(R.string.logFinishTurn), currentTurnScore));
		nextPlayer();
	}

	/** 如果当前玩家是电脑则隐藏"Bank"和"Roll"按钮，否则显示 */
	private void changeButtonVisibility() {
		if (mPlayer[mCurrentPlayer] instanceof BotPlayer) {
			mBankButton.setVisibility(View.INVISIBLE);
			mDiceFragment.getRollButton().setVisibility(View.INVISIBLE);
		}
		else {
			mBankButton.setVisibility(View.VISIBLE);
			mDiceFragment.getRollButton().setVisibility(View.VISIBLE);
		}
	}

	private void nextPlayer() {
		mCurrentPlayer = (mCurrentPlayer + 1) % N_PLAYER;
		mCurrentTurnScore = 0;
		mCurrentTurnScoreTextView.setText("0");
		changeButtonVisibility();
		writeLog("----------------------------------------");
		writeLog(getString(mCurrentPlayer == 0 ? R.string.logYourTurn : R.string.logCPUsTurn));
		writeLog(String.format(getString(R.string.logStartingScore), mScore[mCurrentPlayer]));
		mDiceFragment.activate();
		mDiceFragment.setLeftRollTimes(0);
	}

}
