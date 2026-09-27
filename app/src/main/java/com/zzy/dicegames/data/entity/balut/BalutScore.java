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

    /** 计算机玩家点数（无计算机玩家时为0） */
    @ColumnInfo(name = "computer_points", defaultValue = "0")
    public int computerPoints;

    /** Balut得分次数 */
    @ColumnInfo(name = "num_balut")
    public int numBalut;

    /**
     * @param date 日期
     * @param score 人类玩家得分
     * @param numPlayers 游戏人数
     * @param computerScore 计算机玩家得分（无计算机玩家时为0）
     * @param points 人类玩家点数
     * @param computerPoints 计算机玩家点数（无计算机玩家时为0）
     * @param numBalut Balut得分次数
     */
    public BalutScore(
            @NonNull String date, int score, int numPlayers, int computerScore,
            int points, int computerPoints, int numBalut) {
        super(date, score, numPlayers, computerScore);
        this.points = points;
        this.computerPoints = computerPoints;
        this.numBalut = numBalut;
    }
}
