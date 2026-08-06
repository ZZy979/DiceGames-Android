package com.zzy.dicegames.data.entity.yahtzee;

import com.zzy.dicegames.data.entity.BaseScore;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * Yahtzee得分实体类
 *
 * @author 赵正阳
 */
@Entity(tableName = "yahtzee_score")
public class YahtzeeScore extends BaseScore {
    /** 是否获得奖励分 */
    @ColumnInfo(name = "has_bonus")
    public boolean hasBonus;

    /** Yahtzee是否得分 */
    @ColumnInfo(name = "has_yahtzee")
    public boolean hasYahtzee;

    public YahtzeeScore(@NonNull String date, int score, boolean hasBonus, boolean hasYahtzee) {
        super(date, score);
        this.hasBonus = hasBonus;
        this.hasYahtzee = hasYahtzee;
    }
}
