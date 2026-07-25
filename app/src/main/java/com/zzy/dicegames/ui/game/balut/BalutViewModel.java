package com.zzy.dicegames.ui.game.balut;

import com.zzy.dicegames.data.entity.BalutScore;
import com.zzy.dicegames.ui.game.BaseGameViewModel;
import com.zzy.dicegames.utils.ArrayUtil;

import java.time.LocalDate;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class BalutViewModel extends BaseGameViewModel {
    /** 得分项个数 */
    public static final int NUM_CATEGORIES = 7;

    /** 每个得分项可选择的最大次数 */
    public static final int MAX_SELECTIONS = 4;

    // 得分项编号
    public static final int FOURS = 0;
    public static final int FIVES = 1;
    public static final int SIXES = 2;
    public static final int STRAIGHT = 3;
    public static final int FULL_HOUSE = 4;
    public static final int CHOICE = 5;
    public static final int BALUT = 6;

    /** 每个得分项的得分（未选择的为预估得分） */
    private final MutableLiveData<int[][]> scores = new MutableLiveData<>(new int[NUM_CATEGORIES][MAX_SELECTIONS]);

    /** 每个得分项已选择次数 */
    private final MutableLiveData<int[]> selectCount = new MutableLiveData<>(new int[NUM_CATEGORIES]);

    /** 已达到最大选择次数的得分项个数 */
    private int numSelected = 0;

    /** 获得的游戏总分 */
    private final MutableLiveData<Integer> totalScore = new MutableLiveData<>(0);

    public BalutViewModel() {
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

    public LiveData<Integer> getTotalScore() {
        return totalScore;
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
        switch (category) {
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
        // 掷骰子后已计算过预估得分，此处无需更新scores
        updateTotalScore();
        if (currentSelectCount[category] >= MAX_SELECTIONS)
            numSelected++;

        if (numSelected == NUM_CATEGORIES) {
            gameOver();
        }
        else {
            resetDiceWindow();
            rollDiceWithAnimation();
        }
    }

    private void updateTotalScore() {
        int[][] currentScores = scores.getValue();
        int[] currentSelectCount = selectCount.getValue();
        if (currentScores == null || currentSelectCount == null)
            return;

        int total = 0;
        for (int i = 0; i < NUM_CATEGORIES; i++) {
            for (int j = 0; j < currentSelectCount[i]; j++)
                total += currentScores[i][j];
        }

        totalScore.setValue(total);
    }

    /** 游戏结束 */
    public void gameOver() {
        disableAllDice();
        rollButtonEnabled.setValue(false);
        var score = createScoreEntity();
        int rank = saveScoreToDatabase(score);
        if (gameOverAction != null)
            gameOverAction.accept(new Object[] {score.score, rank});
    }

    /** 创建得分实体 */
    public BalutScore createScoreEntity() {
        int[][] finalScores = scores.getValue();
        if (finalScores == null || totalScore.getValue() == null)
            return null;

        int numBalut = 0;
        for (int j = 0; j < finalScores[BALUT].length; j++) {
            if (finalScores[BALUT][j] > 0)
                numBalut++;
        }
        return new BalutScore(LocalDate.now().toString(), totalScore.getValue(), numBalut);
    }

    /** 将得分保存到数据库，并返回排名 */
    public int saveScoreToDatabase(BalutScore score) {
        var dao = scoreDatabase.balutScoreDao();
        dao.insert(score);
        return dao.rank(score.score);
    }

    @Override
    public void reset() {
        super.reset();
        scores.setValue(new int[NUM_CATEGORIES][MAX_SELECTIONS]);
        selectCount.setValue(new int[NUM_CATEGORIES]);
        numSelected = 0;
        totalScore.setValue(0);
    }
}
