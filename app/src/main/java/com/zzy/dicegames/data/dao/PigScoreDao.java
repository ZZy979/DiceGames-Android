package com.zzy.dicegames.data.dao;

import com.zzy.dicegames.data.entity.pig.PigScore;
import com.zzy.dicegames.data.entity.pig.PigStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * {@link PigScore}类DAO接口
 */
@Dao
public interface PigScoreDao {

    @Query("SELECT * FROM pig_score")
    List<PigScore> findAll();

    @Query("SELECT * FROM pig_score WHERE id = :id")
    PigScore findById(int id);

    @Query("SELECT COUNT(*) FROM pig_score")
    int count();

    @Query(
            "SELECT COUNT(*) AS count, MAX(score) AS maxScore, MIN(score) AS minScore," +
            "    AVG(score) AS avgScore, SUM(score > computer_score) AS winCount " +
            "FROM pig_score"
    )
    LiveData<PigStatistics> statistics();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(PigScore score);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<PigScore> scores);

    @Delete
    void deleteAll(List<PigScore> scores);
}
