package com.zzy.dicegames.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.zzy.dicegames.database.entity.FiveYahtzeeScore;

import java.util.List;

/**
 * {@link FiveYahtzeeScore}类DAO接口
 *
 * @author 赵正阳
 */
@Dao
public interface FiveYahtzeeScoreDao {

	@Query("SELECT * FROM five_yahtzee_score")
	List<FiveYahtzeeScore> findAll();

	@Query("SELECT * FROM five_yahtzee_score ORDER BY score DESC, date DESC LIMIT 10")
	List<FiveYahtzeeScore> findTop10();

	@Query("SELECT score FROM five_yahtzee_score ORDER BY score DESC LIMIT 10")
	List<Integer> findTop10Score();

	@Query("SELECT COUNT(*) FROM five_yahtzee_score")
	int count();

	@Query("SELECT MAX(score) FROM five_yahtzee_score")
	int maxScore();

	@Query("SELECT MIN(score) FROM five_yahtzee_score")
	int minScore();

	@Query("SELECT AVG(score) FROM five_yahtzee_score")
	double averageScore();

	@Query("SELECT SUM(got_bonus) FROM five_yahtzee_score")
	int sumGotBonus();

	@Query("SELECT SUM(got_yahtzee) FROM five_yahtzee_score")
	int sumGotYahtzee();

	@Insert
	void insert(FiveYahtzeeScore fiveYahtzeeScore);

	@Insert
	void insertAll(List<FiveYahtzeeScore> fiveYahtzeeScores);

	@Delete
	void deleteAll(List<FiveYahtzeeScore> fiveYahtzeeScores);

}
