package com.zzy.dicegames.ui.game.yahtzee;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.ui.game.BaseScoreBoardFragment;

import java.util.function.Consumer;

import androidx.lifecycle.LifecycleOwner;

/**
 * Yahtzee计分板Fragment，嵌套于一个{@link AbstractYahtzeeGameFragment}
 *
 * @author 赵正阳
 */
public abstract class AbstractYahtzeeScoreBoardFragment extends BaseScoreBoardFragment<AbstractYahtzeeScoreBoardViewModel> {
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

    /** 每次选择一项后执行的动作 */
    protected Runnable mSelectAction;

    /** 游戏结束时执行的动作 */
    protected Consumer<AbstractYahtzeeScore> mGameOverAction;

    @Override
    protected void setObservers() {
        LifecycleOwner owner = getViewLifecycleOwner();
        mViewModel.getScores().observe(owner, this::onScoresChanged);
        mViewModel.getSelected().observe(owner, this::onSelectedChanged);
        mViewModel.getUpperTotalScore().observe(owner, this::onUpperTotalScoreChanged);
        mViewModel.getBonusScore().observe(owner, this::onBonusScoreChanged);
        mViewModel.getTotalScore().observe(owner, this::onTotalScoreChanged);
    }

    /** 获取得分按钮和标签 */
    protected void initViews(View rootView) {
        int[] scoreButtonIds = getScoreButtonIds();
        mScoreButtons = new Button[scoreButtonIds.length];
        for (int i = 0; i < mScoreButtons.length; i++) {
            int category = i;
            mScoreButtons[i] = rootView.findViewById(scoreButtonIds[i]);
            mScoreButtons[i].setOnClickListener(v -> select(category));
        }

        int[] scoreTextViewIds = getScoreTextViewIds();
        mScoreTextViews = new TextView[scoreTextViewIds.length];
        for (int i = 0; i < mScoreTextViews.length; i++) {
            mScoreTextViews[i] = rootView.findViewById(scoreTextViewIds[i]);
        }

        mUpperTotalScoreTextView = rootView.findViewById(R.id.tvUpperTotal);
        mBonusScoreTextView = rootView.findViewById(R.id.tvBonus);
        mTotalScoreTextView = rootView.findViewById(R.id.tvGameTotal);
    }

    protected abstract AbstractYahtzeeScoreBoardViewModel createViewModel();

    /** 得分项按钮id */
    protected abstract int[] getScoreButtonIds();

    /** 得分项标签id */
    protected abstract int[] getScoreTextViewIds();

    /** 得分项的得分更新时的回调 */
    protected void onScoresChanged(int[] scores) {
        boolean[] selected = mViewModel.getSelected().getValue();
        if (selected == null)
            return;

        for (int i = 0; i < scores.length; i++) {
            if (selected[i])
                mScoreTextViews[i].setText(Integer.toString(scores[i]));
        }
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

    public void setSelectAction(Runnable selectAction) {
        mSelectAction = selectAction;
    }

    public void setGameOverAction(Consumer<AbstractYahtzeeScore> gameOverAction) {
        mGameOverAction = gameOverAction;
    }

    /** 根据骰子点数更新得分 */
    public void updateScores(int[] diceNumbers) {
        boolean[] selected = mViewModel.getSelected().getValue();
        if (selected == null)
            return;

        mViewModel.setDiceNumbers(diceNumbers);
        for (int i = 0; i < mScoreTextViews.length; ++i) {
            if (!selected[i])
                mScoreTextViews[i].setText(Integer.toString(mViewModel.calculateScore(i)));
        }
    }

    /** 选择指定的得分项 */
    protected void select(int category) {
        mViewModel.select(category);
        if (mViewModel.getNumSelected() == mViewModel.getNumCategories()) {
            if (mGameOverAction != null)
                mGameOverAction.accept(getScore());
        }
        else if (mSelectAction != null)
            mSelectAction.run();
    }

    /** 游戏结束时获取得分 */
    protected abstract AbstractYahtzeeScore getScore();
}
