package com.zzy.dicegames.utils.score;

import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.data.entity.crag.CragScore;
import com.zzy.dicegames.data.entity.farkle.FarkleScore;
import com.zzy.dicegames.data.entity.liarsdice.LiarsDiceScore;
import com.zzy.dicegames.data.entity.pig.PigScore;
import com.zzy.dicegames.data.entity.yahtzee.YahtzeeScore;
import com.zzy.dicegames.data.entity.yatzy.MaxiYatzyScore;
import com.zzy.dicegames.data.entity.yatzy.YatzyScore;

import java.util.List;

class TestData {
    public static ScoresDTO scoresDTO;
    public static String xmlString;

    public static ScoresDTO emptyScoresDTO;
    public static String emptyXmlString;

    static {
        var yahtzeeScores = List.of(
                new YahtzeeScore("2026-01-01", 370, true, true),
                new YahtzeeScore("2026-01-02", 270, false, false)
        );
        var yatzyScores = List.of(new YatzyScore("2026-01-15", 300, true, true));
        var maxiYatzyScores = List.of(new MaxiYatzyScore("2026-02-01", 470, false, true));
        var balutScores = List.of(new BalutScore("2026-03-01", 400, 10, 1));
        var liarsDiceScores = List.of(new LiarsDiceScore("2026-05-01", 2, 5, 5));
        var farkleScores = List.of(new FarkleScore("2026-04-01", 10000, 9000));
        var pigScores = List.of(new PigScore("2026-06-01", 100, 85));
        var cragScores = List.of(new CragScore("2026-07-01", 180, true));
        scoresDTO = new ScoresDTO(yahtzeeScores, yatzyScores, maxiYatzyScores, balutScores,
                liarsDiceScores, farkleScores, pigScores, cragScores);

        xmlString = """
                <?xml version='1.0' encoding='utf-8' standalone='yes' ?>
                <scores><YahtzeeScores>
                <YahtzeeScore date="2026-01-01" score="370" has_bonus="true" has_yahtzee="true" />
                <YahtzeeScore date="2026-01-02" score="270" has_bonus="false" has_yahtzee="false" />
                </YahtzeeScores><YatzyScores>
                <YatzyScore date="2026-01-15" score="300" has_bonus="true" has_yatzy="true" />
                </YatzyScores><MaxiYatzyScores>
                <MaxiYatzyScore date="2026-02-01" score="470" has_bonus="false" has_yatzy="true" />
                </MaxiYatzyScores><BalutScores>
                <BalutScore date="2026-03-01" score="400" points="10" num_balut="1" />
                </BalutScores><LiarsDiceScores>
                <LiarsDiceScore date="2026-05-01" score="0" num_players="2" wins="5" losses="5" />
                </LiarsDiceScores><FarkleScores>
                <FarkleScore date="2026-04-01" score="10000" computer_score="9000" />
                </FarkleScores><PigScores>
                <PigScore date="2026-06-01" score="100" computer_score="85" />
                </PigScores><CragScores>
                <CragScore date="2026-07-01" score="180" has_crag="true" />
                </CragScores></scores>
                """.replace("\n", "");

        emptyScoresDTO = new ScoresDTO();

        emptyXmlString = """
                <?xml version='1.0' encoding='utf-8' standalone='yes' ?><scores>
                <YahtzeeScores /><YatzyScores /><MaxiYatzyScores /><BalutScores />
                <LiarsDiceScores /><FarkleScores /><PigScores /><CragScores /></scores>
                """.replace("\n", "");
    }
}
