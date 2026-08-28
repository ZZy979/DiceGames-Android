package com.zzy.dicegames.ui.game.yahtzee;

import android.view.View;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BaseScore;
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
        mTotalScoreTextView = view.findViewById(R.id.tvTotalScore);
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

    @Override
    protected void onDiceRolledChanged(boolean rolled) {
        super.onDiceRolledChanged(rolled);
        updateScorecard();
    }

    /** 得分项的得分更新时的回调 */
    protected void onScoresChanged(int[] scores) {
        boolean[] selected = mViewModel.getSelected().getValue();
        if (selected == null)
            return;

        for (int i = 0; i < scores.length; i++) {
            if (mRolled || selected[i])
                mScoreTextViews[i].setText(Integer.toString(scores[i]));
            else
                mScoreTextViews[i].setText("");
        }
    }

    /** 得分项选择状态更新时的回调 */
    protected void onSelectedChanged(boolean[] selected) {
        for (int i = 0; i < selected.length; i++) {
            boolean candidate = mRolled && !selected[i];
            mScoreTextViews[i].setEnabled(candidate);
            mScoreTextViews[i].setTextColor(getResources().getColor(
                    candidate ? R.color.scorecard_text_candidate : R.color.scorecard_text, null));
            mScoreTextViews[i].setBackgroundColor(getResources().getColor(
                    candidate ? R.color.scorecard_background_candidate : R.color.scorecard_background, null));
        }
    }

    protected void updateScorecard() {
        int[] scores = mViewModel.getScores().getValue();
        boolean[] selected = mViewModel.getSelected().getValue();
        if (scores == null || selected == null)
            return;

        onScoresChanged(scores);
        onSelectedChanged(selected);
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
    protected void onGameOver(Object[] args) {
        showScore((BaseScore) args[0], (int) args[1]);
    }

}
