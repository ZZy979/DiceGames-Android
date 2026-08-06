package com.zzy.dicegames.data.dao;

import com.zzy.dicegames.data.entity.yatzy.MaxiYatzyScore;
import com.zzy.dicegames.data.entity.yatzy.MaxiYatzyStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * {@link MaxiYatzyScore}类DAO接口
 *
 * @author 赵正阳
 */
@Dao
public interface MaxiYatzyScoreDao {

    @Query("SELECT * FROM maxi_yatzy_score")
    List<MaxiYatzyScore> findAll();

    @Query("SELECT * FROM maxi_yatzy_score WHERE id = :id")
    MaxiYatzyScore findById(int id);

    @Query("SELECT * FROM maxi_yatzy_score ORDER BY score DESC, date DESC LIMIT :n")
    LiveData<List<MaxiYatzyScore>> findTop(int n);

    @Query("SELECT COUNT(*) + 1 FROM maxi_yatzy_score WHERE score > :score")
    int rank(int score);

    @Query("SELECT COUNT(*) FROM maxi_yatzy_score")
    int count();

    @Query(
            "SELECT COUNT(*) AS count, MAX(score) AS maxScore, MIN(score) AS minScore, " +
            "    AVG(score) AS avgScore, SUM(has_bonus) AS numBonus, SUM(has_yatzy) AS numYatzy " +
            "FROM maxi_yatzy_score"
    )
    LiveData<MaxiYatzyStatistics> statistics();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(MaxiYatzyScore score);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<MaxiYatzyScore> scores);

    @Delete
    void deleteAll(List<MaxiYatzyScore> scores);

}
