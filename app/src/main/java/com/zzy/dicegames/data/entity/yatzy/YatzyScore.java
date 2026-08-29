package com.zzy.dicegames.data.entity.yatzy;

import com.zzy.dicegames.data.entity.BaseScore;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * Yatzy得分实体类
 */
@Entity(tableName = "yatzy_score")
public class YatzyScore extends BaseScore {
    /** 是否获得奖励分 */
    @ColumnInfo(name = "has_bonus")
    public boolean hasBonus;

    /** Yatzy是否得分 */
    @ColumnInfo(name = "has_yatzy")
    public boolean hasYatzy;

    public YatzyScore(@NonNull String date, int score, boolean hasBonus, boolean hasYatzy) {
        super(date, score);
        this.hasBonus = hasBonus;
        this.hasYatzy = hasYatzy;
    }
}
