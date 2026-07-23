package com.zzy.dicegames.data.dao;

import com.zzy.dicegames.data.entity.SixYahtzeeScore;
import com.zzy.dicegames.data.entity.YahtzeeStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * {@link SixYahtzeeScore}类DAO接口
 *
 * @author 赵正阳
 */
@Dao
public interface SixYahtzeeScoreDao {

    @Query("SELECT * FROM six_yahtzee_score")
    List<SixYahtzeeScore> findAll();

    @Query("SELECT * FROM six_yahtzee_score WHERE id = :id")
    SixYahtzeeScore findById(int id);

    @Query("SELECT * FROM six_yahtzee_score ORDER BY score DESC, date DESC LIMIT :n")
    LiveData<List<SixYahtzeeScore>> findTop(int n);

    @Query("SELECT COUNT(*) + 1 FROM six_yahtzee_score WHERE score > :score")
    int rank(int score);

    @Query("SELECT COUNT(*) FROM six_yahtzee_score")
    int count();

    @Query(
            "SELECT COUNT(*) AS count, MAX(score) AS maxScore, MIN(score) AS minScore, " +
            "    AVG(score) AS avgScore, SUM(has_bonus) AS numBonus, SUM(has_yahtzee) AS numYahtzee " +
            "FROM six_yahtzee_score"
    )
    LiveData<YahtzeeStatistics> statistics();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(SixYahtzeeScore score);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<SixYahtzeeScore> scores);

    @Delete
    void deleteAll(List<SixYahtzeeScore> scores);

}
