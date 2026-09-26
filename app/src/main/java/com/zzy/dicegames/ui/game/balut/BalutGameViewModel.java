package com.zzy.dicegames.ui.game.balut;

import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.ui.game.BaseGameViewModel;
import com.zzy.dicegames.utils.ArrayUtil;

import java.time.LocalDate;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class BalutGameViewModel extends BaseGameViewModel {
    /** 得分项 */
    public enum Category {
        FOURS, FIVES, SIXES, STRAIGHT, FULL_HOUSE, CHOICE, BALUT
    }

    /** 得分项个数 */
    public static final int NUM_CATEGORIES = Category.values().length;

    /** 每个得分项可选择的最大次数 */
    public static final int MAX_SELECTIONS = 4;

    /** 当前玩家的得分项是否可点击 */
    private final MutableLiveData<Boolean> clickable = new MutableLiveData<>(false);

    /**
     * Balut的玩家数据<br>
     * 游戏总分为基类的score
     */
    public static class BalutGameData extends BaseGameData {
        /** 每个得分项的得分（未选择的为预估得分） */
        public final MutableLiveData<int[][]> scores =
                new MutableLiveData<>(new int[NUM_CATEGORIES][MAX_SELECTIONS]);

        /** 每个得分项已选择次数 */
        public final MutableLiveData<int[]> selectCount = new MutableLiveData<>(new int[NUM_CATEGORIES]);

        /** 已达到最大选择次数的得分项个数 */
        public int numSelected = 0;

        /** 每个得分项的总分 */
        public final MutableLiveData<int[]> categoryScores = new MutableLiveData<>(new int[NUM_CATEGORIES]);

        /** 每个得分项的点数 */
        public final MutableLiveData<int[]> categoryPoints = new MutableLiveData<>(new int[NUM_CATEGORIES]);

        /** 总分点数 */
        public final MutableLiveData<Integer> totalScorePoints = new MutableLiveData<>(0);

        /** 总点数 */
        public final MutableLiveData<Integer> totalPoints = new MutableLiveData<>(0);

        @Override
        public void reset() {
            super.reset();
            scores.setValue(new int[NUM_CATEGORIES][MAX_SELECTIONS]);
            selectCount.setValue(new int[NUM_CATEGORIES]);
            numSelected = 0;
            categoryScores.setValue(new int[NUM_CATEGORIES]);
            categoryPoints.setValue(new int[NUM_CATEGORIES]);
            totalScorePoints.setValue(0);
            totalPoints.setValue(0);
        }
    }

    public BalutGameViewModel() {
        super(5, 3, 1, 1);
        initGameData(1);
        disableAllDice();
    }

    @Override
    protected BaseGameData createGameData() {
        return new BalutGameData();
    }

    /** 返回当前玩家的数据 */
    protected BalutGameData data() {
        return (BalutGameData) gameData[getCurrentPlayerValue()];
    }

    public LiveData<Boolean> getClickable() {
        return clickable;
    }

    public boolean isClickable() {
        return Boolean.TRUE.equals(clickable.getValue());
    }

    public LiveData<int[][]> getScores() {
        return data().scores;
    }

    public LiveData<int[]> getSelectCount() {
        return data().selectCount;
    }

    public int getNumSelected() {
        return data().numSelected;
    }

    public LiveData<int[]> getCategoryScores() {
        return data().categoryScores;
    }

    public LiveData<int[]> getCategoryPoints() {
        return data().categoryPoints;
    }

    public LiveData<Integer> getTotalScore() {
        return data().score;
    }

    public LiveData<Integer> getTotalScorePoints() {
        return data().totalScorePoints;
    }

    public LiveData<Integer> getTotalPoints() {
        return data().totalPoints;
    }

    @Override
    public void updateDiceNumbers(int... numbers) {
        super.updateDiceNumbers(numbers);
        updateScores();
    }

    /** 根据骰子点数更新预估得分 */
    protected void updateScores() {
        BalutGameData playerData = data();
        int[] currentSelectCount = playerData.selectCount.getValue();
        int[][] currentScores = playerData.scores.getValue();
        if (currentSelectCount == null || currentScores == null)
            return;

        for (int i = 0; i < currentScores.length; i++) {
            if (currentSelectCount[i] < currentScores[i].length)
                currentScores[i][currentSelectCount[i]] = calculateScore(i);
        }
        playerData.scores.setValue(currentScores);
    }

    /** 根据当前骰子点数计算指定得分项的得分 */
    public int calculateScore(int category) {
        int score = 0;
        switch (Category.values()[category]) {
            case FOURS: case FIVES: case SIXES:
                score = diceCounts[category + 4] * (category + 4);
                break;
            case STRAIGHT:
                if (hasStraight()) score = sumOfDice;
                break;
            case FULL_HOUSE: {
                boolean hasThree = false, hasTwo = false;
                for (int i = 1; i <= 6; i++) {
                    if (diceCounts[i] == 3) hasThree = true;
                    if (diceCounts[i] == 2) hasTwo = true;
                }
                if (hasThree && hasTwo) score = sumOfDice;
                break;
            }
            case CHOICE:
                score = sumOfDice;
                break;
            case BALUT:
                if (isBalut()) score = 20 + sumOfDice;
                break;
        }
        return score;
    }

    /** 是否具有连顺 */
    private boolean hasStraight() {
        for (int i = 2; i <= 5; i++) {
            if (diceCounts[i] == 0)
                return false;
        }
        return diceCounts[1] > 0 || diceCounts[6] > 0;
    }

    /** 判断是否满足Balut：所有骰子点数都相同 */
    private boolean isBalut() {
        int[] numbers = diceNumbers.getValue();
        return numbers != null && ArrayUtil.all(numbers, numbers[0]);
    }

    @Override
    protected void updateDiceWindowEnabled() {
        super.updateDiceWindowEnabled();
        clickable.setValue(!diceRolling && hasRolled());
    }

    /** 选择指定的得分项，更新得分 */
    public void select(int category) {
        BalutGameData playerData = data();
        int[] currentSelectCount = playerData.selectCount.getValue();
        if (currentSelectCount == null || currentSelectCount[category] >= MAX_SELECTIONS)
            return;

        currentSelectCount[category]++;
        playerData.selectCount.setValue(currentSelectCount);
        if (currentSelectCount[category] >= MAX_SELECTIONS)
            playerData.numSelected++;

        // 掷骰子后已计算过预估得分，此处无需更新scores
        updateTotalScore();
        updatePoints();

        if (playerData.numSelected == NUM_CATEGORIES)
            gameOver();
        else
            resetDiceWindow();
    }

    private void updateTotalScore() {
        BalutGameData playerData = data();
        int[][] currentScores = playerData.scores.getValue();
        int[] currentSelectCount = playerData.selectCount.getValue();
        int[] currentCategoryScores = playerData.categoryScores.getValue();
        if (currentScores == null || currentSelectCount == null || currentCategoryScores == null)
            return;

        int total = 0;
        for (int i = 0; i < NUM_CATEGORIES; i++) {
            currentCategoryScores[i] = ArrayUtil.sum(currentScores[i], 0, currentSelectCount[i]);
            total += currentCategoryScores[i];
        }

        playerData.categoryScores.setValue(currentCategoryScores);
        playerData.score.setValue(total);
    }

    private void updatePoints() {
        BalutGameData playerData = data();
        int[][] currentScores = playerData.scores.getValue();
        int[] currentSelectCount = playerData.selectCount.getValue();
        int[] currentCategoryPoints = playerData.categoryPoints.getValue();
        Integer totalScore = playerData.score.getValue();
        if (currentScores == null || currentSelectCount == null || currentCategoryPoints == null
                || totalScore == null)
            return;

        int newTotalScorePoints = 0;
        int newTotalPoints = 0;
        for (int i = 0; i < NUM_CATEGORIES; i++) {
            currentCategoryPoints[i] = calculatePoints(i, currentSelectCount[i], currentScores[i]);
            newTotalPoints += currentCategoryPoints[i];
        }
        if (playerData.numSelected == NUM_CATEGORIES) {
            newTotalScorePoints = calculateTotalScorePoints(totalScore);
            newTotalPoints += newTotalScorePoints;
        }

        playerData.categoryPoints.setValue(currentCategoryPoints);
        playerData.totalScorePoints.setValue(newTotalScorePoints);
        playerData.totalPoints.setValue(newTotalPoints);
    }

    /** 根据指定得分项的得分计算点数 */
    public int calculatePoints(int category, int selectCount, int[] scores) {
        int total = ArrayUtil.sum(scores, 0, selectCount);
        int numScored = ArrayUtil.count(scores, (i, x) -> i < selectCount && x > 0);
        return switch (Category.values()[category]) {
            case FOURS, FIVES, SIXES -> total >= (category + 4) * 13 ? 2 : 0;
            case STRAIGHT -> numScored == MAX_SELECTIONS ? 4 : 0;
            case FULL_HOUSE -> numScored == MAX_SELECTIONS ? 3 : 0;
            case CHOICE -> total >= 100 ? 2 : 0;
            case BALUT -> numScored * 2;
        };
    }

    /** 计算总分点数 */
    public int calculateTotalScorePoints(int totalScore) {
        return Math.max(-2, Math.min(6, totalScore / 50 - 7));
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
    public BalutScore createScoreEntity() {
        BalutGameData playerData = data();
        int[][] finalScores = playerData.scores.getValue();
        Integer totalScore = playerData.score.getValue();
        Integer totalPoints = playerData.totalPoints.getValue();
        if (finalScores == null || totalScore == null || totalPoints == null)
            return null;

        int numBalut = ArrayUtil.count(finalScores[Category.BALUT.ordinal()], x -> x > 0);
        return new BalutScore(LocalDate.now().toString(), totalScore, totalPoints, numBalut);
    }

    /** 将得分保存到数据库，并返回排名 */
    public int saveScoreToDatabase(BalutScore score) {
        var dao = scoreDatabase.balutScoreDao();
        dao.insert(score);
        return dao.rank(score.points, score.score);
    }

    @Override
    public void reset() {
        clickable.setValue(false);
        super.reset();
    }
}
