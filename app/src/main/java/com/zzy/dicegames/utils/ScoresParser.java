package com.zzy.dicegames.utils;

import com.zzy.dicegames.data.entity.BalutScore;
import com.zzy.dicegames.data.entity.FarkleScore;
import com.zzy.dicegames.data.entity.FiveYahtzeeScore;
import com.zzy.dicegames.data.entity.SixYahtzeeScore;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用XmlPullParser的得分数据解析器
 *
 * @author 赵正阳
 */
public class ScoresParser {
    private XmlPullParser parser;

    private List<FiveYahtzeeScore> fiveYahtzeeScores = new ArrayList<>();

    private List<SixYahtzeeScore> sixYahtzeeScores = new ArrayList<>();

    private List<BalutScore> balutScores = new ArrayList<>();

    private List<FarkleScore> farkleScores = new ArrayList<>();

    public ScoresParser(File file) throws FileNotFoundException {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
            parser.setInput(new FileInputStream(file), "utf-8");
        }
        catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    /** 读取并解析XML文件，之后使用{@code getXxx()}获取结果 */
    public void parse() {
        String tagName;
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    if (tagName.equals("FiveYahtzeeScore")) {
                        String date = parser.getAttributeValue(null, "date");
                        int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
                        boolean hasBonus = Boolean.parseBoolean(parser.getAttributeValue(null, "has_bonus"));
                        boolean hasYahtzee = Boolean.parseBoolean(parser.getAttributeValue(null, "has_yahtzee"));
                        fiveYahtzeeScores.add(new FiveYahtzeeScore(date, score, hasBonus, hasYahtzee));
                    }
                    else if (tagName.equals("SixYahtzeeScore")) {
                        String date = parser.getAttributeValue(null, "date");
                        int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
                        boolean hasBonus = Boolean.parseBoolean(parser.getAttributeValue(null, "has_bonus"));
                        boolean hasYahtzee = Boolean.parseBoolean(parser.getAttributeValue(null, "has_yahtzee"));
                        sixYahtzeeScores.add(new SixYahtzeeScore(date, score, hasBonus, hasYahtzee));
                    }
                    else if (tagName.equals("BalutScore")) {
                        String date = parser.getAttributeValue(null, "date");
                        int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
                        int numBalut = Integer.parseInt(parser.getAttributeValue(null, "num_balut"));
                        balutScores.add(new BalutScore(date, score, numBalut));
                    }
                    else if (tagName.equals("FarkleScore")) {
                        String date = parser.getAttributeValue(null, "date");
                        int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
                        int computerScore = Integer.parseInt(parser.getAttributeValue(null, "computer_score"));
                        farkleScores.add(new FarkleScore(date, score, computerScore));
                    }
                }
                eventType = parser.next();
            }
        }
        catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    public List<FiveYahtzeeScore> getFiveYahtzeeScores() {
        return fiveYahtzeeScores;
    }

    public List<SixYahtzeeScore> getSixYahtzeeScores() {
        return sixYahtzeeScores;
    }

    public List<BalutScore> getBalutScores() {
        return balutScores;
    }

    public List<FarkleScore> getFarkleScores() {
        return farkleScores;
    }

}
