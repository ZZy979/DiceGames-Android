package com.zzy.dicegames.ui.game.yatzy;

import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.yatzy.YatzyScore;
import com.zzy.dicegames.ui.game.yahtzee.BaseYahtzeeGameViewModel;
import com.zzy.dicegames.ui.game.yahtzee.KeepPlan;
import com.zzy.dicegames.utils.ArrayUtil;

import java.time.LocalDate;

public class YatzyGameViewModel extends BaseYahtzeeGameViewModel {
    /** 得分项 */
    public enum Category {
        ONES, TWOS, THREES, FOURS, FIVES, SIXES,
        ONE_PAIR, TWO_PAIRS, THREE_OF_A_KIND, FOUR_OF_A_KIND,
        SMALL_STRAIGHT, LARGE_STRAIGHT, FULL_HOUSE, CHANCE, YATZY
    }

    /** 能得分时优先选择的高价值得分项（按优先级排序） */
    private static final Category[] GOOD_CATEGORIES = {
            Category.YATZY, Category.LARGE_STRAIGHT,
            Category.FULL_HOUSE, Category.SMALL_STRAIGHT
    };

    public YatzyGameViewModel() {
        super(5, 3, 1, 2, Category.values().length, 63, 50);
    }

    @Override
    public int calculateScore(int category) {
        int score = 0;
        switch (Category.values()[category]) {
            case ONES: case TWOS: case THREES: case FOURS: case FIVES: case SIXES:
                score = diceCounts[category + 1] * (category + 1);
                break;
            case ONE_PAIR: case TWO_PAIRS: {
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
            case THREE_OF_A_KIND: case FOUR_OF_A_KIND: {
                int n = category - 5;
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
                if (a != 0 && b != 0) score = sumOfDice;
                break;
            }
            case CHANCE:
                score = sumOfDice;
                break;
            case YATZY:
                if (isAllSame()) score = 50;
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
    public YatzyScore createScoreEntity() {
        YahtzeeGameData humanData = data(PLAYER_HUMAN);
        int[] finalScores = humanData.scores.getValue();
        Integer totalScore = humanData.score.getValue();
        Integer bonusScore = humanData.bonusScore.getValue();
        if (finalScores == null || totalScore == null || bonusScore == null)
            return null;
        int computerScore = isMultiplayer() ? getPlayerScoreValue(PLAYER_COMPUTER) : 0;
        return new YatzyScore(LocalDate.now().toString(), totalScore,
                getNumPlayersValue(), computerScore,
                bonusScore > 0, finalScores[finalScores.length - 1] > 0);
    }

    @Override
    public int saveScoreToDatabase(BaseScore score) {
        var dao = scoreDatabase.yatzyScoreDao();
        dao.insert((YatzyScore) score);
        return dao.rank(score.score);
    }

    // ---------- 计算机玩家AI ----------

    /** Yatzy没有Joker规则 */
    @Override
    public boolean isJoker() {
        return false;
    }

    /**
     * 返回能得分的高价值得分项，没有时返回-1
     *
     * @param throwsLeft 剩余投掷次数
     */
    @Override
    public int goodCategory(int throwsLeft) {
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        if (isSelected == null)
            return -1;

        for (Category good : GOOD_CATEGORIES) {
            // 还有投掷次数且大顺已填写时不为小顺保留机会，由rankCategory()决定是否选择小顺
            if (good == Category.SMALL_STRAIGHT && throwsLeft != 0
                    && isSelected[Category.LARGE_STRAIGHT.ordinal()])
                continue;

            int c = good.ordinal();
            if (!isSelected[c] && calculateScore(c) > 0)
                return c;
        }
        return -1;
    }

    /** 指定得分项在选择得分项时的优先级 */
    @Override
    public int rankCategory(int category) {
        int score = calculateScore(category);
        int target = category + 1;
        return switch (Category.values()[category]) {
            // 上区按平均得分（即该点数的颗数）加权
            case ONES, TWOS, THREES, FOURS, FIVES, SIXES ->
                    target >= 4 && diceCounts[target] >= 3 ? 6 * diceCounts[target] + 20 : 6 * diceCounts[target];
            case CHANCE -> score - 10;
            case FOUR_OF_A_KIND -> score + 1;
            default -> score;
        };
    }

    /** 计算指定得分项的保留方案 */
    @Override
    public KeepPlan planForCategory(int[] numbers, int category, int throwsLeft) {
        return switch (Category.values()[category]) {
            case ONES, TWOS, THREES, FOURS, FIVES, SIXES ->
                    planForUpperCategory(numbers, category);
            case ONE_PAIR, TWO_PAIRS ->
                    planForPairs(numbers, category - Category.ONE_PAIR.ordinal() + 1, throwsLeft);
            case THREE_OF_A_KIND, FOUR_OF_A_KIND ->
                    planForSameKind(numbers, throwsLeft);
            case FULL_HOUSE ->
                    planForFullHouse(numbers, throwsLeft);
            // 小顺：1~5
            case SMALL_STRAIGHT ->
                    planForStraight(numbers, new int[] {1}, 5, 13, 0, throwsLeft);
            // 大顺：2~6
            case LARGE_STRAIGHT ->
                    planForStraight(numbers, new int[] {2}, 5, 13, 1, throwsLeft);
            case CHANCE -> {
                boolean[] keep = keepHighDice(numbers);
                yield new KeepPlan(keep, sumOfKeptDice(numbers, keep) / 3);
            }
            case YATZY -> {
                boolean[] keep = keepSameFace(numbers, numDice);
                yield new KeepPlan(keep,
                        rankByMissing(9, throwsLeft, numDice - ArrayUtil.count(keep, true), 0));
            }
        };
    }
}
