package com.zzy.dicegames.database;

import android.content.Context;

import com.zzy.dicegames.database.dao.BalutScoreDao;
import com.zzy.dicegames.database.dao.FarkleScoreDao;
import com.zzy.dicegames.database.dao.FiveYahtzeeScoreDao;
import com.zzy.dicegames.database.dao.SixYahtzeeScoreDao;
import com.zzy.dicegames.database.entity.BalutScore;
import com.zzy.dicegames.database.entity.FarkleScore;
import com.zzy.dicegames.database.entity.FiveYahtzeeScore;
import com.zzy.dicegames.database.entity.SixYahtzeeScore;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * 游戏得分数据库<br>
 * 采用单例模式，使用{@code getInstance()}获得实例
 *
 * @author 赵正阳
 */
@Database(
		entities = {FiveYahtzeeScore.class, SixYahtzeeScore.class, BalutScore.class, FarkleScore.class},
		version = 2
)
public abstract class ScoreDatabase extends RoomDatabase {
	/** 唯一实例 */
	private static ScoreDatabase sInstance;

	/** 数据库文件名 */
	private static final String FILENAME = "score.db";

	static final Migration MIGRATION_1_2 = new Migration(1, 2) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			database.execSQL("CREATE TABLE farkle_score(id INTEGER, date TEXT NOT NULL," +
					" score INTEGER NOT NULL, cpu_score INTEGER NOT NULL, PRIMARY KEY(id))");
		}
	};

	public static synchronized ScoreDatabase getInstance(Context context) {
		if (sInstance == null)
			sInstance = Room.databaseBuilder(context.getApplicationContext(), ScoreDatabase.class, FILENAME)
					.addMigrations(MIGRATION_1_2)
					.allowMainThreadQueries()
					.build();
		return sInstance;
	}

	public static void closeInstance() {
		if (sInstance != null) {
			sInstance.close();
			// 不要保留已关闭的数据库实例
			sInstance = null;
		}
	}

	public abstract FiveYahtzeeScoreDao fiveYahtzeeScoreDao();

	public abstract SixYahtzeeScoreDao sixYahtzeeScoreDao();

	public abstract BalutScoreDao balutScoreDao();

	public abstract FarkleScoreDao farkleScoreDao();

}
