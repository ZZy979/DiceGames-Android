package com.zzy.dicegames.ui.game;

import android.os.Handler;

import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.ui.dice.DiceView;
import com.zzy.dicegames.utils.ArrayUtil;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

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

    /** 掷骰子动画帧数 */
    protected static final int ROLL_DICE_ANIMATION_FRAMES = 10;

    /** 掷骰子动画帧间隔(ms) */
    protected static final long ROLL_DICE_ANIMATION_INTERVAL = 30;

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

    /** 骰子激活状态 */
    protected final MutableLiveData<boolean[]> diceEnabled = new MutableLiveData<>();

    /** Roll按钮激活状态 */
    protected final MutableLiveData<Boolean> rollButtonEnabled = new MutableLiveData<>(true);

    /** 每个点数的出现次数 */
    protected int[] diceCounts = new int[7];

    /** 骰子点数总和 */
    protected int sumOfDice;

    protected Random random = new Random();

    /** 用于异步执行操作 */
    protected Handler handler = new Handler();

    /** 游戏结束时执行的动作 */
    protected Consumer<Object[]> gameOverAction;

    /** 游戏得分数据库 */
    protected ScoreDatabase scoreDatabase;

    /**
     * @param numDice 骰子个数，1~6之间
     * @param maxRolls 最大掷骰子次数，{@link #UNLIMITED_ROLLS}表示无限次数
     */
    protected BaseGameViewModel(int numDice, int maxRolls) {
        if (numDice < MIN_NUM_DICE || numDice > MAX_NUM_DICE)
            throw new IllegalArgumentException("骰子个数必须在1~6之间");
        if (maxRolls <= 0)
            throw new IllegalArgumentException("最大掷骰子次数必须大于0");

        this.numDice = numDice;
        this.maxRolls = maxRolls;
        this.remainingRolls.setValue(maxRolls);
        this.diceNumbers.setValue(ArrayUtil.create(numDice, DiceView.MAX_NUMBER));
        this.diceLocked.setValue(ArrayUtil.create(numDice, false));
        this.diceEnabled.setValue(ArrayUtil.create(numDice, true));
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

    public LiveData<boolean[]> getDiceEnabled() {
        return diceEnabled;
    }

    public LiveData<Boolean> getRollButtonEnabled() {
        return rollButtonEnabled;
    }

    public boolean hasRemainingRolls() {
        Integer remaining = remainingRolls.getValue();
        return remaining != null && remaining > 0;
    }

    protected void unlockAllDice() {
        diceLocked.setValue(ArrayUtil.fill(diceLocked.getValue(), false));
    }

    protected void enableAllDice() {
        diceEnabled.setValue(ArrayUtil.fill(diceEnabled.getValue(), true));
    }

    protected void disableAllDice() {
        diceEnabled.setValue(ArrayUtil.fill(diceEnabled.getValue(), false));
    }

    protected void setRollButtonEnabled(boolean enabled) {
        rollButtonEnabled.setValue(enabled);
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setGameOverAction(Consumer<Object[]> gameOverAction) {
        this.gameOverAction = gameOverAction;
    }

    public void setScoreDatabase(ScoreDatabase scoreDatabase) {
        this.scoreDatabase = scoreDatabase;
    }

    /** 翻转第i个骰子的锁定状态 */
    public void toggleLocked(int i) {
        boolean[] locked = diceLocked.getValue();
        if (locked == null || i < 0 || i > locked.length)
            return;

        locked[i] = !locked[i];
        diceLocked.setValue(locked);
    }

    /** 随机生成未锁定骰子的点数 */
    public int[] generateRandomDiceNumbers() {
        int[] numbers = diceNumbers.getValue();
        boolean[] locked = diceLocked.getValue();
        if (numbers == null || locked == null)
            return null;

        for (int i = 0; i < numbers.length; i++) {
            if (!locked[i])
                numbers[i] = random.nextInt(6) + 1;
        }
        return numbers;
    }

    /** 掷未锁定的骰子，更新骰子点数、计算得分的辅助数据和剩余次数 */
    // 无动画效果，可用于单元测试
    public void rollDice() {
        if (!hasRemainingRolls())
            return;
        decreaseRemainingRolls();
        int[] numbers = generateRandomDiceNumbers();
        updateDiceNumbers(numbers);
    }

    /** 掷骰子（带动画效果） */
    public void rollDiceWithAnimation() {
        if (!hasRemainingRolls())
            return;
        decreaseRemainingRolls();
        rollDiceAnimation(0);
    }

    /** 掷骰子动画帧 */
    protected void rollDiceAnimation(int frame) {
        int[] numbers = generateRandomDiceNumbers();
        if (frame < ROLL_DICE_ANIMATION_FRAMES) {
            diceNumbers.setValue(numbers);
            handler.postDelayed(() -> rollDiceAnimation(frame + 1), ROLL_DICE_ANIMATION_INTERVAL);
        }
        else {
            updateDiceNumbers(numbers);
        }
    }

    /** 更新骰子点数 */
    public void updateDiceNumbers(int... numbers) {
        diceNumbers.setValue(numbers);
        prepareCalculateScore(numbers);
    }

    /** 基于骰子点数准备用于计算得分的辅助数据 */
    protected void prepareCalculateScore(int[] numbers) {
        sumOfDice = 0;
        Arrays.fill(diceCounts, 0);
        for (int n : numbers) {
            sumOfDice += n;
            diceCounts[n]++;
        }
    }

    /** 剩余掷骰子次数减1（除非无限次数） */
    protected void decreaseRemainingRolls() {
        Integer remaining = remainingRolls.getValue();
        if (remaining == null || remaining <= 0 || remaining == UNLIMITED_ROLLS)
            return;

        remaining--;
        remainingRolls.setValue(remaining);

        boolean enabled = remaining > 0;
        rollButtonEnabled.setValue(enabled);
        diceEnabled.setValue(ArrayUtil.fill(diceEnabled.getValue(), enabled));
    }

    /** 重置掷骰子次数，解锁骰子 */
    public void resetDiceWindow() {
        remainingRolls.setValue(maxRolls);
        unlockAllDice();
        enableAllDice();
        rollButtonEnabled.setValue(true);
    }

    /** 重置游戏状态 */
    public void reset() {
        resetDiceWindow();
    }
}
