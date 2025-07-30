package com.zzy.dicegames.ui.game.balut;

import com.zzy.dicegames.ui.game.BaseGameViewModel;

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

    /** 每个得分项的得分 */
    private final MutableLiveData<int[][]> scores = new MutableLiveData<>(new int[NUM_CATEGORIES][MAX_SELECTIONS]);

    /** 每个得分项已选择次数 */
    private final MutableLiveData<int[]> selectCount = new MutableLiveData<>(new int[NUM_CATEGORIES]);

    /** 已达到最大选择次数的得分项个数 */
    private int numSelected = 0;

    /** 获得的游戏总分 */
    private final MutableLiveData<Integer> totalScore = new MutableLiveData<>(0);

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

    /** 计算指定得分项的得分，调用该方法前必须先调用{@link #setDiceNumbers} */
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
        for (int i = 1; i < diceNumbers.length; i++) {
            if (diceNumbers[i] != diceNumbers[0])
                return false;
        }
        return true;
    }

    /** 选择指定的得分项，更新得分 */
    public void select(int category) {
        int[] currentSelectCount = selectCount.getValue();
        int[][] currentScores = scores.getValue();
        if (currentSelectCount == null || currentSelectCount[category] >= MAX_SELECTIONS || currentScores == null)
            return;

        currentScores[category][currentSelectCount[category]] = calculateScore(category);
        currentSelectCount[category]++;

        selectCount.setValue(currentSelectCount);
        scores.setValue(currentScores);
        updateTotalScore();
        if (currentSelectCount[category] >= MAX_SELECTIONS) numSelected++;
    }

    private void updateTotalScore() {
        int[][] currentScores = scores.getValue();
        if (currentScores == null)
            return;

        int total = 0;
        for (int i = 0; i < NUM_CATEGORIES; i++)
            for (int j = 0; j < MAX_SELECTIONS; j++)
                total += currentScores[i][j];

        totalScore.setValue(total);
    }

    /** 重置计分板 */
    @Override
    public void reset() {
        scores.setValue(new int[NUM_CATEGORIES][MAX_SELECTIONS]);
        selectCount.setValue(new int[NUM_CATEGORIES]);
        numSelected = 0;
        totalScore.setValue(0);
    }
}
