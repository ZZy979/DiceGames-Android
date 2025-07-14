package com.zzy.dicegames.ui.dice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zzy.dicegames.R;

import java.util.Arrays;
import java.util.function.Consumer;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

/**
 * 掷骰子窗口，包括一些{@link DiceView 骰子组件}和一个"Roll"按钮
 *
 * @author 赵正阳
 */
public class RollDiceFragment extends Fragment {
    /** 骰子个数最小值 */
    public static final int MIN_DICE_COUNT = 1;

    /** 骰子个数最大值 */
    public static final int MAX_DICE_COUNT = 6;

    /** 默认最大掷骰子次数 */
    public static final int DEFAULT_MAX_ROLLS = 2;

    // ----------传入参数----------
    /** 骰子个数，默认{@link #MAX_DICE_COUNT} */
    private static final String ARG_DICE_COUNT = "diceCount";

    /** 最大掷骰子次数，默认{@link #DEFAULT_MAX_ROLLS} */
    private static final String ARG_MAX_ROLLS = "maxRolls";

    /** 创建视图后是否立即掷骰子，默认{@code true} */
    private static final String ARG_ROLL_ON_CREATE_VIEW = "rollOnCreateView";

    /** 骰子数组 */
    protected DiceView[] mDiceViews = new DiceView[MAX_DICE_COUNT];

    /** "Roll"按钮 */
    protected Button mRollButton;

    protected RollDiceViewModel mViewModel;

    /** 掷骰子监听器 */
    protected Consumer<int[]> mRollListener;

    public static RollDiceFragment newInstance(int diceCount, int maxRolls, boolean rollOnCreateView) {
        RollDiceFragment fragment = new RollDiceFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DICE_COUNT, diceCount);
        args.putInt(ARG_MAX_ROLLS, maxRolls);
        args.putBoolean(ARG_ROLL_ON_CREATE_VIEW, rollOnCreateView);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_roll_dice, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        int diceCount = args.getInt(ARG_DICE_COUNT, MAX_DICE_COUNT);
        int maxRolls = args.getInt(ARG_MAX_ROLLS, DEFAULT_MAX_ROLLS);
        boolean rollOnCreateView = args.getBoolean(ARG_ROLL_ON_CREATE_VIEW, true);

        mViewModel = new ViewModelProvider(this).get(RollDiceViewModel.class);
        mViewModel.init(diceCount, maxRolls);

        mDiceViews[0] = view.findViewById(R.id.dice1);
        mDiceViews[1] = view.findViewById(R.id.dice2);
        mDiceViews[2] = view.findViewById(R.id.dice3);
        mDiceViews[3] = view.findViewById(R.id.dice4);
        mDiceViews[4] = view.findViewById(R.id.dice5);
        mDiceViews[5] = view.findViewById(R.id.dice6);
        for (int i = 0; i < mDiceViews.length; i++) {
            final int position = i;
            mDiceViews[i].setOnClickListener(v -> mViewModel.toggleLocked(position));
            mDiceViews[i].setVisibility(i < diceCount ? View.VISIBLE : View.INVISIBLE);
        }

        mRollButton = view.findViewById(R.id.btnRoll);
        mRollButton.setOnClickListener(v -> mViewModel.rollDice());

        mViewModel.getRemainingRolls().observe(getViewLifecycleOwner(), this::onRemainingRollsChanged);
        mViewModel.getDiceNumbers().observe(getViewLifecycleOwner(), this::onDiceNumbersChanged);
        mViewModel.getDiceLocked().observe(getViewLifecycleOwner(), this::onDiceLockedChanged);

        if (savedInstanceState == null && rollOnCreateView) {
            mViewModel.rollDice();
        }
    }

    /** 剩余掷骰子次数更新时的回调 */
    protected void onRemainingRollsChanged(int remaining) {
        mRollButton.setText(getString(R.string.rollRemaining, remaining));
        boolean enabled = remaining > 0;
        mRollButton.setEnabled(enabled);
        for (DiceView diceView : mDiceViews)
            diceView.setEnabled(enabled);
    }

    /** 骰子点数更新时的回调 */
    protected void onDiceNumbersChanged(int[] numbers) {
        for (int i = 0; i < numbers.length; i++)
            mDiceViews[i].setNumber(numbers[i]);
        if (mRollListener != null)
            mRollListener.accept(Arrays.copyOf(numbers, numbers.length));
    }

    /** 骰子锁定状态更新时的回调 */
    protected void onDiceLockedChanged(boolean[] locked) {
        for (int i = 0; i < locked.length; i++)
            mDiceViews[i].setLocked(locked[i]);
    }

    // TODO 实现FarkleRollDiceFragment后删除
    public DiceView[] getDice() {
        return mDiceViews;
    }

    public Button getRollButton() {
        return mRollButton;
    }

    // TODO 删除
    public void setDiceCount(int diceCount) {
    }

    // TODO 删除
    public void setLeftRollTimes(int leftRollTimes) {
    }

    /** 设置掷骰子监听器 */
    public void setRollListener(Consumer<int[]> listener) {
        mRollListener = listener;
    }

    // TODO 删除
    public void roll() {
    }

    /** 激活"Roll"按钮（重置可点击次数）、解锁骰子并掷骰子 */
    public void activate() {
        mViewModel.reset();
        mViewModel.rollDice();
    }
}
