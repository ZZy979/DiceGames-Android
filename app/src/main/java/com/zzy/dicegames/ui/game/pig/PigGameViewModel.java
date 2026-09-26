package com.zzy.dicegames.ui.game.pig;

import com.zzy.dicegames.data.entity.pig.PigScore;
import com.zzy.dicegames.ui.game.BaseGameViewModel;

import java.time.LocalDate;
import java.util.Optional;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Pig游戏ViewModel
 */
public class PigGameViewModel extends BaseGameViewModel {
    /** 骰子个数 */
    public static final int NUM_DICE = 1;

    /** 玩家数量：玩家和计算机各一位 */
    public static final int NUM_PLAYERS = 2;

    /** 胜利得分 */
    public static final int WINNING_SCORE = 100;

    /** 计算机玩家操作的延迟(ms) */
    private static final int DELAY = 500;

    /** 本轮得分 */
    private final MutableLiveData<Integer> turnScore = new MutableLiveData<>(0);

    /** “保存得分”按钮激活状态 */
    private final MutableLiveData<Boolean> holdButtonEnabled = new MutableLiveData<>(false);

    /** “新游戏”按钮可见状态 */
    private final MutableLiveData<Boolean> newGameButtonVisible = new MutableLiveData<>(false);

    public PigGameViewModel() {
        super(NUM_DICE, UNLIMITED_ROLLS, NUM_PLAYERS, NUM_PLAYERS);
    }

    public LiveData<Integer> getTurnScore() {
        return turnScore;
    }

    public LiveData<Boolean> getHoldButtonEnabled() {
        return holdButtonEnabled;
    }

    public MutableLiveData<Boolean> getNewGameButtonVisible() {
        return newGameButtonVisible;
    }

    @Override
    public void updateDiceNumbers(int... numbers) {
        super.updateDiceNumbers(numbers);

        int value = numbers[0];
        int currentTurnScore = Optional.ofNullable(turnScore.getValue()).orElse(0);
        if (value == 1) {
            // 掷出1点(Pig)，失去本轮得分
            pig();
        }
        else {
            turnScore.setValue(currentTurnScore + value);
            if (getCurrentPlayerScore() + currentTurnScore + value >= WINNING_SCORE)
                win();
            else if (isComputerTurn())
                handler.postDelayed(this::computerTurn, DELAY);
        }
    }

    /** 掷出1点，失去本轮得分 */
    protected void pig() {
        turnScore.setValue(0);
        rollButtonEnabled.setValue(false);
        holdButtonEnabled.setValue(false);
        handler.postDelayed(this::nextPlayer, DELAY);
    }

    /** 达到胜利得分 */
    protected void win() {
        addCurrentPlayerScore(Optional.ofNullable(turnScore.getValue()).orElse(0));
        gameOver();
    }

    /** 电脑玩家回合，决定保存得分还是继续掷骰子 */
    protected void computerTurn() {
        int turnTotal = Optional.ofNullable(turnScore.getValue()).orElse(0);
        if (computerShouldRoll(getPlayerScoreValue(PLAYER_HUMAN), getPlayerScoreValue(PLAYER_COMPUTER), turnTotal))
            handler.postDelayed(this::rollDiceWithAnimation, DELAY);
        else
            handler.postDelayed(this::hold, DELAY);
    }

    /**
     * 电脑玩家是否应继续掷骰子<br>
     * “Keep Pace and End Race”策略：若任一方得分 >= 71
     * 或本轮得分 <= 21 + round((对手得分 - 自己得分) / 8) 则继续掷骰子
     */
    protected boolean computerShouldRoll(int opponentScore, int selfScore, int turnTotal) {
        if (selfScore + turnTotal >= WINNING_SCORE)
            return false;
        return opponentScore >= 71 || selfScore >= 71
                || turnTotal < 21 + Math.round((opponentScore - selfScore) / 8.0);
    }

    /** 保存本轮得分 */
    public void hold() {
        addCurrentPlayerScore(Optional.ofNullable(turnScore.getValue()).orElse(0));
        nextPlayer();
    }

    /** 结束本轮，切换玩家 */
    @Override
    protected void nextPlayer() {
        super.nextPlayer();
        turnScore.setValue(0);
        resetDiceWindow();

        if (isComputerTurn())
            handler.postDelayed(this::rollDiceWithAnimation, DELAY);
    }

    /** 游戏结束 */
    public void gameOver() {
        rollButtonEnabled.setValue(false);
        holdButtonEnabled.setValue(false);
        newGameButtonVisible.setValue(true);
        var score = createScoreEntity();
        saveScoreToDatabase(score);
    }

    /** 创建得分实体 */
    public PigScore createScoreEntity() {
        return new PigScore(LocalDate.now().toString(),
                getPlayerScoreValue(PLAYER_HUMAN), getPlayerScoreValue(PLAYER_COMPUTER));
    }

    /** 将得分保存到数据库 */
    public void saveScoreToDatabase(PigScore score) {
        var dao = scoreDatabase.pigScoreDao();
        dao.insert(score);
    }

    @Override
    protected void updateDiceWindowEnabled() {
        boolean isHuman = isHumanTurn();
        rollButtonEnabled.setValue(isHuman && !diceRolling);
        holdButtonEnabled.setValue(isHuman && !diceRolling && hasRolled());
    }

    @Override
    public void reset() {
        super.reset();
        turnScore.setValue(0);
        newGameButtonVisible.setValue(false);
    }
}
