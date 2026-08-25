package com.zzy.dicegames.data.entity.pig;

import com.zzy.dicegames.data.entity.BaseScore;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * Pig得分实体类
 *
 * @author 赵正阳
 */
@Entity(tableName = "pig_score")
public class PigScore extends BaseScore {
    /** 计算机得分 */
    @ColumnInfo(name = "computer_score")
    public int computerScore;

    public PigScore(@NonNull String date, int score, int computerScore) {
        super(date, score);
        this.computerScore = computerScore;
    }
}
