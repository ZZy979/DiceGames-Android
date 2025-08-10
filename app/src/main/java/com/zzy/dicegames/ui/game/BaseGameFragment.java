package com.zzy.dicegames.ui.game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.ui.dice.DiceView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import static com.zzy.dicegames.ui.game.BaseGameViewModel.*;

/**
 * 游戏Fragment基类
 *
 * @param <V> ViewModel类
 * @author 赵正阳
 */
public abstract class BaseGameFragment<V extends BaseGameViewModel> extends Fragment {
    /** 标题标签*/
    protected TextView mTitleTextView;

    /** 骰子 */
    protected DiceView[] mDiceViews;

    /** Roll按钮 */
    protected Button mRollButton;

    protected V mViewModel;

    /** 用于异步执行UI操作（如掷骰子动画）的线程池 */
    protected ExecutorService executor = Executors.newSingleThreadExecutor();

    protected Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = createViewModel();
        initViews(view);
        setupObservers(getViewLifecycleOwner());
    }

    /** 初始化视图 */
    protected void initViews(View view) {
        mTitleTextView = view.findViewById(R.id.tvTitle);

        int[] diceViewIds = {R.id.dice1, R.id.dice2, R.id.dice3, R.id.dice4, R.id.dice5, R.id.dice6};
        mDiceViews = new DiceView[diceViewIds.length];
        for (int i = 0; i < mDiceViews.length; i++) {
            final int position = i;
            mDiceViews[i] = view.findViewById(diceViewIds[i]);
            mDiceViews[i].setOnClickListener(v -> clickDice(position));
            mDiceViews[i].setVisibility(i < mViewModel.getNumDice() ? View.VISIBLE : View.INVISIBLE);
        }

        mRollButton = view.findViewById(R.id.btnRoll);
        mRollButton.setOnClickListener(v -> rollDice());
    }

    /** 创建游戏ViewModel */
    protected abstract V createViewModel();

    /** 设置ViewModel观察者 */
    protected void setupObservers(LifecycleOwner owner) {
        mViewModel.getRemainingRolls().observe(owner, this::onRemainingRollsChanged);
        mViewModel.getDiceNumbers().observe(owner, this::onDiceNumbersChanged);
        mViewModel.getDiceLocked().observe(owner, this::onDiceLockedChanged);
        mViewModel.getDiceEnabled().observe(owner, this::onDiceEnabledChanged);
        mViewModel.getRollButtonEnabled().observe(owner, this::onRollButtonEnabledChanged);
    }

    /** 剩余掷骰子次数更新时的回调 */
    protected void onRemainingRollsChanged(int remaining) {
        if (remaining == UNLIMITED_ROLLS)
            mRollButton.setText(getString(R.string.roll));
        else
            mRollButton.setText(getString(R.string.rollRemaining, remaining));
    }

    /** 骰子点数更新时的回调 */
    protected void onDiceNumbersChanged(int[] numbers) {
        for (int i = 0; i < numbers.length; i++)
            mDiceViews[i].setNumber(numbers[i]);
    }

    /** 骰子锁定状态更新时的回调 */
    protected void onDiceLockedChanged(boolean[] locked) {
        for (int i = 0; i < locked.length; i++)
            mDiceViews[i].setLocked(locked[i]);
    }

    /** 骰子激活状态更新时的回调 */
    protected void onDiceEnabledChanged(boolean[] enabled) {
        for (int i = 0; i < enabled.length; i++)
            mDiceViews[i].setEnabled(enabled[i]);
    }

    /** Roll按钮激活状态更新时的回调 */
    protected void onRollButtonEnabledChanged(boolean enabled) {
        mRollButton.setEnabled(enabled);
    }

    /** 点击第i个骰子 */
    protected void clickDice(int i) {
        mViewModel.toggleLocked(i);
    }

    /** 掷骰子 */
    protected void rollDice() {
        int totalFrames = 10;  // 动画帧数
        int frameInterval = 30;  // 帧间隔(ms)
        executor.execute(() -> {
            mainHandler.post(() -> mViewModel.setRollButtonEnabled(false));
            for (int i = 0; i < totalFrames; i++) {
                mainHandler.post(() -> mViewModel.rollDice(false));  // 动画效果
                try {
                    Thread.sleep(frameInterval);
                }
                catch (InterruptedException e) {
                    break;
                }
            }
            mainHandler.post(() -> {
                mViewModel.setRollButtonEnabled(true);
                mViewModel.rollDice();  // 真正生效
            });
        });
    }

    /** 重置骰子窗口 */
    protected void resetDiceWindow() {
        mViewModel.resetDiceWindow();
    }

    /** 返回游戏标题 */
    public String getTitle() {
        return mTitleTextView.getText().toString();
    }

    /** 开始一次新游戏 */
    public void startNewGame() {
        mViewModel.reset();
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
     * @param rank  排名
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
                .setTitle(getString(R.string.gameOver))
                .setMessage(String.format("%s: %d", honor, score))
                .setPositiveButton(R.string.ok, (dialog, which) -> startNewGame())
                .show();
    }

}
