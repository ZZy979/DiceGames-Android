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
    protected final int numDice;

    /** 最大掷骰子次数 */
    protected final int maxRolls;

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

    /** 是否正在掷骰子动画 */
    protected boolean diceRolling = false;

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

    public boolean isDiceRolling() {
        return diceRolling;
    }

    /** 本回合是否已掷过骰子 */
    public boolean hasRolled() {
        Integer remaining = remainingRolls.getValue();
        return remaining != null && remaining < maxRolls;
    }

    public boolean hasRemainingRolls() {
        Integer remaining = remainingRolls.getValue();
        return remaining != null && remaining > 0;
    }

    public boolean isUnlimitedRolls() {
        return maxRolls == UNLIMITED_ROLLS;
    }

    protected void unlockAllDice() {
        diceLocked.setValue(ArrayUtil.fill(diceLocked.getValue(), false));
    }

    protected void setAllDiceEnabled(boolean enabled) {
        diceEnabled.setValue(ArrayUtil.fill(diceEnabled.getValue(), enabled));
    }

    protected void enableAllDice() {
        setAllDiceEnabled(true);
    }

    protected void disableAllDice() {
        setAllDiceEnabled(false);
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
        rollDice(generateRandomDiceNumbers());
    }

    /** 掷骰子并指定骰子点数，用于单元测试 */
    public void rollDice(int... numbers) {
        if (!hasRemainingRolls())
            return;
        decreaseRemainingRolls();
        updateDiceNumbers(numbers);
    }

    /** 掷骰子（带动画效果） */
    public void rollDiceWithAnimation() {
        if (diceRolling || !hasRemainingRolls())
            return;
        setDiceRolling(true);
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
            // 动画结束
            setDiceRolling(false);
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

    /**
     * 根据剩余次数和动画状态更新掷骰子窗口的状态
     * 由“是否正在掷骰子”和“剩余掷骰子次数”决定的子类状态应该在此方法中更新
     *
     * 组件可点击状态变化逻辑：
     * | 动作 | remainingRolls | diceRolling | 骰子可点击 | Roll按钮可点击 | 得分项可点击 |
     * | --- | --- | --- | --- | --- | --- |
     * | ①回合开始 | maxRolls | false | × | √ | × |
     * | ②点击Roll按钮，掷骰子动画过程 | > 0, < maxRolls | true | × | × | × |
     * | ③动画结束 | > 0, < maxRolls | false | √ | √ | √ |
     * | ④最后一次点击Roll按钮，掷骰子动画过程 | 0 | true | × | × | × |
     * | ⑤动画结束，掷骰子机会用完 | 0 | false | × | × | √ |
     * | ⑥选择得分项，恢复到①状态 | maxRolls | false | × | √ | × |
     *
     * 总结：
     * 骰子可点击：!diceRolling && remainingRolls > 0 && remainingRolls < maxRolls
     * Roll按钮可点击：!diceRolling && remainingRolls > 0
     * 得分项可点击：!diceRolling && remainingRolls < maxRolls
     */
    protected void updateDiceWindowEnabled() {
        rollButtonEnabled.setValue(!diceRolling && hasRemainingRolls());
        setAllDiceEnabled(!diceRolling && hasRemainingRolls() && hasRolled());
    }

    /** 设置掷骰子动画状态，并更新骰子与Roll按钮的可点击状态 */
    protected void setDiceRolling(boolean rolling) {
        diceRolling = rolling;
        updateDiceWindowEnabled();
    }

    /** 剩余掷骰子次数减1 */
    protected void decreaseRemainingRolls() {
        Integer remaining = remainingRolls.getValue();
        if (remaining == null || remaining <= 0)
            return;

        remaining--;
        remainingRolls.setValue(remaining);
        updateDiceWindowEnabled();
    }

    /** 重置掷骰子次数，解锁骰子 */
    public void resetDiceWindow() {
        remainingRolls.setValue(maxRolls);
        unlockAllDice();
        updateDiceWindowEnabled();
    }

    /** 重置游戏状态 */
    public void reset() {
        resetDiceWindow();
    }
}
