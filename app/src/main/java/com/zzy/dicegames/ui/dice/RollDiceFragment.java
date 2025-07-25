package com.zzy.dicegames.ui.dice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zzy.dicegames.R;

import java.util.function.Consumer;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import static com.zzy.dicegames.ui.dice.RollDiceViewModel.*;

/**
 * 掷骰子窗口，包括一些{@link DiceView 骰子组件}和一个"Roll"按钮
 *
 * @author 赵正阳
 */
public class RollDiceFragment extends Fragment {
    /** 传入参数：骰子个数 */
    private static final String ARG_DICE_COUNT = "diceCount";

    /** 传入参数：最大掷骰子次数 */
    private static final String ARG_MAX_ROLLS = "maxRolls";

    /** 传入参数：创建视图后是否立即掷骰子 */
    private static final String ARG_ROLL_ON_CREATE_VIEW = "rollOnCreateView";

    /** 骰子数组 */
    protected DiceView[] mDiceViews;

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
        int diceCount = args.getInt(ARG_DICE_COUNT);
        int maxRolls = args.getInt(ARG_MAX_ROLLS);
        boolean rollOnCreateView = args.getBoolean(ARG_ROLL_ON_CREATE_VIEW);

        mViewModel = new ViewModelProvider(this, new RollDiceViewModelFactory(diceCount, maxRolls))
                .get(RollDiceViewModel.class);
        initViews(view);

        LifecycleOwner owner = getViewLifecycleOwner();
        mViewModel.getRemainingRolls().observe(owner, this::onRemainingRollsChanged);
        mViewModel.getDiceNumbers().observe(owner, this::onDiceNumbersChanged);
        mViewModel.getDiceLocked().observe(owner, this::onDiceLockedChanged);

        if (savedInstanceState == null && rollOnCreateView) {
            rollDice();
        }
    }

    /** 获取骰子和Roll按钮 */
    protected void initViews(View rootView) {
        int[] diceViewIds = {R.id.dice1, R.id.dice2, R.id.dice3, R.id.dice4, R.id.dice5, R.id.dice6};
        mDiceViews = new DiceView[diceViewIds.length];
        for (int i = 0; i < mDiceViews.length; i++) {
            final int position = i;
            mDiceViews[i] = rootView.findViewById(diceViewIds[i]);
            mDiceViews[i].setOnClickListener(v -> mViewModel.toggleLocked(position));
            mDiceViews[i].setVisibility(i < mViewModel.getDiceCount() ? View.VISIBLE : View.INVISIBLE);
        }

        mRollButton = rootView.findViewById(R.id.btnRoll);
        mRollButton.setOnClickListener(v -> rollDice());
    }

    /** 剩余掷骰子次数更新时的回调 */
    protected void onRemainingRollsChanged(int remaining) {
        if (remaining == UNLIMITED_ROLLS)
            mRollButton.setText(getString(R.string.roll));
        else
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
            mRollListener.accept(numbers);
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

    // TODO 实现FarkleRollDiceFragment后删除
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

    /** 掷骰子并调用监听器 */
    protected void rollDice() {
        // TODO 动画效果
        mViewModel.rollDice();
    }

    /** 激活"Roll"按钮（重置可点击次数）、解锁骰子并掷骰子 */
    public void activate() {
        mViewModel.reset();
        rollDice();
    }
}
