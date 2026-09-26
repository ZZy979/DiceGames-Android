package com.zzy.dicegames.data.entity;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
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

    /** 人类玩家得分 */
    public int score;

    /** 玩家数量 */
    @ColumnInfo(name = "num_players", defaultValue = "1")
    public int numPlayers;

    /** 计算机玩家得分（无计算机玩家时为0） */
    @ColumnInfo(name = "computer_score", defaultValue = "0")
    public int computerScore;

    /** 单人游戏得分 */
    public BaseScore(@NonNull String date, int score) {
        this(date, score, 1, 0);
    }

    /**
     * @param date 日期
     * @param score 人类玩家得分
     * @param numPlayers 游戏人数
     * @param computerScore 计算机玩家得分
     */
    public BaseScore(@NonNull String date, int score, int numPlayers, int computerScore) {
        this.date = date;
        this.score = score;
        this.numPlayers = numPlayers;
        this.computerScore = computerScore;
    }
}
