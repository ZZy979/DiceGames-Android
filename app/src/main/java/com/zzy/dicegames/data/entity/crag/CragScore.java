package com.zzy.dicegames.data.entity.crag;

import com.zzy.dicegames.data.entity.BaseScore;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * Crag得分实体类
 */
@Entity(tableName = "crag_score")
public class CragScore extends BaseScore {
    /** Crag是否得分 */
    @ColumnInfo(name = "has_crag")
    public boolean hasCrag;

    public CragScore(@NonNull String date, int score, boolean hasCrag) {
        super(date, score);
        this.hasCrag = hasCrag;
    }
}
