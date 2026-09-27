package com.zzy.dicegames.ui.game.yahtzee;

import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.yahtzee.YahtzeeScore;
import com.zzy.dicegames.utils.ArrayUtil;

import java.time.LocalDate;

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
    @Override
    public int specialCategory(boolean[] isSelected) {
        if (!isJoker())
            return -1;

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
    @Override
    public int rankCategory(int category) {
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

    /** Joker：已得到Yahtzee且掷出4颗以上相同点数时，只保留其中4颗以争取再次得到Yahtzee */
    @Override
    public KeepPlan specialKeepPlan(int[] numbers) {
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        if (isSelected == null || !hasScoredYahtzee(isSelected) || maxCount() < 4)
            return null;

        return new KeepPlan(keepSameFace(numbers, 4), 0);
    }

    /** 该玩家是否已经得到Yahtzee */
    private boolean hasScoredYahtzee(boolean[] isSelected) {
        int[] scores = data(getCurrentPlayerValue()).scores.getValue();
        return isSelected[Category.YAHTZEE.ordinal()]
                && scores != null && scores[Category.YAHTZEE.ordinal()] > 0;
    }

    /** 计算指定得分项的保留方案 */
    @Override
    public KeepPlan planForCategory(int[] numbers, int category, int throwsLeft) {
        return switch (Category.values()[category]) {
            case ONES, TWOS, THREES, FOURS, FIVES, SIXES ->
                    planForUpperCategory(numbers, category);
            case THREE_OF_A_KIND, FOUR_OF_A_KIND ->
                    planForSameKind(numbers, throwsLeft);
            case FULL_HOUSE ->
                    planForFullHouse(numbers, throwsLeft);
            // 小顺：1~4、2~5、3~6中保留骰子最多的一组
            case SMALL_STRAIGHT ->
                    planForStraight(numbers, new int[] {1, 2, 3}, 4, 9, 0, throwsLeft);
            // 大顺：1~5和2~6中保留骰子最多的一组
            case LARGE_STRAIGHT ->
                    planForStraight(numbers, new int[] {1, 2}, 5, 11, 0, throwsLeft);
            case CHANCE -> {
                boolean[] keep = keepHighDice(numbers);
                yield new KeepPlan(keep, sumOfKeptDice(numbers, keep) / 3);
            }
            case YAHTZEE -> {
                boolean[] keep = keepSameFace(numbers, numDice);
                yield new KeepPlan(keep,
                        rankByMissing(9, throwsLeft, numDice - ArrayUtil.count(keep, true), 0));
            }
        };
    }
}
