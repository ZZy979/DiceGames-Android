package com.zzy.dicegames.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.zzy.dicegames.database.entity.BalutScore;

import java.util.List;

/**
 * {@link BalutScore}类DAO接口
 *
 * @author 赵正阳
 */
@Dao
public interface BalutScoreDao {

	@Query("SELECT * FROM balut_score")
	List<BalutScore> findAll();

	@Query("SELECT * FROM balut_score ORDER BY score DESC LIMIT 10")
	List<BalutScore> findTop10();

	@Query("SELECT score FROM balut_score ORDER BY score DESC LIMIT 10")
	List<Integer> findTop10Score();

	@Query("SELECT COUNT(*) FROM balut_score")
	int count();

	@Query("SELECT MAX(score) FROM balut_score")
	int maxScore();

	@Query("SELECT MIN(score) FROM balut_score")
	int minScore();

	@Query("SELECT AVG(score) FROM balut_score")
	double averageScore();

	@Query("SELECT SUM(got_balut) FROM balut_score")
	int sumGotBalut();

	@Insert
	void insert(BalutScore balutScore);

	@Insert
	void insertAll(List<BalutScore> balutScores);

	@Delete
	void deleteAll(List<BalutScore> balutScores);

}
