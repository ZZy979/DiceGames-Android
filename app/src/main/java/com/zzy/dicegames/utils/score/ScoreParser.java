package com.zzy.dicegames.utils.score;

import android.util.Xml;

import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.data.entity.farkle.FarkleScore;
import com.zzy.dicegames.data.entity.liarsdice.LiarsDiceScore;
import com.zzy.dicegames.data.entity.pig.PigScore;
import com.zzy.dicegames.data.entity.yahtzee.YahtzeeScore;
import com.zzy.dicegames.data.entity.yatzy.MaxiYatzyScore;

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
                    case "YahtzeeScore" -> parseYahtzeeScores();
                    case "MaxiYatzyScore" -> parseMaxiYatzyScores();
                    case "BalutScore" -> parseBalutScores();
                    case "FarkleScore" -> parseFarkleScores();
                    case "LiarsDiceScore" -> parseLiarsDiceScores();
                    case "PigScore" -> parsePigScores();
                }
            }
            eventType = parser.next();
        }

        return scoresDTO;
    }

    private void parseYahtzeeScores() {
        String date = parser.getAttributeValue(null, "date");
        int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
        boolean hasBonus = Boolean.parseBoolean(parser.getAttributeValue(null, "has_bonus"));
        boolean hasYahtzee = Boolean.parseBoolean(parser.getAttributeValue(null, "has_yahtzee"));
        scoresDTO.yahtzeeScores.add(new YahtzeeScore(date, score, hasBonus, hasYahtzee));
    }

    private void parseMaxiYatzyScores() {
        String date = parser.getAttributeValue(null, "date");
        int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
        boolean hasBonus = Boolean.parseBoolean(parser.getAttributeValue(null, "has_bonus"));
        boolean hasYatzy = Boolean.parseBoolean(parser.getAttributeValue(null, "has_yatzy"));
        scoresDTO.maxiYatzyScores.add(new MaxiYatzyScore(date, score, hasBonus, hasYatzy));
    }

    private void parseBalutScores() {
        String date = parser.getAttributeValue(null, "date");
        int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
        int points = Integer.parseInt(parser.getAttributeValue(null, "points"));
        int numBalut = Integer.parseInt(parser.getAttributeValue(null, "num_balut"));
        scoresDTO.balutScores.add(new BalutScore(date, score, points, numBalut));
    }

    private void parseLiarsDiceScores() {
        String date = parser.getAttributeValue(null, "date");
        int numPlayers = Integer.parseInt(parser.getAttributeValue(null, "num_players"));
        int wins = Integer.parseInt(parser.getAttributeValue(null, "wins"));
        int losses = Integer.parseInt(parser.getAttributeValue(null, "losses"));
        scoresDTO.liarsDiceScores.add(new LiarsDiceScore(date, numPlayers, wins, losses));
    }

    private void parseFarkleScores() {
        String date = parser.getAttributeValue(null, "date");
        int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
        int computerScore = Integer.parseInt(parser.getAttributeValue(null, "computer_score"));
        scoresDTO.farkleScores.add(new FarkleScore(date, score, computerScore));
    }

    private void parsePigScores() {
        String date = parser.getAttributeValue(null, "date");
        int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
        int computerScore = Integer.parseInt(parser.getAttributeValue(null, "computer_score"));
        scoresDTO.pigScores.add(new PigScore(date, score, computerScore));
    }
}
