package com.zzy.dicegames.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.zzy.dicegames.database.entity.FiveYahtzeeScore;

import java.util.List;

/**
 * {@link FiveYahtzeeScore}类DAO接口
 *
 * @author 赵正阳
 */
@Dao
public interface FiveYahtzeeScoreDao {

	@Query("SELECT * FROM five_yahtzee_score ORDER BY score DESC LIMIT 10")
	List<FiveYahtzeeScore> findTop10();

	@Query("SELECT score FROM five_yahtzee_score ORDER BY score DESC LIMIT 10")
	List<Integer> findTop10Score();

	@Insert
	void insert(FiveYahtzeeScore fiveYahtzeeScore);

}
