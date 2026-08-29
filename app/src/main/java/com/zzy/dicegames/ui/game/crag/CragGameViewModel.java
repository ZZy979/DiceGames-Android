package com.zzy.dicegames.ui.game.crag;

import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.crag.CragScore;
import com.zzy.dicegames.ui.game.yahtzee.BaseYahtzeeGameViewModel;

import java.time.LocalDate;

public class CragGameViewModel extends BaseYahtzeeGameViewModel {
    /** 得分项 */
    public enum Category {
        ONES, TWOS, THREES, FOURS, FIVES, SIXES,
        LOW_STRAIGHT, HIGH_STRAIGHT, ODD_STRAIGHT, EVEN_STRAIGHT,
        THREE_OF_A_KIND, THIRTEEN, CRAG
    }

    public CragGameViewModel() {
        super(3, 2, Category.values().length, Integer.MAX_VALUE, 0);
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
        int[] finalScores = scores.getValue();
        if (finalScores == null || totalScore.getValue() == null)
            return null;
        return new CragScore(LocalDate.now().toString(), totalScore.getValue(),
                finalScores[Category.CRAG.ordinal()] > 0);
    }

    @Override
    public int saveScoreToDatabase(BaseScore score) {
        var dao = scoreDatabase.cragScoreDao();
        dao.insert((CragScore) score);
        return dao.rank(score.score);
    }
}
