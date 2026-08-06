package com.zzy.dicegames.data.entity.farkle;

import com.zzy.dicegames.data.entity.BaseScore;

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
    public int computerScore;

    public FarkleScore(@NonNull String date, int score, int computerScore) {
        super(date, score);
        this.computerScore = computerScore;
    }
}
