package com.zzy.dicegames.ui.game.yahtzee;

import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.yahtzee.YahtzeeScore;

import java.time.LocalDate;

public class YahtzeeGameViewModel extends BaseYahtzeeGameViewModel {
    /** 得分项 */
    public enum Category {
        ONES, TWOS, THREES, FOURS, FIVES, SIXES,
        THREE_OF_A_KIND, FOUR_OF_A_KIND, FULL_HOUSE,
        SMALL_STRAIGHT, LARGE_STRAIGHT, CHANCE, YAHTZEE
    }

    /**
     * 各得分项的优先级（越大越重要，下标与{@link Category}对应）<br>
     * 计算机玩家在多个得分项都能得分时优先选择优先级高的项，在必须放弃得分项时优先放弃优先级低的项
     */
    private static final int[] CATEGORY_PRIORITY = {
            2, 2, 2, 3, 4, 5,     // ONES ~ SIXES
            1, 3, 4,              // THREE_OF_A_KIND, FOUR_OF_A_KIND, FULL_HOUSE
            5, 6, 0, 7            // SMALL_STRAIGHT, LARGE_STRAIGHT, CHANCE, YAHTZEE
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
        return new YahtzeeScore(LocalDate.now().toString(), totalScore,
                bonusScore > 0, finalScores[finalScores.length - 1] > 0);
    }

    @Override
    public int saveScoreToDatabase(BaseScore score) {
        var dao = scoreDatabase.yahtzeeScoreDao();
        dao.insert((YahtzeeScore) score);
        return dao.rank(score.score);
    }

    // ---------- 计算机玩家AI（启发式优先级表） ----------

    @Override
    protected int computerChooseCategory() {
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        int bestCategory = -1, bestScore = -1, bestPriority = -1;
        int sacrificeCategory = -1, sacrificePriority = Integer.MAX_VALUE;

        for (int c = 0; c < numCategories; c++) {
            if (isSelected != null && isSelected[c])
                continue;

            int score = calculateScore(c);
            if (score > bestScore || (score == bestScore && CATEGORY_PRIORITY[c] > bestPriority)) {
                bestCategory = c;
                bestScore = score;
                bestPriority = CATEGORY_PRIORITY[c];
            }
            if (CATEGORY_PRIORITY[c] < sacrificePriority) {
                sacrificeCategory = c;
                sacrificePriority = CATEGORY_PRIORITY[c];
            }
        }

        // 有得分项能得分时选得分最高者，都不得分时放弃优先级最低的项
        return bestScore > 0 ? bestCategory : sacrificeCategory;
    }

    @Override
    protected boolean[] computerChooseKeep(int category) {
        int[] numbers = diceNumbers.getValue();
        if (numbers == null)
            return new boolean[numDice];

        return switch (Category.values()[category]) {
            case ONES, TWOS, THREES, FOURS, FIVES, SIXES ->
                    keepFace(numbers, category + 1);
            case THREE_OF_A_KIND, FOUR_OF_A_KIND, FULL_HOUSE, YAHTZEE ->
                    keepFace(numbers, mostFrequentFace());
            case SMALL_STRAIGHT, LARGE_STRAIGHT ->
                    keepLongestStraight(numbers);
            case CHANCE ->
                    keepHighDice(numbers);
        };
    }

    @Override
    protected boolean computerShouldRollAgain(int category) {
        if (!hasRemainingRolls())
            return false;

        // 目标得分项还有提高空间时继续掷骰子（如掷出3个6点后争取更多6点）
        if (calculateScore(category) < maxScore(category))
            return true;

        // 目标得分项已到上限，但保留的骰子还差一颗就能换成更高分的得分项
        return canReachBetterCategory(category);
    }

    /** 指定得分项可能达到的最高分 */
    private int maxScore(int category) {
        return switch (Category.values()[category]) {
            case ONES -> 5;
            case TWOS -> 10;
            case THREES -> 15;
            case FOURS -> 20;
            case FIVES -> 25;
            case SIXES -> 30;
            case THREE_OF_A_KIND, FOUR_OF_A_KIND, CHANCE -> 30;   // 5个6点
            case FULL_HOUSE -> 25;
            case SMALL_STRAIGHT -> 30;
            case LARGE_STRAIGHT -> 40;
            case YAHTZEE -> 50;
        };
    }

    /**
     * 保留的骰子是否还差一颗就能换成更高分的得分项<br>
     * 保留的骰子本身已经能保证当前得分项的分数（如保留2、3、4、5仍能得小顺），
     * 因此继续掷骰子只可能更好、不会更差
     */
    private boolean canReachBetterCategory(int category) {
        boolean[] isSelected = data(getCurrentPlayerValue()).selected.getValue();
        if (isSelected == null)
            return false;

        return switch (Category.values()[category]) {
            case SMALL_STRAIGHT -> !isSelected[Category.LARGE_STRAIGHT.ordinal()] && hasStraight(4);
            case THREE_OF_A_KIND, FOUR_OF_A_KIND -> !isSelected[Category.YAHTZEE.ordinal()] && maxCount() == 4;
            default -> false;
        };
    }

    /** 返回保留指定点数的骰子的锁定状态 */
    private boolean[] keepFace(int[] numbers, int face) {
        boolean[] keep = new boolean[numbers.length];
        for (int i = 0; i < numbers.length; i++)
            keep[i] = numbers[i] == face;
        return keep;
    }

    /** 返回保留点数不小于5的骰子的锁定状态 */
    private boolean[] keepHighDice(int[] numbers) {
        boolean[] keep = new boolean[numbers.length];
        for (int i = 0; i < numbers.length; i++)
            keep[i] = numbers[i] >= 5;
        return keep;
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

    /** 返回保留最长连顺中的骰子的锁定状态（连顺中重复点数的骰子只保留一颗，其余重掷） */
    private boolean[] keepLongestStraight(int[] numbers) {
        int bestStart = 1, bestLength = 1, start = 0;
        for (int face = 1; face <= 6; face++) {
            if (diceCounts[face] == 0) {
                start = 0;
                continue;
            }
            if (start == 0)
                start = face;
            int length = face - start + 1;
            if (length > bestLength) {
                bestLength = length;
                bestStart = start;
            }
        }

        boolean[] keep = new boolean[numbers.length];
        boolean[] keptFace = new boolean[7];
        for (int i = 0; i < numbers.length; i++) {
            int face = numbers[i];
            if (face >= bestStart && face < bestStart + bestLength && !keptFace[face]) {
                keep[i] = true;
                keptFace[face] = true;
            }
        }
        return keep;
    }

    /** 出现次数最多的点数的出现次数 */
    private int maxCount() {
        int max = 0;
        for (int face = 1; face <= 6; face++)
            max = Math.max(max, diceCounts[face]);
        return max;
    }
}
