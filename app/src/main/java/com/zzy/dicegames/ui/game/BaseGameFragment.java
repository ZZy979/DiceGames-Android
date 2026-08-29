package com.zzy.dicegames.ui.game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;
import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.ui.dice.DiceView;
import com.zzy.dicegames.ui.game.balut.BalutGameFragment;
import com.zzy.dicegames.ui.game.crag.CragGameFragment;
import com.zzy.dicegames.ui.game.farkle.FarkleGameFragment;
import com.zzy.dicegames.ui.game.liarsdice.LiarsDiceGameFragment;
import com.zzy.dicegames.ui.game.pig.PigGameFragment;
import com.zzy.dicegames.ui.game.rolladice.RollADiceGameFragment;
import com.zzy.dicegames.ui.game.yahtzee.YahtzeeGameFragment;
import com.zzy.dicegames.ui.game.yatzy.MaxiYatzyGameFragment;
import com.zzy.dicegames.ui.game.yatzy.YatzyGameFragment;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import static com.zzy.dicegames.ui.game.BaseGameViewModel.*;

/**
 * 游戏Fragment基类
 *
 * @param <V> ViewModel类
 * @author 赵正阳
 */
public abstract class BaseGameFragment<V extends BaseGameViewModel> extends Fragment {
    /** 标题标签*/
    protected TextView mTitleTextView;

    /** 骰子 */
    protected DiceView[] mDiceViews;

    /** Roll按钮 */
    protected Button mRollButton;

    /** 本回合是否已掷过骰子 */
    protected boolean mRolled;

    protected V mViewModel;

    /** 根据游戏类型创建游戏Fragment */
    public static BaseGameFragment<?> createByGameType(GameType gameType) {
        return switch (gameType) {
            case YAHTZEE -> new YahtzeeGameFragment();
            case YATZY -> new YatzyGameFragment();
            case MAXI_YATZY -> new MaxiYatzyGameFragment();
            case BALUT -> new BalutGameFragment();
            case LIARS_DICE -> new LiarsDiceGameFragment();
            case ROLL_A_DICE -> new RollADiceGameFragment();
            case FARKLE -> new FarkleGameFragment();
            case PIG -> new PigGameFragment();
            case CRAG -> new CragGameFragment();
        };
    }

    /** 返回游戏类型 */
    public abstract GameType getGameType();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = createViewModel();
        mViewModel.setScoreDatabase(ScoreDatabase.getInstance(getContext()));
        mViewModel.setGameOverAction(this::onGameOver);

        initViews(view);
        setupObservers(getViewLifecycleOwner());
    }

    /** 初始化视图 */
    protected void initViews(View view) {
        mTitleTextView = view.findViewById(R.id.tvTitle);

        int[] diceViewIds = {R.id.dice1, R.id.dice2, R.id.dice3, R.id.dice4, R.id.dice5, R.id.dice6};
        int numDice = mViewModel.getNumDice();
        mDiceViews = new DiceView[numDice];
        for (int i = 0; i < diceViewIds.length; i++) {
            final int position = i;
            DiceView diceView = view.findViewById(diceViewIds[i]);
            if (i < numDice) {
                mDiceViews[i] = diceView;
                mDiceViews[i].setOnClickListener(v -> clickDice(position));
                mDiceViews[i].setVisibility(View.VISIBLE);
            } else if (diceView != null) {
                diceView.setVisibility(View.GONE);
            }
        }

        mRollButton = view.findViewById(R.id.btnRoll);
        mRollButton.setOnClickListener(v -> rollDice());
    }

    /** 创建游戏ViewModel */
    protected abstract V createViewModel();

    /** 设置ViewModel观察者 */
    protected void setupObservers(LifecycleOwner owner) {
        mViewModel.getRemainingRolls().observe(owner, this::onRemainingRollsChanged);
        mViewModel.getDiceNumbers().observe(owner, this::onDiceNumbersChanged);
        mViewModel.getDiceLocked().observe(owner, this::onDiceLockedChanged);
        mViewModel.getDiceEnabled().observe(owner, this::onDiceEnabledChanged);
        mViewModel.getRollButtonEnabled().observe(owner, this::onRollButtonEnabledChanged);
        mViewModel.getDiceRolled().observe(owner, this::onDiceRolledChanged);
    }

    /** 剩余掷骰子次数更新时的回调 */
    protected void onRemainingRollsChanged(int remaining) {
        if (remaining == UNLIMITED_ROLLS)
            mRollButton.setText(getString(R.string.roll));
        else
            mRollButton.setText(getString(R.string.rollRemaining, remaining));
    }

    /** 骰子点数更新时的回调 */
    protected void onDiceNumbersChanged(int[] numbers) {
        for (int i = 0; i < numbers.length; i++)
            mDiceViews[i].setNumber(numbers[i]);
    }

    /** 骰子锁定状态更新时的回调 */
    protected void onDiceLockedChanged(boolean[] locked) {
        for (int i = 0; i < locked.length; i++)
            mDiceViews[i].setLocked(locked[i]);
    }

    /** 骰子激活状态更新时的回调 */
    protected void onDiceEnabledChanged(boolean[] enabled) {
        for (int i = 0; i < enabled.length; i++)
            mDiceViews[i].setEnabled(enabled[i]);
    }

    /** Roll按钮激活状态更新时的回调 */
    protected void onRollButtonEnabledChanged(boolean enabled) {
        mRollButton.setEnabled(enabled);
    }

    /** 本回合是否已掷过骰子更新时的回调 */
    protected void onDiceRolledChanged(boolean rolled) {
        mRolled = rolled;
    }

    /** 点击第i个骰子 */
    protected void clickDice(int i) {
        mViewModel.toggleLocked(i);
    }

    /** 掷骰子 */
    protected void rollDice() {
        mViewModel.rollDiceWithAnimation();
    }

    /** 开始一次新游戏 */
    public void startNewGame() {
        mViewModel.reset();
    }

    /** 游戏结束时的回调函数 */
    protected void onGameOver(Object[] args) {}

    /** 弹出窗口，显示得分 */
    public void showScore(BaseScore score, int rank) {
        showGameOverDialog(getScoreMessage(score, rank));
    }

    public String getScoreMessage(BaseScore score, int rank) {
        StringBuilder message = new StringBuilder();
        if (rank == 1)
            message.append(getString(R.string.newHighScore)).append('\n');
        else if (rank >= 2 && rank <= 10)
            message.append(getString(R.string.rankN, rank)).append('\n');
        message.append(String.format("%s %d", getString(R.string.score), score.score));
        return message.toString();
    }

    public void showGameOverDialog(String message) {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.gameOver))
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog, which) -> startNewGame())
                .show();
    }
}
