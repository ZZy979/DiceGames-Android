package com.zzy.dicegames.data.entity.balut;

import com.zzy.dicegames.data.entity.BaseScore;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.annotation.NonNull;

/**
 * Balut得分实体类
 *
 * @author 赵正阳
 */
@Entity(tableName = "balut_score")
public class BalutScore extends BaseScore {
    /** 点数 */
    @ColumnInfo(name = "points")
    public int points;

    /** Balut得分次数 */
    @ColumnInfo(name = "num_balut")
    public int numBalut;

    public BalutScore(@NonNull String date, int score, int points, int numBalut) {
        super(date, score);
        this.points = points;
        this.numBalut = numBalut;
    }
}
