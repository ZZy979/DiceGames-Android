package com.zzy.dicegames.data.dao;

import com.zzy.dicegames.data.entity.liarsdice.LiarsDiceScore;
import com.zzy.dicegames.data.entity.liarsdice.LiarsDiceStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * 大话骰得分DAO接口
 *
 * @author 赵正阳
 */
@Dao
public interface LiarsDiceScoreDao {

    @Query("SELECT * FROM liars_dice_score")
    List<LiarsDiceScore> findAll();

    @Query("SELECT * FROM liars_dice_score WHERE id = :id")
    LiarsDiceScore findById(int id);

    @Query("SELECT COUNT(*) FROM liars_dice_score")
    int count();

    /**
     * 统计指定游戏人数的胜负局数
     */
    @Query(
            "SELECT COUNT(*) AS count, MAX(score) AS maxScore, MIN(score) AS minScore, " +
            "    AVG(score) AS avgScore, :numPlayers AS numPlayers, " +
            "    IFNULL(SUM(wins), 0) AS wins, IFNULL(SUM(losses), 0) AS losses " +
            "FROM liars_dice_score WHERE num_players = :numPlayers"
    )
    LiveData<LiarsDiceStatistics> statistics(int numPlayers);

    /**
     * 统计所有游戏的总胜负局数
     */
    @Query(
            "SELECT COUNT(*) AS count, MAX(score) AS maxScore, MIN(score) AS minScore, " +
            "    AVG(score) AS avgScore, 0 AS numPlayers, " +
            "    IFNULL(SUM(wins), 0) AS wins, IFNULL(SUM(losses), 0) AS losses " +
            "FROM liars_dice_score"
    )
    LiveData<LiarsDiceStatistics> totalStatistics();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(LiarsDiceScore score);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<LiarsDiceScore> scores);

    @Delete
    void deleteAll(List<LiarsDiceScore> scores);
}
