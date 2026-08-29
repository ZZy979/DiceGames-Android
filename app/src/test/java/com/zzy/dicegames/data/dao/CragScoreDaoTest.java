package com.zzy.dicegames.data.dao;

import android.content.Context;

import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.crag.CragScore;
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
public class CragScoreDaoTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ScoreDatabase database;

    private CragScoreDao dao;

    private List<CragScore> testScores = List.of(
            new CragScore("2025-02-01", 100, false),
            new CragScore("2025-02-02", 120, false),
            new CragScore("2025-02-03", 140, false),
            new CragScore("2025-02-04", 160, false),
            new CragScore("2025-02-05", 180, true),
            new CragScore("2025-02-06", 200, true),
            new CragScore("2025-02-07", 220, false),
            new CragScore("2025-02-08", 240, true),
            new CragScore("2025-02-09", 260, true),
            new CragScore("2025-02-10", 280, false),
            new CragScore("2025-02-11", 300, true),
            new CragScore("2025-02-12", 320, true),
            new CragScore("2025-02-13", 340, true)
    );

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        database = Room.inMemoryDatabaseBuilder(context, ScoreDatabase.class)
                .allowMainThreadQueries().build();
        dao = database.cragScoreDao();
        dao.insertAll(testScores);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void testFindAll() {
        List<CragScore> scores = dao.findAll();
        assertEquals(testScores.size(), scores.size());
        for (int i = 0; i < scores.size(); i++) {
            assertEquals(i + 1, scores.get(i).id);
            assertTrue(ScoreUtil.isEqual(testScores.get(i), scores.get(i)));
        }
    }

    @Test
    public void testFindById() {
        var score = dao.findById(12);
        assertNotNull(score);
        assertEquals("2025-02-12", score.date);
        assertEquals(320, score.score);
        assertTrue(score.hasCrag);

        assertNull(dao.findById(999));
    }

    @Test
    public void testFindTop() {
        int[] expected = {340, 320, 300, 280, 260};
        dao.findTop(5).observeForever(topScores -> {
            assertEquals(5, topScores.size());
            for (int i = 0; i < expected.length; i++)
                assertEquals(expected[i], topScores.get(i).score);
        });

        dao.findTop(100).observeForever(topScores -> assertEquals(13, topScores.size()));
    }

    @Test
    public void testRank() {
        assertEquals(1, dao.rank(350));
        assertEquals(1, dao.rank(340));
        assertEquals(5, dao.rank(260));
        assertEquals(10, dao.rank(160));
        assertEquals(13, dao.rank(100));
        assertEquals(14, dao.rank(90));
    }

    @Test
    public void testStatistics() {
        dao.statistics().observeForever(stats -> {
            assertEquals(13, stats.count);
            assertEquals(340, stats.maxScore);
            assertEquals(100, stats.minScore);
            assertEquals(220.0, stats.avgScore, 1e-6);
            assertEquals(7, stats.numCrag);
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
            assertEquals(0, stats.numCrag);
        });
    }

    @Test
    public void testStatisticsObserver() {
        dao.insert(new CragScore("2025-02-14", 220, true));
        dao.statistics().observeForever(stats -> {
            assertEquals(14, stats.count);
            assertEquals(340, stats.maxScore);
            assertEquals(100, stats.minScore);
            assertEquals(220.0, stats.avgScore, 1e-6);
            assertEquals(8, stats.numCrag);
        });
    }

    @Test
    public void testInsert() {
        var score = new CragScore("2025-02-14", 425, true);
        dao.insert(score);
        assertEquals(14, dao.count());
        var actual = dao.findById(14);
        assertNotNull(actual);
        assertEquals(425, actual.score);
    }

    @Test
    public void testInsertAlreadyExist() {
        var score = new CragScore("2025-02-10", 565, true);
        score.id = 10;
        dao.insert(score);
        assertEquals(13, dao.count());
        assertEquals(280, dao.findById(10).score);
    }

    @Test
    public void testDelete() {
        int[] idsToDelete = {-1, 4, 9, 12, 999};
        List<CragScore> scores = new ArrayList<>();
        for (int id : idsToDelete) {
            var s = new CragScore("", 0, false);
            s.id = id;
            scores.add(s);
        }
        dao.deleteAll(scores);
        assertEquals(10, dao.count());
        for (int id : idsToDelete)
            assertNull(dao.findById(id));
    }
}
