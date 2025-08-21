package com.zzy.dicegames.ui.game.yahtzee;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.ui.game.BaseGameFragment;

import androidx.lifecycle.LifecycleOwner;

/**
 * Yahtzee游戏Fragment基类
 *
 * @author 赵正阳
 */
public abstract class BaseYahtzeeFragment extends BaseGameFragment<BaseYahtzeeViewModel> {
    /** 得分项按钮 */
    protected Button[] mScoreButtons;

    /** 得分标签 */
    protected TextView[] mScoreTextViews;

    /** 上区总分标签 */
    protected TextView mUpperTotalScoreTextView;

    /** 奖励分标签 */
    protected TextView mBonusScoreTextView;

    /** 游戏总分标签 */
    protected TextView mTotalScoreTextView;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.setGameOverAction(this::onGameOver);
        if (savedInstanceState == null)
            rollDice();
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);

        // 获取得分按钮和标签
        int[] scoreButtonIds = getScoreButtonIds();
        mScoreButtons = new Button[scoreButtonIds.length];
        for (int i = 0; i < mScoreButtons.length; i++) {
            int category = i;
            mScoreButtons[i] = view.findViewById(scoreButtonIds[i]);
            mScoreButtons[i].setOnClickListener(v -> select(category));
        }

        int[] scoreTextViewIds = getScoreTextViewIds();
        mScoreTextViews = new TextView[scoreTextViewIds.length];
        for (int i = 0; i < mScoreTextViews.length; i++) {
            mScoreTextViews[i] = view.findViewById(scoreTextViewIds[i]);
        }

        mUpperTotalScoreTextView = view.findViewById(R.id.tvUpperTotal);
        mBonusScoreTextView = view.findViewById(R.id.tvBonus);
        mTotalScoreTextView = view.findViewById(R.id.tvGameTotal);
    }

    /** 得分项按钮id */
    protected abstract int[] getScoreButtonIds();

    /** 得分项标签id */
    protected abstract int[] getScoreTextViewIds();

    protected abstract BaseYahtzeeViewModel createViewModel();

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        super.setupObservers(owner);
        mViewModel.getScores().observe(owner, this::onScoresChanged);
        mViewModel.getSelected().observe(owner, this::onSelectedChanged);
        mViewModel.getUpperTotalScore().observe(owner, this::onUpperTotalScoreChanged);
        mViewModel.getBonusScore().observe(owner, this::onBonusScoreChanged);
        mViewModel.getTotalScore().observe(owner, this::onTotalScoreChanged);
    }

    /** 得分项的得分更新时的回调 */
    protected void onScoresChanged(int[] scores) {
        for (int i = 0; i < scores.length; i++)
            mScoreTextViews[i].setText(Integer.toString(scores[i]));
    }

    /** 得分项选择状态更新时的回调 */
    protected void onSelectedChanged(boolean[] selected) {
        for (int i = 0; i < selected.length; i++) {
            mScoreButtons[i].setEnabled(!selected[i]);
            mScoreTextViews[i].setTextColor(selected[i] ? Color.RED : Color.BLACK);
        }
    }

    /** 上区总分更新时的回调 */
    protected void onUpperTotalScoreChanged(int upperTotalScore) {
        mUpperTotalScoreTextView.setText(Integer.toString(upperTotalScore));
    }

    /** 奖励分更新时的回调 */
    protected void onBonusScoreChanged(int bonusScore) {
        mBonusScoreTextView.setText(Integer.toString(bonusScore));
    }

    /** 游戏总分更新时的回调 */
    protected void onTotalScoreChanged(int totalScore) {
        mTotalScoreTextView.setText(Integer.toString(totalScore));
    }

    /** 选择指定的得分项 */
    protected void select(int category) {
        mViewModel.select(category);
    }

    @Override
    public void startNewGame() {
        super.startNewGame();
        rollDice();
    }

    /** 游戏结束时的回调函数 */
    protected void onGameOver(Object[] args) {
        showScore((int) args[0], (int) args[1]);
    }

}
