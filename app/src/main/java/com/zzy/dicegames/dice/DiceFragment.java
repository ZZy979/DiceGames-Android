package com.zzy.dicegames.dice;


import android.app.Fragment;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zzy.dicegames.R;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

/**
 * 骰子窗口，包括一些{@link Dice 骰子组件}和一个“换”(Roll)按钮<br>
 * <h3>骰子</h3>
 * 通过{@code setDiceCount()}设置骰子个数(1~6)；<br>
 * 通过{@code getDiceNumbers()}和{@code setDiceNumbers()}获取和设置骰子点数<br>
 * <h3>"Roll"按钮</h3>
 * 点击"Roll"按钮掷骰子（锁定的骰子除外），当剩余次数为0时按钮将不可用<br>
 * 通过{@code setRollTimes()}设置可点击次数（默认为2）并重新激活，设置为非正数表示不限次数<br>
 * 通过{@code activateRollButton()}重新激活"Roll"按钮
 * <h3>掷骰子监听器</h3>
 * 指定每次掷骰子之后要执行的动作
 * <h1>注意：</h1>
 * 除了{@code setRollListener()}之外的所有方法均不能在{@code onCreateView()}执行之前调用
 * （例如{@code MainActivity.onCreate()}中），否则会产生空指针异常！<br>
 * 创建{@code DiceFragment}时的初始化参数通过{@link #setArguments(Bundle)}方法传入：
 * <ul>
 *     <li>diceCount：骰子点数，默认6</li>
 *     <li>rollTimes："Roll"按钮可点击次数，默认2</li>
 * </ul>
 *
 * @author 赵正阳
 */
public class DiceFragment extends Fragment {
	/** 骰子个数最小值 */
	public static final int MIN_DICE_NUM = 1;

	/** 骰子个数最大值 */
	public static final int MAX_DICE_NUM = 6;

	/** 骰子个数 */
	private int mDiceCount = MAX_DICE_NUM;

	/** 骰子数组 */
	private Dice[] mDice = new Dice[MAX_DICE_NUM];

	/** "Roll"按钮 */
	private Button mRollButton;

	/** "Roll"按钮可点击次数 */
	private int mRollTimes;

	/** 剩余点击次数 */
	private int mLeftRollTimes;

	/** 掷骰子监听器 */
	private Consumer<int[]> mRollListener;

	public DiceFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_dice, container, false);
		mDice[0] = rootView.findViewById(R.id.dice1);
		mDice[1] = rootView.findViewById(R.id.dice2);
		mDice[2] = rootView.findViewById(R.id.dice3);
		mDice[3] = rootView.findViewById(R.id.dice4);
		mDice[4] = rootView.findViewById(R.id.dice5);
		mDice[5] = rootView.findViewById(R.id.dice6);

		mRollButton = rootView.findViewById(R.id.btnRoll);
		mRollButton.setOnClickListener(v -> {
			if (mRollTimes > 0) {
				if (mLeftRollTimes > 0) {
					setLeftRollTimes(mLeftRollTimes - 1);
					if (mLeftRollTimes == 0)
						for (Dice dice : mDice)
							dice.setEnabled(false);
					roll();
				}
			}
			else
				roll();
		});

		Bundle bundle = getArguments();
		if (bundle != null) {
			setDiceCount(bundle.getInt("diceCount", MAX_DICE_NUM));
			setRollTimes(bundle.getInt("rollTimes", 2));
		}
		return rootView;
	}

	/** 返回骰子个数 */
	public int getDiceCount() {
		return mDiceCount;
	}

	/**
	 * 设置骰子个数
	 *
	 * @throws IllegalArgumentException 如果设置的骰子个数不在1~6之间
	 */
	public void setDiceCount(int diceCount) {
		if (diceCount < MIN_DICE_NUM || diceCount > MAX_DICE_NUM)
			throw new IllegalArgumentException(String.format("骰子个数必须在%d~%d之间", MIN_DICE_NUM, MAX_DICE_NUM));
		mDiceCount = diceCount;
		for (int i = 0; i < mDice.length; ++i)
			mDice[i].setVisibility(i < diceCount ? View.VISIBLE : View.INVISIBLE);
	}

	/**
	 * 设置"Roll"按钮的可点击次数，小于等于0表示不限次数<br>
	 * 设置之后将重新激活"Roll"按钮并掷骰子
	 */
	public void setRollTimes(int rollTimes) {
		mRollTimes = rollTimes;
		if (rollTimes <= 0) {
			mRollButton.setText(getContext().getString(R.string.roll));
			mRollButton.setEnabled(true);
		}
		activateRollButton();
	}

	/**
	 * 设置剩余点击次数，并更新标签和可用状态
	 *
	 * @throws IllegalArgumentException 如果设置的值不在[0, mRollTimes]之间
	 */
	public void setLeftRollTimes(int leftRollTimes) {
		if (leftRollTimes < 0 || leftRollTimes > mRollTimes)
			throw new IllegalArgumentException("剩余点击次数必须在0~" + mRollTimes + "之间");
		mLeftRollTimes = leftRollTimes;
		mRollButton.setText(String.format("%s(%d)", getContext().getString(R.string.roll), leftRollTimes));
		mRollButton.setEnabled(mLeftRollTimes != 0);
	}

	/** 设置掷骰子监听器 */
	public void setRollListener(Consumer<int[]> listener) {
		mRollListener = listener;
	}

	/** 掷骰子<strong>一次</strong>，锁定的骰子除外 */
	private void rollOnce() {
		for (Dice dice : mDice) {
			dice.setNumber(new Random().nextInt(6) + 1);
		}
	}

	/**
	 * 掷骰子并调用掷骰子监听器<br>
	 * 多次调用{@code rollOnce()}从而制造动画效果，但仅对最后一次的结果调用监听器
	 */
	private void roll() {
		// 在单独的线程中执行，否则每次掷骰子之后无法立即重新绘制
		new Thread(() -> {
			// 若没有这一句会报错：Can't create handler inside thread xx that has not called Looper.prepare()
			Looper.prepare();
			for (int i = 0; i < 10; ++i) {
				rollOnce();
				try {
					Thread.sleep(30);
				}
				catch (InterruptedException ignored) {
				}
			}
			if (mRollListener != null)
				mRollListener.accept(getDiceNumbers());
		}).start();
	}

	/** 重新激活"Roll"按钮、解锁骰子并掷骰子 */
	public void activateRollButton() {
		if (mRollTimes > 0)
			setLeftRollTimes(mRollTimes);
		for (Dice dice : mDice) {
			dice.setEnabled(true);
			dice.setLocked(false);
		}
		roll();
	}

	/** 返回骰子点数的数组 */
	public int[] getDiceNumbers() {
		return Arrays.stream(mDice)
				.filter(dice -> dice.getVisibility() == View.VISIBLE)
				.mapToInt(Dice::getNumber)
				.toArray();
	}

	/**
	 * 设置骰子点数<small>（作弊用）</small>
	 *
	 * @param newNumbers 新点数的数组，受影响的骰子个数是{@code mDiceCount}和{@code newNumbers.length}的最小值
	 * @throws IllegalArgumentException 如果有起作用的元素不在1~6之间
	 */
	public void setDiceNumbers(int[] newNumbers) {
		for (int i = 0; i < mDiceCount && i < newNumbers.length; ++i)
			mDice[i].forceSetNumber(newNumbers[i]);
		if (mRollListener != null)
			mRollListener.accept(getDiceNumbers());
	}

}
