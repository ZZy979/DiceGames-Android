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

    public YahtzeeGameViewModel() {
        super(5, 3, Category.values().length, 63, 35);
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

    @Override
    public YahtzeeScore createScoreEntity() {
        int[] finalScores = scores.getValue();
        if (finalScores == null || totalScore.getValue() == null || bonusScore.getValue() == null)
            return null;
        return new YahtzeeScore(LocalDate.now().toString(), totalScore.getValue(),
                bonusScore.getValue() > 0, finalScores[finalScores.length - 1] > 0);
    }

    @Override
    public int saveScoreToDatabase(BaseScore score) {
        var dao = scoreDatabase.yahtzeeScoreDao();
        dao.insert((YahtzeeScore) score);
        return dao.rank(score.score);
    }
}
