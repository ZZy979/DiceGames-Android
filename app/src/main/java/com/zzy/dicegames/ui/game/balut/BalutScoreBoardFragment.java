package com.zzy.dicegames.ui.game.balut;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BalutScore;
import com.zzy.dicegames.ui.game.BaseScoreBoardFragment;

import java.time.LocalDate;
import java.util.function.Consumer;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import static com.zzy.dicegames.ui.game.balut.BalutScoreBoardViewModel.*;

/**
 * Balut计分板Fragment，嵌套于一个{@link BalutGameFragment}
 *
 * @author 赵正阳
 */
public class BalutScoreBoardFragment extends BaseScoreBoardFragment<BalutScoreBoardViewModel> {
    /** 得分项按钮 */
    private Button[] mScoreButtons;

    /** 得分标签 */
    private TextView[][] mScoreTextViews;

    /** 游戏总分标签 */
    private TextView mTotalScoreTextView;

    /** 每次选择一项后执行的动作 */
    private Runnable mSelectAction;

    /** 游戏结束时执行的动作 */
    private Consumer<BalutScore> mGameOverAction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_balut_score_board, container, false);
    }

    @Override
    protected BalutScoreBoardViewModel createViewModel() {
        return new ViewModelProvider(this).get(BalutScoreBoardViewModel.class);
    }

    @Override
    protected void setObservers() {
        LifecycleOwner owner = getViewLifecycleOwner();
        mViewModel.getScores().observe(owner, this::onScoresChanged);
        mViewModel.getSelectCount().observe(owner, this::onSelectCountChanged);
        mViewModel.getTotalScore().observe(owner, this::onTotalScoreChanged);
    }

    /** 获取得分按钮和标签 */
    @Override
    protected void initViews(View rootView) {
        int[] scoreButtonIds = new int[] {
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btnStraight,
                R.id.btnFullHouse, R.id.btnChoice, R.id.btnBalut
        };
        mScoreButtons = new Button[scoreButtonIds.length];
        for (int i = 0; i < mScoreButtons.length; i++) {
            int category = i;
            mScoreButtons[i] = rootView.findViewById(scoreButtonIds[i]);
            mScoreButtons[i].setOnClickListener(v -> select(category));
        }

        int[][] scoreTextViewIds = new int[][] {
                {R.id.tv41, R.id.tv42, R.id.tv43, R.id.tv44},
                {R.id.tv51, R.id.tv52, R.id.tv53, R.id.tv54},
                {R.id.tv61, R.id.tv62, R.id.tv63, R.id.tv64},
                {R.id.tvStraight1, R.id.tvStraight2, R.id.tvStraight3, R.id.tvStraight4},
                {R.id.tvFullHouse1, R.id.tvFullHouse2, R.id.tvFullHouse3, R.id.tvFullHouse4},
                {R.id.tvChoice1, R.id.tvChoice2, R.id.tvChoice3, R.id.tvChoice4},
                {R.id.tvBalut1, R.id.tvBalut2, R.id.tvBalut3, R.id.tvBalut4}
        };
        mScoreTextViews = new TextView[scoreTextViewIds.length][scoreTextViewIds[0].length];
        for (int i = 0; i < mScoreTextViews.length; ++i) {
            for (int j = 0; j < mScoreTextViews[i].length; j++)
                mScoreTextViews[i][j] = rootView.findViewById(scoreTextViewIds[i][j]);
        }

        mTotalScoreTextView = rootView.findViewById(R.id.tvGameTotal);
    }

    /** 得分项的得分更新时的回调 */
    private void onScoresChanged(int[][] scores) {
        int[] selectCount = mViewModel.getSelectCount().getValue();
        if (selectCount == null)
            return;

        for (int i = 0; i < scores.length; i++) {
            for (int j = 0; j < scores[i].length; j++) {
                if (j < selectCount[i])
                    mScoreTextViews[i][j].setText(Integer.toString(scores[i][j]));
            }
        }
    }

    /** 得分项已选择次数更新时的回调 */
    private void onSelectCountChanged(int[] selectCount) {
        for (int i = 0; i < selectCount.length; i++) {
            mScoreButtons[i].setEnabled(selectCount[i] < MAX_SELECTIONS);
            for (int j = 0; j < mScoreTextViews[i].length; j++)
                mScoreTextViews[i][j].setTextColor(j < selectCount[i] ? Color.RED : Color.BLACK);
        }
    }

    /** 游戏总分更新时的回调 */
    private void onTotalScoreChanged(int totalScore) {
        mTotalScoreTextView.setText(Integer.toString(totalScore));
    }

    public void setSelectAction(Runnable selectAction) {
        mSelectAction = selectAction;
    }

    public void setGameOverAction(Consumer<BalutScore> gameOverAction) {
        mGameOverAction = gameOverAction;
    }

    /** 根据骰子点数更新得分 */
    public void updateScores(int[] diceNumbers) {
        int[] selectCount = mViewModel.getSelectCount().getValue();
        if (selectCount == null)
            return;

        mViewModel.setDiceNumbers(diceNumbers);
        for (int i = 0; i < mScoreTextViews.length; ++i)
            if (selectCount[i] < mScoreTextViews[i].length)
                mScoreTextViews[i][selectCount[i]].setText(Integer.toString(mViewModel.calculateScore(i)));
    }

    /** 选择指定的得分项 */
    private void select(int category) {
        mViewModel.select(category);
        if (mViewModel.getNumSelected() == NUM_CATEGORIES) {
            if (mGameOverAction != null)
                mGameOverAction.accept(getScore());
        }
        else if (mSelectAction != null)
            mSelectAction.run();
    }

    /** 游戏结束时获取得分 */
    private BalutScore getScore() {
        int[][] scores = mViewModel.getScores().getValue();
        if (mViewModel.getTotalScore().getValue() == null || scores == null)
            return null;

        int gotBalut = 0;
        for (int j = 0; j < scores[BALUT].length; j++) {
            if (scores[BALUT][j] > 0)
                gotBalut++;
        }
        return new BalutScore(LocalDate.now().toString(),
                mViewModel.getTotalScore().getValue(), gotBalut);
    }
}
