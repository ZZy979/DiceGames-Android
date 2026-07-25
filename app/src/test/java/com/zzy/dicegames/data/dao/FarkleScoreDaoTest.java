package com.zzy.dicegames.data.dao;

import android.content.Context;

import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.FarkleScore;
import com.zzy.dicegames.utils.score.ScoreUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class FarkleScoreDaoTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ScoreDatabase database;

    private FarkleScoreDao dao;

    private List<FarkleScore> testScores = List.of(
            new FarkleScore("2025-04-01", 10450, 8500),
            new FarkleScore("2025-04-02", 10000, 9000),
            new FarkleScore("2025-04-03", 8800, 10550),
            new FarkleScore("2025-04-04", 10500, 6700),
            new FarkleScore("2025-04-05", 6250, 10050),
            new FarkleScore("2025-04-06", 11050, 7500),
            new FarkleScore("2025-04-07", 10200, 8400),
            new FarkleScore("2025-04-08", 7900, 10100)
    );

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        database = Room.inMemoryDatabaseBuilder(context, ScoreDatabase.class)
                .allowMainThreadQueries().build();
        dao = database.farkleScoreDao();
        dao.insertAll(testScores);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void testFindAll() {
        List<FarkleScore> scores = dao.findAll();
        assertEquals(testScores.size(), scores.size());
        for (int i = 0; i < scores.size(); i++) {
            assertEquals(i + 1, scores.get(i).id);
            assertTrue(ScoreUtil.isEqual(testScores.get(i), scores.get(i)));
        }
    }

    @Test
    public void testFindById() {
        var score = dao.findById(6);
        assertNotNull(score);
        assertEquals("2025-04-06", score.date);
        assertEquals(11050, score.score);
        assertEquals(7500, score.computerScore);

        assertNull(dao.findById(999));
    }

    @Test
    public void testStatistics() {
        dao.statistics().observeForever(stats -> {
            assertEquals(8, stats.count);
            assertEquals(5, stats.winCount);
        });
    }

    @Test
    public void testStatisticsOnEmptyDatabase() {
        for (int i = 0; i < testScores.size(); i++)
            testScores.get(i).id = i + 1;
        dao.deleteAll(testScores);
        dao.statistics().observeForever(stats -> {
            assertEquals(0, stats.count);
            assertEquals(0, stats.winCount);
        });
    }

    @Test
    public void testStatisticsObserver() {
        dao.insert(new FarkleScore("2025-04-09", 11000, 9800));
        dao.statistics().observeForever(stats -> {
            assertEquals(9, stats.count);
            assertEquals(6, stats.winCount);
        });
    }

    @Test
    public void testInsert() {
        var score = new FarkleScore("2025-04-09", 9800, 10650);
        dao.insert(score);
        assertEquals(9, dao.count());
        var actual = dao.findById(9);
        assertNotNull(actual);
        assertEquals(9800, actual.score);
    }

    @Test
    public void testInsertAlreadyExist() {
        var score = new FarkleScore("2025-04-02", 10050, 9050);
        score.id = 2;
        dao.insert(score);
        assertEquals(8, dao.count());
        assertEquals(10000, dao.findById(2).score);
    }

    @Test
    public void testDelete() {
        int[] idsToDelete = {3, 7, 999};
        List<FarkleScore> scores = new ArrayList<>();
        for (int id : idsToDelete) {
            var s = new FarkleScore("", 0, 0);
            s.id = id;
            scores.add(s);
        }
        dao.deleteAll(scores);
        assertEquals(6, dao.count());
        for (int id : idsToDelete)
            assertNull(dao.findById(id));
    }
}
