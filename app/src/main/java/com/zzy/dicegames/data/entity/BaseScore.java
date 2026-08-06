package com.zzy.dicegames.data.entity;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.PrimaryKey;

/**
 * 游戏得分实体基类
 *
 * @author 赵正阳
 */
public abstract class BaseScore implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    /** yyyy-MM-dd */
    @NonNull
    public String date;

    public int score;

    public BaseScore(@NonNull String date, int score) {
        this.date = date;
        this.score = score;
    }
}
