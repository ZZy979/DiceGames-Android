package com.zzy.dicegames.data.dao;

import com.zzy.dicegames.data.entity.BalutScore;
import com.zzy.dicegames.data.entity.BalutStatistics;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * {@link BalutScore}类DAO接口
 *
 * @author 赵正阳
 */
@Dao
public interface BalutScoreDao {

    @Query("SELECT * FROM balut_score")
    List<BalutScore> findAll();

    @Query("SELECT * FROM balut_score WHERE id = :id")
    BalutScore findById(int id);

    @Query("SELECT * FROM balut_score ORDER BY score DESC, date DESC LIMIT :n")
    List<BalutScore> findTop(int n);

    @Query("SELECT COUNT(*) + 1 FROM balut_score WHERE score > :score")
    int rank(int score);

    @Query(
            "SELECT COUNT(*) AS count, MAX(score) AS maxScore, MIN(score) AS minScore, " +
            "    AVG(score) AS avgScore, SUM(num_balut) AS numBalut " +
            "FROM balut_score"
    )
    BalutStatistics statistics();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(BalutScore score);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<BalutScore> scores);

    @Delete
    void deleteAll(List<BalutScore> scores);

}
