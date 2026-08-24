package com.zzy.dicegames.data.entity.liarsdice;

import com.zzy.dicegames.data.entity.BaseScore;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * 大话骰得分实体类
 *
 * @author 赵正阳
 */
@Entity(tableName = "liars_dice_score")
public class LiarsDiceScore extends BaseScore {
    /** 游戏人数 */
    @ColumnInfo(name = "num_players")
    public int numPlayers;

    /** 胜局数 */
    @ColumnInfo(name = "wins")
    public int wins;

    /** 负局数 */
    @ColumnInfo(name = "losses")
    public int losses;

    public LiarsDiceScore(@NonNull String date, int numPlayers, int wins, int losses) {
        super(date, 0);
        this.numPlayers = numPlayers;
        this.wins = wins;
        this.losses = losses;
    }
}
