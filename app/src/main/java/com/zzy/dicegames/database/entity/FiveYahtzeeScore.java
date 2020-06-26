package com.zzy.dicegames.database.entity;

import androidx.room.Entity;
import androidx.annotation.NonNull;

/**
 * 5骰Yahtzee得分实体类
 *
 * @author 赵正阳
 */
@Entity(tableName = "five_yahtzee_score")
public class FiveYahtzeeScore extends AbstractYahtzeeScore {

	public FiveYahtzeeScore(String date, @NonNull Integer score, @NonNull Integer gotBonus, @NonNull Integer gotYahtzee) {
		super(date, score, gotBonus, gotYahtzee);
	}

}
