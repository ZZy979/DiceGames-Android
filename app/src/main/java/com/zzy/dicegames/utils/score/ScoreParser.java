package com.zzy.dicegames.utils.score;

import android.util.Xml;

import com.zzy.dicegames.data.entity.BalutScore;
import com.zzy.dicegames.data.entity.FarkleScore;
import com.zzy.dicegames.data.entity.FiveYahtzeeScore;
import com.zzy.dicegames.data.entity.SixYahtzeeScore;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * 得分数据解析器
 *
 * @author 赵正阳
 */
public class ScoreParser {
    private final InputStream inputStream;

    private XmlPullParser parser;

    private ScoresDTO scoresDTO = new ScoresDTO();

    public ScoreParser(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public ScoresDTO parse() throws IOException, XmlPullParserException {
        parser = Xml.newPullParser();
        parser.setInput(inputStream, "utf-8");

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                switch (parser.getName()) {
                    case "FiveYahtzeeScore" -> parseFiveYahtzeeScores();
                    case "SixYahtzeeScore" -> parseSixYahtzeeScores();
                    case "BalutScore" -> parseBalutScores();
                    case "FarkleScore" -> parseFarkleScores();
                }
            }
            eventType = parser.next();
        }

        return scoresDTO;
    }

    private void parseFiveYahtzeeScores() {
        String date = parser.getAttributeValue(null, "date");
        int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
        boolean hasBonus = Boolean.parseBoolean(parser.getAttributeValue(null, "has_bonus"));
        boolean hasYahtzee = Boolean.parseBoolean(parser.getAttributeValue(null, "has_yahtzee"));
        scoresDTO.fiveYahtzeeScores.add(new FiveYahtzeeScore(date, score, hasBonus, hasYahtzee));
    }

    private void parseSixYahtzeeScores() {
        String date = parser.getAttributeValue(null, "date");
        int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
        boolean hasBonus = Boolean.parseBoolean(parser.getAttributeValue(null, "has_bonus"));
        boolean hasYahtzee = Boolean.parseBoolean(parser.getAttributeValue(null, "has_yahtzee"));
        scoresDTO.sixYahtzeeScores.add(new SixYahtzeeScore(date, score, hasBonus, hasYahtzee));
    }

    private void parseBalutScores() {
        String date = parser.getAttributeValue(null, "date");
        int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
        int numBalut = Integer.parseInt(parser.getAttributeValue(null, "num_balut"));
        scoresDTO.balutScores.add(new BalutScore(date, score, numBalut));
    }

    private void parseFarkleScores() {
        String date = parser.getAttributeValue(null, "date");
        int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
        int computerScore = Integer.parseInt(parser.getAttributeValue(null, "computer_score"));
        scoresDTO.farkleScores.add(new FarkleScore(date, score, computerScore));
    }
}
