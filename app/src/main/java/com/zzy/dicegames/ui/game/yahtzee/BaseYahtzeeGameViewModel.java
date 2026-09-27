package com.zzy.dicegames.ui.game.yahtzee;

import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.ui.game.BaseGameViewModel;
import com.zzy.dicegames.utils.ArrayUtil;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public abstract class BaseYahtzeeGameViewModel extends BaseGameViewModel {
    /** 上区得分项个数 */
    public static final int NUM_UPPER_CATEGORIES = 6;

    /** 得分项个数 */
    protected final int numCategories;

    /** 上区总分达到多少时获得奖励分 */
    protected final int bonusThreshold;

    /** 奖励分值 */
    protected final int bonusValue;

    /** 当前玩家的得分项是否可点击 */
    protected final MutableLiveData<Boolean> clickable = new MutableLiveData<>(false);

    /**
     * 记分板类游戏的玩家数据<br>
     * 每个玩家有自己的得分项得分、已选择状态、上区总分和奖励分，游戏总分为基类的score
     */
    public static class YahtzeeGameData extends BaseGameData {
        /** 每个得分项的得分（未选择的为预估得分） */
        public final MutableLiveData<int[]> scores;

        /** 每个得分项是否已选择 */
        public final MutableLiveData<boolean[]> selected;

        /** 已选择得分项个数 */
        public int numSelected = 0;

        /** 获得的上区总分 */
        public final MutableLiveData<Integer> upperTotalScore = new MutableLiveData<>(0);

        /** 获得的奖励分 */
        public final MutableLiveData<Integer> bonusScore = new MutableLiveData<>(0);

        public YahtzeeGameData(int numCategories) {
            scores = new MutableLiveData<>(new int[numCategories]);
            selected = new MutableLiveData<>(new boolean[numCategories]);
        }

        @Override
        public void reset() {
            super.reset();
            int numCategories = scores.getValue() == null ? 0 : scores.getValue().length;
            scores.setValue(new int[numCategories]);
            selected.setValue(new boolean[numCategories]);
            numSelected = 0;
            upperTotalScore.setValue(0);
            bonusScore.setValue(0);
        }
    }

    protected BaseYahtzeeGameViewModel(
            int numDice, int maxRolls, int minPlayers, int maxPlayers,
            int numCategories, int bonusThreshold, int bonusValue) {
        super(numDice, maxRolls, minPlayers, maxPlayers);
        this.numCategories = numCategories;
        this.bonusThreshold = bonusThreshold;
        this.bonusValue = bonusValue;
        initGameData(minPlayers);
        disableAllDice();
    }

    @Override
    protected BaseGameData createGameData() {
        return new YahtzeeGameData(numCategories);
    }

    /** 返回指定玩家的数据 */
    public YahtzeeGameData data(int player) {
        return (YahtzeeGameData) gameData[player];
    }

    public int getNumCategories() {
        return numCategories;
    }

    public int getBonusThreshold() {
        return bonusThreshold;
    }

    public int getBonusValue() {
        return bonusValue;
    }

    public LiveData<Boolean> getClickable() {
        return clickable;
    }

    public boolean isClickable() {
        return Boolean.TRUE.equals(clickable.getValue());
    }

    @Override
    public void updateDiceNumbers(int... numbers) {
        super.updateDiceNumbers(numbers);
        updatePreviewScores();
        if (isComputerTurn() && !diceRolling)
            postComputerAction(this::computerTurn);
    }

    /** 根据骰子点数更新当前玩家的预估得分 */
    protected void updatePreviewScores() {
        YahtzeeGameData currentData = data(getCurrentPlayerValue());
        boolean[] isSelected = currentData.selected.getValue();
        int[] currentScores = currentData.scores.getValue();
        if (isSelected == null || currentScores == null)
            return;

        for (int i = 0; i < currentScores.length; i++) {
            if (!isSelected[i])
                currentScores[i] = calculateScore(i);
        }
        currentData.scores.setValue(currentScores);
    }

    /** 根据当前骰子点数计算指定得分项的得分 */
    public abstract int calculateScore(int category);

    /** 是否所有骰子点数都相同 */
    protected boolean isAllSame() {
        int[] numbers = diceNumbers.getValue();
        return numbers != null && ArrayUtil.all(numbers, numbers[0]);
    }

    /** 当前玩家是否满足Joker规则：满足Yahtzee，且Yahtzee和上区对应的数字已经选过 */
    protected boolean isJoker() {
        int[] numbers = diceNumbers.getValue();
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        if (numbers == null || isSelected == null)
            return false;

        return isAllSame() && isSelected[numbers[0] - 1] && isSelected[numCategories - 1];
    }

    @Override
    protected void updateDiceWindowEnabled() {
        boolean humanTurn = isHumanTurn();
        rollButtonEnabled.setValue(humanTurn && !diceRolling && hasRemainingRolls());
        setAllDiceEnabled(humanTurn && !diceRolling && hasRemainingRolls() && hasRolled());
        clickable.setValue(humanTurn && !diceRolling && hasRolled());
    }

    /** 选择指定的得分项，更新得分（供玩家点击） */
    public void select(int category) {
        if (!isHumanTurn())
            return;
        doSelect(category);
    }

    /** 选择当前玩家的指定得分项，然后轮到下一位玩家（可由玩家或计算机调用） */
    protected void doSelect(int category) {
        YahtzeeGameData currentData = data(getCurrentPlayerValue());
        boolean[] currentSelected = currentData.selected.getValue();
        if (currentSelected == null || currentSelected[category])
            return;

        currentSelected[category] = true;
        currentData.selected.setValue(currentSelected);
        currentData.numSelected++;

        updateScores(currentData, category);

        if (isAllPlayersFinished()) {
            gameOver();
            return;
        }
        nextPlayer();
        startTurn();
    }

    /** 开始当前玩家的回合 */
    protected void startTurn() {
        resetDiceWindow();
        if (isComputerTurn())
            postComputerAction(this::computerTurn);
    }

    /** 计算机玩家回合：第一次先掷骰子，之后根据策略决定继续掷骰子（并锁定要保留的骰子）或选择得分项 */
    protected void computerTurn() {
        if (!isComputerTurn() || diceRolling)
            return;

        // 第一次投掷，无需保留骰子
        if (!hasRolled()) {
            rollDiceWithAnimation();
            return;
        }

        // 投掷结束或触发Joker，选择得分项
        if (!hasRemainingRolls() || isJoker()) {
            int category = computerChooseCategory();
            if (category >= 0)
                doSelect(category);
            return;
        }

        // 还有剩余投掷次数，先分析该保留哪些骰子
        analyzeDiceToKeep();
    }

    /**
     * 计算机玩家分析该保留哪些骰子<br>
     * 调用{@link #doSelect(int)}直接选择得分项，
     * 或调用{@link #keepDiceAndRoll(boolean[])}锁定要保留的骰子后继续掷骰子
     */
    // TODO 标记为abstract
    protected void analyzeDiceToKeep() {}

    /** 锁定要保留的骰子并掷骰子 */
    protected void keepDiceAndRoll(boolean[] keep) {
        setDiceLocked(keep);
        postComputerAction(this::rollDiceWithAnimation);
    }

    /** 计算机玩家选择一个未选择的得分项 */
    // TODO 标记为abstract
    protected int computerChooseCategory() { return -1; }

    /** 是否所有玩家都已选完得分项 */
    protected boolean isAllPlayersFinished() {
        for (BaseGameData playerData : gameData) {
            if (((YahtzeeGameData) playerData).numSelected < numCategories)
                return false;
        }
        return true;
    }

    /** 更新指定玩家刚选择的得分项的得分、上区总分、奖励分和游戏总分 */
    private void updateScores(YahtzeeGameData playerData, int category) {
        int[] currentScores = playerData.scores.getValue();
        boolean[] isSelected = playerData.selected.getValue();
        if (currentScores == null || isSelected == null)
            return;

        // 总是重新计算刚选择的得分项：掷骰子后计算的只是预估得分，
        // 且计算机玩家在界面上不显示预估得分，需在此时更新scores以显示该得分项的得分
        currentScores[category] = calculateScore(category);
        playerData.scores.setValue(currentScores);

        int upperTotal = 0;
        for (int i = 0; i < NUM_UPPER_CATEGORIES; i++) {
            if (isSelected[i])
                upperTotal += currentScores[i];
        }

        int bonus = upperTotal >= bonusThreshold ? bonusValue : 0;
        int total = upperTotal + bonus;
        for (int i = NUM_UPPER_CATEGORIES; i < numCategories; i++) {
            if (isSelected[i])
                total += currentScores[i];
        }

        playerData.upperTotalScore.setValue(upperTotal);
        playerData.bonusScore.setValue(bonus);
        playerData.score.setValue(total);
    }

    /** 游戏结束 */
    public void gameOver() {
        disableAllDice();
        rollButtonEnabled.setValue(false);
        var score = createScoreEntity();
        int rank = saveScoreToDatabase(score);
        if (gameOverAction != null)
            gameOverAction.accept(new Object[] {score, rank});
    }

    /** 创建得分实体 */
    public abstract BaseScore createScoreEntity();

    /** 将得分保存到数据库，并返回排名 */
    public abstract int saveScoreToDatabase(BaseScore score);

    @Override
    public void reset() {
        clickable.setValue(false);
        super.reset();
        startTurn();
    }
}
