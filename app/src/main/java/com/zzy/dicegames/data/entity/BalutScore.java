package com.zzy.dicegames.data.entity;

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
    /** Balut得分次数 */
    @ColumnInfo(name = "num_balut")
    private int numBalut;

    public BalutScore(@NonNull String date, int score, int numBalut) {
        super(date, score);
        this.numBalut = numBalut;
    }

    public int getNumBalut() {
        return numBalut;
    }

    public void setNumBalut(int numBalut) {
        this.numBalut = numBalut;
    }

}
