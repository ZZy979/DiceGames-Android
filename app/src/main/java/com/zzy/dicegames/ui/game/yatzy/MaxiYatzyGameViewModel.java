package com.zzy.dicegames.ui.game.yatzy;

import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.yatzy.MaxiYatzyScore;
import com.zzy.dicegames.ui.game.yahtzee.BaseYahtzeeGameViewModel;

import java.time.LocalDate;

public class MaxiYatzyGameViewModel extends BaseYahtzeeGameViewModel {
    /** 得分项 */
    public enum Category {
        ONES, TWOS, THREES, FOURS, FIVES, SIXES,
        ONE_PAIR, TWO_PAIRS, THREE_PAIRS,
        THREE_OF_A_KIND, FOUR_OF_A_KIND, FIVE_OF_A_KIND,
        SMALL_STRAIGHT, LARGE_STRAIGHT, FULL_STRAIGHT,
        FULL_HOUSE, CASTLE, TOWER, CHANCE, YATZY
    }

    public MaxiYatzyGameViewModel() {
        super(6, 3, Category.values().length, 84, 100);
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
            case YATZY:
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
        int[] finalScores = scores.getValue();
        if (finalScores == null || totalScore.getValue() == null || bonusScore.getValue() == null)
            return null;
        return new MaxiYatzyScore(LocalDate.now().toString(), totalScore.getValue(),
                bonusScore.getValue() > 0, finalScores[finalScores.length - 1] > 0);
    }

    @Override
    public int saveScoreToDatabase(BaseScore score) {
        var dao = scoreDatabase.maxiYatzyScoreDao();
        dao.insert((MaxiYatzyScore) score);
        return dao.rank(score.score);
    }
}
