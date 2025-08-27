package com.zzy.dicegames.ui.game.yahtzee;

import com.zzy.dicegames.data.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.data.entity.FiveYahtzeeScore;

import java.time.LocalDate;

public class FiveYahtzeeViewModel extends BaseYahtzeeViewModel {
    // 得分项编号
    public static final int ONES = 0;
    public static final int TWOS = 1;
    public static final int THREES = 2;
    public static final int FOURS = 3;
    public static final int FIVES = 4;
    public static final int SIXES = 5;
    public static final int TWO_PAIRS = 6;
    public static final int THREE_OF_A_KIND = 7;
    public static final int FOUR_OF_A_KIND = 8;
    public static final int FULL_HOUSE = 9;
    public static final int SMALL_STRAIGHT = 10;
    public static final int LARGE_STRAIGHT = 11;
    public static final int CHANCE = 12;
    public static final int YAHTZEE = 13;

    public FiveYahtzeeViewModel() {
        super(5, 3, 14, 63, 50);
    }

    @Override
    public int calculateScore(int category) {
        boolean isJoker = isJoker();
        int score = 0;
        switch (category) {
            case ONES: case TWOS: case THREES: case FOURS: case FIVES: case SIXES:
                score = diceCounts[category + 1] * (category + 1);
                break;
            case TWO_PAIRS: {
                int pairs = 0;
                for (int i = 1; i <= 6; i++) {
                    if (diceCounts[i] >= 2)
                        pairs++;
                }
                if (pairs == 2 || isJoker) score = sumOfDice;
                break;
            }
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
            case FULL_HOUSE: {
                boolean hasThree = false, hasTwo = false;
                for (int i = 1; i <= 6; i++) {
                    if (diceCounts[i] == 3) hasThree = true;
                    if (diceCounts[i] == 2) hasTwo = true;
                }
                if ((hasThree && hasTwo) || isJoker) score = 25;
                break;
            }
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
                if (isYahtzee()) score = 50;
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

    @Override
    public FiveYahtzeeScore createScoreEntity() {
        int[] finalScores = scores.getValue();
        if (finalScores == null || totalScore.getValue() == null || bonusScore.getValue() == null)
            return null;
        return new FiveYahtzeeScore(LocalDate.now().toString(), totalScore.getValue(),
                bonusScore.getValue() > 0, finalScores[finalScores.length - 1] > 0);
    }

    @Override
    public int saveScoreToDatabase(AbstractYahtzeeScore score) {
        var dao = scoreDatabase.fiveYahtzeeScoreDao();
        dao.insert((FiveYahtzeeScore) score);
        return dao.findTop10Score().indexOf(score.score) + 1;
    }
}
