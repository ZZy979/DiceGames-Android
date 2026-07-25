package com.zzy.dicegames.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

/**
 * 6骰Yahtzee得分实体类
 *
 * @author 赵正阳
 */
@Entity(tableName = "six_yahtzee_score")
public class SixYahtzeeScore extends BaseYahtzeeScore {
    public SixYahtzeeScore(@NonNull String date, int score, boolean hasBonus, boolean hasYahtzee) {
        super(date, score, hasBonus, hasYahtzee);
    }
}
