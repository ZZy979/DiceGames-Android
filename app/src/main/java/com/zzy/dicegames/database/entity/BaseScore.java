package com.zzy.dicegames.database.entity;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.PrimaryKey;

/**
 * 游戏得分实体类基类
 *
 * @author 赵正阳
 */
public abstract class BaseScore implements Serializable {
	@PrimaryKey(autoGenerate = true)
	protected Integer id;

	/** yyyy-MM-dd */
	@NonNull
	protected String date;

	@NonNull
	protected Integer score;

	public BaseScore(@NonNull String date, @NonNull Integer score) {
		this.date = date;
		this.score = score;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@NonNull
	public String getDate() {
		return date;
	}

	public void setDate(@NonNull String date) {
		this.date = date;
	}

	@NonNull
	public Integer getScore() {
		return score;
	}

	public void setScore(@NonNull Integer score) {
		this.score = score;
	}

}
