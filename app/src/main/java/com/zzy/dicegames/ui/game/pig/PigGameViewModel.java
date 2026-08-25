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

    /** 玩家数量 */
    public static final int NUM_PLAYERS = 2;

    // 玩家编号
    public static final int PLAYER_HUMAN = 0;
    public static final int PLAYER_COMPUTER = 1;

    /** 胜利得分 */
    public static final int WINNING_SCORE = 100;

    /** 计算机玩家操作的延迟(ms) */
    private static final int DELAY = 500;

    /** 当前玩家 */
    private final MutableLiveData<Integer> currentPlayer = new MutableLiveData<>(PLAYER_HUMAN);

    /** 玩家得分 */
    private final MutableLiveData<int[]> playerScores = new MutableLiveData<>(new int[NUM_PLAYERS]);

    /** 本轮得分 */
    private final MutableLiveData<Integer> turnScore = new MutableLiveData<>(0);

    /** “保存得分”按钮激活状态 */
    private final MutableLiveData<Boolean> holdButtonEnabled = new MutableLiveData<>(false);

    /** “新游戏”按钮可见状态 */
    private final MutableLiveData<Boolean> newGameButtonVisible = new MutableLiveData<>(false);

    public PigGameViewModel() {
        super(NUM_DICE, UNLIMITED_ROLLS);
    }

    public LiveData<Integer> getCurrentPlayer() {
        return currentPlayer;
    }

    public LiveData<int[]> getPlayerScores() {
        return playerScores;
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

    public boolean isHumanTurn() {
        return currentPlayer.getValue() != null && currentPlayer.getValue() == PLAYER_HUMAN;
    }

    public boolean isComputerTurn() {
        return currentPlayer.getValue() != null && currentPlayer.getValue() == PLAYER_COMPUTER;
    }

    public int getCurrentPlayerScore() {
        int[] scores = playerScores.getValue();
        Integer player = currentPlayer.getValue();
        if (scores == null || player == null)
            return 0;
        return scores[player];
    }

    protected void addCurrentPlayerScore(int score) {
        int[] scores = playerScores.getValue();
        Integer player = currentPlayer.getValue();
        if (scores == null || player == null)
            return;

        scores[player] += score;
        playerScores.setValue(scores);
    }

    @Override
    public void rollDice() {
        rollButtonEnabled.setValue(false);
        super.rollDice();
    }

    @Override
    public void rollDiceWithAnimation() {
        rollButtonEnabled.setValue(false);
        super.rollDiceWithAnimation();
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
            else if (isComputerTurn()) {
                holdButtonEnabled.setValue(false);
                handler.postDelayed(this::computerTurn, DELAY);
            }
            else {
                holdButtonEnabled.setValue(true);
                rollButtonEnabled.setValue(true);
            }
        }
    }

    /** 掷出1点，失去本轮得分 */
    protected void pig() {
        turnScore.setValue(0);
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
        int[] scores = playerScores.getValue();
        if (scores == null)
            return;
        int turnTotal = Optional.ofNullable(turnScore.getValue()).orElse(0);
        if (computerShouldRoll(scores[PLAYER_HUMAN], scores[PLAYER_COMPUTER], turnTotal))
            handler.postDelayed(this::rollDiceWithAnimation, DELAY);
        else
            handler.postDelayed(this::hold, DELAY);
    }

    /**
     * 电脑玩家是否应继续掷骰子<br>
     * “Keep Pace and End Race”策略：若任一方得分 >= 71
     * 或本轮得分未 <= 21 + round((对手得分 - 自己得分) / 8) 则继续掷骰子
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
    protected void nextPlayer() {
        Integer player = currentPlayer.getValue();
        if (player == null)
            return;

        currentPlayer.setValue((player + 1) % NUM_PLAYERS);
        turnScore.setValue(0);
        holdButtonEnabled.setValue(false);
        rollButtonEnabled.setValue(isHumanTurn());

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
        int[] scores = playerScores.getValue();
        if (scores == null)
            return null;
        return new PigScore(LocalDate.now().toString(), scores[PLAYER_HUMAN], scores[PLAYER_COMPUTER]);
    }

    /** 将得分保存到数据库 */
    public void saveScoreToDatabase(PigScore score) {
        var dao = scoreDatabase.pigScoreDao();
        dao.insert(score);
    }

    @Override
    public void resetDiceWindow() {
        super.resetDiceWindow();
        rollButtonEnabled.setValue(isHumanTurn());
    }

    @Override
    public void reset() {
        currentPlayer.setValue(PLAYER_HUMAN);
        super.reset();
        playerScores.setValue(new int[NUM_PLAYERS]);
        turnScore.setValue(0);
        holdButtonEnabled.setValue(false);
        newGameButtonVisible.setValue(false);
    }
}
