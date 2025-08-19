package com.zzy.dicegames.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * Farkle得分实体类
 *
 * @author 赵正阳
 */
@Entity(tableName = "farkle_score")
public class FarkleScore extends BaseScore {
    /** 计算机得分 */
    @ColumnInfo(name = "computer_score")
    private int computerScore;

    public FarkleScore(@NonNull String date, int score, int computerScore) {
        super(date, score);
        this.computerScore = computerScore;
    }

    public int getComputerScore() {
        return computerScore;
    }

    public void setComputerScore(int computerScore) {
        this.computerScore = computerScore;
    }

}
