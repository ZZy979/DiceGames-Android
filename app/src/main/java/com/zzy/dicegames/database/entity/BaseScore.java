package com.zzy.dicegames.database.entity;

import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * 游戏得分实体类基类
 *
 * @author 赵正阳
 */
public abstract class BaseScore implements Serializable {
	@PrimaryKey(autoGenerate = true)
	private Integer id;

	/** yyyy-MM-dd */
	@NonNull
	private String date;

	@NonNull
	private Integer score;

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
