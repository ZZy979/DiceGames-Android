package com.zzy.dicegames.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.annotation.NonNull;

/**
 * Balut得分实体类
 *
 * @author 赵正阳
 */
@Entity(tableName = "balut_score")
public class BalutScore extends BaseScore {
	/** Balut得分次数 */
	@ColumnInfo(name = "got_balut")
	@NonNull
	private Integer gotBalut;

	public BalutScore(@NonNull String date, @NonNull Integer score, @NonNull Integer gotBalut) {
		super(date, score);
		this.gotBalut = gotBalut;
	}

	@NonNull
	public Integer getGotBalut() {
		return gotBalut;
	}

	public void setGotBalut(@NonNull Integer gotBalut) {
		this.gotBalut = gotBalut;
	}

}
