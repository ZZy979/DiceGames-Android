package com.zzy.dicegames.ui.game.crag;

import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.crag.CragScore;
import com.zzy.dicegames.ui.game.yahtzee.BaseYahtzeeGameViewModel;
import com.zzy.dicegames.ui.game.yahtzee.KeepPlan;

import java.time.LocalDate;

public class CragGameViewModel extends BaseYahtzeeGameViewModel {
    /** 得分项 */
    public enum Category {
        ONES, TWOS, THREES, FOURS, FIVES, SIXES,
        LOW_STRAIGHT, HIGH_STRAIGHT, ODD_STRAIGHT, EVEN_STRAIGHT,
        THREE_OF_A_KIND, THIRTEEN, CRAG
    }

    /** 能得分时优先选择的高价值得分项（按优先级排序） */
    private static final Category[] GOOD_CATEGORIES = {
            Category.THREE_OF_A_KIND,
            Category.LOW_STRAIGHT, Category.HIGH_STRAIGHT,
            Category.ODD_STRAIGHT, Category.EVEN_STRAIGHT,
            Category.THIRTEEN, Category.CRAG
    };

    public CragGameViewModel() {
        super(3, 2, 1, 2, Category.values().length, Integer.MAX_VALUE, 0);
    }

    @Override
    public int calculateScore(int category) {
        int score = 0;
        switch (Category.values()[category]) {
            case ONES: case TWOS: case THREES: case FOURS: case FIVES: case SIXES:
                score = diceCounts[category + 1] * (category + 1);
                break;
            case LOW_STRAIGHT:  // 1,2,3
                if (hasAll(1, 2, 3)) score = 20;
                break;
            case HIGH_STRAIGHT:  // 4,5,6
                if (hasAll(4, 5, 6)) score = 20;
                break;
            case ODD_STRAIGHT:  // 1,3,5
                if (hasAll(1, 3, 5)) score = 20;
                break;
            case EVEN_STRAIGHT:  // 2,4,6
                if (hasAll(2, 4, 6)) score = 20;
                break;
            case THREE_OF_A_KIND:
                if (isAllSame()) score = 25;
                break;
            case THIRTEEN:
                if (sumOfDice == 13) score = 26;
                break;
            case CRAG:
                if (sumOfDice == 13 && hasPair()) score = 50;
                break;
        }
        return score;
    }

    /** 是否包含所有指定的点数 */
    private boolean hasAll(int... faces) {
        for (int face : faces) {
            if (diceCounts[face] == 0)
                return false;
        }
        return true;
    }

    /** 是否包含一对 */
    private boolean hasPair() {
        for (int i = 1; i <= 6; i++) {
            if (diceCounts[i] >= 2)
                return true;
        }
        return false;
    }

    @Override
    public CragScore createScoreEntity() {
        YahtzeeGameData humanData = data(PLAYER_HUMAN);
        int[] finalScores = humanData.scores.getValue();
        Integer totalScore = humanData.score.getValue();
        if (finalScores == null || totalScore == null)
            return null;
        int computerScore = isMultiplayer() ? getPlayerScoreValue(PLAYER_COMPUTER) : 0;
        return new CragScore(LocalDate.now().toString(), totalScore,
                getNumPlayersValue(), computerScore,
                finalScores[Category.CRAG.ordinal()] > 0);
    }

    @Override
    public int saveScoreToDatabase(BaseScore score) {
        var dao = scoreDatabase.cragScoreDao();
        dao.insert((CragScore) score);
        return dao.rank(score.score);
    }

    // ---------- 计算机玩家AI ----------

    /** Crag没有Joker规则 */
    @Override
    public boolean isJoker() {
        return false;
    }

    /** 返回能得分的高价值得分项，没有时返回-1 */
    @Override
    public int goodCategory(int throwsLeft) {
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        if (isSelected == null)
            return -1;

        for (Category good : GOOD_CATEGORIES) {
            int c = good.ordinal();
            if (!isSelected[c] && calculateScore(c) > 0)
                return c;
        }
        return -1;
    }

    /** 指定得分项在选择得分项时的优先级 */
    @Override
    public int rankCategory(int category) {
        // 上区按平均得分（即该点数的颗数）加权
        if (category < NUM_UPPER_CATEGORIES) {
            int face = category + 1;
            return face >= 4 && diceCounts[face] >= 3 ? 6 * diceCounts[face] + 20 : 6 * diceCounts[face];
        }
        return calculateScore(category);
    }

    /**
     * 计算上区得分项的保留方案：保留该点数的全部骰子<br>
     * Crag中3~6点只要出现次数最多就能获得额外优先级
     */
    @Override
    protected KeepPlan planForUpperCategory(int[] numbers, int category) {
        int face = category + 1;
        int rank = face * diceCounts[face] + face;
        if (face >= 3 && mostFrequentFace() == face)
            rank = 15 + face;
        return new KeepPlan(keepFace(numbers, face), rank);
    }

    /** 计算指定得分项的保留方案 */
    @Override
    public KeepPlan planForCategory(int[] numbers, int category, int throwsLeft) {
        return switch (Category.values()[category]) {
            case ONES, TWOS, THREES, FOURS, FIVES, SIXES ->
                    planForUpperCategory(numbers, category);
            case THREE_OF_A_KIND ->
                    planForThreeOfAKind(numbers, throwsLeft);
            case LOW_STRAIGHT ->
                    planForFacesStraight(numbers, new int[] {1, 2, 3}, 11, 15, throwsLeft);
            case HIGH_STRAIGHT ->
                    planForFacesStraight(numbers, new int[] {4, 5, 6}, 11, 15, throwsLeft);
            case ODD_STRAIGHT ->
                    planForFacesStraight(numbers, new int[] {1, 3, 5}, 11, 15, throwsLeft);
            case EVEN_STRAIGHT ->
                    planForFacesStraight(numbers, new int[] {2, 4, 6}, 11, 15, throwsLeft);
            case THIRTEEN ->
                    planForThirteen(numbers, throwsLeft);
            case CRAG ->
                    planForCrag(numbers, throwsLeft);
        };
    }

    /**
     * 计算3个同点的保留方案：最后一次投掷且已有2颗以上同点时保留该点数，<br>
     * 否则保留出现次数达到3次的点数
     */
    private KeepPlan planForThreeOfAKind(int[] numbers, int throwsLeft) {
        int mostFrequent = mostFrequentFace();
        int face = throwsLeft == 1 && diceCounts[mostFrequent] >= 2 ? mostFrequent : 0;
        if (face == 0) {
            for (int f : sortedFaces()) {
                if (diceCounts[f] >= 3) {
                    face = f;
                    break;
                }
            }
        }

        if (face == 0)
            return new KeepPlan(new boolean[numDice], 3);

        return new KeepPlan(keepFace(numbers, face), face * diceCounts[face]);
    }

    /**
     * 计算十三点的保留方案：保留和值不超过13且尽量大的若干颗骰子（至少留一颗重掷），<br>
     * 使剩余的骰子有可能凑出13与保留和值之差
     */
    private KeepPlan planForThirteen(int[] numbers, int throwsLeft) {
        return planForSum13(numbers, throwsLeft, 21, false);
    }

    /** 计算Crag的保留方案：与十三点相同，但保留的骰子中必须含一对 */
    private KeepPlan planForCrag(int[] numbers, int throwsLeft) {
        return planForSum13(numbers, throwsLeft, 20, true);
    }

    /**
     * 计算凑出13分的保留方案：枚举所有保留方案，取保留骰子最多、和值最大的一种，<br>
     * 并要求保留的和值不超过13、剩余的骰子有可能凑出13与保留和值之差
     *
     * @param weight 优先级权重
     * @param needPair 保留的骰子中是否必须含一对
     */
    private KeepPlan planForSum13(int[] numbers, int throwsLeft, int weight, boolean needPair) {
        int bestMask = 0, bestCount = 0, bestSum = 0;

        for (int mask = 1; mask < (1 << numDice); mask++) {
            int count = Integer.bitCount(mask);
            // 至少要留一颗骰子重掷
            if (count >= numDice || (needPair && !hasPairIn(numbers, mask)))
                continue;

            int sum = sumOfKeptDice(numbers, toKeep(mask));
            int target = 13 - sum;
            // 保留的和值不能超过13，且剩余的骰子要有可能凑出13与和值之差
            if (sum > 13 || target < numDice - count || target > 6 * (numDice - count))
                continue;

            // 保留的骰子越多、和值越大越好
            if (count > bestCount || (count == bestCount && sum > bestSum)) {
                bestMask = mask;
                bestCount = count;
                bestSum = sum;
            }
        }

        return new KeepPlan(toKeep(bestMask),
                rankByMissing(weight, throwsLeft, numDice - bestCount, 0));
    }

    /** 将骰子的保留掩码转换为锁定状态 */
    private boolean[] toKeep(int mask) {
        boolean[] keep = new boolean[numDice];
        for (int i = 0; i < numDice; i++)
            keep[i] = (mask & (1 << i)) != 0;
        return keep;
    }

    /** 指定的骰子中是否含有一对 */
    private static boolean hasPairIn(int[] numbers, int mask) {
        boolean[] used = new boolean[7];
        for (int i = 0; i < numbers.length; i++) {
            if ((mask & (1 << i)) == 0)
                continue;
            if (used[numbers[i]])
                return true;
            used[numbers[i]] = true;
        }
        return false;
    }
}
