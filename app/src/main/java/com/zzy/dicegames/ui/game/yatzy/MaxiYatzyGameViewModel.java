package com.zzy.dicegames.ui.game.yatzy;

import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.yatzy.MaxiYatzyScore;
import com.zzy.dicegames.ui.game.yahtzee.BaseYahtzeeGameViewModel;
import com.zzy.dicegames.ui.game.yahtzee.KeepPlan;
import com.zzy.dicegames.utils.ArrayUtil;

import java.time.LocalDate;
import java.util.List;

public class MaxiYatzyGameViewModel extends BaseYahtzeeGameViewModel {
    /** 得分项 */
    public enum Category {
        ONES, TWOS, THREES, FOURS, FIVES, SIXES,
        ONE_PAIR, TWO_PAIRS, THREE_PAIRS,
        THREE_OF_A_KIND, FOUR_OF_A_KIND, FIVE_OF_A_KIND,
        SMALL_STRAIGHT, LARGE_STRAIGHT, FULL_STRAIGHT,
        FULL_HOUSE, CASTLE, TOWER, CHANCE, MAXI_YATZY
    }

    /**
     * 能得分时优先选择的高价值得分项（按优先级排序）<br>
     * 城堡和塔在同时可得时一定优于葫芦（城堡比葫芦多一颗次多的骰子），因此排在葫芦之前
     */
    private static final Category[] GOOD_CATEGORIES = {
            Category.MAXI_YATZY, Category.FULL_STRAIGHT,
            Category.TOWER, Category.CASTLE,
            Category.LARGE_STRAIGHT, Category.FULL_HOUSE, Category.SMALL_STRAIGHT
    };

    public MaxiYatzyGameViewModel() {
        super(6, 3, 1, 2, Category.values().length, 84, 100);
    }

    @Override
    public int calculateScore(int category) {
        int score = 0;
        switch (Category.values()[category]) {
            case ONES: case TWOS: case THREES: case FOURS: case FIVES: case SIXES:
                score = diceCounts[category + 1] * (category + 1);
                break;
            case ONE_PAIR: case TWO_PAIRS: case THREE_PAIRS: {
                int n = category - 5;
                int pairs = 0, pairSum = 0;
                for (int i = 6; i >= 1; --i) {
                    if (diceCounts[i] >= 2) {
                        pairs++;
                        pairSum += 2 * i;
                        if (pairs == n) {
                            score = pairSum;
                            break;
                        }
                    }
                }
                break;
            }
            case THREE_OF_A_KIND: case FOUR_OF_A_KIND: case FIVE_OF_A_KIND: {
                int n = category - 6;
                for (int i = 6; i >= 1; --i) {
                    if (diceCounts[i] >= n) {
                        score = n * i;
                        break;
                    }
                }
                break;
            }
            case SMALL_STRAIGHT:
                if (hasStraight(1, 5)) score = 15;
                break;
            case LARGE_STRAIGHT:
                if (hasStraight(2, 6)) score = 20;
                break;
            case FULL_STRAIGHT:
                if (hasStraight(1, 6)) score = 21;
                break;
            case FULL_HOUSE: {  // AAABB
                // 寻找三个同点
                int a = 0;
                for (int i = 6; i >= 1; i--) {
                    if (diceCounts[i] >= 3) {
                        a = i;
                        break;
                    }
                }
                // 寻找一对
                int b = 0;
                for (int i = 6; i >= 1; i--) {
                    if (diceCounts[i] >= 2 && i != a) {
                        b = i;
                        break;
                    }
                }
                if (a != 0 && b != 0) score = 3 * a + 2 * b;
                break;
            }
            case CASTLE: {  // AAABBB
                int threeOfAKind = 0;
                for (int i = 1; i <= 6; i++) {
                    if (diceCounts[i] >= 3)
                        threeOfAKind++;
                }
                if (threeOfAKind == 2) score = sumOfDice;
                break;
            }
            case TOWER: {  // AAAABB
                boolean hasFour = false, hasTwo = false;
                for (int i = 1; i <= 6; i++) {
                    if (diceCounts[i] == 4) hasFour = true;
                    if (diceCounts[i] == 2) hasTwo = true;
                }
                if ((hasFour && hasTwo)) score = sumOfDice;
                break;
            }
            case CHANCE:
                score = sumOfDice;
                break;
            case MAXI_YATZY:
                if (isAllSame()) score = 100;
                break;
        }
        return score;
    }

    /** 是否具有m到n的连顺 */
    private boolean hasStraight(int m, int n) {
        for (int i = m; i <= n; i++) {
            if (diceCounts[i] == 0)
                return false;
        }
        return true;
    }

    @Override
    public MaxiYatzyScore createScoreEntity() {
        YahtzeeGameData humanData = data(PLAYER_HUMAN);
        int[] finalScores = humanData.scores.getValue();
        Integer totalScore = humanData.score.getValue();
        Integer bonusScore = humanData.bonusScore.getValue();
        if (finalScores == null || totalScore == null || bonusScore == null)
            return null;
        int computerScore = isMultiplayer() ? getPlayerScoreValue(PLAYER_COMPUTER) : 0;
        return new MaxiYatzyScore(LocalDate.now().toString(), totalScore,
                getNumPlayersValue(), computerScore,
                bonusScore > 0, finalScores[finalScores.length - 1] > 0);
    }

    @Override
    public int saveScoreToDatabase(BaseScore score) {
        var dao = scoreDatabase.maxiYatzyScoreDao();
        dao.insert((MaxiYatzyScore) score);
        return dao.rank(score.score);
    }

    // ---------- 计算机玩家AI ----------

    /** Maxi Yatzy没有Joker规则 */
    @Override
    public boolean isJoker() {
        return false;
    }

    /**
     * 返回能得分的高价值得分项，没有时返回-1<br>
     * 还有投掷次数时，若更高级的得分项还没填写（小顺→大顺→全顺、葫芦→城堡/塔），
     * 则不直接选择低级得分项，而是继续掷骰子争取更高级的得分项
     *
     * @param throwsLeft 剩余投掷次数
     */
    @Override
    public int goodCategory(int throwsLeft) {
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        if (isSelected == null)
            return -1;

        for (Category good : GOOD_CATEGORIES) {
            if (throwsLeft != 0 && hasUnfilledBetterCategory(good, isSelected))
                continue;

            int c = good.ordinal();
            if (!isSelected[c] && calculateScore(c) > 0)
                return c;
        }
        return -1;
    }

    /** 是否还有更高级的得分项没有填写（小顺/大顺→全顺、葫芦→城堡/塔） */
    private boolean hasUnfilledBetterCategory(Category category, boolean[] isSelected) {
        return switch (category) {
            case SMALL_STRAIGHT, LARGE_STRAIGHT -> !isSelected[Category.FULL_STRAIGHT.ordinal()];
            case FULL_HOUSE -> !isSelected[Category.CASTLE.ordinal()]
                    || !isSelected[Category.TOWER.ordinal()];
            default -> false;
        };
    }

    /**
     * 已有葫芦且城堡或塔还没填写时，保留葫芦的骰子继续争取城堡/塔<br>
     * 即使掷不出城堡或塔，葫芦仍然成立，不会损失分数
     */
    @Override
    public KeepPlan specialKeepPlan(int[] numbers) {
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        if (isSelected == null || !hasRemainingRolls())
            return null;

        // 城堡和塔都已填写时无需继续争取
        if (isSelected[Category.CASTLE.ordinal()] && isSelected[Category.TOWER.ordinal()])
            return null;

        // 葫芦已填写或未成形时无需保留
        if (isSelected[Category.FULL_HOUSE.ordinal()] || calculateScore(Category.FULL_HOUSE.ordinal()) == 0)
            return null;

        List<Integer> faces = sortedFaces();
        boolean[] keep = new boolean[numDice];
        markFace(numbers, keep, faces.get(0), 3);
        markFace(numbers, keep, faces.get(1), 2);
        return new KeepPlan(keep, 0);
    }

    /** 指定得分项在选择得分项时的优先级 */
    @Override
    public int rankCategory(int category) {
        int score = calculateScore(category);
        int target = category + 1;
        return switch (Category.values()[category]) {
            // 上区按平均得分（即该点数的颗数）加权
            case ONES, TWOS, THREES, FOURS, FIVES, SIXES ->
                    target >= 4 && diceCounts[target] >= 4 ? 6 * diceCounts[target] + 20 : 6 * diceCounts[target];
            case CHANCE -> score - 10;
            case FOUR_OF_A_KIND -> score + 1;
            case FIVE_OF_A_KIND -> score + 2;
            default -> score;
        };
    }

    /** 计算指定得分项的保留方案 */
    @Override
    public KeepPlan planForCategory(int[] numbers, int category, int throwsLeft) {
        return switch (Category.values()[category]) {
            case ONES, TWOS, THREES, FOURS, FIVES, SIXES ->
                    planForUpperCategory(numbers, category);
            case ONE_PAIR, TWO_PAIRS, THREE_PAIRS ->
                    planForPairs(numbers, category - Category.ONE_PAIR.ordinal() + 1, throwsLeft);
            case THREE_OF_A_KIND, FOUR_OF_A_KIND, FIVE_OF_A_KIND ->
                    planForSameKind(numbers, throwsLeft);
            // 小顺：1~5，大顺：2~6，全顺：1~6
            case SMALL_STRAIGHT ->
                    planForStraight(numbers, new int[] {1}, 5, 13, 0, throwsLeft);
            case LARGE_STRAIGHT ->
                    planForStraight(numbers, new int[] {2}, 5, 13, 1, throwsLeft);
            case FULL_STRAIGHT ->
                    planForStraight(numbers, new int[] {1}, 6, 13, 2, throwsLeft);
            case FULL_HOUSE ->
                    planForFullHouse(numbers, throwsLeft);
            case CASTLE ->
                    planForCastle(numbers, throwsLeft);
            case TOWER ->
                    planForTower(numbers, throwsLeft);
            case CHANCE -> {
                boolean[] keep = keepHighDice(numbers);
                yield new KeepPlan(keep, sumOfKeptDice(numbers, keep) / 3);
            }
            case MAXI_YATZY -> {
                boolean[] keep = keepSameFace(numbers, numDice);
                yield new KeepPlan(keep,
                        rankByMissing(9, throwsLeft, numDice - ArrayUtil.count(keep, true), 0));
            }
        };
    }

    /** 计算城堡（AAABBB）的保留方案：保留出现次数最多的两个点数的各3颗 */
    private KeepPlan planForCastle(int[] numbers, int throwsLeft) {
        List<Integer> faces = sortedFaces();
        boolean[] keep = new boolean[numDice];
        markFace(numbers, keep, faces.get(0), 3);
        markFace(numbers, keep, faces.get(1), 3);
        return new KeepPlan(keep,
                rankByMissing(14, throwsLeft, numDice - ArrayUtil.count(keep, true), 0));
    }

    /** 计算高塔（AAAABB）的保留方案：保留出现次数最多的点数的4颗和次多的点数的2颗 */
    private KeepPlan planForTower(int[] numbers, int throwsLeft) {
        List<Integer> faces = sortedFaces();
        boolean[] keep = new boolean[numDice];
        markFace(numbers, keep, faces.get(0), 4);
        markFace(numbers, keep, faces.get(1), 2);
        return new KeepPlan(keep,
                rankByMissing(14, throwsLeft, numDice - ArrayUtil.count(keep, true), 0));
    }
}
