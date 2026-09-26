package com.zzy.dicegames.ui.game.yahtzee;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.ui.game.BaseGameFragment;
import com.zzy.dicegames.ui.game.BaseGameViewModel;
import com.zzy.dicegames.ui.game.yahtzee.BaseYahtzeeGameViewModel.YahtzeeGameData;

import java.util.ArrayList;
import java.util.List;

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

    /** 表头标签，每个玩家一个 */
    protected TextView[] mPlayerHeaderTextViews;

    /** 已绑定观察者的玩家数据 */
    private final List<YahtzeeGameData> mBoundPlayerData = new ArrayList<>();

    /** 动态生成的记分板标签，切换玩家数量时需要移除 */
    private final List<TextView> mAddedCells = new ArrayList<>();

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        buildScorecard();
    }

    /** 按玩家数量重建记分板（第一个玩家的标签在布局文件中，其余玩家的标签动态添加） */
    private void buildScorecard() {
        for (TextView cell : mAddedCells) {
            ViewGroup row = (ViewGroup) cell.getParent();
            if (row != null)
                row.removeView(cell);
        }
        mAddedCells.clear();

        View view = getView();
        if (view == null)
            return;

        int numPlayers = mViewModel.getNumPlayersValue();
        int[] scoreTextViewIds = getScoreTextViewIds();

        mScoreTextViews = new TextView[numPlayers][];
        mUpperTotalScoreTextViews = new TextView[numPlayers];
        mBonusScoreTextViews = new TextView[numPlayers];
        mTotalScoreTextViews = new TextView[numPlayers];
        mPlayerHeaderTextViews = new TextView[numPlayers];

        // 第一个玩家（人类）的记分板标签在布局文件中，只有该列可以点击
        mScoreTextViews[0] = new TextView[scoreTextViewIds.length];
        for (int i = 0; i < scoreTextViewIds.length; i++) {
            int category = i;
            mScoreTextViews[0][i] = view.findViewById(scoreTextViewIds[i]);
            mScoreTextViews[0][i].setOnClickListener(v -> select(category));
        }
        mUpperTotalScoreTextViews[0] = view.findViewById(R.id.tvUpperTotal);
        mBonusScoreTextViews[0] = view.findViewById(R.id.tvBonus);
        mTotalScoreTextViews[0] = view.findViewById(R.id.tvTotalScore);
        mPlayerHeaderTextViews[0] = view.findViewById(R.id.tvHeaderScore);

        // 其余玩家的记分板标签动态添加到对应的行
        for (int p = 1; p < numPlayers; p++) {
            mScoreTextViews[p] = new TextView[scoreTextViewIds.length];
            for (int i = 0; i < scoreTextViewIds.length; i++)
                mScoreTextViews[p][i] = addCell(mScoreTextViews[0][i]);
            mUpperTotalScoreTextViews[p] = addCell(mUpperTotalScoreTextViews[0]);
            mBonusScoreTextViews[p] = addCell(mBonusScoreTextViews[0]);
            mTotalScoreTextViews[p] = addCell(mTotalScoreTextViews[0]);
            mPlayerHeaderTextViews[p] = addCell(mPlayerHeaderTextViews[0]);
        }

        // 多人模式下表头显示玩家名称，单人模式下显示“得分”
        boolean multiplayer = mViewModel.isMultiplayer();
        for (int p = 0; p < numPlayers; p++) {
            TextView header = mPlayerHeaderTextViews[p];
            if (header == null)
                continue;
            header.setText(!multiplayer ? R.string.score
                    : p == BaseGameViewModel.PLAYER_HUMAN ? R.string.playerYou : R.string.playerComputer);
        }
    }

    /** 在锚点标签所在的行末尾添加一个同样式的标签 */
    private TextView addCell(TextView anchor) {
        if (anchor == null)
            return null;

        ViewGroup row = (ViewGroup) anchor.getParent();
        if (row == null)
            return null;

        TextView cell = (TextView) LayoutInflater.from(getContext())
                .inflate(R.layout.scorecard_cell, row, false);
        row.addView(cell);
        mAddedCells.add(cell);
        return cell;
    }

    /** 得分项标签id（第一个玩家的记分板标签在布局文件中，其余玩家的标签动态生成） */
    protected abstract int[] getScoreTextViewIds();

    protected abstract BaseYahtzeeGameViewModel createViewModel();

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        super.setupObservers(owner);
        mViewModel.getClickable().observe(owner, this::onClickableChanged);
        mViewModel.getCurrentPlayer().observe(owner, this::onCurrentPlayerChanged);
        // 观察玩家数量：注册时会立即回调一次，据此构建记分板并绑定玩家数据观察者
        mViewModel.getNumPlayers().observe(owner, n -> onNumPlayersChanged(owner));
    }

    /** 玩家数量变化时重建记分板并重新绑定玩家数据观察者 */
    private void onNumPlayersChanged(LifecycleOwner owner) {
        for (YahtzeeGameData playerData : mBoundPlayerData) {
            playerData.scores.removeObservers(owner);
            playerData.selected.removeObservers(owner);
            playerData.upperTotalScore.removeObservers(owner);
            playerData.bonusScore.removeObservers(owner);
            playerData.score.removeObservers(owner);
        }
        mBoundPlayerData.clear();

        buildScorecard();
        onCurrentPlayerChanged(mViewModel.getCurrentPlayerValue());

        for (int p = 0; p < mViewModel.getNumPlayersValue(); p++) {
            final int player = p;
            YahtzeeGameData playerData = mViewModel.data(p);
            playerData.scores.observe(owner, scores -> onScoresChanged(player, scores));
            playerData.selected.observe(owner, selected -> onSelectedChanged(player, selected));
            playerData.upperTotalScore.observe(owner, score -> onUpperTotalScoreChanged(player, score));
            playerData.bonusScore.observe(owner, score -> onBonusScoreChanged(player, score));
            playerData.score.observe(owner, score -> onTotalScoreChanged(player, score));
            mBoundPlayerData.add(playerData);
        }
    }

    /** 当前玩家更新时的回调：多人模式下高亮当前玩家的表头 */
    protected void onCurrentPlayerChanged(int currentPlayer) {
        if (mPlayerHeaderTextViews == null)
            return;

        boolean multiplayer = mViewModel.isMultiplayer();
        for (int p = 0; p < mPlayerHeaderTextViews.length; p++) {
            TextView header = mPlayerHeaderTextViews[p];
            if (header != null)
                header.setTextColor(multiplayer && p == currentPlayer ? Color.RED : Color.BLACK);
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
    public String getScoreMessage(BaseScore score, int rank) {
        String message = super.getScoreMessage(score, rank);
        if (!mViewModel.isMultiplayer())
            return message;

        int humanScore = mViewModel.getPlayerScoreValue(BaseGameViewModel.PLAYER_HUMAN);
        int computerScore = mViewModel.getPlayerScoreValue(BaseGameViewModel.PLAYER_COMPUTER);
        String result = humanScore > computerScore ? getString(R.string.youWin)
                : humanScore < computerScore ? getString(R.string.youLose) : getString(R.string.draw);
        return result + "\n"
                + getString(R.string.multiplayerScoreFormat,
                        getString(R.string.playerYou), humanScore,
                        computerScore, getString(R.string.playerComputer))
                + "\n" + message;
    }

    @Override
    protected void onGameOver(Object[] args) {
        showScore((BaseScore) args[0], (int) args[1]);
    }

}
