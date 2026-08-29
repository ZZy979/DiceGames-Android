package com.zzy.dicegames.data.dao;

import com.zzy.dicegames.data.entity.yatzy.YatzyScore;
import com.zzy.dicegames.data.entity.yatzy.YatzyStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * {@link YatzyScore}类DAO接口
 */
@Dao
public interface YatzyScoreDao {

    @Query("SELECT * FROM yatzy_score")
    List<YatzyScore> findAll();

    @Query("SELECT * FROM yatzy_score WHERE id = :id")
    YatzyScore findById(int id);

    @Query("SELECT * FROM yatzy_score ORDER BY score DESC, date DESC LIMIT :n")
    LiveData<List<YatzyScore>> findTop(int n);

    @Query("SELECT COUNT(*) + 1 FROM yatzy_score WHERE score > :score")
    int rank(int score);

    @Query("SELECT COUNT(*) FROM yatzy_score")
    int count();

    @Query(
            "SELECT COUNT(*) AS count, MAX(score) AS maxScore, MIN(score) AS minScore, " +
            "    AVG(score) AS avgScore, SUM(has_bonus) AS numBonus, SUM(has_yatzy) AS numYatzy " +
            "FROM yatzy_score"
    )
    LiveData<YatzyStatistics> statistics();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(YatzyScore score);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<YatzyScore> scores);

    @Delete
    void deleteAll(List<YatzyScore> scores);

}
