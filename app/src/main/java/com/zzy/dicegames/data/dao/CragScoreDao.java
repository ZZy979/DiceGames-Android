package com.zzy.dicegames.data.dao;

import com.zzy.dicegames.data.entity.crag.CragScore;
import com.zzy.dicegames.data.entity.crag.CragStatistics;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * {@link CragScore}类DAO接口
 */
@Dao
public interface CragScoreDao {

    @Query("SELECT * FROM crag_score")
    List<CragScore> findAll();

    @Query("SELECT * FROM crag_score WHERE id = :id")
    CragScore findById(int id);

    @Query("SELECT * FROM crag_score ORDER BY score DESC, date DESC LIMIT :n")
    LiveData<List<CragScore>> findTop(int n);

    @Query("SELECT COUNT(*) + 1 FROM crag_score WHERE score > :score")
    int rank(int score);

    @Query("SELECT COUNT(*) FROM crag_score")
    int count();

    @Query(
            "SELECT COUNT(*) AS count, MAX(score) AS maxScore, MIN(score) AS minScore, " +
            "    AVG(score) AS avgScore, SUM(has_crag) AS numCrag " +
            "FROM crag_score"
    )
    LiveData<CragStatistics> statistics();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(CragScore score);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<CragScore> scores);

    @Delete
    void deleteAll(List<CragScore> scores);

}
