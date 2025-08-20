package com.zzy.dicegames.ui.game.farkle;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.ui.game.BaseGameFragment;

import java.util.List;

import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.zzy.dicegames.ui.game.farkle.FarkleViewModel.*;

/**
 * Farkle游戏Fragment
 *
 * @author 赵正阳
 */
public class FarkleFragment extends BaseGameFragment<FarkleViewModel> {
    /** 玩家名称标签 */
    private TextView[] mPlayerNameTextViews;

    /** 玩家得分标签 */
    private TextView[] mPlayerScoreTextViews;

    /** 本轮得分标签 */
    private TextView mTurnScoreTextView;

    /** “保存得分”按钮 */
    private Button mBankButton;

    /** “新游戏”按钮 */
    private Button mNewGameButton;

    /** 日志列表 */
    private RecyclerView mLogView;

    private LogAdapter mLogAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_farkle, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.setGameOverAction(this::onGameOver);
        if (savedInstanceState == null) {
            mViewModel.addLog(R.string.logGameBegins);
            mViewModel.addLog(R.string.logYourTurn);
            mViewModel.addLog(R.string.logStartingScore, 0);
        }
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);

        int[] playerNameTextViewIds = {R.id.tvPlayerYou, R.id.tvPlayerComputer};
        mPlayerNameTextViews = new TextView[NUM_PLAYERS];
        for (int i = 0; i < mPlayerNameTextViews.length; i++)
            mPlayerNameTextViews[i] = view.findViewById(playerNameTextViewIds[i]);

        int[] playerScoreTextViewIds = {R.id.tvYourScore, R.id.tvComputerScore};
        mPlayerScoreTextViews = new TextView[NUM_PLAYERS];
        for (int i = 0; i < mPlayerScoreTextViews.length; i++)
            mPlayerScoreTextViews[i] = view.findViewById(playerScoreTextViewIds[i]);

        mTurnScoreTextView = view.findViewById(R.id.tvTurnScore);

        mBankButton = view.findViewById(R.id.btnBank);
        mBankButton.setOnClickListener(v -> mViewModel.bank());

        mNewGameButton = view.findViewById(R.id.btnNewGame);
        mNewGameButton.setOnClickListener(v -> startNewGame());

        mLogView = view.findViewById(R.id.rvLog);
        mLogView.setLayoutManager(new LinearLayoutManager(getContext()));
        mLogAdapter = new LogAdapter(getContext());
        mLogView.setAdapter(mLogAdapter);
    }

    @Override
    protected FarkleViewModel createViewModel() {
        return new ViewModelProvider(this).get(FarkleViewModel.class);
    }

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        super.setupObservers(owner);
        mViewModel.getCurrentPlayer().observe(owner, this::onCurrentPlayerChanged);
        mViewModel.getPlayerScores().observe(owner, this::onPlayerScoresChanged);
        mViewModel.getEstimatedTurnScore().observe(owner, this::onEstimatedTurnScoreChanged);
        mViewModel.getBankButtonEnabled().observe(owner, this::onBankButtonEnabledChanged);
        mViewModel.getNewGameButtonVisible().observe(owner, this::onNewGameButtonVisibleChanged);
        mViewModel.getGameLog().observe(owner, this::onGameLogChanged);
    }

    /** 当前玩家更新时的回调 */
    private void onCurrentPlayerChanged(int currentPlayer) {
        for (int i = 0; i < mPlayerNameTextViews.length; i++)
            mPlayerNameTextViews[i].setTextColor(i == currentPlayer ? Color.RED : Color.BLACK);
    }

    /** 玩家得分更新时的回调 */
    private void onPlayerScoresChanged(int[] playerScores) {
        for (int i = 0; i < playerScores.length; i++)
            mPlayerScoreTextViews[i].setText(Integer.toString(playerScores[i]));
    }

    /** 本轮得分更新时的回调 */
    private void onEstimatedTurnScoreChanged(int estimatedTurnScore) {
        mTurnScoreTextView.setText(Integer.toString(estimatedTurnScore));
    }

    /** “保存得分”按钮激活状态更新时的回调 */
    private void onBankButtonEnabledChanged(boolean enabled) {
        mBankButton.setEnabled(enabled);
    }

    /** “新游戏”按钮可见状态更新时的回调 */
    private void onNewGameButtonVisibleChanged(boolean visible) {
        mNewGameButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /** 游戏日志更新时的回调 */
    private void onGameLogChanged(List<Pair<Integer, Object[]>> gameLog) {
        mLogAdapter.setLog(gameLog);
        if (!gameLog.isEmpty())
            mLogView.smoothScrollToPosition(gameLog.size() - 1);
    }

    @Override
    protected void clickDice(int i) {
        if (mViewModel.isHumanTurn())
            mViewModel.toggleLocked(i);
    }

    /** 游戏结束时的回调函数 */
    private void onGameOver() {
        var score = mViewModel.createScoreEntity();
        mViewModel.saveScoreToDatabase(score);
    }

}
