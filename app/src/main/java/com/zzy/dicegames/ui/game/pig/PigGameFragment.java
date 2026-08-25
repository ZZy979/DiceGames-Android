package com.zzy.dicegames.ui.game.pig;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;
import com.zzy.dicegames.ui.game.BaseGameFragment;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import static com.zzy.dicegames.ui.game.pig.PigGameViewModel.*;

/**
 * Pig游戏Fragment
 */
public class PigGameFragment extends BaseGameFragment<PigGameViewModel> {
    /** 玩家名称标签 */
    private TextView[] mPlayerNameTextViews;

    /** 玩家得分标签 */
    private TextView[] mPlayerScoreTextViews;

    /** 本轮得分标签 */
    private TextView mTurnScoreTextView;

    /** “保存得分”按钮 */
    private Button mHoldButton;

    /** “新游戏”按钮 */
    private Button mNewGameButton;

    @Override
    public GameType getGameType() {
        return GameType.PIG;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pig_game, container, false);
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

        mHoldButton = view.findViewById(R.id.btnHold);
        mHoldButton.setOnClickListener(v -> mViewModel.hold());

        mNewGameButton = view.findViewById(R.id.btnNewGame);
        mNewGameButton.setOnClickListener(v -> startNewGame());
    }

    @Override
    protected PigGameViewModel createViewModel() {
        return new ViewModelProvider(this).get(PigGameViewModel.class);
    }

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        super.setupObservers(owner);
        mViewModel.getCurrentPlayer().observe(owner, this::onCurrentPlayerChanged);
        mViewModel.getPlayerScores().observe(owner, this::onPlayerScoresChanged);
        mViewModel.getTurnScore().observe(owner, this::onTurnScoreChanged);
        mViewModel.getHoldButtonEnabled().observe(owner, this::onHoldButtonEnabledChanged);
        mViewModel.getNewGameButtonVisible().observe(owner, this::onNewGameButtonVisibleChanged);
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
    private void onTurnScoreChanged(int turnScore) {
        mTurnScoreTextView.setText(Integer.toString(turnScore));
    }

    /** “保存得分”按钮激活状态更新时的回调 */
    private void onHoldButtonEnabledChanged(boolean enabled) {
        mHoldButton.setEnabled(enabled);
    }

    /** “新游戏”按钮可见状态更新时的回调 */
    private void onNewGameButtonVisibleChanged(boolean visible) {
        mNewGameButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void clickDice(int i) {
        // Pig游戏中骰子不可点击
    }
}
