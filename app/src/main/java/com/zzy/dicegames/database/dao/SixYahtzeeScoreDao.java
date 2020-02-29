package com.zzy.dicegames.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.zzy.dicegames.database.entity.SixYahtzeeScore;

import java.util.List;

/**
 * {@link SixYahtzeeScore}类DAO接口
 *
 * @author 赵正阳
 */
@Dao
public interface SixYahtzeeScoreDao {

	@Query("SELECT * FROM six_yahtzee_score")
	List<SixYahtzeeScore> findAll();

	@Query("SELECT * FROM six_yahtzee_score ORDER BY score DESC, date DESC LIMIT 10")
	List<SixYahtzeeScore> findTop10();

	@Query("SELECT score FROM six_yahtzee_score ORDER BY score DESC LIMIT 10")
	List<Integer> findTop10Score();

	@Query("SELECT COUNT(*) FROM six_yahtzee_score")
	int count();

	@Query("SELECT MAX(score) FROM six_yahtzee_score")
	int maxScore();

	@Query("SELECT MIN(score) FROM six_yahtzee_score")
	int minScore();

	@Query("SELECT AVG(score) FROM six_yahtzee_score")
	double averageScore();

	@Query("SELECT SUM(got_bonus) FROM six_yahtzee_score")
	int sumGotBonus();

	@Query("SELECT SUM(got_yahtzee) FROM six_yahtzee_score")
	int sumGotYahtzee();

	@Insert
	void insert(SixYahtzeeScore sixYahtzeeScore);

	@Insert
	void insertAll(List<SixYahtzeeScore> sixYahtzeeScores);

	@Delete
	void deleteAll(List<SixYahtzeeScore> sixYahtzeeScores);

}
