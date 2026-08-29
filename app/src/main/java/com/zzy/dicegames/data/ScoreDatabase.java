package com.zzy.dicegames.data;

import android.content.Context;

import com.zzy.dicegames.data.dao.BalutScoreDao;
import com.zzy.dicegames.data.dao.CragScoreDao;
import com.zzy.dicegames.data.dao.FarkleScoreDao;
import com.zzy.dicegames.data.dao.LiarsDiceScoreDao;
import com.zzy.dicegames.data.dao.MaxiYatzyScoreDao;
import com.zzy.dicegames.data.dao.PigScoreDao;
import com.zzy.dicegames.data.dao.YahtzeeScoreDao;
import com.zzy.dicegames.data.dao.YatzyScoreDao;
import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.data.entity.crag.CragScore;
import com.zzy.dicegames.data.entity.farkle.FarkleScore;
import com.zzy.dicegames.data.entity.liarsdice.LiarsDiceScore;
import com.zzy.dicegames.data.entity.pig.PigScore;
import com.zzy.dicegames.data.entity.yahtzee.YahtzeeScore;
import com.zzy.dicegames.data.entity.yatzy.MaxiYatzyScore;
import com.zzy.dicegames.data.entity.yatzy.YatzyScore;

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
        entities = {
                YahtzeeScore.class, YatzyScore.class, MaxiYatzyScore.class, BalutScore.class,
                LiarsDiceScore.class, FarkleScore.class, PigScore.class, CragScore.class
        },
        version = 5,
        autoMigrations = {
                @AutoMigration(from = 1, to = 2),
                @AutoMigration(from = 2, to = 3),
                @AutoMigration(from = 3, to = 4),
                @AutoMigration(from = 4, to = 5)
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

    public abstract YahtzeeScoreDao yahtzeeScoreDao();

    public abstract YatzyScoreDao yatzyScoreDao();

    public abstract MaxiYatzyScoreDao maxiYatzyScoreDao();

    public abstract BalutScoreDao balutScoreDao();

    public abstract LiarsDiceScoreDao liarsDiceScoreDao();

    public abstract FarkleScoreDao farkleScoreDao();

    public abstract PigScoreDao pigScoreDao();

    public abstract CragScoreDao cragScoreDao();
}
