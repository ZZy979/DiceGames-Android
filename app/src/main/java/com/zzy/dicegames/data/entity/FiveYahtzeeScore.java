package com.zzy.dicegames.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

/**
 * 5骰Yahtzee得分实体类
 *
 * @author 赵正阳
 */
@Entity(tableName = "five_yahtzee_score")
public class FiveYahtzeeScore extends BaseYahtzeeScore {
    public FiveYahtzeeScore(@NonNull String date, int score, boolean hasBonus, boolean hasYahtzee) {
        super(date, score, hasBonus, hasYahtzee);
    }
}
