package com.zzy.dicegames.database.dao;

import android.arch.persistence.room.Dao;
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

	@Query("SELECT * FROM balut_score ORDER BY score DESC LIMIT 10")
	List<BalutScore> findTop10();

	@Query("SELECT score FROM balut_score ORDER BY score DESC LIMIT 10")
	List<Integer> findTop10Score();

	@Insert
	void insert(BalutScore balutScore);

}
