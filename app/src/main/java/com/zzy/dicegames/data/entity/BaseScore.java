package com.zzy.dicegames.data.entity;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.PrimaryKey;

/**
 * 游戏得分实体类基类
 *
 * @author 赵正阳
 */
public abstract class BaseScore implements Serializable {
    @PrimaryKey(autoGenerate = true)
    protected int id;

    /** yyyy-MM-dd */
    @NonNull
    protected String date;

    protected int score;

    public BaseScore(@NonNull String date, int score) {
        this.date = date;
        this.score = score;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

}
