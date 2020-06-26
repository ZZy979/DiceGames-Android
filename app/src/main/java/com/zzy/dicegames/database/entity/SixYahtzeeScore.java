package com.zzy.dicegames.database.entity;

import androidx.room.Entity;
import androidx.annotation.NonNull;

/**
 * 6骰Yahtzee得分实体类
 *
 * @author 赵正阳
 */
@Entity(tableName = "six_yahtzee_score")
public class SixYahtzeeScore extends AbstractYahtzeeScore {

	public SixYahtzeeScore(String date, @NonNull Integer score, @NonNull Integer gotBonus, @NonNull Integer gotYahtzee) {
		super(date, score, gotBonus, gotYahtzee);
	}

}
