package com.zzy.dicegames.data;

import android.content.Context;

import com.zzy.dicegames.data.dao.BalutScoreDao;
import com.zzy.dicegames.data.dao.FarkleScoreDao;
import com.zzy.dicegames.data.dao.FiveYahtzeeScoreDao;
import com.zzy.dicegames.data.dao.SixYahtzeeScoreDao;
import com.zzy.dicegames.data.entity.BalutScore;
import com.zzy.dicegames.data.entity.FarkleScore;
import com.zzy.dicegames.data.entity.FiveYahtzeeScore;
import com.zzy.dicegames.data.entity.SixYahtzeeScore;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * 游戏得分数据库<br>
 * 采用单例模式，使用{@code getInstance()}获得实例
 *
 * @author 赵正阳
 */
@Database(
        entities = {FiveYahtzeeScore.class, SixYahtzeeScore.class, BalutScore.class, FarkleScore.class},
        version = 2,
        autoMigrations = {
                @AutoMigration(from = 1, to = 2)
        }
)
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
