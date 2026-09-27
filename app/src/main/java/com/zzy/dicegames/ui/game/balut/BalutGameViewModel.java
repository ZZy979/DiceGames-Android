package com.zzy.dicegames.ui.game.balut;

import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.ui.game.BaseGameViewModel;
import com.zzy.dicegames.ui.game.yahtzee.KeepPlan;
import com.zzy.dicegames.utils.ArrayUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    /** 能立即得分时优先选择的高价值得分项（按优先级排序） */
    private static final Category[] GOOD_CATEGORIES = {
            Category.BALUT, Category.STRAIGHT, Category.FULL_HOUSE
    };

    /** 当前玩家的得分项是否可点击 */
    private final MutableLiveData<Boolean> clickable = new MutableLiveData<>(false);

    /**
     * Balut的玩家数据<br>
     * 游戏总分为基类的score
     */
    public static class BalutGameData extends BaseGameData {
        /** 每个得分项的得分（未选择的为预估得分） */
        public final MutableLiveData<int[][]> scores =
                new MutableLiveData<>(new int[NUM_CATEGORIES][MAX_SELECTIONS]);

        /** 每个得分项已选择次数 */
        public final MutableLiveData<int[]> selectCount = new MutableLiveData<>(new int[NUM_CATEGORIES]);

        /** 已达到最大选择次数的得分项个数 */
        public int numSelected = 0;

        /** 每个得分项的总分 */
        public final MutableLiveData<int[]> categoryScores = new MutableLiveData<>(new int[NUM_CATEGORIES]);

        /** 每个得分项的点数 */
        public final MutableLiveData<int[]> categoryPoints = new MutableLiveData<>(new int[NUM_CATEGORIES]);

        /** 总分点数 */
        public final MutableLiveData<Integer> totalScorePoints = new MutableLiveData<>(0);

        /** 总点数 */
        public final MutableLiveData<Integer> totalPoints = new MutableLiveData<>(0);

        @Override
        public void reset() {
            super.reset();
            scores.setValue(new int[NUM_CATEGORIES][MAX_SELECTIONS]);
            selectCount.setValue(new int[NUM_CATEGORIES]);
            numSelected = 0;
            categoryScores.setValue(new int[NUM_CATEGORIES]);
            categoryPoints.setValue(new int[NUM_CATEGORIES]);
            totalScorePoints.setValue(0);
            totalPoints.setValue(0);
        }
    }

    public BalutGameViewModel() {
        super(5, 3, 1, 2);
        initGameData(1);
        disableAllDice();
    }

    @Override
    protected BaseGameData createGameData() {
        return new BalutGameData();
    }

    /** 返回当前玩家的数据 */
    protected BalutGameData data() {
        return data(getCurrentPlayerValue());
    }

    /** 返回指定玩家的数据 */
    public BalutGameData data(int player) {
        return (BalutGameData) gameData[player];
    }

    public LiveData<Boolean> getClickable() {
        return clickable;
    }

    public boolean isClickable() {
        return Boolean.TRUE.equals(clickable.getValue());
    }

    public LiveData<int[][]> getScores() {
        return data().scores;
    }

    public LiveData<int[]> getSelectCount() {
        return data().selectCount;
    }

    public int getNumSelected() {
        return data().numSelected;
    }

    public LiveData<int[]> getCategoryScores() {
        return data().categoryScores;
    }

    public LiveData<int[]> getCategoryPoints() {
        return data().categoryPoints;
    }

    public LiveData<Integer> getTotalScore() {
        return data().score;
    }

    public LiveData<Integer> getTotalScorePoints() {
        return data().totalScorePoints;
    }

    public LiveData<Integer> getTotalPoints() {
        return data().totalPoints;
    }

    @Override
    public void updateDiceNumbers(int... numbers) {
        super.updateDiceNumbers(numbers);
        updateScores();
        if (isComputerTurn() && !diceRolling)
            postComputerAction(this::computerTurn);
    }

    /** 根据骰子点数更新预估得分 */
    protected void updateScores() {
        BalutGameData playerData = data();
        int[] currentSelectCount = playerData.selectCount.getValue();
        int[][] currentScores = playerData.scores.getValue();
        if (currentSelectCount == null || currentScores == null)
            return;

        for (int i = 0; i < currentScores.length; i++) {
            if (currentSelectCount[i] < currentScores[i].length)
                currentScores[i][currentSelectCount[i]] = calculateScore(i);
        }
        playerData.scores.setValue(currentScores);
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
        BalutGameData playerData = data();
        int[] currentSelectCount = playerData.selectCount.getValue();
        if (currentSelectCount == null || currentSelectCount[category] >= MAX_SELECTIONS)
            return;

        currentSelectCount[category]++;
        playerData.selectCount.setValue(currentSelectCount);
        if (currentSelectCount[category] >= MAX_SELECTIONS)
            playerData.numSelected++;

        // 掷骰子后已计算过预估得分，此处无需更新scores
        updateTotalScore();
        updatePoints();

        if (isAllPlayersFinished()) {
            gameOver();
            return;
        }
        nextPlayer();
        startTurn();
    }

    /** 是否所有玩家都已选完所有格子 */
    protected boolean isAllPlayersFinished() {
        for (BaseGameData playerData : gameData) {
            if (((BalutGameData) playerData).numSelected < NUM_CATEGORIES)
                return false;
        }
        return true;
    }

    /** 开始当前玩家的回合 */
    protected void startTurn() {
        resetDiceWindow();
        if (isComputerTurn())
            postComputerAction(this::computerTurn);
    }

    private void updateTotalScore() {
        BalutGameData playerData = data();
        int[][] currentScores = playerData.scores.getValue();
        int[] currentSelectCount = playerData.selectCount.getValue();
        int[] currentCategoryScores = playerData.categoryScores.getValue();
        if (currentScores == null || currentSelectCount == null || currentCategoryScores == null)
            return;

        int total = 0;
        for (int i = 0; i < NUM_CATEGORIES; i++) {
            currentCategoryScores[i] = ArrayUtil.sum(currentScores[i], 0, currentSelectCount[i]);
            total += currentCategoryScores[i];
        }

        playerData.categoryScores.setValue(currentCategoryScores);
        playerData.score.setValue(total);
    }

    private void updatePoints() {
        BalutGameData playerData = data();
        int[][] currentScores = playerData.scores.getValue();
        int[] currentSelectCount = playerData.selectCount.getValue();
        int[] currentCategoryPoints = playerData.categoryPoints.getValue();
        Integer totalScore = playerData.score.getValue();
        if (currentScores == null || currentSelectCount == null || currentCategoryPoints == null
                || totalScore == null)
            return;

        int newTotalScorePoints = 0;
        int newTotalPoints = 0;
        for (int i = 0; i < NUM_CATEGORIES; i++) {
            currentCategoryPoints[i] = calculatePoints(i, currentSelectCount[i], currentScores[i]);
            newTotalPoints += currentCategoryPoints[i];
        }
        if (playerData.numSelected == NUM_CATEGORIES) {
            newTotalScorePoints = calculateTotalScorePoints(totalScore);
            newTotalPoints += newTotalScorePoints;
        }

        playerData.categoryPoints.setValue(currentCategoryPoints);
        playerData.totalScorePoints.setValue(newTotalScorePoints);
        playerData.totalPoints.setValue(newTotalPoints);
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

    /** 创建得分实体（保存人类玩家的得分和点数，最后结束游戏的可能是计算机玩家，因此不能用当前玩家的数据） */
    public BalutScore createScoreEntity() {
        BalutGameData humanData = data(PLAYER_HUMAN);
        int[][] finalScores = humanData.scores.getValue();
        Integer totalScore = humanData.score.getValue();
        Integer totalPoints = humanData.totalPoints.getValue();
        if (finalScores == null || totalScore == null || totalPoints == null)
            return null;

        int numBalut = ArrayUtil.count(finalScores[Category.BALUT.ordinal()], x -> x > 0);
        // 无计算机玩家时计算机玩家的得分和点数均为0
        int computerScore = 0, computerPoints = 0;
        if (isMultiplayer()) {
            BalutGameData computerData = data(PLAYER_COMPUTER);
            Integer score = computerData.score.getValue(), points = computerData.totalPoints.getValue();
            computerScore = score == null ? 0 : score;
            computerPoints = points == null ? 0 : points;
        }
        return new BalutScore(LocalDate.now().toString(), totalScore, getNumPlayersValue(),
                computerScore, totalPoints, computerPoints, numBalut);
    }

    /** 将得分保存到数据库，并返回排名 */
    public int saveScoreToDatabase(BalutScore score) {
        var dao = scoreDatabase.balutScoreDao();
        dao.insert(score);
        return dao.rank(score.points, score.score);
    }

    // ---------- 计算机玩家策略 ----------

    /** 计算机玩家回合：第一次先掷骰子，之后根据策略决定继续掷骰子（并锁定要保留的骰子）或选择得分项 */
    protected void computerTurn() {
        if (!isComputerTurn() || diceRolling)
            return;

        // 第一次投掷，无需保留骰子
        if (!hasRolled()) {
            rollDiceWithAnimation();
            return;
        }

        // 投掷次数已用完，选择得分项
        if (!hasRemainingRolls()) {
            selectComputerCategory();
            return;
        }

        // 还有剩余投掷次数，先分析该保留哪些骰子
        analyzeDiceToKeep();
    }

    /**
     * 计算机玩家分析该保留哪些骰子：能立即得到高价值得分项时选择该得分项，<br>
     * 否则保留优先级最高的方案
     */
    protected void analyzeDiceToKeep() {
        // 能立即得到高价值得分项时不再掷骰子
        int category = goodCategory();
        if (category >= 0) {
            doSelect(category);
            return;
        }

        boolean[] keep = chooseDiceToKeep(getRemainingRollsValue());
        // 所有骰子都要保留时再掷骰子没有意义，直接选择得分项
        if (ArrayUtil.all(keep, true)) {
            selectComputerCategory();
            return;
        }
        setDiceLocked(keep);
        postComputerAction(this::rollDiceWithAnimation);
    }

    /** 计算机玩家选择得分项 */
    protected void selectComputerCategory() {
        int category = computerChooseCategory();
        if (category >= 0)
            doSelect(category);
    }

    /**
     * 计算机玩家选择一个还有空格的得分项：优先选择能立即得分的高价值得分项，<br>
     * 否则选择优先级最高的得分项（优先级相同时取下标较小的项），没有可选择的得分项时返回-1
     */
    protected int computerChooseCategory() {
        int category = goodCategory();
        if (category >= 0)
            return category;

        int bestCategory = -1, bestRank = Integer.MIN_VALUE;
        for (int c = 0; c < NUM_CATEGORIES; c++) {
            int rank = rankCategory(c);
            if (rank > bestRank) {
                bestRank = rank;
                bestCategory = c;
            }
        }
        return bestCategory;
    }

    /** 能立即得分的第一个高价值得分项，没有时返回-1 */
    protected int goodCategory() {
        for (Category goodCategory : GOOD_CATEGORIES) {
            int category = goodCategory.ordinal();
            if (canSelect(category) && calculateScore(category) > 0)
                return category;
        }
        return -1;
    }

    /** 指定得分项是否还有可选择的格子 */
    protected boolean canSelect(int category) {
        int[] selectCount = data().selectCount.getValue();
        return selectCount != null && selectCount[category] < MAX_SELECTIONS;
    }

    /** 指定得分项在选择得分项时的优先级（没有可选择的格子时返回{@link Integer#MIN_VALUE}） */
    protected int rankCategory(int category) {
        if (!canSelect(category))
            return Integer.MIN_VALUE;

        int score = calculateScore(category);
        return switch (Category.values()[category]) {
            // 选择总是可以得到当前骰子总点数，降低其优先级以优先选择其他得分项
            case CHOICE -> score - 10;
            // 上区得分项的平均每颗骰子点数较高时优先选择
            case FOURS, FIVES, SIXES -> {
                int avg = score / (category + 4);
                yield avg >= 3 ? avg * 6 + 20 : avg * 6;
            }
            default -> score;
        };
    }

    /**
     * 为每个还有空格的得分项计算保留方案，返回优先级最高的方案的保留状态
     *
     * @param throwsLeft 剩余投掷次数
     */
    protected boolean[] chooseDiceToKeep(int throwsLeft) {
        int[] numbers = diceNumbers.getValue();
        if (numbers == null)
            return new boolean[numDice];

        // 优先级相同时取下标较小的得分项的方案
        int bestRank = Integer.MIN_VALUE;
        boolean[] bestKeep = new boolean[numDice];
        for (int c = 0; c < NUM_CATEGORIES; c++) {
            KeepPlan plan = planForCategory(numbers, c, throwsLeft);
            if (plan != null && plan.rank() > bestRank) {
                bestRank = plan.rank();
                bestKeep = plan.keep();
            }
        }
        return bestKeep;
    }

    /** 计算指定得分项的保留方案，没有可选择的格子时返回null */
    protected KeepPlan planForCategory(int[] numbers, int category, int throwsLeft) {
        if (!canSelect(category))
            return null;

        return switch (Category.values()[category]) {
            case FOURS, FIVES, SIXES -> planForFace(numbers, category + 4);
            case STRAIGHT -> planForStraight(numbers, throwsLeft);
            case FULL_HOUSE -> planForFullHouse(numbers, throwsLeft);
            case CHOICE -> planForChoice(numbers);
            case BALUT -> planForBalut(numbers, throwsLeft);
        };
    }

    /**
     * 计算保留指定点数（4、5、6点）的骰子的方案：<br>
     * 该点数为出现次数最多的点数时按有望得到点数奖励分考虑，否则按当前得分考虑
     */
    protected KeepPlan planForFace(int[] numbers, int face) {
        int count = diceCounts[face];
        int rank = mostFrequentFace() == face ? 15 + face : face * count + face;
        return new KeepPlan(keepFace(numbers, face), rank);
    }

    /** 计算连顺的保留方案：保留1~5或2~6中骰子较多的一组 */
    protected KeepPlan planForStraight(int[] numbers, int throwsLeft) {
        boolean[] bestKeep = null;
        int bestCount = -1;
        for (int start = 1; start <= 2; start++) {
            boolean[] keep = keepStraight(numbers, start, numDice);
            int count = ArrayUtil.count(keep, true);
            // 保留骰子数相同时取起点较小的方案
            if (count > bestCount) {
                bestCount = count;
                bestKeep = keep;
            }
        }
        return new KeepPlan(bestKeep, rankByMissing(13, throwsLeft, numDice - bestCount));
    }

    /** 计算葫芦的保留方案：保留出现次数最多的点数的3颗和次多的点数的2颗 */
    protected KeepPlan planForFullHouse(int[] numbers, int throwsLeft) {
        List<Integer> faces = sortedFaces();
        // 所有骰子点数都不同，无法在剩余次数内凑成葫芦
        if (diceCounts[faces.get(0)] == 1)
            return new KeepPlan(new boolean[numDice], 1);

        boolean[] keep = new boolean[numDice];
        markFace(numbers, keep, faces.get(0), 3);
        markFace(numbers, keep, faces.get(1), 2);
        return new KeepPlan(keep, rankByMissing(8, throwsLeft, numDice - ArrayUtil.count(keep, true)));
    }

    /** 计算选择（任意组合）的保留方案：保留点数不小于5的骰子 */
    protected KeepPlan planForChoice(int[] numbers) {
        boolean[] keep = keepHighDice(numbers);
        return new KeepPlan(keep, sumOfKeptDice(numbers, keep) / 3);
    }

    /** 计算Balut的保留方案：保留出现次数最多的点数的全部骰子 */
    protected KeepPlan planForBalut(int[] numbers, int throwsLeft) {
        int face = mostFrequentFace();
        int count = diceCounts[face];
        int rank = rankByMissing(9, throwsLeft, numDice - count);
        // 点数较小且数量较少时不值得追求Balut
        if (face < 4 && count < 4)
            rank /= 2;
        return new KeepPlan(keepSameFace(numbers, count), rank);
    }

    /** 还差missing颗骰子成组合时的优先级（已集齐时为最高优先级） */
    private static int rankByMissing(int weight, int throwsLeft, int missing) {
        return missing <= 0 ? Integer.MAX_VALUE : weight * throwsLeft / missing;
    }

    /** 在keep中标记最多count颗点数为face且未标记的骰子 */
    private static void markFace(int[] numbers, boolean[] keep, int face, int count) {
        for (int i = 0; i < numbers.length && count > 0; i++) {
            if (numbers[i] == face && !keep[i]) {
                keep[i] = true;
                count--;
            }
        }
    }

    /** 返回保留指定点数的骰子的锁定状态 */
    private static boolean[] keepFace(int[] numbers, int face) {
        boolean[] keep = new boolean[numbers.length];
        for (int i = 0; i < numbers.length; i++)
            keep[i] = numbers[i] == face;
        return keep;
    }

    /** 返回保留出现次数最多的点数中最多count颗骰子的锁定状态 */
    private boolean[] keepSameFace(int[] numbers, int count) {
        boolean[] keep = new boolean[numbers.length];
        markFace(numbers, keep, mostFrequentFace(), count);
        return keep;
    }

    /** 返回保留点数不小于5的骰子的锁定状态 */
    private static boolean[] keepHighDice(int[] numbers) {
        boolean[] keep = new boolean[numbers.length];
        for (int i = 0; i < numbers.length; i++)
            keep[i] = numbers[i] >= 5;
        return keep;
    }

    /**
     * 返回保留从startFace开始、长度不超过length的连顺中的骰子的锁定状态<br>
     * 遇到缺失的点数即停止，每个点数只保留一颗骰子
     */
    private boolean[] keepStraight(int[] numbers, int startFace, int length) {
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
    private static int sumOfKeptDice(int[] numbers, boolean[] keep) {
        int sum = 0;
        for (int i = 0; i < numbers.length && i < keep.length; i++) {
            if (keep[i])
                sum += numbers[i];
        }
        return sum;
    }

    /** 各点数按（出现次数降序，点数降序）排序后的结果 */
    private List<Integer> sortedFaces() {
        List<Integer> faces = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        faces.sort((a, b) -> diceCounts[a] != diceCounts[b]
                ? diceCounts[b] - diceCounts[a] : b - a);
        return faces;
    }

    /** 出现次数最多的点数（次数相同时取点数较大的） */
    private int mostFrequentFace() {
        int bestFace = 1;
        for (int face = 2; face <= 6; face++) {
            if (diceCounts[face] >= diceCounts[bestFace])
                bestFace = face;
        }
        return bestFace;
    }

    @Override
    public void reset() {
        clickable.setValue(false);
        super.reset();
        startTurn();
    }
}
