package com.zzy.dicegames.ui.game.yahtzee;

public class SixYahtzeeViewModel extends BaseYahtzeeViewModel {
    // 得分项编号
    public static final int ONES = 0;
    public static final int TWOS = 1;
    public static final int THREES = 2;
    public static final int FOURS = 3;
    public static final int FIVES = 4;
    public static final int SIXES = 5;
    public static final int ONE_PAIR = 6;
    public static final int TWO_PAIRS = 7;
    public static final int THREE_PAIRS = 8;
    public static final int THREE_OF_A_KIND = 9;
    public static final int FOUR_OF_A_KIND = 10;
    public static final int FIVE_OF_A_KIND = 11;
    public static final int SMALL_STRAIGHT = 12;
    public static final int LARGE_STRAIGHT = 13;
    public static final int FULL_STRAIGHT = 14;
    public static final int HUT = 15;
    public static final int HOUSE = 16;
    public static final int TOWER = 17;
    public static final int CHANCE = 18;
    public static final int YAHTZEE = 19;

    public SixYahtzeeViewModel() {
        super.init(20, 84, 100);
    }

    @Override
    public int calculateScore(int category) {
        boolean isJoker = isJoker();
        int score = 0;
        switch (category) {
            case ONES: case TWOS: case THREES: case FOURS: case FIVES: case SIXES:
                score = diceCounts[category + 1] * (category + 1);
                break;
            case ONE_PAIR: case TWO_PAIRS: case THREE_PAIRS:
                if (isJoker)
                    score = sumOfDice;
                else {
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
                }
                break;
            case THREE_OF_A_KIND: case FOUR_OF_A_KIND: case FIVE_OF_A_KIND:
                if (isJoker)
                    score = sumOfDice;
                else {
                    int n = category - 6;
                    for (int i = 6; i >= 1; --i) {
                        if (diceCounts[i] >= n) {
                            score = n * i;
                            break;
                        }
                    }
                }
                break;
            case SMALL_STRAIGHT:
                if (hasStraight(1, 5) || isJoker) score = 15;
                break;
            case LARGE_STRAIGHT:
                if (hasStraight(2, 6) || isJoker) score = 20;
                break;
            case FULL_STRAIGHT:
                if (hasStraight(1, 6) || isJoker) score = 21;
                break;
            case HUT:  // AAABB
                if (isJoker)
                    score = sumOfDice;
                else {
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
                }
                break;
            case HOUSE: {  // AAABBB
                int threeOfAKind = 0;
                for (int i = 1; i <= 6; i++) {
                    if (diceCounts[i] >= 3)
                        threeOfAKind++;
                }
                if (threeOfAKind == 2 || isJoker) score = sumOfDice;
                break;
            }
            case TOWER: {  // AAAABB
                boolean hasFour = false, hasTwo = false;
                for (int i = 1; i <= 6; i++) {
                    if (diceCounts[i] == 4) hasFour = true;
                    if (diceCounts[i] == 2) hasTwo = true;
                }
                if ((hasFour && hasTwo) || isJoker) score = sumOfDice;
                break;
            }
            case CHANCE:
                score = sumOfDice;
                break;
            case YAHTZEE:
                if (isYahtzee()) score = 100;
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
}
