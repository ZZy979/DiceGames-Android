package com.zzy.dicegames.ui.game.yahtzee;

import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.yahtzee.YahtzeeScore;
import com.zzy.dicegames.utils.ArrayUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class YahtzeeGameViewModel extends BaseYahtzeeGameViewModel {
    /** 得分项 */
    public enum Category {
        ONES, TWOS, THREES, FOURS, FIVES, SIXES,
        THREE_OF_A_KIND, FOUR_OF_A_KIND, FULL_HOUSE,
        SMALL_STRAIGHT, LARGE_STRAIGHT, CHANCE, YAHTZEE
    }

    /** 能得分时优先选择的高价值得分项（按优先级排序） */
    private static final Category[] GOOD_CATEGORIES = {
            Category.YAHTZEE, Category.LARGE_STRAIGHT,
            Category.FULL_HOUSE, Category.SMALL_STRAIGHT
    };

    /** 得分项的保留方案：要保留的骰子及其优先级，优先级越大越值得保留 */
    private record KeepPlan(boolean[] keep, int rank) {}

    public YahtzeeGameViewModel() {
        super(5, 3, 1, 2, Category.values().length, 63, 35);
    }

    @Override
    public int calculateScore(int category) {
        boolean isJoker = isJoker();
        int score = 0;
        switch (Category.values()[category]) {
            case ONES: case TWOS: case THREES: case FOURS: case FIVES: case SIXES:
                score = diceCounts[category + 1] * (category + 1);
                break;
            case THREE_OF_A_KIND:
                for (int i = 1; i <= 6; i++) {
                    if (diceCounts[i] >= 3 || isJoker) {
                        score = sumOfDice;
                        break;
                    }
                }
                break;
            case FOUR_OF_A_KIND:
                for (int i = 1; i <= 6; i++) {
                    if (diceCounts[i] >= 4 || isJoker) {
                        score = sumOfDice;
                        break;
                    }
                }
                break;
            case FULL_HOUSE:
                if (isFullHouse() || isJoker) score = 25;
                break;
            case SMALL_STRAIGHT:
                if (hasStraight(4) || isJoker) score = 30;
                break;
            case LARGE_STRAIGHT:
                if (hasStraight(5) || isJoker) score = 40;
                break;
            case CHANCE:
                score = sumOfDice;
                break;
            case YAHTZEE:
                if (isAllSame()) score = 50;
                break;
        }
        return score;
    }

    /** 是否具有长度超过length的连顺 */
    private boolean hasStraight(int length) {
        int consecutive = 0;
        for (int i = 1; i <= 6; i++) {
            if (diceCounts[i] > 0) {
                consecutive++;
                if (consecutive >= length)
                    return true;
            }
            else
                consecutive = 0;
        }
        return false;
    }

    /** 是否满足葫芦（三个同点+一对） */
    private boolean isFullHouse() {
        boolean hasThree = false, hasTwo = false;
        for (int i = 1; i <= 6; i++) {
            if (diceCounts[i] == 3) hasThree = true;
            if (diceCounts[i] == 2) hasTwo = true;
        }
        return hasThree && hasTwo;
    }

    @Override
    public YahtzeeScore createScoreEntity() {
        YahtzeeGameData humanData = data(PLAYER_HUMAN);
        int[] finalScores = humanData.scores.getValue();
        Integer totalScore = humanData.score.getValue();
        Integer bonusScore = humanData.bonusScore.getValue();
        if (finalScores == null || totalScore == null || bonusScore == null)
            return null;
        int computerScore = isMultiplayer() ? getPlayerScoreValue(PLAYER_COMPUTER) : 0;
        return new YahtzeeScore(LocalDate.now().toString(), totalScore,
                getNumPlayersValue(), computerScore,
                bonusScore > 0, finalScores[finalScores.length - 1] > 0);
    }

    @Override
    public int saveScoreToDatabase(BaseScore score) {
        var dao = scoreDatabase.yahtzeeScoreDao();
        dao.insert((YahtzeeScore) score);
        return dao.rank(score.score);
    }

    // ---------- 计算机玩家AI ----------

    /**
     * 分析该保留哪些骰子：能直接得到高价值得分项时选择该得分项，<br>
     * 否则为每个未填写的得分项计算一个保留方案，采用其中优先级最高的方案
     */
    @Override
    protected void analyzeDiceToKeep() {
        int throwsLeft = getRemainingRollsValue();
        if (throwsLeft <= 0)
            return;

        // 能直接得到高价值得分项时不再掷骰子
        int category = goodCategory(throwsLeft);
        if (category >= 0) {
            doSelect(category);
            return;
        }

        keepDiceAndRoll(chooseDiceToKeep(throwsLeft));
    }

    @Override
    protected int computerChooseCategory() {
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        if (isSelected == null)
            return -1;

        // Joker：选择得分最高的得分项
        if (isJoker())
            return jokerCategory(isSelected);

        // 优先选择能得分的高价值得分项
        int category = goodCategory(0);
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

    /**
     * 返回能得分的高价值得分项，没有时返回-1
     *
     * @param throwsLeft 剩余投掷次数
     */
    private int goodCategory(int throwsLeft) {
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        if (isSelected == null)
            return -1;

        for (Category good : GOOD_CATEGORIES) {
            // 已无投掷次数且大顺还未填写时不为小顺保留机会，由rankCategory()决定是否选择小顺
            if (good == Category.SMALL_STRAIGHT && throwsLeft == 0
                    && !isSelected[Category.LARGE_STRAIGHT.ordinal()])
                continue;

            int c = good.ordinal();
            if (!isSelected[c] && calculateScore(c) > 0)
                return c;
        }
        return -1;
    }

    /** Joker时选择得分最高的得分项（CHANCE的得分减15，得分相同时取下标较大的项） */
    private int jokerCategory(boolean[] isSelected) {
        int bestCategory = -1, bestScore = Integer.MIN_VALUE;
        for (int c = 0; c < numCategories; c++) {
            if (isSelected[c])
                continue;

            int score = calculateScore(c);
            if (Category.values()[c] == Category.CHANCE)
                score = Math.max(score - 15, 0);
            if (score >= bestScore) {
                bestScore = score;
                bestCategory = c;
            }
        }
        return bestCategory;
    }

    /** 指定得分项在选择得分项时的优先级 */
    private int rankCategory(int category) {
        int score = calculateScore(category);
        int target = category + 1;
        return switch (Category.values()[category]) {
            // 上区按平均得分（即该点数的颗数）加权，4~6点上区有3颗以上时额外奖励
            case ONES, TWOS, THREES, FOURS, FIVES, SIXES ->
                    target >= 4 && diceCounts[target] >= 3 ? 6 * diceCounts[target] + 20 : 6 * diceCounts[target];
            case CHANCE -> score - 10;
            case FOUR_OF_A_KIND -> score + 2;
            default -> score;
        };
    }

    /**
     * 为每个未填写的得分项计算保留方案，返回优先级最高的方案的保留状态
     *
     * @param throwsLeft 剩余投掷次数
     */
    protected boolean[] chooseDiceToKeep(int throwsLeft) {
        int[] numbers = diceNumbers.getValue();
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        if (numbers == null || isSelected == null)
            return new boolean[numDice];

        // Joker：已得到Yahtzee且掷出4颗以上相同点数时，只保留其中4颗以争取再次得到Yahtzee
        if (hasScoredYahtzee(isSelected) && maxCount() >= 4)
            return keepSameFace(numbers, 4);

        // 优先级相同时取下标较小的得分项的方案
        int bestRank = Integer.MIN_VALUE;
        boolean[] bestKeep = new boolean[numDice];
        for (int c = 0; c < numCategories; c++) {
            if (isSelected[c])
                continue;

            KeepPlan plan = planForCategory(numbers, c, throwsLeft);
            if (plan.rank() > bestRank) {
                bestRank = plan.rank();
                bestKeep = plan.keep();
            }
        }
        return bestKeep;
    }

    /** 计算指定得分项的保留方案 */
    private KeepPlan planForCategory(int[] numbers, int category, int throwsLeft) {
        return switch (Category.values()[category]) {
            case ONES, TWOS, THREES, FOURS, FIVES, SIXES ->
                    planForUpperCategory(numbers, category);
            case THREE_OF_A_KIND, FOUR_OF_A_KIND ->
                    planForSameKind(numbers, throwsLeft);
            case FULL_HOUSE ->
                    planForFullHouse(numbers, throwsLeft);
            // 小顺：1~4、2~5、3~6中保留骰子最多的一组
            case SMALL_STRAIGHT ->
                    planForStraight(numbers, new int[] {1, 2, 3}, 4, 9, throwsLeft);
            // 大顺：1~5和2~6中保留骰子最多的一组
            case LARGE_STRAIGHT ->
                    planForStraight(numbers, new int[] {1, 2}, 5, 11, throwsLeft);
            case CHANCE -> {
                boolean[] keep = keepHighDice(numbers);
                yield new KeepPlan(keep, sumOfKeptDice(numbers, keep) / 3);
            }
            case YAHTZEE -> {
                boolean[] keep = keepSameFace(numbers, numDice);
                yield new KeepPlan(keep,
                        rankByMissing(9, throwsLeft, numDice - ArrayUtil.count(keep, true)));
            }
        };
    }

    /** 计算上区得分项的保留方案：保留该点数的全部骰子 */
    private KeepPlan planForUpperCategory(int[] numbers, int category) {
        int face = category + 1;
        int count = diceCounts[face];
        // 该点数已是出现次数最多的点数且有3颗以上时，优先考虑上区奖励分
        int rank = mostFrequentFace() == face && count >= 3 ? 20 + face : face * (count + 1);
        return new KeepPlan(keepFace(numbers, face), rank);
    }

    /**
     * 计算3个同点/4个同点的保留方案：优先保留5、6点的骰子，<br>
     * 最后一次投掷且已有3颗以上不小于3的点数时保留出现次数最多的点数
     */
    private KeepPlan planForSameKind(int[] numbers, int throwsLeft) {
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
    private KeepPlan planForFullHouse(int[] numbers, int throwsLeft) {
        List<Integer> faces = sortedFaces();
        if (diceCounts[faces.get(0)] == 1)
            return new KeepPlan(new boolean[numDice], 1);

        boolean[] keep = new boolean[numDice];
        markFace(numbers, keep, faces.get(0), 3);
        markFace(numbers, keep, faces.get(1), 2);
        return new KeepPlan(keep, rankByMissing(8, throwsLeft, numDice - ArrayUtil.count(keep, true)));
    }

    /**
     * 计算连顺的保留方案：从各候选起点开始的连顺中取保留骰子最多的一组
     *
     * @param starts 候选起点
     * @param length 连顺长度
     * @param weight 优先级权重
     */
    private KeepPlan planForStraight(int[] numbers, int[] starts, int length, int weight, int throwsLeft) {
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
        return new KeepPlan(bestKeep, rankByMissing(weight, throwsLeft, length - bestCount));
    }

    /** 还差missing颗骰子成组合时的优先级，已集齐时为最高优先级 */
    private static int rankByMissing(int weight, int throwsLeft, int missing) {
        return missing <= 0 ? Integer.MAX_VALUE : weight * throwsLeft / missing;
    }

    /** 该玩家是否已经得到Yahtzee */
    private boolean hasScoredYahtzee(boolean[] isSelected) {
        int[] scores = data(getCurrentPlayerValue()).scores.getValue();
        return isSelected[Category.YAHTZEE.ordinal()]
                && scores != null && scores[Category.YAHTZEE.ordinal()] > 0;
    }

    /** 各点数按（出现次数降序，点数降序）排序后的结果 */
    private List<Integer> sortedFaces() {
        List<Integer> faces = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        faces.sort((a, b) -> diceCounts[a] != diceCounts[b]
                ? diceCounts[b] - diceCounts[a] : b - a);
        return faces;
    }

    /** 返回保留指定点数的骰子的锁定状态 */
    private boolean[] keepFace(int[] numbers, int face) {
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

    /** 在keep中标记最多count颗点数为face且未标记的骰子 */
    private static void markFace(int[] numbers, boolean[] keep, int face, int count) {
        for (int i = 0; i < numbers.length && count > 0; i++) {
            if (numbers[i] == face && !keep[i]) {
                keep[i] = true;
                count--;
            }
        }
    }

    /** 返回保留点数不小于5的骰子的锁定状态 */
    private boolean[] keepHighDice(int[] numbers) {
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

    /** 出现次数最多的点数（次数相同时取点数较大的） */
    private int mostFrequentFace() {
        int bestFace = 1;
        for (int face = 2; face <= 6; face++) {
            if (diceCounts[face] >= diceCounts[bestFace])
                bestFace = face;
        }
        return bestFace;
    }

    /** 出现次数最多的点数的出现次数 */
    private int maxCount() {
        int max = 0;
        for (int face = 1; face <= 6; face++)
            max = Math.max(max, diceCounts[face]);
        return max;
    }
}
