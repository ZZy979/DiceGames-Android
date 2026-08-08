package com.zzy.dicegames.data.dao;

import android.content.Context;

import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.balut.BalutScore;
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
            new BalutScore("2025-03-01", 400, 14, 1),
            new BalutScore("2025-03-02", 330, 6, 0),
            new BalutScore("2025-03-03", 380, 10, 0),
            new BalutScore("2025-03-04", 550, 23, 3),
            new BalutScore("2025-03-05", 420, 14, 1),
            new BalutScore("2025-03-06", 290, 3, 0),
            new BalutScore("2025-03-07", 600, 25, 4),
            new BalutScore("2025-03-08", 500, 20, 2),
            new BalutScore("2025-03-09", 280, 2, 0),
            new BalutScore("2025-03-10", 470, 18, 2),
            new BalutScore("2025-03-11", 350, 8, 1)
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
        int[] expectedPoints = {25, 23, 20, 18, 14};
        int[] expectedScores = {600, 550, 500, 470, 420};
        dao.findTop(5).observeForever(topScores -> {
            assertEquals(5, topScores.size());
            for (int i = 0; i < topScores.size(); i++) {
                assertEquals(expectedPoints[i], topScores.get(i).points);
                assertEquals(expectedScores[i], topScores.get(i).score);
            }
        });

        dao.findTop(100).observeForever(topScores -> assertEquals(11, topScores.size()));
    }

    @Test
    public void testRank() {
        assertEquals(1, dao.rank(27, 630));
        assertEquals(1, dao.rank(26, 590));
        assertEquals(1, dao.rank(25, 600));
        assertEquals(2, dao.rank(25, 590));
        assertEquals(11, dao.rank(2, 285));
        assertEquals(12, dao.rank(1, 270));
    }

    @Test
    public void testStatistics() {
        dao.statistics().observeForever(stats -> {
            assertEquals(11, stats.count);
            assertEquals(600, stats.maxScore);
            assertEquals(280, stats.minScore);
            assertEquals(415.454545, stats.avgScore, 1e-6);
            assertEquals(25, stats.maxPoints);
            assertEquals(2, stats.minPoints);
            assertEquals(13.0, stats.avgPoints, 1e-6);
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
            assertEquals(0, stats.maxPoints);
            assertEquals(0, stats.minPoints);
            assertEquals(0.0, stats.avgPoints, 1e-6);
            assertEquals(0, stats.numBalut);
        });
    }

    @Test
    public void testStatisticsObserver() {
        dao.insert(new BalutScore("2025-03-12", 450, 15, 2));
        dao.statistics().observeForever(stats -> {
            assertEquals(12, stats.count);
            assertEquals(600, stats.maxScore);
            assertEquals(280, stats.minScore);
            assertEquals(418.333333, stats.avgScore, 1e-6);
            assertEquals(25, stats.maxPoints);
            assertEquals(2, stats.minPoints);
            assertEquals(13.166667, stats.avgPoints, 1e-6);
            assertEquals(16, stats.numBalut);
        });
    }

    @Test
    public void testInsert() {
        var score = new BalutScore("2025-03-12", 460, 15, 2);
        dao.insert(score);
        assertEquals(12, dao.count());
        var actual = dao.findById(12);
        assertNotNull(actual);
        assertEquals(460, actual.score);
        assertEquals(15, actual.points);
    }

    @Test
    public void testInsertAlreadyExist() {
        var score = new BalutScore("2025-03-03", 385, 11, 1);
        score.id = 3;
        dao.insert(score);
        assertEquals(11, dao.count());
        var actual = dao.findById(3);
        assertEquals(380, actual.score);
        assertEquals(10, actual.points);
    }

    @Test
    public void testDelete() {
        int[] idsToDelete = {0, 3, 7, 10, 999};
        List<BalutScore> scores = new ArrayList<>();
        for (int id : idsToDelete) {
            var s = new BalutScore("", 0, 0, 0);
            s.id = id;
            scores.add(s);
        }
        dao.deleteAll(scores);
        assertEquals(8, dao.count());
        for (int id : idsToDelete)
            assertNull(dao.findById(id));
    }
}
