package com.zzy.dicegames.ui.game.balut;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;
import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.ui.game.BaseGameFragment;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

/**
 * Balut游戏Fragment
 *
 * @author 赵正阳
 */
public class BalutGameFragment extends BaseGameFragment<BalutGameViewModel> {
    /** 得分标签 */
    private TextView[][] mScoreTextViews;

    /** 每个得分项的总分标签 */
    private TextView[] mCategoryScoreTextViews;

    /** 每个得分项的点数标签 */
    private TextView[] mCategoryPointsTextViews;

    /** 游戏总分标签 */
    private TextView mTotalScoreTextView;

    /** 总分点数标签 */
    private TextView mTotalScorePointsTextView;

    /** 总点数标签 */
    private TextView mTotalPointsTextView;

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
        mScoreTextViews = new TextView[scoreTextViewIds.length][scoreTextViewIds[0].length];
        for (int i = 0; i < mScoreTextViews.length; i++) {
            for (int j = 0; j < mScoreTextViews[i].length; j++) {
                int category = i;
                mScoreTextViews[i][j] = view.findViewById(scoreTextViewIds[i][j]);
                mScoreTextViews[i][j].setOnClickListener(v -> select(category));
            }
        }

        // 获取得分项总分标签
        int[] categoryScoreTextViewIds = {
                R.id.tvFoursScore, R.id.tvFivesScore, R.id.tvSixesScore,
                R.id.tvStraightScore, R.id.tvFullHouseScore, R.id.tvChoiceScore, R.id.tvBalutScore
        };
        mCategoryScoreTextViews = new TextView[categoryScoreTextViewIds.length];
        for (int i = 0; i < mCategoryScoreTextViews.length; i++)
            mCategoryScoreTextViews[i] = view.findViewById(categoryScoreTextViewIds[i]);

        // 获取得分项点数标签
        int[] categoryPointsTextViewIds = {
                R.id.tvFoursPoints, R.id.tvFivesPoints, R.id.tvSixesPoints,
                R.id.tvStraightPoints, R.id.tvFullHousePoints, R.id.tvChoicePoints, R.id.tvBalutPoints
        };
        mCategoryPointsTextViews = new TextView[categoryPointsTextViewIds.length];
        for (int i = 0; i < mCategoryPointsTextViews.length; i++)
            mCategoryPointsTextViews[i] = view.findViewById(categoryPointsTextViewIds[i]);

        mTotalScoreTextView = view.findViewById(R.id.tvTotalScore);
        mTotalScorePointsTextView = view.findViewById(R.id.tvTotalScorePoints);
        mTotalPointsTextView = view.findViewById(R.id.tvTotalPoints);
    }

    @Override
    protected BalutGameViewModel createViewModel() {
        return new ViewModelProvider(this).get(BalutGameViewModel.class);
    }

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        super.setupObservers(owner);
        mViewModel.getScores().observe(owner, this::onScoresChanged);
        mViewModel.getSelectCount().observe(owner, this::onSelectCountChanged);
        mViewModel.getCategoryScores().observe(owner, this::onCategoryScoresChanged);
        mViewModel.getCategoryPoints().observe(owner, this::onCategoryPointsChanged);
        mViewModel.getTotalScore().observe(owner, this::onTotalScoreChanged);
        mViewModel.getTotalScorePoints().observe(owner, this::onTotalScorePointsChanged);
        mViewModel.getTotalPoints().observe(owner, this::onTotalPointsChanged);
    }

    @Override
    protected void onDiceRolledChanged(boolean rolled) {
        super.onDiceRolledChanged(rolled);
        updateScorecard();
    }

    /** 得分项的得分更新时的回调 */
    private void onScoresChanged(int[][] scores) {
        int[] selectCount = mViewModel.getSelectCount().getValue();
        if (selectCount == null)
            return;

        for (int i = 0; i < scores.length; i++) {
            for (int j = 0; j < scores[i].length; j++) {
                if (j < selectCount[i] || mRolled && j == selectCount[i])
                    mScoreTextViews[i][j].setText(Integer.toString(scores[i][j]));
                else
                    mScoreTextViews[i][j].setText("");
            }
        }
    }

    /** 得分项已选择次数更新时的回调 */
    private void onSelectCountChanged(int[] selectCount) {
        for (int i = 0; i < selectCount.length; i++) {
            for (int j = 0; j < mScoreTextViews[i].length; j++) {
                boolean candidate = mRolled && j == selectCount[i];
                mScoreTextViews[i][j].setEnabled(candidate);
                mScoreTextViews[i][j].setTextColor(getResources().getColor(
                        candidate ? R.color.scorecard_text_candidate : R.color.scorecard_text, null));
                mScoreTextViews[i][j].setBackgroundColor(getResources().getColor(
                        candidate ? R.color.scorecard_background_candidate : R.color.scorecard_background, null));
            }
        }
    }

    protected void updateScorecard() {
        int[][] scores = mViewModel.getScores().getValue();
        int[] selectCount = mViewModel.getSelectCount().getValue();
        if (scores == null || selectCount == null)
            return;

        onScoresChanged(scores);
        onSelectCountChanged(selectCount);
    }

    /** 每个得分项的总分更新时的回调 */
    private void onCategoryScoresChanged(int[] categoryScores) {
        for (int i = 0; i < categoryScores.length; i++)
            mCategoryScoreTextViews[i].setText(Integer.toString(categoryScores[i]));
    }

    /** 每个得分项的点数更新时的回调 */
    private void onCategoryPointsChanged(int[] categoryPoints) {
        for (int i = 0; i < categoryPoints.length; i++)
            mCategoryPointsTextViews[i].setText(Integer.toString(categoryPoints[i]));
    }

    /** 游戏总分更新时的回调 */
    private void onTotalScoreChanged(int totalScore) {
        mTotalScoreTextView.setText(Integer.toString(totalScore));
    }

    /** 总分点数更新时的回调 */
    private void onTotalScorePointsChanged(int totalScorePoints) {
        mTotalScorePointsTextView.setText(Integer.toString(totalScorePoints));
    }

    /** 总点数更新时的回调 */
    private void onTotalPointsChanged(int totalPoints) {
        mTotalPointsTextView.setText(Integer.toString(totalPoints));
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
        return super.getScoreMessage(score, rank)
                + String.format(", %s %d", getString(R.string.points), ((BalutScore) score).points);
    }
}
