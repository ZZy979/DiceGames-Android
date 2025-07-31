package com.zzy.dicegames.ui.game;

import com.zzy.dicegames.ui.dice.DiceView;

import java.util.Arrays;
import java.util.Random;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BaseGameViewModel extends ViewModel {
    /** 骰子个数最小值 */
    public static final int MIN_NUM_DICE = 1;

    /** 骰子个数最大值 */
    public static final int MAX_NUM_DICE = 6;

    /** 无限次数 */
    public static final int UNLIMITED_ROLLS = Integer.MAX_VALUE;

    /** 骰子个数 */
    protected int numDice;

    /** 最大掷骰子次数 */
    protected int maxRolls;

    /** 剩余掷骰子次数 */
    protected final MutableLiveData<Integer> remainingRolls = new MutableLiveData<>();

    // 骰子点数通过掷骰子改变，影响得分计算；骰子锁定状态受玩家控制，一般只影响显示，因此二者分开表示
    /** 骰子点数 */
    protected final MutableLiveData<int[]> diceNumbers = new MutableLiveData<>();

    /** 骰子锁定状态 */
    protected final MutableLiveData<boolean[]> diceLocked = new MutableLiveData<>();

    /** 每个点数的出现次数 */
    protected int[] diceCounts = new int[7];

    /** 骰子点数总和 */
    protected int sumOfDice;

    protected Random random = new Random();

    /**
     * @param numDice 骰子个数，1~6之间
     * @param maxRolls 最大掷骰子次数，{@link #UNLIMITED_ROLLS}表示无限次数
     */
    protected BaseGameViewModel(int numDice, int maxRolls) {
        if (numDice < MIN_NUM_DICE || numDice > MAX_NUM_DICE)
            throw new IllegalArgumentException("骰子个数必须在1~6之间");

        this.numDice = numDice;
        this.maxRolls = maxRolls;
        this.remainingRolls.setValue(maxRolls);

        int[] diceNumbers = new int[numDice];
        Arrays.fill(diceNumbers, DiceView.MAX_NUMBER);
        this.diceNumbers.setValue(diceNumbers);
        this.diceLocked.setValue(new boolean[numDice]);
    }

    public int getNumDice() {
        return numDice;
    }

    public int getMaxRolls() {
        return maxRolls;
    }

    public LiveData<Integer> getRemainingRolls() {
        return remainingRolls;
    }

    public LiveData<int[]> getDiceNumbers() {
        return diceNumbers;
    }

    public LiveData<boolean[]> getDiceLocked() {
        return diceLocked;
    }

    /** 翻转第position个骰子的锁定状态 */
    public void toggleLocked(int position) {
        boolean[] locked = diceLocked.getValue();
        if (locked == null || position < 0 || position > locked.length)
            return;

        locked[position] = !locked[position];
        diceLocked.setValue(locked);
    }

    /** 掷一次骰子，更新未锁定骰子的点数，并将剩余次数减1（除非无限次数） */
    public void rollDice() {
        if (remainingRolls.getValue() == null || remainingRolls.getValue() <= 0)
            return;

        int[] numbers = diceNumbers.getValue();
        boolean[] locked = diceLocked.getValue();
        if (numbers == null || locked == null)
            return;

        for (int i = 0; i < numbers.length; i++) {
            if (!locked[i])
                numbers[i] = random.nextInt(6) + 1;
        }
        setDiceNumbers(numbers);

        if (maxRolls != UNLIMITED_ROLLS)
            remainingRolls.setValue(remainingRolls.getValue() - 1);
    }

    public void setDiceNumbers(int... numbers) {
        sumOfDice = 0;
        Arrays.fill(diceCounts, 0);
        for (int n : numbers) {
            sumOfDice += n;
            diceCounts[n]++;
        }
        // diceNumbers的观察者（如updateScores()）依赖diceCounts和sumOfDice，因此最后更新diceNumbers
        diceNumbers.setValue(numbers);
    }

    /** 重置掷骰子次数，解锁骰子 */
    protected void resetDiceWindow() {
        boolean[] locked = diceLocked.getValue();
        if (locked == null)
            return;

        Arrays.fill(locked, false);
        diceLocked.setValue(locked);
        remainingRolls.setValue(maxRolls);
    }

    /** 重置游戏状态 */
    public void reset() {
        resetDiceWindow();
    }
}
