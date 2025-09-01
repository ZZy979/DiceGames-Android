package com.zzy.dicegames.data.dao;

import com.zzy.dicegames.data.entity.FiveYahtzeeScore;
import com.zzy.dicegames.data.entity.YahtzeeStatistics;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * {@link FiveYahtzeeScore}类DAO接口
 *
 * @author 赵正阳
 */
@Dao
public interface FiveYahtzeeScoreDao {

    @Query("SELECT * FROM five_yahtzee_score")
    List<FiveYahtzeeScore> findAll();

    @Query("SELECT * FROM five_yahtzee_score WHERE id = :id")
    FiveYahtzeeScore findById(int id);

    @Query("SELECT * FROM five_yahtzee_score ORDER BY score DESC, date DESC LIMIT :n")
    List<FiveYahtzeeScore> findTop(int n);

    @Query("SELECT COUNT(*) + 1 FROM five_yahtzee_score WHERE score > :score")
    int rank(int score);

    @Query(
            "SELECT COUNT(*) AS count, MAX(score) AS maxScore, MIN(score) AS minScore, " +
            "    AVG(score) AS avgScore, SUM(has_bonus) AS numBonus, SUM(has_yahtzee) AS numYahtzee " +
            "FROM five_yahtzee_score"
    )
    YahtzeeStatistics statistics();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(FiveYahtzeeScore score);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<FiveYahtzeeScore> scores);

    @Delete
    void deleteAll(List<FiveYahtzeeScore> scores);

}
