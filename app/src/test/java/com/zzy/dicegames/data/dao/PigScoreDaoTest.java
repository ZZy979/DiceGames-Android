package com.zzy.dicegames.data.dao;

import android.content.Context;

import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.pig.PigScore;
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
public class PigScoreDaoTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ScoreDatabase database;

    private PigScoreDao dao;

    private List<PigScore> testScores = List.of(
            new PigScore("2025-04-01", 100, 85),
            new PigScore("2025-04-02", 85, 100),
            new PigScore("2025-04-03", 92, 88),
            new PigScore("2025-04-04", 100, 90),
            new PigScore("2025-04-05", 78, 100),
            new PigScore("2025-04-06", 95, 100),
            new PigScore("2025-04-07", 100, 92),
            new PigScore("2025-04-08", 88, 100)
    );

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        database = Room.inMemoryDatabaseBuilder(context, ScoreDatabase.class)
                .allowMainThreadQueries().build();
        dao = database.pigScoreDao();
        dao.insertAll(testScores);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void testFindAll() {
        List<PigScore> scores = dao.findAll();
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
        assertEquals(95, score.score);
        assertEquals(100, score.computerScore);

        assertNull(dao.findById(999));
    }

    @Test
    public void testStatistics() {
        dao.statistics().observeForever(stats -> {
            assertEquals(8, stats.count);
            assertEquals(4, stats.winCount);
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
        dao.insert(new PigScore("2025-04-09", 100, 80));
        dao.statistics().observeForever(stats -> {
            assertEquals(9, stats.count);
            assertEquals(5, stats.winCount);
        });
    }

    @Test
    public void testInsert() {
        var score = new PigScore("2025-04-09", 98, 100);
        dao.insert(score);
        assertEquals(9, dao.count());
        var actual = dao.findById(9);
        assertNotNull(actual);
        assertEquals(98, actual.score);
    }

    @Test
    public void testInsertAlreadyExist() {
        var score = new PigScore("2025-04-02", 100, 50);
        score.id = 2;
        dao.insert(score);
        assertEquals(8, dao.count());
        assertEquals(85, dao.findById(2).score);
    }

    @Test
    public void testDelete() {
        int[] idsToDelete = {3, 7, 999};
        List<PigScore> scores = new ArrayList<>();
        for (int id : idsToDelete) {
            var s = new PigScore("", 0, 0);
            s.id = id;
            scores.add(s);
        }
        dao.deleteAll(scores);
        assertEquals(6, dao.count());
        for (int id : idsToDelete)
            assertNull(dao.findById(id));
    }
}
