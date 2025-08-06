package com.zzy.dicegames.ui.game.yahtzee;

import com.zzy.dicegames.ui.game.BaseGameViewModel;
import com.zzy.dicegames.utils.ArrayUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public abstract class BaseYahtzeeViewModel extends BaseGameViewModel {
    /** 上区得分项个数 */
    public static final int NUM_UPPER_CATEGORIES = 6;

    /** 得分项个数 */
    protected int numCategories;

    /** 上区总分达到多少时获得奖励分 */
    protected int bonusThreshold;

    /** 奖励分值 */
    protected int bonusValue;

    /** 每个得分项的得分 */
    protected final MutableLiveData<int[]> scores = new MutableLiveData<>();

    /** 每个得分项是否已选择 */
    protected final MutableLiveData<boolean[]> selected = new MutableLiveData<>();

    /** 已选择得分项个数 */
    // 除了选择得分项，初始化、销毁重建时也会触发observer（此时已选择个数并未改变），因此不适合用于判断游戏结束
    protected int numSelected = 0;

    /** 获得的上区总分 */
    protected final MutableLiveData<Integer> upperTotalScore = new MutableLiveData<>(0);

    /** 获得的奖励分 */
    protected final MutableLiveData<Integer> bonusScore = new MutableLiveData<>(0);

    /** 获得的游戏总分 */
    protected final MutableLiveData<Integer> totalScore = new MutableLiveData<>(0);

    protected BaseYahtzeeViewModel(
            int numDice, int maxRolls, int numCategories, int bonusThreshold, int bonusValue) {
        super(numDice, maxRolls);
        this.numCategories = numCategories;
        this.bonusThreshold = bonusThreshold;
        this.bonusValue = bonusValue;
        this.scores.setValue(new int[numCategories]);
        this.selected.setValue(new boolean[numCategories]);
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

    public LiveData<int[]> getScores() {
        return scores;
    }

    public LiveData<boolean[]> getSelected() {
        return selected;
    }

    public int getNumSelected() {
        return numSelected;
    }

    public LiveData<Integer> getUpperTotalScore() {
        return upperTotalScore;
    }

    public LiveData<Integer> getBonusScore() {
        return bonusScore;
    }

    public LiveData<Integer> getTotalScore() {
        return totalScore;
    }

    /** 根据当前骰子点数计算指定得分项的得分 */
    public abstract int calculateScore(int category);

    /** 判断是否满足Yahtzee：所有骰子点数都相同 */
    protected boolean isYahtzee() {
        int[] numbers = diceNumbers.getValue();
        return numbers != null && ArrayUtils.all(numbers, numbers[0]);
    }

    /** 是否满足Joker规则：满足Yahtzee，且Yahtzee和上区对应的数字已经选过 */
    protected boolean isJoker() {
        int[] numbers = diceNumbers.getValue();
        boolean[] isSelected = selected.getValue();
        if (numbers == null || isSelected == null)
            return false;

        return isYahtzee() && isSelected[numbers[0] - 1] && isSelected[numCategories - 1];
    }

    /** 选择指定的得分项，更新得分 */
    public void select(int category) {
        boolean[] currentSelected = selected.getValue();
        int[] currentScores = scores.getValue();
        if (currentSelected == null || currentSelected[category] || currentScores == null)
            return;

        currentScores[category] = calculateScore(category);
        currentSelected[category] = true;

        selected.setValue(currentSelected);
        scores.setValue(currentScores);
        updateBonusAndTotalScore();
        numSelected++;
    }

    private void updateBonusAndTotalScore() {
        int[] currentScores = scores.getValue();
        if (currentScores == null)
            return;

        int upperTotal = 0;
        for (int i = 0; i < NUM_UPPER_CATEGORIES; i++)
            upperTotal += currentScores[i];

        int bonus = upperTotal >= bonusThreshold ? bonusValue : 0;
        int total = upperTotal + bonus;
        for (int i = NUM_UPPER_CATEGORIES; i < numCategories; i++)
            total += currentScores[i];

        upperTotalScore.setValue(upperTotal);
        bonusScore.setValue(bonus);
        totalScore.setValue(total);
    }

    @Override
    public void reset() {
        super.reset();
        scores.setValue(new int[numCategories]);
        selected.setValue(new boolean[numCategories]);
        numSelected = 0;
        upperTotalScore.setValue(0);
        bonusScore.setValue(0);
        totalScore.setValue(0);
    }
}
