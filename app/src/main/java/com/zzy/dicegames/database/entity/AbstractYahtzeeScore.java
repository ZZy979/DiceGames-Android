package com.zzy.dicegames.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.support.annotation.NonNull;

/**
 * Yahtzee得分实体类基类
 *
 * @author 赵正阳
 */
public abstract class AbstractYahtzeeScore extends BaseScore {
	/** 是否获得奖励分 */
	@ColumnInfo(name = "got_bonus")
	@NonNull
	private Integer gotBonus;

	/** Yahtzee是否得分 */
	@ColumnInfo(name = "got_yahtzee")
	@NonNull
	private Integer gotYahtzee;

	public AbstractYahtzeeScore(@NonNull String date, @NonNull Integer score, @NonNull Integer gotBonus, @NonNull Integer gotYahtzee) {
		super(date, score);
		this.gotBonus = gotBonus;
		this.gotYahtzee = gotYahtzee;
	}

	@NonNull
	public Integer getGotBonus() {
		return gotBonus;
	}

	public void setGotBonus(@NonNull Integer gotBonus) {
		this.gotBonus = gotBonus;
	}

	@NonNull
	public Integer getGotYahtzee() {
		return gotYahtzee;
	}

	public void setGotYahtzee(@NonNull Integer gotYahtzee) {
		this.gotYahtzee = gotYahtzee;
	}

}
