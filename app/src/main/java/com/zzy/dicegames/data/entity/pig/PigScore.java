package com.zzy.dicegames.data.entity.pig;

import com.zzy.dicegames.data.entity.BaseScore;

import androidx.annotation.NonNull;
import androidx.room.Entity;

/**
 * Pig得分实体类
 *
 * @author 赵正阳
 */
@Entity(tableName = "pig_score")
public class PigScore extends BaseScore {
    public PigScore(@NonNull String date, int score, int computerScore) {
        super(date, score, 2, computerScore);
    }
}
