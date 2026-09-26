package com.zzy.dicegames.ui.game.yahtzee;

import android.view.View;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.ui.game.BaseGameFragment;
import com.zzy.dicegames.ui.game.yahtzee.BaseYahtzeeGameViewModel.YahtzeeGameData;

import androidx.lifecycle.LifecycleOwner;

/**
 * Yahtzee游戏Fragment基类
 *
 * @author 赵正阳
 */
public abstract class BaseYahtzeeGameFragment extends BaseGameFragment<BaseYahtzeeGameViewModel> {
    /** 得分标签，mScoreTextViews[玩家][得分项] */
    protected TextView[][] mScoreTextViews;

    /** 上区总分标签，每个玩家一个 */
    protected TextView[] mUpperTotalScoreTextViews;

    /** 奖励分标签，每个玩家一个 */
    protected TextView[] mBonusScoreTextViews;

    /** 游戏总分标签，每个玩家一个 */
    protected TextView[] mTotalScoreTextViews;

    @Override
    protected void initViews(View view) {
        super.initViews(view);

        int numPlayers = mViewModel.getNumPlayersValue();
        int[] scoreTextViewIds = getScoreTextViewIds();

        mScoreTextViews = new TextView[numPlayers][];
        mUpperTotalScoreTextViews = new TextView[numPlayers];
        mBonusScoreTextViews = new TextView[numPlayers];
        mTotalScoreTextViews = new TextView[numPlayers];

        // 第一个玩家的记分板标签在布局文件中，其余玩家的标签由子类（多人模式）添加
        mScoreTextViews[0] = new TextView[scoreTextViewIds.length];
        for (int i = 0; i < scoreTextViewIds.length; i++) {
            int category = i;
            mScoreTextViews[0][i] = view.findViewById(scoreTextViewIds[i]);
            mScoreTextViews[0][i].setOnClickListener(v -> select(category));
        }

        mUpperTotalScoreTextViews[0] = view.findViewById(R.id.tvUpperTotal);
        mBonusScoreTextViews[0] = view.findViewById(R.id.tvBonus);
        mTotalScoreTextViews[0] = view.findViewById(R.id.tvTotalScore);
    }

    /** 得分项标签id */
    protected abstract int[] getScoreTextViewIds();

    protected abstract BaseYahtzeeGameViewModel createViewModel();

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        super.setupObservers(owner);
        mViewModel.getClickable().observe(owner, this::onClickableChanged);
        for (int p = 0; p < mViewModel.getNumPlayersValue(); p++) {
            final int player = p;
            YahtzeeGameData data = mViewModel.data(p);
            data.scores.observe(owner, scores -> onScoresChanged(player, scores));
            data.selected.observe(owner, selected -> onSelectedChanged(player, selected));
            data.upperTotalScore.observe(owner, score -> onUpperTotalScoreChanged(player, score));
            data.bonusScore.observe(owner, score -> onBonusScoreChanged(player, score));
            data.score.observe(owner, score -> onTotalScoreChanged(player, score));
        }
    }

    /** 得分项可点击状态更新时的回调 */
    protected void onClickableChanged(boolean clickable) {
        for (int p = 0; p < mScoreTextViews.length; p++) {
            if (mScoreTextViews[p] == null)
                continue;
            YahtzeeGameData data = mViewModel.data(p);
            onScoresChanged(p, data.scores.getValue());
            onSelectedChanged(p, data.selected.getValue());
        }
    }

    /** 指定玩家的得分项得分更新时的回调 */
    protected void onScoresChanged(int player, int[] scores) {
        if (scores == null || player >= mScoreTextViews.length || mScoreTextViews[player] == null)
            return;

        boolean[] selected = mViewModel.data(player).selected.getValue();
        if (selected == null)
            return;

        boolean clickable = player == mViewModel.getCurrentPlayerValue() && mViewModel.isClickable();
        for (int i = 0; i < scores.length; i++)
            mScoreTextViews[player][i].setText(
                    selected[i] || clickable ? Integer.toString(scores[i]) : "");
    }

    /** 指定玩家的得分项选择状态更新时的回调 */
    protected void onSelectedChanged(int player, boolean[] selected) {
        if (selected == null || player >= mScoreTextViews.length || mScoreTextViews[player] == null)
            return;

        boolean candidate = player == mViewModel.getCurrentPlayerValue() && mViewModel.isClickable();
        for (int i = 0; i < selected.length; i++) {
            boolean enabled = candidate && !selected[i];
            mScoreTextViews[player][i].setEnabled(enabled);
            mScoreTextViews[player][i].setTextColor(getResources().getColor(
                    enabled ? R.color.scorecard_text_candidate : R.color.scorecard_text, null));
            mScoreTextViews[player][i].setBackgroundColor(getResources().getColor(
                    enabled ? R.color.scorecard_background_candidate : R.color.scorecard_background, null));
        }
    }

    /** 指定玩家的上区总分更新时的回调 */
    protected void onUpperTotalScoreChanged(int player, int upperTotalScore) {
        if (player < mUpperTotalScoreTextViews.length && mUpperTotalScoreTextViews[player] != null)
            mUpperTotalScoreTextViews[player].setText(Integer.toString(upperTotalScore));
    }

    /** 指定玩家的奖励分更新时的回调 */
    protected void onBonusScoreChanged(int player, int bonusScore) {
        if (player < mBonusScoreTextViews.length && mBonusScoreTextViews[player] != null)
            mBonusScoreTextViews[player].setText(Integer.toString(bonusScore));
    }

    /** 指定玩家的游戏总分更新时的回调 */
    protected void onTotalScoreChanged(int player, int totalScore) {
        if (player < mTotalScoreTextViews.length && mTotalScoreTextViews[player] != null)
            mTotalScoreTextViews[player].setText(Integer.toString(totalScore));
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
