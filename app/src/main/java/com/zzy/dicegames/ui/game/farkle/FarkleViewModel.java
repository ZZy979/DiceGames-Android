package com.zzy.dicegames.ui.game.farkle;

import com.zzy.dicegames.R;
import com.zzy.dicegames.ui.game.BaseGameViewModel;
import com.zzy.dicegames.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class FarkleViewModel extends BaseGameViewModel {
    /** 骰子个数 */
    public static final int NUM_DICE = 6;

    /** 玩家数量 */
    public static final int NUM_PLAYERS = 2;

    // 玩家编号
    public static final int PLAYER_HUMAN = 0;
    public static final int PLAYER_COMPUTER = 1;

    /** 胜利得分 */
    public static final int WINNING_SCORE = 10000;

    /** 计算机玩家操作的延迟(ms) */
    private static final int DELAY = 1000;

    // 骰子激活状态：enabled=false表示本轮已保留（对应locked一定为true）

    /** 本次所掷的骰子(enabled=true)点数，其他骰子用0表示 */
    private final int[] rolledDiceNumbers = new int[NUM_DICE];

    /** 本次所掷且锁定的骰子(enabled=true, locked=true)点数，其他骰子用0表示 */
    private final int[] lockedRolledDiceNumbers = new int[NUM_DICE];

    /** 当前玩家 */
    private final MutableLiveData<Integer> currentPlayer = new MutableLiveData<>(PLAYER_HUMAN);

    /** 玩家得分 */
    private final MutableLiveData<int[]> playerScores = new MutableLiveData<>(new int[NUM_PLAYERS]);

    /** 本轮已保留的得分的骰子个数 */
    private int numKeptScoringDice = 0;

    /** 本轮已积累的得分 */
    private int accumulatedTurnScore = 0;

    /** 本轮预估得分（已积累+锁定的骰子） */
    private final MutableLiveData<Integer> estimatedTurnScore = new MutableLiveData<>(0);

    /** “保存得分”按钮激活状态 */
    private final MutableLiveData<Boolean> bankButtonEnabled = new MutableLiveData<>(false);

    /** 游戏日志 */
    private final MutableLiveData<List<Pair<Integer, Object[]>>> gameLog = new MutableLiveData<>(new ArrayList<>());

    public FarkleViewModel() {
        super(NUM_DICE, UNLIMITED_ROLLS);
        disableAllDice();
    }

    public LiveData<Integer> getCurrentPlayer() {
        return currentPlayer;
    }

    public LiveData<int[]> getPlayerScores() {
        return playerScores;
    }

    public LiveData<Integer> getEstimatedTurnScore() {
        return estimatedTurnScore;
    }

    public LiveData<Boolean> getBankButtonEnabled() {
        return bankButtonEnabled;
    }

    public LiveData<List<Pair<Integer, Object[]>>> getGameLog() {
        return gameLog;
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

    public int numLockedDice() {
        return ArrayUtils.count(diceLocked.getValue(), true);
    }

    public boolean isAllDiceLocked() {
        return ArrayUtils.all(diceLocked.getValue(), true);
    }

    public boolean isAllDiceDisabled() {
        return ArrayUtils.all(diceEnabled.getValue(), false);
    }

    /**
     * 添加一条日志
     *
     * @param resId 字符串资源id
     * @param args 格式化参数
     */
    protected void addLog(int resId, Object... args) {
        var log = gameLog.getValue();
        if (log == null)
            return;

        log.add(Pair.create(resId, args));
        gameLog.setValue(log);
    }

    /**
     * 添加一条包含骰子点数的日志
     *
     * @param resId 字符串资源id
     * @param numbers 骰子点数
     */
    protected void addDiceNumbersLog(int resId, int[] numbers) {
        addLog(resId, ArrayUtils.join(ArrayUtils.filter(numbers, x -> x != 0), ","));
    }

    /** 禁用本轮已保留的骰子 */
    private void disableLockedDice() {
        boolean[] locked = diceLocked.getValue();
        boolean[] enabled = diceEnabled.getValue();
        if (locked == null || enabled == null)
            return;

        for (int i = 0; i < enabled.length; i++)
            enabled[i] = !locked[i];
        diceEnabled.setValue(enabled);
    }

    @Override
    public void toggleLocked(int i) {
        super.toggleLocked(i);
        updateLockedRolledDiceNumbers();

        // 更新本轮预估得分
        Pair<Integer, Integer> result = calculateScore(lockedRolledDiceNumbers);
        estimatedTurnScore.setValue(accumulatedTurnScore + result.first);

        boolean hasScoringDice = result.second > 0;
        rollButtonEnabled.setValue(isHumanTurn() && hasScoringDice && !isAllDiceLocked());
    }

    private void updateLockedRolledDiceNumbers() {
        boolean[] locked = diceLocked.getValue();
        if (locked == null)
            return;

        for (int i = 0; i < lockedRolledDiceNumbers.length; i++)
            lockedRolledDiceNumbers[i] = locked[i] ? rolledDiceNumbers[i] : 0;
    }

    protected void beforeRollDice() {
        if (isAllDiceDisabled()) {
            // 本轮第一次掷骰子或发生了Hot Dice
            enableAllDice();
        }
        else {
            addDiceNumbersLog(R.string.logDiceKept, lockedRolledDiceNumbers);
            numKeptScoringDice += calculateScore(lockedRolledDiceNumbers).second;
            disableLockedDice();
        }
    }

    @Override
    public void rollDice() {
        beforeRollDice();
        super.rollDice();
    }

    @Override
    public void rollDiceWithAnimation() {
        beforeRollDice();
        super.rollDiceWithAnimation();
    }

    private void updateRolledNumbers() {
        int[] numbers = diceNumbers.getValue();
        boolean[] enabled = diceEnabled.getValue();
        if (numbers == null || enabled == null)
            return;

        for (int i = 0; i < rolledDiceNumbers.length; i++)
            rolledDiceNumbers[i] = enabled[i] ? numbers[i] : 0;
    }

    @Override
    public void updateDiceNumbers(int... numbers) {
        super.updateDiceNumbers(numbers);
        updateRolledNumbers();
        addDiceNumbersLog(R.string.logDiceRolled, rolledDiceNumbers);

        Pair<Integer, Integer> result = calculateScore(rolledDiceNumbers);
        if (result.second == 0) {
            farkle();
        }
        else {
            accumulatedTurnScore = Optional.ofNullable(estimatedTurnScore.getValue()).orElse(0);
            if (getCurrentPlayerScore() + accumulatedTurnScore + result.first >= WINNING_SCORE)
                win(result.first);
            else if (numKeptScoringDice + result.second == NUM_DICE)
                hotDice(result.first);
            else {
                bankButtonEnabled.setValue(isHumanTurn());
                if (isComputerTurn())
                    handler.postDelayed(this::computerTurn, DELAY);
            }
        }
    }

    /**
     * 计算最大可能的得分和得分的骰子个数
     *
     * @param diceNumbers 骰子点数，点数为0的不参与计算
     * @return 得分和得分的骰子个数
     */
    public Pair<Integer, Integer> calculateScore(int... diceNumbers) {
        prepareCalculateScore(diceNumbers);

        // 连顺
        if (hasStraight())
            return Pair.create(1500, 6);

        // 3对
        if (hasThreePairs())
            return Pair.create(750, 6);

        int score = 0, numScoringDice = 0, n = 0;

        // 3个及以上同点
        for (int i = 1; i <= 6; ++i) {
            if ((n = diceCounts[i]) >= 3) {
                score += (n - 2) * (i == 1 ? 1000 : i * 100);
                numScoringDice += n;
            }
        }

        // 单个1点和5点
        if ((n = diceCounts[1]) <= 2) {
            score += n * 100;
            numScoringDice += n;
        }
        if ((n = diceCounts[5]) <= 2) {
            score += n * 50;
            numScoringDice += n;
        }

        return Pair.create(score, numScoringDice);
    }

    /** 返回得分的骰子点数/下标 */
    public int[] getScoringDice(boolean getIndex, int... diceNumbers) {
        prepareCalculateScore(diceNumbers);
        if (hasStraight() || hasThreePairs())
            return getIndex ? new int[] {0, 1, 2, 3, 4, 5} : diceNumbers;

        int[] indices = new int[diceNumbers.length];
        int numScoringDice = 0;
        for (int i = 0; i < diceNumbers.length; i++) {
            int d = diceNumbers[i], n = diceCounts[d];
            if (d == 1 || d == 5 || (d > 0 && n >= 3))
                indices[numScoringDice++] = getIndex ? i : d;
        }
        return Arrays.copyOf(indices, numScoringDice);
    }

    /** 是否满足连顺 */
    private boolean hasStraight() {
        for (int i = 1; i <= 6; i++) {
            if (diceCounts[i] == 0)
                return false;
        }
        return true;
    }

    /** 是否满足3对 */
    private boolean hasThreePairs() {
        int pairs = 0;
        for (int i = 1; i <= 6; i++) {
            if (diceCounts[i] == 2)
                pairs++;
        }
        return pairs == 3;
    }

    protected void farkle() {
        addLog(R.string.logFarkle);
        disableAllDice();
        handler.postDelayed(this::nextPlayer, DELAY);
    }

    protected void win(int lastRollScore) {
        addLog(isHumanTurn() ? R.string.logYouWin : R.string.logComputerWins);
        accumulatedTurnScore += lastRollScore;
        estimatedTurnScore.setValue(accumulatedTurnScore);
        addCurrentPlayerScore(accumulatedTurnScore);

        disableAllDice();
        bankButtonEnabled.setValue(false);
        rollButtonEnabled.setValue(false);
        // mNewGameButton.setVisibility(View.VISIBLE);
        // gameOver
    }

    protected void hotDice(int lastRollScore) {
        addLog(R.string.logHotDice, lastRollScore);
        accumulatedTurnScore += lastRollScore;
        estimatedTurnScore.setValue(accumulatedTurnScore);
        numKeptScoringDice = 0;
        bankButtonEnabled.setValue(false);
        resetDiceWindow();

        if (isComputerTurn())
            handler.postDelayed(this::rollDiceWithAnimation, DELAY);
    }

    /** 电脑玩家回合，决定保存得分还是继续掷骰子 */
    protected void computerTurn() {
        // 锁定所有得分的骰子
        for (int i : getScoringDice(true, rolledDiceNumbers))
            toggleLocked(i);

        // 决策继续还是结束
        int numLocked = numLockedDice();
        int score = Optional.ofNullable(estimatedTurnScore.getValue()).orElse(0);
        if (computerShouldRollAgain(numLocked, score))
            handler.postDelayed(this::rollDiceWithAnimation, DELAY);
        else
            handler.postDelayed(this::bank, DELAY);
    }

    private boolean computerShouldRollAgain(int numLocked, int score) {
        if (score >= 500)
            return false;
        else if (numLocked <= 3 || score <= 300)
            return true;
        else
            return random.nextDouble() > 0.5;
    }

    /** 保存本轮已积累的得分 */
    public void bank() {
        // 最后一次掷骰子的得分
        Pair<Integer, Integer> result = calculateScore(rolledDiceNumbers);
        addDiceNumbersLog(R.string.logDiceKept, getScoringDice(false, rolledDiceNumbers));
        int finalTurnScore = accumulatedTurnScore + result.first;
        addCurrentPlayerScore(finalTurnScore);
        addLog(R.string.logFinishTurn, finalTurnScore);
        nextPlayer();
    }

    /** 结束本轮，切换玩家 */
    protected void nextPlayer() {
        Integer player = currentPlayer.getValue();
        if (player == null)
            return;

        currentPlayer.setValue((player + 1) % NUM_PLAYERS);
        numKeptScoringDice = 0;
        accumulatedTurnScore = 0;
        estimatedTurnScore.setValue(0);
        bankButtonEnabled.setValue(false);
        resetDiceWindow();

        addLog(R.string.logSeparator);
        addLog(isHumanTurn() ? R.string.logYourTurn : R.string.logComputerTurn);
        addLog(R.string.logStartingScore, getCurrentPlayerScore());

        if (isComputerTurn())
            handler.postDelayed(this::rollDiceWithAnimation, DELAY);
    }

    @Override
    public void resetDiceWindow() {
        unlockAllDice();
        disableAllDice();
        rollButtonEnabled.setValue(isHumanTurn());
    }

    @Override
    public void reset() {
        currentPlayer.setValue(PLAYER_HUMAN);
        super.reset();
        playerScores.setValue(new int[NUM_PLAYERS]);
        numKeptScoringDice = 0;
        accumulatedTurnScore = 0;
        estimatedTurnScore.setValue(0);
        bankButtonEnabled.setValue(false);
        gameLog.setValue(new ArrayList<>());
    }
}
