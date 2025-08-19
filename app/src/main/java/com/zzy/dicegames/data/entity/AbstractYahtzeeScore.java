package com.zzy.dicegames.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

/**
 * Yahtzee得分实体类基类
 *
 * @author 赵正阳
 */
public abstract class AbstractYahtzeeScore extends BaseScore {
    /** 是否获得奖励分 */
    @ColumnInfo(name = "has_bonus")
    protected boolean hasBonus;

    /** Yahtzee是否得分 */
    @ColumnInfo(name = "has_yahtzee")
    protected boolean hasYahtzee;

    public AbstractYahtzeeScore(@NonNull String date, int score, boolean hasBonus, boolean hasYahtzee) {
        super(date, score);
        this.hasBonus = hasBonus;
        this.hasYahtzee = hasYahtzee;
    }

    public boolean isHasBonus() {
        return hasBonus;
    }

    public void setHasBonus(boolean hasBonus) {
        this.hasBonus = hasBonus;
    }

    public boolean isHasYahtzee() {
        return hasYahtzee;
    }

    public void setHasYahtzee(boolean hasYahtzee) {
        this.hasYahtzee = hasYahtzee;
    }

}
