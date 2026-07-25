package com.zzy.dicegames.common;

import android.content.Context;

import com.zzy.dicegames.R;

/** 游戏类型 */
public enum GameType {
    /** 5骰Yahtzee */
    FIVE_YAHTZEE(R.string.fiveYahtzee),

    /** 6骰Yahtzee */
    SIX_YAHTZEE(R.string.sixYahtzee),

    /** Balut */
    BALUT(R.string.balut),

//    /** 大话骰子 */
//    LIAR_DICE(R.string.liarDice),

    /** 掷骰子 */
    ROLL_A_DICE(R.string.rollADice),

    /** Farkle */
    FARKLE(R.string.farkle);

    /** 游戏类型名称的字符串资源id */
    private int nameResId;

    GameType(int nameResId) {
        this.nameResId = nameResId;
    }

    public int getNameResId() {
        return nameResId;
    }

    /** 返回所有游戏类型名称的数组 */
    public static String[] getAllNames(Context context) {
        var names = new String[values().length];
        for (int i = 0; i < values().length; i++)
            names[i] = context.getString(values()[i].nameResId);
        return names;
    }
}
