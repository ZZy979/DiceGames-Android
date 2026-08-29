package com.zzy.dicegames.utils.score;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static com.zzy.dicegames.utils.score.TestData.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class ScoreParserTest {
    @Test
    public void testParseSuccess() throws Exception {
        try (var inputStream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))) {
            var parser = new ScoreParser(inputStream);
            ScoresDTO actual = parser.parse();
            compareScoresDTO(scoresDTO, actual);
        }
    }

    @Test
    public void testParseFailed() throws Exception {
        String input = "invalid XML";
        try (var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
            var parser = new ScoreParser(inputStream);
            assertThrows(XmlPullParserException.class, parser::parse);
        }
    }

    @Test
    public void testParseEmptyData() throws Exception {
        try (var inputStream = new ByteArrayInputStream(emptyXmlString.getBytes(StandardCharsets.UTF_8))) {
            var parser = new ScoreParser(inputStream);
            ScoresDTO actual = parser.parse();
            compareScoresDTO(emptyScoresDTO, actual);
        }

        try (var inputStream = new ByteArrayInputStream(new byte[0])) {
            var parser = new ScoreParser(inputStream);
            ScoresDTO actual = parser.parse();
            compareScoresDTO(emptyScoresDTO, actual);
        }
    }

    private void compareScoresDTO(ScoresDTO a, ScoresDTO b) {
        assertEquals(a.yahtzeeScores.size(), b.yahtzeeScores.size());
        for (int i = 0; i < a.yahtzeeScores.size(); i++)
            assertTrue(ScoreUtil.isEqual(a.yahtzeeScores.get(i), b.yahtzeeScores.get(i)));

        assertEquals(a.yatzyScores.size(), b.yatzyScores.size());
        for (int i = 0; i < a.yatzyScores.size(); i++)
            assertTrue(ScoreUtil.isEqual(a.yatzyScores.get(i), b.yatzyScores.get(i)));

        assertEquals(a.maxiYatzyScores.size(), b.maxiYatzyScores.size());
        for (int i = 0; i < a.maxiYatzyScores.size(); i++)
            assertTrue(ScoreUtil.isEqual(a.maxiYatzyScores.get(i), b.maxiYatzyScores.get(i)));

        assertEquals(a.balutScores.size(), b.balutScores.size());
        for (int i = 0; i < a.balutScores.size(); i++)
            assertTrue(ScoreUtil.isEqual(a.balutScores.get(i), b.balutScores.get(i)));

        assertEquals(a.liarsDiceScores.size(), b.liarsDiceScores.size());
        for (int i = 0; i < a.liarsDiceScores.size(); i++)
            assertTrue(ScoreUtil.isEqual(a.liarsDiceScores.get(i), b.liarsDiceScores.get(i)));

        assertEquals(a.farkleScores.size(), b.farkleScores.size());
        for (int i = 0; i < a.farkleScores.size(); i++)
            assertTrue(ScoreUtil.isEqual(a.farkleScores.get(i), b.farkleScores.get(i)));

        assertEquals(a.pigScores.size(), b.pigScores.size());
        for (int i = 0; i < a.pigScores.size(); i++)
            assertTrue(ScoreUtil.isEqual(a.pigScores.get(i), b.pigScores.get(i)));

        assertEquals(a.cragScores.size(), b.cragScores.size());
        for (int i = 0; i < a.cragScores.size(); i++)
            assertTrue(ScoreUtil.isEqual(a.cragScores.get(i), b.cragScores.get(i)));
    }
}
