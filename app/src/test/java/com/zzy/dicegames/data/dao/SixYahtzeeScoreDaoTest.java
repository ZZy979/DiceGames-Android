package com.zzy.dicegames.data.dao;

import android.content.Context;

import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.SixYahtzeeScore;

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
public class SixYahtzeeScoreDaoTest {
    private ScoreDatabase database;

    private SixYahtzeeScoreDao dao;

    private List<SixYahtzeeScore> testScores = List.of(
            new SixYahtzeeScore("2025-02-01", 340, false, false),
            new SixYahtzeeScore("2025-02-02", 470, true, false),
            new SixYahtzeeScore("2025-02-03", 530, true, true),
            new SixYahtzeeScore("2025-02-04", 230, false, false),
            new SixYahtzeeScore("2025-02-05", 400, true, false),
            new SixYahtzeeScore("2025-02-06", 190, false, false),
            new SixYahtzeeScore("2025-02-07", 380, true, false),
            new SixYahtzeeScore("2025-02-08", 310, false, false),
            new SixYahtzeeScore("2025-02-09", 440, false, true),
            new SixYahtzeeScore("2025-02-10", 560, true, true),
            new SixYahtzeeScore("2025-02-11", 270, false, false),
            new SixYahtzeeScore("2025-02-12", 450, true, false),
            new SixYahtzeeScore("2025-02-13", 500, true, true)
    );

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.getApplication();
        database = Room.inMemoryDatabaseBuilder(context, ScoreDatabase.class)
                .allowMainThreadQueries().build();
        dao = database.sixYahtzeeScoreDao();
        dao.insertAll(testScores);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void testFindAll() {
        List<SixYahtzeeScore> scores = dao.findAll();
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
        var score = dao.findById(12);
        assertNotNull(score);
        assertEquals("2025-02-12", score.date);
        assertEquals(450, score.score);
        assertTrue(score.hasBonus);
        assertFalse(score.hasYahtzee);

        assertNull(dao.findById(999));
    }

    @Test
    public void testFindTop() {
        var topScores = dao.findTop(5);
        int[] expected = {560, 530, 500, 470, 450};
        assertEquals(5, topScores.size());
        for (int i = 0; i < expected.length; i++)
            assertEquals(expected[i], topScores.get(i).score);

        topScores = dao.findTop(100);
        assertEquals(13, topScores.size());
    }

    @Test
    public void testRank() {
        assertEquals(1, dao.rank(570));
        assertEquals(1, dao.rank(560));
        assertEquals(6, dao.rank(440));
        assertEquals(11, dao.rank(290));
        assertEquals(13, dao.rank(190));
        assertEquals(14, dao.rank(185));
    }

    @Test
    public void testStatistics() {
        var stats = dao.statistics();
        assertEquals(13, stats.count);
        assertEquals(560, stats.maxScore);
        assertEquals(190, stats.minScore);
        assertEquals(390.0, stats.avgScore, 1e-6);
        assertEquals(7, stats.numBonus);
        assertEquals(4, stats.numYahtzee);
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
        var score = new SixYahtzeeScore("2025-02-14", 425, true, false);
        dao.insert(score);
        assertEquals(14, dao.statistics().count);
        var actual = dao.findById(14);
        assertNotNull(actual);
        assertEquals(425, actual.score);
    }

    @Test
    public void testInsertAlreadyExist() {
        var score = new SixYahtzeeScore("2025-02-10", 565, true, true);
        score.id = 10;
        dao.insert(score);
        assertEquals(13, dao.statistics().count);
        assertEquals(560, dao.findById(10).score);
    }

    @Test
    public void testDelete() {
        int[] idsToDelete = {-1, 4, 9, 12, 999};
        List<SixYahtzeeScore> scores = new ArrayList<>();
        for (int id : idsToDelete) {
            var s = new SixYahtzeeScore("", 0, false, false);
            s.id = id;
            scores.add(s);
        }
        dao.deleteAll(scores);
        assertEquals(10, dao.statistics().count);
        for (int id : idsToDelete)
            assertNull(dao.findById(id));
    }
}
