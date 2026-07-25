package com.zzy.dicegames.data.dao;

import android.content.Context;

import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.BalutScore;
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
public class BalutScoreDaoTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ScoreDatabase database;

    private BalutScoreDao dao;

    private List<BalutScore> testScores = List.of(
            new BalutScore("2025-03-01", 400, 1),
            new BalutScore("2025-03-02", 330, 0),
            new BalutScore("2025-03-03", 380, 0),
            new BalutScore("2025-03-04", 550, 3),
            new BalutScore("2025-03-05", 420, 1),
            new BalutScore("2025-03-06", 290, 0),
            new BalutScore("2025-03-07", 600, 4),
            new BalutScore("2025-03-08", 500, 2),
            new BalutScore("2025-03-09", 280, 0),
            new BalutScore("2025-03-10", 470, 2),
            new BalutScore("2025-03-11", 350, 1)
    );

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        database = Room.inMemoryDatabaseBuilder(context, ScoreDatabase.class)
                .allowMainThreadQueries().build();
        dao = database.balutScoreDao();
        dao.insertAll(testScores);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void testFindAll() {
        List<BalutScore> scores = dao.findAll();
        assertEquals(testScores.size(), scores.size());
        for (int i = 0; i < scores.size(); i++) {
            assertEquals(i + 1, scores.get(i).id);
            assertTrue(ScoreUtil.isEqual(testScores.get(i), scores.get(i)));
        }
    }

    @Test
    public void testFindById() {
        var score = dao.findById(7);
        assertNotNull(score);
        assertEquals("2025-03-07", score.date);
        assertEquals(600, score.score);
        assertEquals(4, score.numBalut);

        assertNull(dao.findById(999));
    }

    @Test
    public void testFindTop() {
        int[] expected = {600, 550, 500, 470, 420};
        dao.findTop(5).observeForever(topScores -> {
            assertEquals(5, topScores.size());
            for (int i = 0; i < expected.length; i++)
                assertEquals(expected[i], topScores.get(i).score);
        });

        dao.findTop(100).observeForever(topScores -> assertEquals(11, topScores.size()));
    }

    @Test
    public void testRank() {
        assertEquals(1, dao.rank(630));
        assertEquals(1, dao.rank(600));
        assertEquals(5, dao.rank(450));
        assertEquals(9, dao.rank(340));
        assertEquals(11, dao.rank(280));
        assertEquals(12, dao.rank(270));
    }

    @Test
    public void testStatistics() {
        dao.statistics().observeForever(stats -> {
            assertEquals(11, stats.count);
            assertEquals(600, stats.maxScore);
            assertEquals(280, stats.minScore);
            assertEquals(415.454545, stats.avgScore, 1e-6);
            assertEquals(14, stats.numBalut);
        });
    }

    @Test
    public void testStatisticsOnEmptyDatabase() {
        for (int i = 0; i < testScores.size(); i++)
            testScores.get(i).id = i + 1;
        dao.deleteAll(testScores);
        dao.statistics().observeForever(stats -> {
            assertEquals(0, stats.count);
            assertEquals(0, stats.maxScore);
            assertEquals(0, stats.minScore);
            assertEquals(0.0, stats.avgScore, 1e-6);
            assertEquals(0, stats.numBalut);
        });
    }

    @Test
    public void testStatisticsObserver() {
        dao.insert(new BalutScore("2025-03-12", 450, 2));
        dao.statistics().observeForever(stats -> {
            assertEquals(12, stats.count);
            assertEquals(600, stats.maxScore);
            assertEquals(280, stats.minScore);
            assertEquals(418.333333, stats.avgScore, 1e-6);
            assertEquals(16, stats.numBalut);
        });
    }

    @Test
    public void testInsert() {
        var score = new BalutScore("2025-03-12", 460, 2);
        dao.insert(score);
        assertEquals(12, dao.count());
        var actual = dao.findById(12);
        assertNotNull(actual);
        assertEquals(460, actual.score);
    }

    @Test
    public void testInsertAlreadyExist() {
        var score = new BalutScore("2025-03-03", 385, 1);
        score.id = 3;
        dao.insert(score);
        assertEquals(11, dao.count());
        assertEquals(380, dao.findById(3).score);
    }

    @Test
    public void testDelete() {
        int[] idsToDelete = {0, 3, 7, 10, 999};
        List<BalutScore> scores = new ArrayList<>();
        for (int id : idsToDelete) {
            var s = new BalutScore("", 0, 0);
            s.id = id;
            scores.add(s);
        }
        dao.deleteAll(scores);
        assertEquals(8, dao.count());
        for (int id : idsToDelete)
            assertNull(dao.findById(id));
    }
}
