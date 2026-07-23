package com.zzy.dicegames.data.dao;

import com.zzy.dicegames.data.entity.FarkleScore;
import com.zzy.dicegames.data.entity.FarkleStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * {@link}类DAO接口
 *
 * @author 赵正阳
 */
@Dao
public interface FarkleScoreDao {

    @Query("SELECT * FROM farkle_score")
    List<FarkleScore> findAll();

    @Query("SELECT * FROM farkle_score WHERE id = :id")
    FarkleScore findById(int id);

    @Query("SELECT COUNT(*) FROM farkle_score")
    int count();

    @Query(
            "SELECT COUNT(*) AS count, SUM(score > computer_score) AS winCount " +
            "FROM farkle_score"
    )
    LiveData<FarkleStatistics> statistics();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(FarkleScore score);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<FarkleScore> scores);

    @Delete
    void deleteAll(List<FarkleScore> scores);
}
