package com.zzy.dicegames.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.zzy.dicegames.database.dao.BalutScoreDao;
import com.zzy.dicegames.database.dao.FiveYahtzeeScoreDao;
import com.zzy.dicegames.database.dao.SixYahtzeeScoreDao;
import com.zzy.dicegames.database.entity.BalutScore;
import com.zzy.dicegames.database.entity.FiveYahtzeeScore;
import com.zzy.dicegames.database.entity.SixYahtzeeScore;

/**
 * 游戏得分数据库<br>
 * 采用单例模式，使用{@code getInstance()}获得实例
 *
 * @author 赵正阳
 */
@Database(
		entities = {FiveYahtzeeScore.class, SixYahtzeeScore.class, BalutScore.class},
		version = 1)
public abstract class ScoreDatabase extends RoomDatabase {
	/** 唯一实例 */
	private static ScoreDatabase sInstance;

	/** 数据库文件名 */
	private static final String FILENAME = "score.db";

	public static synchronized ScoreDatabase getInstance(Context context) {
		if (sInstance == null)
			sInstance = Room.databaseBuilder(context.getApplicationContext(), ScoreDatabase.class, FILENAME)
					.allowMainThreadQueries()
					.build();
		return sInstance;
	}

	public abstract FiveYahtzeeScoreDao fiveYahtzeeScoreDao();

	public abstract SixYahtzeeScoreDao sixYahtzeeScoreDao();

	public abstract BalutScoreDao balutScoreDao();

}
