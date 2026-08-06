package com.zzy.dicegames.data;

import android.content.Context;

import com.zzy.dicegames.data.dao.BalutScoreDao;
import com.zzy.dicegames.data.dao.FarkleScoreDao;
import com.zzy.dicegames.data.dao.MaxiYatzyScoreDao;
import com.zzy.dicegames.data.dao.YahtzeeScoreDao;
import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.data.entity.farkle.FarkleScore;
import com.zzy.dicegames.data.entity.yahtzee.YahtzeeScore;
import com.zzy.dicegames.data.entity.yatzy.MaxiYatzyScore;

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
        entities = {YahtzeeScore.class, MaxiYatzyScore.class, BalutScore.class, FarkleScore.class},
        version = 1
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

    public abstract MaxiYatzyScoreDao maxiYatzyScoreDao();

    public abstract BalutScoreDao balutScoreDao();

    public abstract FarkleScoreDao farkleScoreDao();

}
