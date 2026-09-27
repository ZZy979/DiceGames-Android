package com.zzy.dicegames.ui.game.yahtzee;

import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.ui.game.BaseGameViewModel;
import com.zzy.dicegames.utils.ArrayUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public abstract class BaseYahtzeeGameViewModel extends BaseGameViewModel {
    /** 上区得分项个数 */
    public static final int NUM_UPPER_CATEGORIES = 6;

    /** 得分项个数 */
    protected final int numCategories;

    /** 上区总分达到多少时获得奖励分 */
    protected final int bonusThreshold;

    /** 奖励分值 */
    protected final int bonusValue;

    /** 当前玩家的得分项是否可点击 */
    protected final MutableLiveData<Boolean> clickable = new MutableLiveData<>(false);

    /**
     * 记分板类游戏的玩家数据<br>
     * 每个玩家有自己的得分项得分、已选择状态、上区总分和奖励分，游戏总分为基类的score
     */
    public static class YahtzeeGameData extends BaseGameData {
        /** 每个得分项的得分（未选择的为预估得分） */
        public final MutableLiveData<int[]> scores;

        /** 每个得分项是否已选择 */
        public final MutableLiveData<boolean[]> selected;

        /** 已选择得分项个数 */
        public int numSelected = 0;

        /** 获得的上区总分 */
        public final MutableLiveData<Integer> upperTotalScore = new MutableLiveData<>(0);

        /** 获得的奖励分 */
        public final MutableLiveData<Integer> bonusScore = new MutableLiveData<>(0);

        public YahtzeeGameData(int numCategories) {
            scores = new MutableLiveData<>(new int[numCategories]);
            selected = new MutableLiveData<>(new boolean[numCategories]);
        }

        @Override
        public void reset() {
            super.reset();
            int numCategories = scores.getValue() == null ? 0 : scores.getValue().length;
            scores.setValue(new int[numCategories]);
            selected.setValue(new boolean[numCategories]);
            numSelected = 0;
            upperTotalScore.setValue(0);
            bonusScore.setValue(0);
        }
    }

    protected BaseYahtzeeGameViewModel(
            int numDice, int maxRolls, int minPlayers, int maxPlayers,
            int numCategories, int bonusThreshold, int bonusValue) {
        super(numDice, maxRolls, minPlayers, maxPlayers);
        this.numCategories = numCategories;
        this.bonusThreshold = bonusThreshold;
        this.bonusValue = bonusValue;
        initGameData(minPlayers);
        disableAllDice();
    }

    @Override
    public BaseGameData createGameData() {
        return new YahtzeeGameData(numCategories);
    }

    /** 返回指定玩家的数据 */
    public YahtzeeGameData data(int player) {
        return (YahtzeeGameData) gameData[player];
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

    public LiveData<Boolean> getClickable() {
        return clickable;
    }

    public boolean isClickable() {
        return Boolean.TRUE.equals(clickable.getValue());
    }

    @Override
    public void updateDiceNumbers(int... numbers) {
        super.updateDiceNumbers(numbers);
        updatePreviewScores();
        if (isComputerTurn() && !diceRolling)
            postComputerAction(this::computerTurn);
    }

    /** 根据骰子点数更新当前玩家的预估得分 */
    protected void updatePreviewScores() {
        YahtzeeGameData currentData = data(getCurrentPlayerValue());
        boolean[] isSelected = currentData.selected.getValue();
        int[] currentScores = currentData.scores.getValue();
        if (isSelected == null || currentScores == null)
            return;

        for (int i = 0; i < currentScores.length; i++) {
            if (!isSelected[i])
                currentScores[i] = calculateScore(i);
        }
        currentData.scores.setValue(currentScores);
    }

    /** 根据当前骰子点数计算指定得分项的得分 */
    public abstract int calculateScore(int category);

    /** 是否所有骰子点数都相同 */
    public boolean isAllSame() {
        int[] numbers = diceNumbers.getValue();
        return numbers != null && ArrayUtil.all(numbers, numbers[0]);
    }

    /** 当前玩家是否满足Joker规则：满足Yahtzee，且Yahtzee和上区对应的数字已经选过 */
    public boolean isJoker() {
        int[] numbers = diceNumbers.getValue();
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        if (numbers == null || isSelected == null)
            return false;

        return isAllSame() && isSelected[numbers[0] - 1] && isSelected[numCategories - 1];
    }

    @Override
    protected void updateDiceWindowEnabled() {
        boolean humanTurn = isHumanTurn();
        rollButtonEnabled.setValue(humanTurn && !diceRolling && hasRemainingRolls());
        setAllDiceEnabled(humanTurn && !diceRolling && hasRemainingRolls() && hasRolled());
        clickable.setValue(humanTurn && !diceRolling && hasRolled());
    }

    /** 选择指定的得分项，更新得分（供玩家点击） */
    public void select(int category) {
        if (!isHumanTurn())
            return;
        doSelect(category);
    }

    /** 选择当前玩家的指定得分项，然后轮到下一位玩家（可由玩家或计算机调用） */
    protected void doSelect(int category) {
        YahtzeeGameData currentData = data(getCurrentPlayerValue());
        boolean[] currentSelected = currentData.selected.getValue();
        if (currentSelected == null || currentSelected[category])
            return;

        currentSelected[category] = true;
        currentData.selected.setValue(currentSelected);
        currentData.numSelected++;

        updateScores(currentData, category);

        if (isAllPlayersFinished()) {
            gameOver();
            return;
        }
        nextPlayer();
        startTurn();
    }

    /** 开始当前玩家的回合 */
    protected void startTurn() {
        resetDiceWindow();
        if (isComputerTurn())
            postComputerAction(this::computerTurn);
    }

    /** 计算机玩家回合：第一次先掷骰子，之后根据策略决定继续掷骰子（并锁定要保留的骰子）或选择得分项 */
    protected void computerTurn() {
        if (!isComputerTurn() || diceRolling)
            return;

        // 第一次投掷，无需保留骰子
        if (!hasRolled()) {
            rollDiceWithAnimation();
            return;
        }

        // 投掷结束或触发Joker，选择得分项
        if (!hasRemainingRolls() || isJoker()) {
            int category = computerChooseCategory();
            if (category >= 0)
                doSelect(category);
            return;
        }

        // 还有剩余投掷次数，先分析该保留哪些骰子
        analyzeDiceToKeep();
    }

    /**
     * 计算机玩家分析该保留哪些骰子：能直接得到高价值得分项时选择该得分项，<br>
     * 否则保留优先级最高的方案
     */
    public void analyzeDiceToKeep() {
        int throwsLeft = getRemainingRollsValue();

        // 能直接得到高价值得分项时不再掷骰子
        int category = goodCategory(throwsLeft);
        if (category >= 0) {
            doSelect(category);
            return;
        }

        boolean[] keep = chooseDiceToKeep(throwsLeft);
        // 所有骰子都要保留时再掷骰子没有意义，直接选择得分项
        if (ArrayUtil.all(keep, true)) {
            category = computerChooseCategory();
            if (category >= 0)
                doSelect(category);
            return;
        }
        keepDiceAndRoll(keep);
    }

    /** 锁定要保留的骰子并掷骰子 */
    protected void keepDiceAndRoll(boolean[] keep) {
        setDiceLocked(keep);
        postComputerAction(this::rollDiceWithAnimation);
    }

    /**
     * 计算机玩家选择一个未选择的得分项：依次尝试特殊规则（如Joker）和能得分的高价值得分项，<br>
     * 最后选择优先级最高的得分项，没有可选择的得分项时返回-1
     */
    public int computerChooseCategory() {
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        if (isSelected == null)
            return -1;

        // 特殊规则（如Joker）
        int category = specialCategory(isSelected);
        if (category >= 0)
            return category;

        // 能得分的高价值得分项
        category = goodCategory(0);
        if (category >= 0)
            return category;

        // 否则按各得分项的优先级选择（优先级相同时取下标较大的项）
        int bestCategory = -1, bestRank = Integer.MIN_VALUE;
        for (int c = 0; c < numCategories; c++) {
            if (isSelected[c])
                continue;

            int rank = rankCategory(c);
            if (rank >= bestRank) {
                bestRank = rank;
                bestCategory = c;
            }
        }
        return bestCategory;
    }

    // ---------- 计算机玩家AI的公共实现 ----------

    /** 能得分时优先选择的高价值得分项，没有时返回-1（默认没有特殊的高价值得分项） */
    public int goodCategory(int throwsLeft) {
        return -1;
    }

    /** 特殊规则下要选择的得分项（如Joker），没有时返回-1（默认没有特殊规则） */
    public int specialCategory(boolean[] isSelected) {
        return -1;
    }

    /** 特殊规则下的保留方案（如Joker），没有时返回null（默认没有特殊规则） */
    public KeepPlan specialKeepPlan(int[] numbers) {
        return null;
    }

    /** 指定得分项在选择得分项时的优先级（默认直接使用得分） */
    public int rankCategory(int category) {
        return calculateScore(category);
    }

    /**
     * 为每个未填写的得分项计算保留方案，返回优先级最高的方案的保留状态
     *
     * @param throwsLeft 剩余投掷次数
     */
    public boolean[] chooseDiceToKeep(int throwsLeft) {
        int[] numbers = diceNumbers.getValue();
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        if (numbers == null || isSelected == null)
            return new boolean[numDice];

        // 特殊规则（如Joker）
        KeepPlan special = specialKeepPlan(numbers);
        if (special != null)
            return special.keep();

        // 优先级相同时取下标较小的得分项的方案
        int bestRank = Integer.MIN_VALUE;
        boolean[] bestKeep = new boolean[numDice];
        for (int c = 0; c < numCategories; c++) {
            if (isSelected[c])
                continue;

            KeepPlan plan = planForCategory(numbers, c, throwsLeft);
            if (plan != null && plan.rank() > bestRank) {
                bestRank = plan.rank();
                bestKeep = plan.keep();
            }
        }
        return bestKeep;
    }

    /** 计算指定得分项的保留方案，默认返回null表示该得分项没有保留方案 */
    public KeepPlan planForCategory(int[] numbers, int category, int throwsLeft) {
        return null;
    }

    /** 还差missing颗骰子成组合时的优先级（已集齐时为最高优先级），bonus为额外优先级 */
    protected static int rankByMissing(int weight, int throwsLeft, int missing, int bonus) {
        return missing <= 0 ? Integer.MAX_VALUE : weight * throwsLeft / missing + bonus;
    }

    /** 计算上区得分项的保留方案：保留该点数的全部骰子 */
    protected KeepPlan planForUpperCategory(int[] numbers, int category) {
        int face = category + 1;
        int count = diceCounts[face];
        // 该点数已是出现次数最多的点数且有3颗以上时，优先考虑上区奖励分
        int rank = mostFrequentFace() == face && count >= 3 ? 20 + face : face * count + face;
        return new KeepPlan(keepFace(numbers, face), rank);
    }

    /**
     * 计算3个同点/4个同点的保留方案：优先保留5、6点的骰子，<br>
     * 最后一次投掷且已有3颗以上不小于3的点数时保留出现次数最多的点数
     */
    protected KeepPlan planForSameKind(int[] numbers, int throwsLeft) {
        int face = sameKindFace(throwsLeft);
        if (face == 0)
            return new KeepPlan(new boolean[numDice], 3);

        return new KeepPlan(keepFace(numbers, face), face * diceCounts[face]);
    }

    /** 3个同点/4个同点要保留的点数，返回0表示没有值得保留的点数 */
    private int sameKindFace(int throwsLeft) {
        List<Integer> faces = sortedFaces();
        int mostFrequent = faces.get(0);
        if (mostFrequent >= 5)
            return mostFrequent;

        int second = faces.get(1);
        if (second >= 5 && diceCounts[second] > 0)
            return second;
        if (throwsLeft == 1 && diceCounts[mostFrequent] >= 3 && mostFrequent >= 3)
            return mostFrequent;

        for (int face : faces) {
            if (face >= 5 && diceCounts[face] > 0)
                return face;
        }
        return 0;
    }

    /** 计算葫芦的保留方案：保留出现次数最多的点数的3颗和次多的点数的2颗 */
    protected KeepPlan planForFullHouse(int[] numbers, int throwsLeft) {
        List<Integer> faces = sortedFaces();
        if (diceCounts[faces.get(0)] == 1)
            return new KeepPlan(new boolean[numDice], 1);

        boolean[] keep = new boolean[numDice];
        markFace(numbers, keep, faces.get(0), 3);
        markFace(numbers, keep, faces.get(1), 2);
        return new KeepPlan(keep, rankByMissing(8, throwsLeft, numDice - ArrayUtil.count(keep, true), 0));
    }

    /** 计算n对的保留方案：保留点数最大的n对（不足n对时保留已凑齐的对） */
    protected KeepPlan planForPairs(int[] numbers, int n, int throwsLeft) {
        boolean[] keep = new boolean[numDice];
        int pairs = 0;
        for (int face = 6; face >= 1 && pairs < n; face--) {
            if (diceCounts[face] >= 2) {
                pairs++;
                markFace(numbers, keep, face, 2);
            }
        }
        return new KeepPlan(keep, rankByMissing(6, throwsLeft, numDice - ArrayUtil.count(keep, true), 0));
    }

    /**
     * 计算连顺的保留方案：从各候选起点开始的连顺中取保留骰子最多的一组
     *
     * @param starts 候选起点
     * @param length 连顺长度
     * @param weight 优先级权重
     * @param bonus 额外优先级
     */
    protected KeepPlan planForStraight(int[] numbers, int[] starts, int length, int weight, int bonus, int throwsLeft) {
        boolean[] bestKeep = new boolean[numDice];
        int bestCount = 0;
        for (int start : starts) {
            boolean[] keep = keepStraight(numbers, start, length);
            int count = ArrayUtil.count(keep, true);
            // 保留骰子数相同时取起点较小的方案
            if (count > bestCount) {
                bestCount = count;
                bestKeep = keep;
            }
        }
        return new KeepPlan(bestKeep, rankByMissing(weight, throwsLeft, length - bestCount, bonus));
    }

    /**
     * 计算顺子的保留方案：保留各点数中的一颗骰子（点数不连续时同样适用）<br>
     * 与{@link #planForStraight(int[], int[], int, int, int, int)}的区别是后者遇到缺失的点数即停止
     *
     * @param faces 顺子包含的点数
     * @param weight 优先级权重
     * @param bonus 还差1颗时的额外优先级
     */
    protected KeepPlan planForFacesStraight(int[] numbers, int[] faces, int weight, int bonus, int throwsLeft) {
        boolean[] keep = new boolean[numDice];
        for (int face : faces)
            markFace(numbers, keep, face, 1);

        int missing = numDice - ArrayUtil.count(keep, true);
        return new KeepPlan(keep, rankByMissing(weight, throwsLeft, missing, missing == 1 ? bonus : 0));
    }

    /** 各点数按（出现次数降序，点数降序）排序后的结果 */
    protected List<Integer> sortedFaces() {
        List<Integer> faces = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        faces.sort((a, b) -> diceCounts[a] != diceCounts[b]
                ? diceCounts[b] - diceCounts[a] : b - a);
        return faces;
    }

    /** 在keep中标记最多count颗点数为face且未标记的骰子 */
    protected static void markFace(int[] numbers, boolean[] keep, int face, int count) {
        for (int i = 0; i < numbers.length && count > 0; i++) {
            if (numbers[i] == face && !keep[i]) {
                keep[i] = true;
                count--;
            }
        }
    }

    /** 返回保留指定点数的骰子的锁定状态 */
    protected static boolean[] keepFace(int[] numbers, int face) {
        boolean[] keep = new boolean[numbers.length];
        for (int i = 0; i < numbers.length; i++)
            keep[i] = numbers[i] == face;
        return keep;
    }

    /** 返回保留出现次数最多的点数中最多count颗骰子的锁定状态 */
    protected boolean[] keepSameFace(int[] numbers, int count) {
        boolean[] keep = new boolean[numbers.length];
        markFace(numbers, keep, mostFrequentFace(), count);
        return keep;
    }

    /** 返回保留点数不小于5的骰子的锁定状态 */
    protected static boolean[] keepHighDice(int[] numbers) {
        boolean[] keep = new boolean[numbers.length];
        for (int i = 0; i < numbers.length; i++)
            keep[i] = numbers[i] >= 5;
        return keep;
    }

    /**
     * 返回保留从startFace开始、长度不超过length的连顺中的骰子的锁定状态<br>
     * 遇到缺失的点数即停止，每个点数只保留一颗骰子
     */
    protected boolean[] keepStraight(int[] numbers, int startFace, int length) {
        boolean[] keep = new boolean[numbers.length];
        for (int face = startFace; face < startFace + length && face <= 6; face++) {
            if (diceCounts[face] == 0)
                break;

            for (int i = 0; i < numbers.length; i++) {
                if (numbers[i] == face) {
                    keep[i] = true;
                    break;
                }
            }
        }
        return keep;
    }

    /** 已保留的骰子的点数之和 */
    protected static int sumOfKeptDice(int[] numbers, boolean[] keep) {
        int sum = 0;
        for (int i = 0; i < numbers.length && i < keep.length; i++) {
            if (keep[i])
                sum += numbers[i];
        }
        return sum;
    }

    /** 出现次数最多的点数（次数相同时取点数较大的） */
    protected int mostFrequentFace() {
        int bestFace = 1;
        for (int face = 2; face <= 6; face++) {
            if (diceCounts[face] >= diceCounts[bestFace])
                bestFace = face;
        }
        return bestFace;
    }

    /** 出现次数最多的点数的出现次数 */
    protected int maxCount() {
        int max = 0;
        for (int face = 1; face <= 6; face++)
            max = Math.max(max, diceCounts[face]);
        return max;
    }

    /** 是否所有玩家都已选完得分项 */
    protected boolean isAllPlayersFinished() {
        for (BaseGameData playerData : gameData) {
            if (((YahtzeeGameData) playerData).numSelected < numCategories)
                return false;
        }
        return true;
    }

    /** 更新指定玩家刚选择的得分项的得分、上区总分、奖励分和游戏总分 */
    private void updateScores(YahtzeeGameData playerData, int category) {
        int[] currentScores = playerData.scores.getValue();
        boolean[] isSelected = playerData.selected.getValue();
        if (currentScores == null || isSelected == null)
            return;

        // 总是重新计算刚选择的得分项：掷骰子后计算的只是预估得分，
        // 且计算机玩家在界面上不显示预估得分，需在此时更新scores以显示该得分项的得分
        currentScores[category] = calculateScore(category);
        playerData.scores.setValue(currentScores);

        int upperTotal = 0;
        for (int i = 0; i < NUM_UPPER_CATEGORIES; i++) {
            if (isSelected[i])
                upperTotal += currentScores[i];
        }

        int bonus = upperTotal >= bonusThreshold ? bonusValue : 0;
        int total = upperTotal + bonus;
        for (int i = NUM_UPPER_CATEGORIES; i < numCategories; i++) {
            if (isSelected[i])
                total += currentScores[i];
        }

        playerData.upperTotalScore.setValue(upperTotal);
        playerData.bonusScore.setValue(bonus);
        playerData.score.setValue(total);
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
    public abstract BaseScore createScoreEntity();

    /** 将得分保存到数据库，并返回排名 */
    public abstract int saveScoreToDatabase(BaseScore score);

    @Override
    public void reset() {
        clickable.setValue(false);
        super.reset();
        startTurn();
    }
}
