package com.zzy.dicegames.ui.game.balut;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;
import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.ui.game.BaseGameFragment;
import com.zzy.dicegames.ui.game.BaseGameViewModel;
import com.zzy.dicegames.ui.game.balut.BalutGameViewModel.BalutGameData;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

/**
 * Balut游戏Fragment
 *
 * @author 赵正阳
 */
public class BalutGameFragment extends BaseGameFragment<BalutGameViewModel> {
    /** 计分板容器，每个玩家一个计分板 */
    private LinearLayout mScorecardContainer;

    /** 得分标签，mScoreTextViews[玩家][得分项][第几次] */
    private TextView[][][] mScoreTextViews;

    /** 每个得分项的总分标签，mCategoryScoreTextViews[玩家][得分项] */
    private TextView[][] mCategoryScoreTextViews;

    /** 每个得分项的点数标签，mCategoryPointsTextViews[玩家][得分项] */
    private TextView[][] mCategoryPointsTextViews;

    /** 游戏总分标签，每个玩家一个 */
    private TextView[] mTotalScoreTextViews;

    /** 总分点数标签，每个玩家一个 */
    private TextView[] mTotalScorePointsTextViews;

    /** 总点数标签，每个玩家一个 */
    private TextView[] mTotalPointsTextViews;

    /** 玩家名称标签，每个玩家一个（单人游戏时为null） */
    private TextView[] mPlayerNameTextViews;

    /** 已绑定观察者的玩家数据 */
    private final List<BalutGameData> mBoundPlayerData = new ArrayList<>();

    /** 动态生成的计分板视图，切换玩家数量时需要移除 */
    private final List<View> mAddedScorecardViews = new ArrayList<>();

    @Override
    public GameType getGameType() {
        return GameType.BALUT;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_balut_game, container, false);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        mScorecardContainer = view.findViewById(R.id.scorecardContainer);
        buildScorecards();
    }

    /** 按玩家数量重建计分板（第一个玩家的计分板在布局文件中，其余玩家的计分板动态生成） */
    private void buildScorecards() {
        for (View scorecardView : mAddedScorecardViews)
            mScorecardContainer.removeView(scorecardView);
        mAddedScorecardViews.clear();

        View view = getView();
        if (view == null)
            return;

        int numPlayers = mViewModel.getNumPlayersValue();
        mScoreTextViews = new TextView[numPlayers][][];
        mCategoryScoreTextViews = new TextView[numPlayers][];
        mCategoryPointsTextViews = new TextView[numPlayers][];
        mTotalScoreTextViews = new TextView[numPlayers];
        mTotalScorePointsTextViews = new TextView[numPlayers];
        mTotalPointsTextViews = new TextView[numPlayers];
        mPlayerNameTextViews = new TextView[numPlayers];

        // 多人模式下在计分板上方显示玩家名称
        if (mViewModel.isMultiplayer()) {
            TextView nameTextView = createPlayerNameTextView(BaseGameViewModel.PLAYER_HUMAN);
            // 人类玩家的名称标签添加在布局文件中的计分板上方
            mScorecardContainer.addView(nameTextView, 0);
            mAddedScorecardViews.add(nameTextView);
            mPlayerNameTextViews[BaseGameViewModel.PLAYER_HUMAN] = nameTextView;
        }

        // 第一个玩家（人类）的计分板在布局文件中，只有该计分板可以点击
        bindScorecard(0, view);

        // 其余玩家的计分板动态生成并添加到下方，计分板上方显示玩家名称
        for (int p = 1; p < numPlayers; p++) {
            TextView nameTextView = createPlayerNameTextView(p);
            View scorecard = LayoutInflater.from(getContext())
                    .inflate(R.layout.balut_scorecard, mScorecardContainer, false);
            mScorecardContainer.addView(nameTextView);
            mScorecardContainer.addView(scorecard);
            mAddedScorecardViews.add(nameTextView);
            mAddedScorecardViews.add(scorecard);
            mPlayerNameTextViews[p] = nameTextView;
            bindScorecard(p, scorecard);
        }

        onCurrentPlayerChanged(mViewModel.getCurrentPlayerValue());
    }

    /** 创建指定玩家的名称标签 */
    private TextView createPlayerNameTextView(int player) {
        TextView nameTextView = (TextView) LayoutInflater.from(getContext())
                .inflate(R.layout.balut_player_name, mScorecardContainer, false);
        nameTextView.setText(player == BaseGameViewModel.PLAYER_HUMAN
                ? R.string.playerYou : R.string.playerComputer);
        return nameTextView;
    }

    /** 绑定指定玩家计分板上的标签 */
    private void bindScorecard(int player, View scorecard) {
        // 获取得分标签
        int[][] scoreTextViewIds = {
                {R.id.tvFours1, R.id.tvFours2, R.id.tvFours3, R.id.tvFours4},
                {R.id.tvFives1, R.id.tvFives2, R.id.tvFives3, R.id.tvFives4},
                {R.id.tvSixes1, R.id.tvSixes2, R.id.tvSixes3, R.id.tvSixes4},
                {R.id.tvStraight1, R.id.tvStraight2, R.id.tvStraight3, R.id.tvStraight4},
                {R.id.tvFullHouse1, R.id.tvFullHouse2, R.id.tvFullHouse3, R.id.tvFullHouse4},
                {R.id.tvChoice1, R.id.tvChoice2, R.id.tvChoice3, R.id.tvChoice4},
                {R.id.tvBalut1, R.id.tvBalut2, R.id.tvBalut3, R.id.tvBalut4}
        };
        mScoreTextViews[player] = new TextView[scoreTextViewIds.length][];
        for (int i = 0; i < scoreTextViewIds.length; i++) {
            mScoreTextViews[player][i] = new TextView[scoreTextViewIds[i].length];
            for (int j = 0; j < scoreTextViewIds[i].length; j++) {
                int category = i;
                TextView scoreTextView = scorecard.findViewById(scoreTextViewIds[i][j]);
                // 只有人类玩家的计分板可以点击
                if (player == BaseGameViewModel.PLAYER_HUMAN)
                    scoreTextView.setOnClickListener(v -> select(category));
                mScoreTextViews[player][i][j] = scoreTextView;
            }
        }

        // 获取得分项总分标签
        int[] categoryScoreTextViewIds = {
                R.id.tvFoursScore, R.id.tvFivesScore, R.id.tvSixesScore,
                R.id.tvStraightScore, R.id.tvFullHouseScore, R.id.tvChoiceScore, R.id.tvBalutScore
        };
        mCategoryScoreTextViews[player] = new TextView[categoryScoreTextViewIds.length];
        for (int i = 0; i < categoryScoreTextViewIds.length; i++)
            mCategoryScoreTextViews[player][i] = scorecard.findViewById(categoryScoreTextViewIds[i]);

        // 获取得分项点数标签
        int[] categoryPointsTextViewIds = {
                R.id.tvFoursPoints, R.id.tvFivesPoints, R.id.tvSixesPoints,
                R.id.tvStraightPoints, R.id.tvFullHousePoints, R.id.tvChoicePoints, R.id.tvBalutPoints
        };
        mCategoryPointsTextViews[player] = new TextView[categoryPointsTextViewIds.length];
        for (int i = 0; i < categoryPointsTextViewIds.length; i++)
            mCategoryPointsTextViews[player][i] = scorecard.findViewById(categoryPointsTextViewIds[i]);

        mTotalScoreTextViews[player] = scorecard.findViewById(R.id.tvTotalScore);
        mTotalScorePointsTextViews[player] = scorecard.findViewById(R.id.tvTotalScorePoints);
        mTotalPointsTextViews[player] = scorecard.findViewById(R.id.tvTotalPoints);
    }

    @Override
    protected BalutGameViewModel createViewModel() {
        return new ViewModelProvider(this).get(BalutGameViewModel.class);
    }

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        super.setupObservers(owner);
        mViewModel.getClickable().observe(owner, this::onClickableChanged);
        mViewModel.getCurrentPlayer().observe(owner, this::onCurrentPlayerChanged);
        // 观察玩家数量：注册时会立即回调一次，据此重建计分板并绑定玩家数据观察者
        mViewModel.getNumPlayers().observe(owner, n -> onNumPlayersChanged(owner));
    }
    /** 玩家数量变化时重建计分板并重新绑定玩家数据观察者 */
    private void onNumPlayersChanged(LifecycleOwner owner) {
        for (BalutGameData playerData : mBoundPlayerData) {
            playerData.scores.removeObservers(owner);
            playerData.selectCount.removeObservers(owner);
            playerData.categoryScores.removeObservers(owner);
            playerData.categoryPoints.removeObservers(owner);
            playerData.score.removeObservers(owner);
            playerData.totalScorePoints.removeObservers(owner);
            playerData.totalPoints.removeObservers(owner);
        }
        mBoundPlayerData.clear();

        buildScorecards();

        for (int p = 0; p < mViewModel.getNumPlayersValue(); p++) {
            final int player = p;
            BalutGameData playerData = mViewModel.data(p);
            playerData.scores.observe(owner, scores -> onScoresChanged(player, scores));
            playerData.selectCount.observe(owner, counts -> onSelectCountChanged(player, counts));
            playerData.categoryScores.observe(owner, scores -> onCategoryScoresChanged(player, scores));
            playerData.categoryPoints.observe(owner, points -> onCategoryPointsChanged(player, points));
            playerData.score.observe(owner, score -> onTotalScoreChanged(player, score));
            playerData.totalScorePoints.observe(owner, points -> onTotalScorePointsChanged(player, points));
            playerData.totalPoints.observe(owner, points -> onTotalPointsChanged(player, points));
            mBoundPlayerData.add(playerData);
        }

        // 计分板重建后标签的内容为空，重新显示各玩家已有的数据
        onClickableChanged(mViewModel.isClickable());
    }

    /**
     * 当前玩家更新时的回调：多人模式下高亮当前玩家的名称<br>
     * （计分板重建时也会调用，因此可保证新生成的名称标签样式正确）
     */
    protected void onCurrentPlayerChanged(int currentPlayer) {
        if (mPlayerNameTextViews == null)
            return;

        for (int p = 0; p < mPlayerNameTextViews.length; p++) {
            TextView nameTextView = mPlayerNameTextViews[p];
            if (nameTextView != null)
                nameTextView.setTextColor(
                        mViewModel.isMultiplayer() && p == currentPlayer ? Color.RED : Color.BLACK);
        }
    }

    /** 得分项可点击状态更新时的回调 */
    protected void onClickableChanged(boolean clickable) {
        if (mScoreTextViews == null)
            return;

        for (int p = 0; p < mScoreTextViews.length; p++) {
            if (mScoreTextViews[p] == null)
                continue;

            BalutGameData playerData = mViewModel.data(p);
            onScoresChanged(p, playerData.scores.getValue());
            onSelectCountChanged(p, playerData.selectCount.getValue());
        }
    }

    /** 指定玩家的得分项得分更新时的回调 */
    protected void onScoresChanged(int player, int[][] scores) {
        if (mScoreTextViews == null || mScoreTextViews[player] == null)
            return;

        int[] selectCount = mViewModel.data(player).selectCount.getValue();
        if (scores == null || selectCount == null)
            return;

        // 显示已选择格子的得分，以及当前玩家可选择的格子的预估得分
        boolean clickable = player == mViewModel.getCurrentPlayerValue() && mViewModel.isClickable();
        for (int i = 0; i < scores.length; i++) {
            for (int j = 0; j < scores[i].length; j++)
                mScoreTextViews[player][i][j].setText(
                        j < selectCount[i] || j == selectCount[i] && clickable ?
                                Integer.toString(scores[i][j]) : "");
        }
    }

    /** 指定玩家的得分项已选择次数更新时的回调 */
    protected void onSelectCountChanged(int player, int[] selectCount) {
        if (mScoreTextViews == null || mScoreTextViews[player] == null || selectCount == null)
            return;

        boolean clickable = player == mViewModel.getCurrentPlayerValue() && mViewModel.isClickable();
        for (int i = 0; i < selectCount.length; i++) {
            for (int j = 0; j < mScoreTextViews[player][i].length; j++) {
                boolean candidate = j == selectCount[i] && clickable;
                TextView scoreTextView = mScoreTextViews[player][i][j];
                scoreTextView.setEnabled(candidate);
                scoreTextView.setTextColor(getResources().getColor(
                        candidate ? R.color.scorecard_text_candidate : R.color.scorecard_text, null));
                scoreTextView.setBackgroundColor(getResources().getColor(
                        candidate ? R.color.scorecard_background_candidate : R.color.scorecard_background, null));
            }
        }
    }

    /** 指定玩家的每个得分项的总分更新时的回调 */
    protected void onCategoryScoresChanged(int player, int[] categoryScores) {
        if (mCategoryScoreTextViews == null || mCategoryScoreTextViews[player] == null
                || categoryScores == null)
            return;

        for (int i = 0; i < categoryScores.length; i++)
            mCategoryScoreTextViews[player][i].setText(Integer.toString(categoryScores[i]));
    }

    /** 指定玩家的每个得分项的点数更新时的回调 */
    protected void onCategoryPointsChanged(int player, int[] categoryPoints) {
        if (mCategoryPointsTextViews == null || mCategoryPointsTextViews[player] == null
                || categoryPoints == null)
            return;

        for (int i = 0; i < categoryPoints.length; i++)
            mCategoryPointsTextViews[player][i].setText(Integer.toString(categoryPoints[i]));
    }

    /** 指定玩家的游戏总分更新时的回调 */
    protected void onTotalScoreChanged(int player, int totalScore) {
        if (mTotalScoreTextViews != null && mTotalScoreTextViews[player] != null)
            mTotalScoreTextViews[player].setText(Integer.toString(totalScore));
    }

    /** 指定玩家的总分点数更新时的回调 */
    protected void onTotalScorePointsChanged(int player, int totalScorePoints) {
        if (mTotalScorePointsTextViews != null && mTotalScorePointsTextViews[player] != null)
            mTotalScorePointsTextViews[player].setText(Integer.toString(totalScorePoints));
    }

    /** 指定玩家的总点数更新时的回调 */
    protected void onTotalPointsChanged(int player, int totalPoints) {
        if (mTotalPointsTextViews != null && mTotalPointsTextViews[player] != null)
            mTotalPointsTextViews[player].setText(Integer.toString(totalPoints));
    }

    /** 选择指定的得分项 */
    private void select(int category) {
        mViewModel.select(category);
    }

    /** 游戏结束时的回调函数 */
    protected void onGameOver(Object[] args) {
        showScore((BaseScore) args[0], (int) args[1]);
    }

    @Override
    public String getScoreMessage(BaseScore score, int rank) {
        String message = super.getScoreMessage(score, rank)
                + String.format(", %s %d", getString(R.string.points), ((BalutScore) score).points);
        if (!mViewModel.isMultiplayer())
            return message;

        // Balut先比较点数，点数相同时再比较得分
        int comparison = Integer.compare(getPlayerPoints(BaseGameViewModel.PLAYER_HUMAN),
                getPlayerPoints(BaseGameViewModel.PLAYER_COMPUTER));
        if (comparison == 0)
            comparison = Integer.compare(
                    mViewModel.getPlayerScoreValue(BaseGameViewModel.PLAYER_HUMAN),
                    mViewModel.getPlayerScoreValue(BaseGameViewModel.PLAYER_COMPUTER));
        String result = comparison > 0 ? getString(R.string.youWin)
                : comparison < 0 ? getString(R.string.youLose) : getString(R.string.draw);
        return result + "\n"
                + getString(R.string.multiplayerScoreFormat,
                        getString(R.string.playerYou), getPlayerPoints(BaseGameViewModel.PLAYER_HUMAN),
                        getPlayerPoints(BaseGameViewModel.PLAYER_COMPUTER), getString(R.string.playerComputer))
                + "\n" + message;
    }

    /** 返回指定玩家的点数 */
    private int getPlayerPoints(int player) {
        Integer points = mViewModel.data(player).totalPoints.getValue();
        return points == null ? 0 : points;
    }
}
