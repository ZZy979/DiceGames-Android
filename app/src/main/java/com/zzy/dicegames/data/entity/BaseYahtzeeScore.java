package com.zzy.dicegames.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

/**
 * Yahtzee得分实体类基类
 *
 * @author 赵正阳
 */
public abstract class BaseYahtzeeScore extends BaseScore {
    /** 是否获得奖励分 */
    @ColumnInfo(name = "has_bonus")
    public boolean hasBonus;

    /** Yahtzee是否得分 */
    @ColumnInfo(name = "has_yahtzee")
    public boolean hasYahtzee;

    public BaseYahtzeeScore(@NonNull String date, int score, boolean hasBonus, boolean hasYahtzee) {
        super(date, score);
        this.hasBonus = hasBonus;
        this.hasYahtzee = hasYahtzee;
    }
}
