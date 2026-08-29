package com.zzy.dicegames.common;

import com.zzy.dicegames.R;

/** 游戏类型 */
public enum GameType {
    /** 快艇骰子 */
    YAHTZEE(R.string.yahtzeeTitle),

    /** Yatzy */
    YATZY(R.string.yatzy),

    /** Maxi Yatzy */
    MAXI_YATZY(R.string.maxiYatzy),

    /** Balut */
    BALUT(R.string.balut),

    /** 大话骰 */
    LIARS_DICE(R.string.liarsDice),

    /** 掷骰子 */
    ROLL_A_DICE(R.string.rollADice),

    /** Farkle */
    FARKLE(R.string.farkle),

    /** Pig */
    PIG(R.string.pig),

    /** Crag */
    CRAG(R.string.crag);

    /** 游戏类型名称的字符串资源id */
    private final int nameResId;

    GameType(int nameResId) {
        this.nameResId = nameResId;
    }

    public int getNameResId() {
        return nameResId;
    }
}
