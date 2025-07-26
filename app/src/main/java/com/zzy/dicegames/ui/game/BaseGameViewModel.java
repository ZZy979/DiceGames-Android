package com.zzy.dicegames.ui.game;

import java.util.Arrays;

import androidx.lifecycle.ViewModel;

public abstract class BaseGameViewModel extends ViewModel {
    /** 骰子点数 */
    protected int[] diceNumbers;

    /** 每个点数的出现次数 */
    protected int[] diceCounts = new int[7];

    /** 骰子点数总和 */
    protected int sumOfDice;

    public void setDiceNumbers(int... diceNumbers) {
        this.diceNumbers = Arrays.copyOf(diceNumbers, diceNumbers.length);
        Arrays.sort(this.diceNumbers);

        sumOfDice = 0;
        Arrays.fill(diceCounts, 0);
        for (int n : diceNumbers) {
            sumOfDice += n;
            diceCounts[n]++;
        }
    }

    /** 重置计分板 */
    public void reset() {}
}
