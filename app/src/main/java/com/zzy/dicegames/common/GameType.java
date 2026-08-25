package com.zzy.dicegames.common;

import com.zzy.dicegames.R;

/** 游戏类型 */
public enum GameType {
    /** Yahtzee */
    YAHTZEE(R.string.yahtzeeTitle),

    /** Maxi Yatzy */
    MAXI_YATZY(R.string.maxiYatzyTitle),

    /** Balut */
    BALUT(R.string.balut),

    /** 大话骰 */
    LIARS_DICE(R.string.liarsDice),

    /** 掷骰子 */
    ROLL_A_DICE(R.string.rollADice),

    /** Farkle */
    FARKLE(R.string.farkle);

    /** 游戏类型名称的字符串资源id */
    private final int nameResId;

    GameType(int nameResId) {
        this.nameResId = nameResId;
    }

    public int getNameResId() {
        return nameResId;
    }
}
