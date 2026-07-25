package com.zzy.dicegames.utils.score;

import com.zzy.dicegames.data.entity.BalutScore;
import com.zzy.dicegames.data.entity.FarkleScore;
import com.zzy.dicegames.data.entity.FiveYahtzeeScore;
import com.zzy.dicegames.data.entity.SixYahtzeeScore;

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
                <?xml version='1.0' encoding='utf-8' standalone='yes' ?><scores><FiveYahtzeeScores>
                <FiveYahtzeeScore date="2026-01-01" score="370" has_bonus="true" has_yahtzee="true" />
                <FiveYahtzeeScore date="2026-01-02" score="270" has_bonus="false" has_yahtzee="false" />
                </FiveYahtzeeScores><SixYahtzeeScores>
                <SixYahtzeeScore date="2026-02-01" score="470" has_bonus="true" has_yahtzee="false" />
                </SixYahtzeeScores><BalutScores>
                <BalutScore date="2026-03-01" score="400" num_balut="1" />
                </BalutScores><FarkleScores>
                <FarkleScore date="2026-04-01" score="10000" computer_score="9000" />
                </FarkleScores></scores>
                """.replace("\n", "");

        var fiveYahtzeeScores = List.of(
                new FiveYahtzeeScore("2026-01-01", 370, true, true),
                new FiveYahtzeeScore("2026-01-02", 270, false, false)
        );
        var sixYahtzeeScores = List.of(new SixYahtzeeScore("2026-02-01", 470, true, false));
        var balutScores = List.of(new BalutScore("2026-03-01", 400, 1));
        var farkleScores = List.of(new FarkleScore("2026-04-01", 10000, 9000));
        var expected = new ScoresDTO(fiveYahtzeeScores, sixYahtzeeScores, balutScores, farkleScores);

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
        assertEquals(a.fiveYahtzeeScores.size(), b.fiveYahtzeeScores.size());
        for (int i = 0; i < a.fiveYahtzeeScores.size(); i++)
            assertTrue(ScoreUtil.isEqual(a.fiveYahtzeeScores.get(i), b.fiveYahtzeeScores.get(i)));

        assertEquals(a.sixYahtzeeScores.size(), b.sixYahtzeeScores.size());
        for (int i = 0; i < a.sixYahtzeeScores.size(); i++)
            assertTrue(ScoreUtil.isEqual(a.sixYahtzeeScores.get(i), b.sixYahtzeeScores.get(i)));

        assertEquals(a.balutScores.size(), b.balutScores.size());
        for (int i = 0; i < a.balutScores.size(); i++)
            assertTrue(ScoreUtil.isEqual(a.balutScores.get(i), b.balutScores.get(i)));

        assertEquals(a.farkleScores.size(), b.farkleScores.size());
        for (int i = 0; i < a.farkleScores.size(); i++)
            assertTrue(ScoreUtil.isEqual(a.farkleScores.get(i), b.farkleScores.get(i)));
    }
}
