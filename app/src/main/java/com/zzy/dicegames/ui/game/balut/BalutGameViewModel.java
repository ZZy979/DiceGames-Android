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

    /** 每个得分项的得分（未选择的为预估得分） */
    private final MutableLiveData<int[][]> scores = new MutableLiveData<>(new int[NUM_CATEGORIES][MAX_SELECTIONS]);

    /** 每个得分项已选择次数 */
    private final MutableLiveData<int[]> selectCount = new MutableLiveData<>(new int[NUM_CATEGORIES]);

    /** 已达到最大选择次数的得分项个数 */
    private int numSelected = 0;

    /** 每个得分项的总分 */
    private final MutableLiveData<int[]> categoryScores = new MutableLiveData<>(new int[NUM_CATEGORIES]);

    /** 每个得分项的点数 */
    private final MutableLiveData<int[]> categoryPoints = new MutableLiveData<>(new int[NUM_CATEGORIES]);

    /** 获得的游戏总分 */
    private final MutableLiveData<Integer> totalScore = new MutableLiveData<>(0);

    /** 总分点数 */
    private final MutableLiveData<Integer> totalScorePoints = new MutableLiveData<>(0);

    /** 总点数 */
    private final MutableLiveData<Integer> totalPoints = new MutableLiveData<>(0);

    public BalutGameViewModel() {
        super(5, 3);
    }

    public LiveData<int[][]> getScores() {
        return scores;
    }

    public LiveData<int[]> getSelectCount() {
        return selectCount;
    }

    public int getNumSelected() {
        return numSelected;
    }

    public LiveData<int[]> getCategoryScores() {
        return categoryScores;
    }

    public LiveData<int[]> getCategoryPoints() {
        return categoryPoints;
    }

    public LiveData<Integer> getTotalScore() {
        return totalScore;
    }

    public LiveData<Integer> getTotalScorePoints() {
        return totalScorePoints;
    }

    public LiveData<Integer> getTotalPoints() {
        return totalPoints;
    }

    @Override
    public void updateDiceNumbers(int... numbers) {
        super.updateDiceNumbers(numbers);
        updateScores();
    }

    /** 根据骰子点数更新预估得分 */
    protected void updateScores() {
        int[] currentSelectCount = selectCount.getValue();
        int[][] currentScores = scores.getValue();
        if (currentSelectCount == null || currentScores == null)
            return;

        for (int i = 0; i < currentScores.length; i++) {
            if (currentSelectCount[i] < currentScores[i].length)
                currentScores[i][currentSelectCount[i]] = calculateScore(i);
        }
        scores.setValue(currentScores);
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

    /** 选择指定的得分项，更新得分 */
    public void select(int category) {
        int[] currentSelectCount = selectCount.getValue();
        if (currentSelectCount == null || currentSelectCount[category] >= MAX_SELECTIONS)
            return;

        currentSelectCount[category]++;
        selectCount.setValue(currentSelectCount);
        if (currentSelectCount[category] >= MAX_SELECTIONS)
            numSelected++;

        // 掷骰子后已计算过预估得分，此处无需更新scores
        updateTotalScore();
        updatePoints();

        if (numSelected == NUM_CATEGORIES) {
            gameOver();
        }
        else {
            resetDiceWindow();
            rollDiceWithAnimation();  // TODO 改为手动掷骰子
        }
    }

    private void updateTotalScore() {
        int[][] currentScores = scores.getValue();
        int[] currentSelectCount = selectCount.getValue();
        int[] currentCategoryScores = categoryScores.getValue();
        if (currentScores == null || currentSelectCount == null || currentCategoryScores == null)
            return;

        int total = 0;
        for (int i = 0; i < NUM_CATEGORIES; i++) {
            currentCategoryScores[i] = ArrayUtil.sum(currentScores[i], 0, currentSelectCount[i]);
            total += currentCategoryScores[i];
        }

        categoryScores.setValue(currentCategoryScores);
        totalScore.setValue(total);
    }

    private void updatePoints() {
        int[][] currentScores = scores.getValue();
        int[] currentSelectCount = selectCount.getValue();
        int[] currentCategoryPoints = categoryPoints.getValue();
        if (currentScores == null || currentSelectCount == null || currentCategoryPoints == null
                || totalScore.getValue() == null)
            return;

        int newTotalScorePoints = 0;
        int newTotalPoints = 0;
        for (int i = 0; i < NUM_CATEGORIES; i++) {
            currentCategoryPoints[i] = calculatePoints(i, currentSelectCount[i], currentScores[i]);
            newTotalPoints += currentCategoryPoints[i];
        }
        if (numSelected == NUM_CATEGORIES) {
            newTotalScorePoints = calculateTotalScorePoints(totalScore.getValue());
            newTotalPoints += newTotalScorePoints;
        }

        categoryPoints.setValue(currentCategoryPoints);
        totalScorePoints.setValue(newTotalScorePoints);
        totalPoints.setValue(newTotalPoints);
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
        int[][] finalScores = scores.getValue();
        if (finalScores == null || totalScore.getValue() == null || totalPoints.getValue() == null)
            return null;

        int numBalut = ArrayUtil.count(finalScores[Category.BALUT.ordinal()], x -> x > 0);
        return new BalutScore(LocalDate.now().toString(), totalScore.getValue(), totalPoints.getValue(), numBalut);
    }

    /** 将得分保存到数据库，并返回排名 */
    public int saveScoreToDatabase(BalutScore score) {
        var dao = scoreDatabase.balutScoreDao();
        dao.insert(score);
        return dao.rank(score.points, score.score);
    }

    @Override
    public void reset() {
        super.reset();
        scores.setValue(new int[NUM_CATEGORIES][MAX_SELECTIONS]);
        selectCount.setValue(new int[NUM_CATEGORIES]);
        numSelected = 0;
        categoryScores.setValue(new int[NUM_CATEGORIES]);
        categoryPoints.setValue(new int[NUM_CATEGORIES]);
        totalScore.setValue(0);
        totalScorePoints.setValue(0);
        totalPoints.setValue(0);
    }
}
