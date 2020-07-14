package com.zzy.dicegames.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * Farkle得分实体类
 *
 * @author 赵正阳
 */
@Entity(tableName = "farkle_score")
public class FarkleScore extends BaseScore {
	/** CPU得分 */
	@ColumnInfo(name = "cpu_score")
	@NonNull
	private Integer cpuScore;

	public FarkleScore(@NonNull String date, @NonNull Integer score, @NonNull Integer cpuScore) {
		super(date, score);
		this.cpuScore = cpuScore;
	}

	@NonNull
	public Integer getCpuScore() {
		return cpuScore;
	}

	public void setCpuScore(@NonNull Integer cpuScore) {
		this.cpuScore = cpuScore;
	}

}
