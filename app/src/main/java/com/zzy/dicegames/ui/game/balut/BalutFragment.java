package com.zzy.dicegames.ui.game.balut;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.dao.BalutScoreDao;
import com.zzy.dicegames.data.entity.BalutScore;
import com.zzy.dicegames.ui.game.BaseGameFragment;

import java.time.LocalDate;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import static com.zzy.dicegames.ui.game.balut.BalutViewModel.*;

/**
 * Balut游戏Fragment
 *
 * @author 赵正阳
 */
public class BalutFragment extends BaseGameFragment<BalutViewModel> {
    /** 得分项按钮 */
    private Button[] mScoreButtons;

    /** 得分标签 */
    private TextView[][] mScoreTextViews;

    /** 游戏总分标签 */
    private TextView mTotalScoreTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_balut, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null)
            rollDice();
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);

        // 获取得分按钮和标签
        int[] scoreButtonIds = {
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btnStraight,
                R.id.btnFullHouse, R.id.btnChoice, R.id.btnBalut
        };
        mScoreButtons = new Button[scoreButtonIds.length];
        for (int i = 0; i < mScoreButtons.length; i++) {
            int category = i;
            mScoreButtons[i] = view.findViewById(scoreButtonIds[i]);
            mScoreButtons[i].setOnClickListener(v -> select(category));
        }

        int[][] scoreTextViewIds = {
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
                mScoreTextViews[i][j] = view.findViewById(scoreTextViewIds[i][j]);
        }

        mTotalScoreTextView = view.findViewById(R.id.tvGameTotal);
    }

    @Override
    protected BalutViewModel createViewModel() {
        return new ViewModelProvider(this).get(BalutViewModel.class);
    }

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        super.setupObservers(owner);
        mViewModel.getScores().observe(owner, this::onScoresChanged);
        mViewModel.getSelectCount().observe(owner, this::onSelectCountChanged);
        mViewModel.getTotalScore().observe(owner, this::onTotalScoreChanged);
    }

    @Override
    protected void onDiceNumbersChanged(int[] numbers) {
        super.onDiceNumbersChanged(numbers);
        updateScores();
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
                else if (j > selectCount[i])
                    mScoreTextViews[i][j].setText("0");
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

    /** 根据骰子点数更新得分 */
    // TODO 移至ViewModel，增加estimatedScores
    private void updateScores() {
        int[] selectCount = mViewModel.getSelectCount().getValue();
        if (selectCount == null)
            return;

        for (int i = 0; i < mScoreTextViews.length; ++i)
            if (selectCount[i] < mScoreTextViews[i].length)
                mScoreTextViews[i][selectCount[i]].setText(Integer.toString(mViewModel.calculateScore(i)));
    }

    /** 选择指定的得分项 */
    private void select(int category) {
        mViewModel.select(category);
        if (mViewModel.getNumSelected() == NUM_CATEGORIES)
            onGameOver(getScore());
        else
            resetDiceWindow();
    }

    @Override
    protected void resetDiceWindow() {
        super.resetDiceWindow();
        rollDice();
    }

    @Override
    public void startNewGame() {
        super.startNewGame();
        rollDice();
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

    /**
     * 游戏结束时的回调函数，保存得分并开始新游戏<br>
     * 将该方法设置为计分板的监听器，游戏结束时计分板将以本局得分为参数调用该监听器
     */
    private void onGameOver(BalutScore score) {
        int rank = saveScore(score);
        showScore(score.getScore(), rank);
    }

    /** 保存得分，返回该得分在前10名中的名次，0表示不在前10名中 */
    private int saveScore(BalutScore score) {
        BalutScoreDao balutScoreDao = ScoreDatabase.getInstance(getContext()).balutScoreDao();
        balutScoreDao.insert(score);
        return balutScoreDao.findTop10Score().indexOf(score.getScore()) + 1;
    }

}
