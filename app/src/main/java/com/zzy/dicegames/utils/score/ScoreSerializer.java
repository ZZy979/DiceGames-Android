package com.zzy.dicegames.utils.score;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 得分数据序列化器
 *
 * @author 赵正阳
 */
public class ScoreSerializer {
    private final OutputStream outputStream;

    private XmlSerializer serializer;

    private ScoresDTO scoresDTO;

    public ScoreSerializer(OutputStream outputStream, ScoresDTO scoresDTO) {
        this.outputStream = outputStream;
        this.scoresDTO = scoresDTO;
    }

    public void serialize() throws IOException {
        serializer = Xml.newSerializer();
        serializer.setOutput(outputStream, "utf-8");
        serializer.startDocument("utf-8", true);
        serializer.startTag(null, "scores");

        serializeYahtzeeScores();
        serializeMaxiYatzyScores();
        serializeBalutScores();
        serializeLiarsDiceScores();
        serializeFarkleScores();

        serializer.endTag(null, "scores");
        serializer.endDocument();
    }

    private void serializeYahtzeeScores() throws IOException {
        serializer.startTag(null, "YahtzeeScores");
        for (var score : scoresDTO.yahtzeeScores) {
            serializer.startTag(null, "YahtzeeScore")
                    .attribute(null, "date", score.date)
                    .attribute(null, "score", Integer.toString(score.score))
                    .attribute(null, "has_bonus", Boolean.toString(score.hasBonus))
                    .attribute(null, "has_yahtzee", Boolean.toString(score.hasYahtzee))
                    .endTag(null, "YahtzeeScore");
        }
        serializer.endTag(null, "YahtzeeScores");
    }

    private void serializeMaxiYatzyScores() throws IOException {
        serializer.startTag(null, "MaxiYatzyScores");
        for (var score : scoresDTO.maxiYatzyScores)
            serializer.startTag(null, "MaxiYatzyScore")
                    .attribute(null, "date", score.date)
                    .attribute(null, "score", Integer.toString(score.score))
                    .attribute(null, "has_bonus", Boolean.toString(score.hasBonus))
                    .attribute(null, "has_yatzy", Boolean.toString(score.hasYatzy))
                    .endTag(null, "MaxiYatzyScore");
        serializer.endTag(null, "MaxiYatzyScores");
    }

    private void serializeBalutScores() throws IOException {
        serializer.startTag(null, "BalutScores");
        for (var score : scoresDTO.balutScores)
            serializer.startTag(null, "BalutScore")
                    .attribute(null, "date", score.date)
                    .attribute(null, "score", Integer.toString(score.score))
                    .attribute(null, "points", Integer.toString(score.points))
                    .attribute(null, "num_balut", Integer.toString(score.numBalut))
                    .endTag(null, "BalutScore");
        serializer.endTag(null, "BalutScores");
    }

    private void serializeLiarsDiceScores() throws IOException {
        serializer.startTag(null, "LiarsDiceScores");
        for (var score : scoresDTO.liarsDiceScores)
            serializer.startTag(null, "LiarsDiceScore")
                    .attribute(null, "date", score.date)
                    .attribute(null, "score", Integer.toString(score.score))
                    .attribute(null, "num_players", Integer.toString(score.numPlayers))
                    .attribute(null, "wins", Integer.toString(score.wins))
                    .attribute(null, "losses", Integer.toString(score.losses))
                    .endTag(null, "LiarsDiceScore");
        serializer.endTag(null, "LiarsDiceScores");
    }

    private void serializeFarkleScores() throws IOException {
        serializer.startTag(null, "FarkleScores");
        for (var score : scoresDTO.farkleScores)
            serializer.startTag(null, "FarkleScore")
                    .attribute(null, "date", score.date)
                    .attribute(null, "score", Integer.toString(score.score))
                    .attribute(null, "computer_score", Integer.toString(score.computerScore))
                    .endTag(null, "FarkleScore");
        serializer.endTag(null, "FarkleScores");
    }
}
