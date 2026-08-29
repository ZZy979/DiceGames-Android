package com.zzy.dicegames.data.dao;

import android.content.Context;

import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.yatzy.YatzyScore;
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
public class YatzyScoreDaoTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ScoreDatabase database;

    private YatzyScoreDao dao;

    private List<YatzyScore> testScores = List.of(
            new YatzyScore("2025-03-01", 260, true, true),
            new YatzyScore("2025-03-02", 320, true, true),
            new YatzyScore("2025-03-03", 230, false, false),
            new YatzyScore("2025-03-04", 300, true, true),
            new YatzyScore("2025-03-05", 250, true, false),
            new YatzyScore("2025-03-06", 140, false, false),
            new YatzyScore("2025-03-07", 210, false, false),
            new YatzyScore("2025-03-08", 220, false, true),
            new YatzyScore("2025-03-09", 200, false, false),
            new YatzyScore("2025-03-10", 280, true, true),
            new YatzyScore("2025-03-11", 240, true, false),
            new YatzyScore("2025-03-12", 230, false, false)
    );

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        database = Room.inMemoryDatabaseBuilder(context, ScoreDatabase.class)
                .allowMainThreadQueries().build();
        dao = database.yatzyScoreDao();
        dao.insertAll(testScores);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void testFindAll() {
        List<YatzyScore> scores = dao.findAll();
        assertEquals(testScores.size(), scores.size());
        for (int i = 0; i < scores.size(); i++) {
            assertEquals(i + 1, scores.get(i).id);
            assertTrue(ScoreUtil.isEqual(testScores.get(i), scores.get(i)));
        }
    }

    @Test
    public void testFindById() {
        var score = dao.findById(5);
        assertNotNull(score);
        assertEquals("2025-03-05", score.date);
        assertEquals(250, score.score);
        assertTrue(score.hasBonus);
        assertFalse(score.hasYatzy);

        assertNull(dao.findById(999));
    }

    @Test
    public void testFindTop() {
        int[] expected = {320, 300, 280, 260, 250};
        dao.findTop(5).observeForever(topScores -> {
            assertEquals(5, topScores.size());
            for (int i = 0; i < expected.length; i++)
                assertEquals(expected[i], topScores.get(i).score);
        });

        dao.findTop(100).observeForever(topScores -> assertEquals(12, topScores.size()));
    }

    @Test
    public void testRank() {
        assertEquals(1, dao.rank(330));
        assertEquals(1, dao.rank(320));
        assertEquals(5, dao.rank(250));
        assertEquals(7, dao.rank(230));
        assertEquals(12, dao.rank(140));
        assertEquals(13, dao.rank(135));
    }

    @Test
    public void testStatistics() {
        dao.statistics().observeForever(stats -> {
            assertEquals(12, stats.count);
            assertEquals(320, stats.maxScore);
            assertEquals(140, stats.minScore);
            assertEquals(240.0, stats.avgScore, 1e-6);
            assertEquals(6, stats.numBonus);
            assertEquals(5, stats.numYatzy);
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
            assertEquals(0, stats.numBonus);
            assertEquals(0, stats.numYatzy);
        });
    }

    @Test
    public void testStatisticsObserver() {
        dao.insert(new YatzyScore("2025-03-13", 240, true, false));
        dao.statistics().observeForever(stats -> {
            assertEquals(13, stats.count);
            assertEquals(320, stats.maxScore);
            assertEquals(140, stats.minScore);
            assertEquals(240.0, stats.avgScore, 1e-6);
            assertEquals(7, stats.numBonus);
            assertEquals(5, stats.numYatzy);
        });
    }

    @Test
    public void testInsert() {
        var score = new YatzyScore("2025-03-13", 275, true, false);
        dao.insert(score);
        assertEquals(13, dao.count());
        var actual = dao.findById(13);
        assertNotNull(actual);
        assertEquals(275, actual.score);
    }

    @Test
    public void testInsertAlreadyExist() {
        var score = new YatzyScore("2025-03-04", 310, true, true);
        score.id = 4;
        dao.insert(score);
        assertEquals(12, dao.count());
        assertEquals(300, dao.findById(4).score);
    }

    @Test
    public void testDelete() {
        int[] idsToDelete = {-1, 4, 9, 12, 999};
        List<YatzyScore> scores = new ArrayList<>();
        for (int id : idsToDelete) {
            var s = new YatzyScore("", 0, false, false);
            s.id = id;
            scores.add(s);
        }
        dao.deleteAll(scores);
        assertEquals(9, dao.count());
        for (int id : idsToDelete)
            assertNull(dao.findById(id));
    }
}
