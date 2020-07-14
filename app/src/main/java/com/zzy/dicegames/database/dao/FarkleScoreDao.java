package com.zzy.dicegames.database.dao;

import com.zzy.dicegames.database.entity.FarkleScore;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
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

	@Query("SELECT COUNT(*) FROM farkle_score")
	int count();

	@Query("SELECT COUNT(*) FROM farkle_score WHERE score > cpu_score")
	int winCount();

	@Insert
	void insert(FarkleScore farkleScore);

	@Insert
	void insertAll(List<FarkleScore> farkleScores);

	@Delete
	void deleteAll(List<FarkleScore> farkleScores);
}
