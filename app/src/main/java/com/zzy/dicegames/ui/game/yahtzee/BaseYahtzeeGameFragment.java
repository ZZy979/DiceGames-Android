package com.zzy.dicegames.ui.game.yahtzee;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.ui.game.BaseGameFragment;

import androidx.lifecycle.LifecycleOwner;

/**
 * Yahtzee游戏Fragment基类
 *
 * @author 赵正阳
 */
public abstract class BaseYahtzeeGameFragment extends BaseGameFragment<BaseYahtzeeGameViewModel> {
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
            rollDice();  // TODO 改为手动掷骰子
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);

        // 获取得分标签
        int[] scoreTextViewIds = getScoreTextViewIds();
        mScoreTextViews = new TextView[scoreTextViewIds.length];
        for (int i = 0; i < mScoreTextViews.length; i++) {
            int category = i;
            mScoreTextViews[i] = view.findViewById(scoreTextViewIds[i]);
            mScoreTextViews[i].setOnClickListener(v -> select(category));
        }

        mUpperTotalScoreTextView = view.findViewById(R.id.tvUpperTotal);
        mBonusScoreTextView = view.findViewById(R.id.tvBonus);
        mTotalScoreTextView = view.findViewById(R.id.tvGameTotal);
    }

    /** 得分项标签id */
    protected abstract int[] getScoreTextViewIds();

    protected abstract BaseYahtzeeGameViewModel createViewModel();

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
            mScoreTextViews[i].setEnabled(!selected[i]);
            mScoreTextViews[i].setTextColor(getResources().getColor(
                    selected[i] ? R.color.scorecard_text : R.color.scorecard_text_candidate, null));
            mScoreTextViews[i].setBackgroundColor(getResources().getColor(
                    selected[i] ? R.color.scorecard_background : R.color.scorecard_background_candidate, null));
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
