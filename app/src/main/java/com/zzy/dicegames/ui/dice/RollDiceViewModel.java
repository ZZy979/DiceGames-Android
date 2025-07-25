package com.zzy.dicegames.ui.dice;

import java.util.Arrays;
import java.util.Random;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/** {@link RollDiceFragment}的ViewModel */
public class RollDiceViewModel extends ViewModel {
    /** 骰子个数最小值 */
    public static final int MIN_DICE_COUNT = 1;

    /** 骰子个数最大值 */
    public static final int MAX_DICE_COUNT = 6;

    /** 无限次数 */
    public static final int UNLIMITED_ROLLS = Integer.MAX_VALUE;

    /** 骰子个数 */
    private int diceCount;

    /** 最大掷骰子次数 */
    private int maxRolls;

    /** 剩余掷骰子次数 */
    private final MutableLiveData<Integer> remainingRolls = new MutableLiveData<>();

    // 骰子点数通过掷骰子改变，影响得分计算；骰子锁定状态受玩家控制，一般只影响显示，因此二者分开表示
    /** 骰子点数 */
    private final MutableLiveData<int[]> diceNumbers = new MutableLiveData<>();

    /** 骰子锁定状态 */
    private final MutableLiveData<boolean[]> diceLocked = new MutableLiveData<>();

    private Random random = new Random();

    /**
     * @param diceCount 骰子个数，1~6之间
     * @param maxRolls 最大掷骰子次数，{@link #UNLIMITED_ROLLS}表示无限次数
     */
    public RollDiceViewModel(int diceCount, int maxRolls) {
        if (diceCount < MIN_DICE_COUNT || diceCount > MAX_DICE_COUNT)
            throw new IllegalArgumentException("骰子个数必须在1~6之间");

        this.diceCount = diceCount;
        this.maxRolls = maxRolls;
        this.remainingRolls.setValue(maxRolls);

        int[] diceNumbers = new int[diceCount];
        boolean[] diceLocked = new boolean[diceCount];
        Arrays.fill(diceNumbers, DiceView.MAX_NUMBER);
        Arrays.fill(diceLocked, false);
        this.diceLocked.setValue(diceLocked);
        this.diceNumbers.setValue(diceNumbers);
    }

    public int getDiceCount() {
        return diceCount;
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
        diceNumbers.setValue(numbers);
        if (maxRolls != UNLIMITED_ROLLS)
            remainingRolls.setValue(remainingRolls.getValue() - 1);
    }

    /** 重置掷骰子次数，解锁骰子 */
    public void reset() {
        boolean[] locked = diceLocked.getValue();
        if (locked == null)
            return;

        Arrays.fill(locked, false);
        diceLocked.setValue(locked);
        remainingRolls.setValue(maxRolls);
    }
}
