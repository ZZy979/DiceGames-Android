package com.zzy.dicegames.data.dao;

import android.content.Context;

import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.liarsdice.LiarsDiceScore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class LiarsDiceScoreDaoTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ScoreDatabase database;

    private LiarsDiceScoreDao dao;

    private List<LiarsDiceScore> testScores = List.of(
            new LiarsDiceScore("2026-08-01", 2, 5, 5),
            new LiarsDiceScore("2026-08-02", 2, 6, 4),
            new LiarsDiceScore("2026-08-03", 2, 4, 6),
            new LiarsDiceScore("2026-08-04", 3, 4, 1),
            new LiarsDiceScore("2026-08-05", 3, 7, 3),
            new LiarsDiceScore("2026-08-06", 4, 2, 3),
            new LiarsDiceScore("2026-08-07", 4, 9, 1)
    );

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        database = Room.inMemoryDatabaseBuilder(context, ScoreDatabase.class)
                .allowMainThreadQueries().build();
        dao = database.liarsDiceScoreDao();
        dao.insertAll(testScores);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void testFindAll() {
        List<LiarsDiceScore> scores = dao.findAll();
        assertEquals(testScores.size(), scores.size());
        for (int i = 0; i < scores.size(); i++) {
            assertEquals(i + 1, scores.get(i).id);
            assertEquals(0, scores.get(i).score);  // 父类score固定填0
            LiarsDiceScore expected = testScores.get(i);
            LiarsDiceScore actual = scores.get(i);
            assertEquals(expected.date, actual.date);
            assertEquals(expected.numPlayers, actual.numPlayers);
            assertEquals(expected.wins, actual.wins);
            assertEquals(expected.losses, actual.losses);
        }
    }

    @Test
    public void testFindById() {
        var score = dao.findById(6);
        assertNotNull(score);
        assertEquals("2026-08-06", score.date);
        assertEquals(4, score.numPlayers);
        assertEquals(2, score.wins);
        assertEquals(3, score.losses);

        assertNull(dao.findById(999));
    }

    @Test
    public void testStatistics() {
        // 2人：5+6+4=15胜，5+4+6=15负
        dao.statistics(2).observeForever(stats -> {
            assertEquals(2, stats.numPlayers);
            assertEquals(15, stats.wins);
            assertEquals(15, stats.losses);
            assertEquals(30, stats.getTotalGames());
            assertEquals(0.5, stats.getWinRate(), 0.0001);
        });
        // 3人：4+7=11胜，1+3=4负
        dao.statistics(3).observeForever(stats -> {
            assertEquals(3, stats.numPlayers);
            assertEquals(11, stats.wins);
            assertEquals(4, stats.losses);
        });
        // 4人：2+9=11胜，3+1=4负
        dao.statistics(4).observeForever(stats -> {
            assertEquals(4, stats.numPlayers);
            assertEquals(11, stats.wins);
            assertEquals(4, stats.losses);
        });
    }

    @Test
    public void testTotalStatistics() {
        dao.totalStatistics().observeForever(stats -> {
            assertEquals(37, stats.wins);   // 15+11+11
            assertEquals(23, stats.losses);  // 15+4+4
            assertEquals(60, stats.getTotalGames());
        });
    }

    @Test
    public void testStatisticsOnEmptyDatabase() {
        for (int i = 0; i < testScores.size(); i++)
            testScores.get(i).id = i + 1;
        dao.deleteAll(testScores);
        dao.statistics(2).observeForever(stats -> {
            assertEquals(0, stats.wins);
            assertEquals(0, stats.losses);
            assertEquals(0.0, stats.getWinRate(), 0.0001);
        });
        dao.totalStatistics().observeForever(stats -> {
            assertEquals(0, stats.wins);
            assertEquals(0, stats.losses);
        });
    }

    @Test
    public void testCount() {
        assertEquals(testScores.size(), dao.count());
    }

    @Test
    public void testInsert() {
        var score = new LiarsDiceScore("2026-08-08", 4, 8, 2);
        dao.insert(score);
        assertEquals(testScores.size() + 1, dao.count());
        var saved = dao.findById(testScores.size() + 1);
        assertNotNull(saved);
        assertEquals(0, saved.score);
        assertEquals(4, saved.numPlayers);
        assertEquals(8, saved.wins);
        assertEquals(2, saved.losses);
    }
}
