package com.zzy.dicegames.ui.game.liarsdice;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;
import com.zzy.dicegames.ui.dice.DiceView;
import com.zzy.dicegames.ui.game.BaseGameFragment;

import java.util.List;

import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.zzy.dicegames.ui.game.liarsdice.LiarsDiceGameViewModel.*;

/**
 * 大话骰游戏Fragment
 *
 * @author 赵正阳
 */
public class LiarsDiceGameFragment extends BaseGameFragment<LiarsDiceGameViewModel> {
    /** 玩家胜负记录标签 */
    private TextView[] mPlayerRecordTextViews;

    /** 当前叫数标签 */
    private TextView mBidInfoTextView;

    /** 局数标签 */
    private TextView mRoundTextView;

    /** 数量选择按钮和标签 */
    private Button mDecQuantityButton, mIncQuantityButton;

    private TextView mQuantityTextView;

    /** 点数选择按钮和骰子 */
    private Button mDecFaceButton, mIncFaceButton;

    private DiceView mFaceDiceView;

    /** 斋复选框 */
    private CheckBox mZhaiCheckBox;

    /** 叫数预览标签 */
    private TextView mSelectedBidPreview;

    /** 叫数按钮 */
    private Button mBidButton;

    /** 开骰按钮 */
    private Button mChallengeButton;

    /** 人数按钮 */
    private Button mPlayersButton;

    /** 日志列表 */
    private RecyclerView mLogView;

    private LiarsDiceLogAdapter mLogAdapter;

    @Override
    public GameType getGameType() {
        return GameType.LIARS_DICE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liars_dice_game, container, false);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);

        // 大话骰中骰子在每轮开始时自动掷出，隐藏Roll按钮且骰子不可点击
        mRollButton.setVisibility(View.GONE);

        int[] playerRecordTextViewIds = {
                R.id.tvPlayerDice0, R.id.tvPlayerDice1, R.id.tvPlayerDice2, R.id.tvPlayerDice3};
        mPlayerRecordTextViews = new TextView[MAX_PLAYERS];
        for (int i = 0; i < mPlayerRecordTextViews.length; i++)
            mPlayerRecordTextViews[i] = view.findViewById(playerRecordTextViewIds[i]);

        mBidInfoTextView = view.findViewById(R.id.tvBidInfo);
        mRoundTextView = view.findViewById(R.id.tvRound);

        mDecQuantityButton = view.findViewById(R.id.btnDecQuantity);
        mIncQuantityButton = view.findViewById(R.id.btnIncQuantity);
        mQuantityTextView = view.findViewById(R.id.tvQuantity);

        mDecFaceButton = view.findViewById(R.id.btnDecFace);
        mIncFaceButton = view.findViewById(R.id.btnIncFace);
        mFaceDiceView = view.findViewById(R.id.diceFace);

        mZhaiCheckBox = view.findViewById(R.id.cbZhai);
        mSelectedBidPreview = view.findViewById(R.id.tvSelectedBidPreview);
        mBidButton = view.findViewById(R.id.btnBid);
        mChallengeButton = view.findViewById(R.id.btnChallenge);

        mPlayersButton = view.findViewById(R.id.btnPlayers);
        mPlayersButton.setOnClickListener(v -> selectNumPlayers());

        mDecQuantityButton.setOnClickListener(v -> changeSelectedQuantity(-1));
        mIncQuantityButton.setOnClickListener(v -> changeSelectedQuantity(1));
        mDecFaceButton.setOnClickListener(v -> changeSelectedFace(-1));
        mIncFaceButton.setOnClickListener(v -> changeSelectedFace(1));
        mZhaiCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> mViewModel.setSelectedZhai(isChecked));
        mBidButton.setOnClickListener(v -> mViewModel.makeBid(mViewModel.getSelectedBid()));
        mChallengeButton.setOnClickListener(v -> mViewModel.challenge());

        mLogView = view.findViewById(R.id.rvLog);
        mLogView.setLayoutManager(new LinearLayoutManager(getContext()));
        mLogAdapter = new LiarsDiceLogAdapter(getContext());
        mLogView.setAdapter(mLogAdapter);
    }

    @Override
    protected LiarsDiceGameViewModel createViewModel() {
        return new ViewModelProvider(this).get(LiarsDiceGameViewModel.class);
    }

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        super.setupObservers(owner);
        mViewModel.getCurrentPlayer().observe(owner, this::onCurrentPlayerChanged);
        mViewModel.getCurrentRound().observe(owner, this::onCurrentRoundChanged);
        mViewModel.getWinLossRecords().observe(owner, this::onWinLossRecordsChanged);
        mViewModel.getCurrentBid().observe(owner, this::onCurrentBidChanged);
        mViewModel.getSelectedQuantity().observe(owner, this::onSelectedQuantityChanged);
        mViewModel.getSelectedFace().observe(owner, this::onSelectedFaceChanged);
        mViewModel.getSelectedZhai().observe(owner, this::onSelectedZhaiChanged);
        mViewModel.getBidValid().observe(owner, this::onBidValidChanged);
        mViewModel.getBidControlsEnabled().observe(owner, this::onBidControlsEnabledChanged);
        mViewModel.getBidButtonEnabled().observe(owner, this::onBidButtonEnabledChanged);
        mViewModel.getChallengeButtonEnabled().observe(owner, this::onChallengeButtonEnabledChanged);
        mViewModel.getRevealResult().observe(owner, this::onRevealResultChanged);
        mViewModel.getGameLog().observe(owner, this::onGameLogChanged);
        mViewModel.getRanking().observe(owner, this::onRankingChanged);
    }

    @Override
    protected void onDiceNumbersChanged(int[] numbers) {
        for (int i = 0; i < mDiceViews.length; i++) {
            if (i < numbers.length && numbers[i] != 0) {
                mDiceViews[i].setNumber(numbers[i]);
                mDiceViews[i].setVisibility(View.VISIBLE);
            }
            else {
                mDiceViews[i].setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void clickDice(int i) {
        // 大话骰中骰子不可手动锁定，仅用于开骰时高亮
    }

    /** 当前玩家更新时的回调 */
    private void onCurrentPlayerChanged(int currentPlayer) {
        for (int i = 0; i < mPlayerRecordTextViews.length; i++)
            mPlayerRecordTextViews[i].setTextColor(i == currentPlayer ? Color.RED : Color.BLACK);
    }

    /** 当前局数更新时的回调 */
    private void onCurrentRoundChanged(int round) {
        mRoundTextView.setText(getString(R.string.roundIndicator, round, TOTAL_ROUNDS));
    }

    /** 玩家胜负记录更新时的回调 */
    private void onWinLossRecordsChanged(int[][] records) {
        for (int i = 0; i < mPlayerRecordTextViews.length; i++) {
            TextView textView = mPlayerRecordTextViews[i];
            if (i < records.length) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(getString(
                        R.string.winLossRecord, getPlayerName(i), records[i][0], records[i][1]));
                Integer currentPlayer = mViewModel.getCurrentPlayer().getValue();
                textView.setTextColor(currentPlayer != null && currentPlayer == i ? Color.RED : Color.BLACK);
            }
            else {
                textView.setVisibility(View.GONE);
            }
        }
        mPlayersButton.setText(getString(R.string.playersFormat, records.length));
    }

    /** 当前叫数更新时的回调 */
    private void onCurrentBidChanged(Bid bid) {
        if (bid == null)
            mBidInfoTextView.setText(R.string.noBid);
        else
            mBidInfoTextView.setText(getString(R.string.currentBidFormat, formatBid(bid)));
    }

    /** 选择的叫数数量更新时的回调 */
    private void onSelectedQuantityChanged(int quantity) {
        mQuantityTextView.setText(getString(R.string.quantityFormat, quantity));
        updateSelectedBidPreview();
    }

    /** 选择的叫数点数更新时的回调 */
    private void onSelectedFaceChanged(int face) {
        mFaceDiceView.setNumber(face);
        mZhaiCheckBox.setEnabled(face != 1);  // 叫1点默认斋，不可切换
        updateSelectedBidPreview();
    }

    /** 选择的叫数是否斋更新时的回调 */
    private void onSelectedZhaiChanged(boolean zhai) {
        mZhaiCheckBox.setChecked(zhai);
        updateSelectedBidPreview();
    }

    /** 叫数合法性更新时的回调 */
    private void onBidValidChanged(boolean valid) {
        updateSelectedBidPreview();
    }

    /** 叫数选择器激活状态更新时的回调 */
    private void onBidControlsEnabledChanged(boolean enabled) {
        mDecQuantityButton.setEnabled(enabled);
        mIncQuantityButton.setEnabled(enabled);
        mDecFaceButton.setEnabled(enabled);
        mIncFaceButton.setEnabled(enabled);
        // 叫1点时斋复选框保持禁用（默认斋）
        Integer face = mViewModel.getSelectedFace().getValue();
        mZhaiCheckBox.setEnabled(enabled && face != null && face != 1);
    }

    /** 叫数按钮激活状态更新时的回调 */
    private void onBidButtonEnabledChanged(boolean enabled) {
        mBidButton.setEnabled(enabled);
    }

    /** 开骰按钮激活状态更新时的回调 */
    private void onChallengeButtonEnabledChanged(boolean enabled) {
        mChallengeButton.setEnabled(enabled);
    }

    /** 游戏日志更新时的回调 */
    private void onGameLogChanged(List<Pair<Integer, Object[]>> gameLog) {
        mLogAdapter.setLog(gameLog);
        if (!gameLog.isEmpty())
            mLogView.smoothScrollToPosition(gameLog.size() - 1);
    }

    /** 开骰结果更新时的回调 */
    private void onRevealResultChanged(RevealResult result) {
        if (result != null)
            showRevealDialog(result);
    }

    /** 最终排名更新时的回调 */
    private void onRankingChanged(List<Integer> ranking) {
        if (ranking == null)
            return;
        int[][] records = mViewModel.getWinLossRecords().getValue();
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < ranking.size(); i++) {
            int p = ranking.get(i);
            message.append(getString(
                    R.string.rankingFormat, i + 1, getPlayerName(p), records[p][0], records[p][1]));
            if (i < ranking.size() - 1)
                message.append('\n');
        }
        showGameOverDialog(message.toString());
    }

    /** 更新叫数预览标签 */
    private void updateSelectedBidPreview() {
        mSelectedBidPreview.setText(formatBid(mViewModel.getSelectedBid()));
        Boolean valid = mViewModel.getBidValid().getValue();
        mSelectedBidPreview.setTextColor(valid != null && valid ? Color.BLACK : Color.RED);
    }

    /** 改变选择的叫数数量 */
    private void changeSelectedQuantity(int delta) {
        Integer quantity = mViewModel.getSelectedQuantity().getValue();
        if (quantity != null)
            mViewModel.setSelectedQuantity(quantity + delta);
    }

    /** 改变选择的叫数点数 */
    private void changeSelectedFace(int delta) {
        Integer face = mViewModel.getSelectedFace().getValue();
        if (face != null)
            mViewModel.setSelectedFace(face + delta);
    }

    /** 选择玩家数量 */
    private void selectNumPlayers() {
        String[] options = new String[MAX_PLAYERS - MIN_PLAYERS + 1];
        for (int i = 0; i < options.length; i++)
            options[i] = getString(R.string.numPlayersFormat, i + MIN_PLAYERS);
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.selectNumPlayers)
                .setItems(options, (dialog, which) -> mViewModel.newGame(which + MIN_PLAYERS))
                .show();
    }

    /** 显示开骰结果对话框 */
    private void showRevealDialog(RevealResult result) {
        View content = LayoutInflater.from(getContext())
                .inflate(R.layout.liars_dice_reveal_dialog, null);
        TextView messageTextView = content.findViewById(R.id.tvRevealMessage);
        messageTextView.setText(buildRevealMessage(result));

        LinearLayout container = content.findViewById(R.id.revealContainer);
        int[] diceViewIds = {R.id.dice1, R.id.dice2, R.id.dice3, R.id.dice4, R.id.dice5};
        for (int p = 0; p < result.numPlayers; p++) {
            View row = LayoutInflater.from(getContext())
                    .inflate(R.layout.liars_dice_reveal_player, container, false);
            TextView nameTextView = row.findViewById(R.id.tvPlayerName);
            nameTextView.setText(getPlayerName(p));

            for (int i = 0; i < diceViewIds.length; i++) {
                DiceView diceView = row.findViewById(diceViewIds[i]);
                int d = result.dice[p][i];
                diceView.setNumber(d);
                diceView.setLocked(shouldLock(d, result.bid.face, result.bid.zhai));
            }
            container.addView(row);
        }

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.revealTitle)
                .setView(content)
                .setPositiveButton(R.string.ok, (dialog, which) -> mViewModel.continueAfterReveal())
                .setCancelable(false)
                .show();
    }

    /** 构造开骰结果消息 */
    private String buildRevealMessage(RevealResult result) {
        String bidInfo = formatBid(result.bid);
        String actual = getString(R.string.revealActualCount, result.actualCount);
        String outcome = result.bidTrue
                ? getString(R.string.revealChallengerLoses, getPlayerName(result.challenger))
                : getString(R.string.revealBidderLoses, getPlayerName(result.loser));
        return getString(R.string.revealBidInfo, bidInfo) + "\n" + actual + "\n" + outcome;
    }

    /** 格式化叫数 */
    private String formatBid(Bid bid) {
        return bid.zhai
                ? getString(R.string.bidCallZhai, bid.quantity, bid.face)
                : getString(R.string.bidCallFei, bid.quantity, bid.face);
    }

    /** 返回玩家名称 */
    private String getPlayerName(int p) {
        return getString(playerNameResId(p));
    }
}
