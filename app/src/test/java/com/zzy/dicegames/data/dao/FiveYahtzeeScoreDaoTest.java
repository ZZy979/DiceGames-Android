package com.zzy.dicegames.data.dao;

import android.content.Context;

import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.FiveYahtzeeScore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import androidx.room.Room;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class FiveYahtzeeScoreDaoTest {
    private ScoreDatabase database;

    private FiveYahtzeeScoreDao dao;

    private List<FiveYahtzeeScore> testScores = List.of(
            new FiveYahtzeeScore("2025-01-01", 300, true, true),
            new FiveYahtzeeScore("2025-01-02", 370, true, true),
            new FiveYahtzeeScore("2025-01-03", 270, false, false),
            new FiveYahtzeeScore("2025-01-04", 350, true, true),
            new FiveYahtzeeScore("2025-01-05", 290, true, false),
            new FiveYahtzeeScore("2025-01-06", 160, false, false),
            new FiveYahtzeeScore("2025-01-07", 240, false, false),
            new FiveYahtzeeScore("2025-01-08", 260, false, true),
            new FiveYahtzeeScore("2025-01-09", 230, false, false),
            new FiveYahtzeeScore("2025-01-10", 320, true, true),
            new FiveYahtzeeScore("2025-01-11", 280, true, false),
            new FiveYahtzeeScore("2025-01-12", 200, false, false)
    );

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        database = Room.inMemoryDatabaseBuilder(context, ScoreDatabase.class)
                .allowMainThreadQueries().build();
        dao = database.fiveYahtzeeScoreDao();
        dao.insertAll(testScores);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void testFindAll() {
        List<FiveYahtzeeScore> scores = dao.findAll();
        assertEquals(testScores.size(), scores.size());
        for (int i = 0; i < scores.size(); i++) {
            assertEquals(i + 1, scores.get(i).id);
            assertEquals(testScores.get(i).date, scores.get(i).date);
            assertEquals(testScores.get(i).score, scores.get(i).score);
            assertEquals(testScores.get(i).hasBonus, scores.get(i).hasBonus);
            assertEquals(testScores.get(i).hasYahtzee, scores.get(i).hasYahtzee);
        }
    }

    @Test
    public void testFindById() {
        var score = dao.findById(8);
        assertNotNull(score);
        assertEquals("2025-01-08", score.date);
        assertEquals(260, score.score);
        assertFalse(score.hasBonus);
        assertTrue(score.hasYahtzee);

        assertNull(dao.findById(999));
    }

    @Test
    public void testFindTop() {
        var topScores = dao.findTop(5);
        int[] expected = {370, 350, 320, 300, 290};
        assertEquals(5, topScores.size());
        for (int i = 0; i < expected.length; i++)
            assertEquals(expected[i], topScores.get(i).score);

        topScores = dao.findTop(100);
        assertEquals(12, topScores.size());
    }

    @Test
    public void testRank() {
        assertEquals(1, dao.rank(375));
        assertEquals(1, dao.rank(370));
        assertEquals(4, dao.rank(300));
        assertEquals(7, dao.rank(275));
        assertEquals(12, dao.rank(160));
        assertEquals(13, dao.rank(155));
    }

    @Test
    public void testStatistics() {
        var stats = dao.statistics();
        assertEquals(12, stats.count);
        assertEquals(370, stats.maxScore);
        assertEquals(160, stats.minScore);
        assertEquals(272.5, stats.avgScore, 1e-6);
        assertEquals(6, stats.numBonus);
        assertEquals(5, stats.numYahtzee);
    }

    @Test
    public void testStatisticsOnEmptyDatabase() {
        for (int i = 0; i < testScores.size(); i++)
            testScores.get(i).id = i + 1;
        dao.deleteAll(testScores);

        var stats = dao.statistics();
        assertEquals(0, stats.count);
        assertEquals(0, stats.maxScore);
        assertEquals(0, stats.minScore);
        assertEquals(0.0, stats.avgScore, 1e-6);
        assertEquals(0, stats.numBonus);
        assertEquals(0, stats.numYahtzee);
    }

    @Test
    public void testInsert() {
        var score = new FiveYahtzeeScore("2025-01-13", 255, true, false);
        dao.insert(score);
        assertEquals(13, dao.statistics().count);
        var actual = dao.findById(13);
        assertNotNull(actual);
        assertEquals(255, actual.score);
    }

    @Test
    public void testInsertAlreadyExist() {
        var score = new FiveYahtzeeScore("2025-01-07", 245, false, false);
        score.id = 7;
        dao.insert(score);
        assertEquals(12, dao.statistics().count);
        assertEquals(240, dao.findById(7).score);
    }

    @Test
    public void testDelete() {
        int[] idsToDelete = {2, 5, 10, 999};
        List<FiveYahtzeeScore> scores = new ArrayList<>();
        for (int id : idsToDelete) {
            var s = new FiveYahtzeeScore("", 0, false, false);
            s.id = id;
            scores.add(s);
        }
        dao.deleteAll(scores);
        assertEquals(9, dao.statistics().count);
        for (int id : idsToDelete)
            assertNull(dao.findById(id));
    }
}
