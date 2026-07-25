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

        serializeFiveYahtzeeScores();
        serializeSixYahtzeeScores();
        serializeBalutScores();
        serializeFarkleScores();

        serializer.endTag(null, "scores");
        serializer.endDocument();
    }

    private void serializeFiveYahtzeeScores() throws IOException {
        serializer.startTag(null, "FiveYahtzeeScores");
        for (var score : scoresDTO.fiveYahtzeeScores) {
            serializer.startTag(null, "FiveYahtzeeScore")
                    .attribute(null, "date", score.date)
                    .attribute(null, "score", Integer.toString(score.score))
                    .attribute(null, "has_bonus", Boolean.toString(score.hasBonus))
                    .attribute(null, "has_yahtzee", Boolean.toString(score.hasYahtzee))
                    .endTag(null, "FiveYahtzeeScore");
        }
        serializer.endTag(null, "FiveYahtzeeScores");
    }

    private void serializeSixYahtzeeScores() throws IOException {
        serializer.startTag(null, "SixYahtzeeScores");
        for (var score : scoresDTO.sixYahtzeeScores)
            serializer.startTag(null, "SixYahtzeeScore")
                    .attribute(null, "date", score.date)
                    .attribute(null, "score", Integer.toString(score.score))
                    .attribute(null, "has_bonus", Boolean.toString(score.hasBonus))
                    .attribute(null, "has_yahtzee", Boolean.toString(score.hasYahtzee))
                    .endTag(null, "SixYahtzeeScore");
        serializer.endTag(null, "SixYahtzeeScores");
    }

    private void serializeBalutScores() throws IOException {
        serializer.startTag(null, "BalutScores");
        for (var score : scoresDTO.balutScores)
            serializer.startTag(null, "BalutScore")
                    .attribute(null, "date", score.date)
                    .attribute(null, "score", Integer.toString(score.score))
                    .attribute(null, "num_balut", Integer.toString(score.numBalut))
                    .endTag(null, "BalutScore");
        serializer.endTag(null, "BalutScores");
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
