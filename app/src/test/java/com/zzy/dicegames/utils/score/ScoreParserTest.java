package com.zzy.dicegames.utils.score;

import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.data.entity.farkle.FarkleScore;
import com.zzy.dicegames.data.entity.liarsdice.LiarsDiceScore;
import com.zzy.dicegames.data.entity.yahtzee.YahtzeeScore;
import com.zzy.dicegames.data.entity.yatzy.MaxiYatzyScore;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class ScoreParserTest {
    @Test
    public void testParseSuccess() throws Exception {
        String input = """
                <?xml version='1.0' encoding='utf-8' standalone='yes' ?><scores><YahtzeeScores>
                <YahtzeeScore date="2026-01-01" score="370" has_bonus="true" has_yahtzee="true" />
                <YahtzeeScore date="2026-01-02" score="270" has_bonus="false" has_yahtzee="false" />
                </YahtzeeScores><MaxiYatzyScores>
                <MaxiYatzyScore date="2026-02-01" score="470" has_bonus="true" has_yahtzee="false" />
                </MaxiYatzyScores><BalutScores>
                <BalutScore date="2026-03-01" score="400" points="10" num_balut="1" />
                </BalutScores><LiarsDiceScores>
                <LiarsDiceScore date="2026-05-01" score="0" num_players="2" wins="5" losses="5" />
                </LiarsDiceScores><FarkleScores>
                <FarkleScore date="2026-04-01" score="10000" computer_score="9000" />
                </FarkleScores></scores>
                """.replace("\n", "");

        var yahtzeeScores = List.of(
                new YahtzeeScore("2026-01-01", 370, true, true),
                new YahtzeeScore("2026-01-02", 270, false, false)
        );
        var maxiYatzyScores = List.of(new MaxiYatzyScore("2026-02-01", 470, true, false));
        var balutScores = List.of(new BalutScore("2026-03-01", 400, 10, 1));
        var farkleScores = List.of(new FarkleScore("2026-04-01", 10000, 9000));
        var liarsDiceScores = List.of(new LiarsDiceScore("2026-05-01", 2, 5, 5));
        var expected = new ScoresDTO(yahtzeeScores, maxiYatzyScores, balutScores, liarsDiceScores, farkleScores);

        try (var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
            var parser = new ScoreParser(inputStream);
            ScoresDTO actual = parser.parse();
            compareScoresDTO(expected, actual);
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
        var expected = new ScoresDTO();
        try (var inputStream = new ByteArrayInputStream(new byte[0])) {
            var parser = new ScoreParser(inputStream);
            ScoresDTO actual = parser.parse();
            compareScoresDTO(expected, actual);
        }
    }

    private void compareScoresDTO(ScoresDTO a, ScoresDTO b) {
        assertEquals(a.yahtzeeScores.size(), b.yahtzeeScores.size());
        for (int i = 0; i < a.yahtzeeScores.size(); i++)
            assertTrue(ScoreUtil.isEqual(a.yahtzeeScores.get(i), b.yahtzeeScores.get(i)));

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
    }
}
