package com.zzy.dicegames.utils.score;

import com.zzy.dicegames.data.entity.BalutScore;
import com.zzy.dicegames.data.entity.FarkleScore;
import com.zzy.dicegames.data.entity.FiveYahtzeeScore;
import com.zzy.dicegames.data.entity.SixYahtzeeScore;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class ScoreSerializerTest {
    @Test
    public void testSerialize() throws IOException {
        var fiveYahtzeeScores = List.of(
                new FiveYahtzeeScore("2026-01-01", 370, true, true),
                new FiveYahtzeeScore("2026-01-02", 270, false, false)
        );
        var sixYahtzeeScores = List.of(new SixYahtzeeScore("2026-02-01", 470, true, false));
        var balutScores = List.of(new BalutScore("2026-03-01", 400, 1));
        var farkleScores = List.of(new FarkleScore("2026-04-01", 10000, 9000));
        var scoresDTO = new ScoresDTO(fiveYahtzeeScores, sixYahtzeeScores, balutScores, farkleScores);

        String expected = """
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

        try (var outputStream = new ByteArrayOutputStream()) {
            var serializer = new ScoreSerializer(outputStream, scoresDTO);
            serializer.serialize();
            assertEquals(expected, outputStream.toString(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testSerializeEmptyData() throws IOException {
        var scoresDTO = new ScoresDTO();
        String expected = """
                <?xml version='1.0' encoding='utf-8' standalone='yes' ?><scores>
                <FiveYahtzeeScores /><SixYahtzeeScores /><BalutScores /><FarkleScores /></scores>
                """.replace("\n", "");

        try (var outputStream = new ByteArrayOutputStream()) {
            var serializer = new ScoreSerializer(outputStream, scoresDTO);
            serializer.serialize();
            assertEquals(expected, outputStream.toString(StandardCharsets.UTF_8));
        }
    }
}
