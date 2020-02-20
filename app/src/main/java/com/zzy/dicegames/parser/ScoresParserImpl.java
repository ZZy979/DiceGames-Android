package com.zzy.dicegames.parser;

import com.zzy.dicegames.database.entity.BalutScore;
import com.zzy.dicegames.database.entity.FiveYahtzeeScore;
import com.zzy.dicegames.database.entity.SixYahtzeeScore;

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
public class ScoresParserImpl implements ScoresParser {
	private File file;

	private XmlPullParserFactory factory;

	public ScoresParserImpl(File file) throws FileNotFoundException {
		if (!file.exists())
			throw new FileNotFoundException();
		this.file = file;
		try {
			this.factory = XmlPullParserFactory.newInstance();
		}
		catch (XmlPullParserException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<FiveYahtzeeScore> parseFiveYahtzeeScores() {
		List<FiveYahtzeeScore> result = new ArrayList<>();
		try {
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new FileInputStream(file), "utf-8");
			String tagName;
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
					if (tagName.equals("FiveYahtzeeScore")) {
						String date = parser.getAttributeValue(null, "date");
						int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
						int gotBonus = Integer.parseInt(parser.getAttributeValue(null, "got_bonus"));
						int gotYahtzee = Integer.parseInt(parser.getAttributeValue(null, "got_yahtzee"));
						result.add(new FiveYahtzeeScore(date, score, gotBonus, gotYahtzee));
					}
				}
				eventType = parser.next();
			}
		}
		catch (XmlPullParserException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<SixYahtzeeScore> parseSixYahtzeeScores() {
		List<SixYahtzeeScore> result = new ArrayList<>();
		try {
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new FileInputStream(file), "utf-8");
			String tagName;
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
					if (tagName.equals("SixYahtzeeScore")) {
						String date = parser.getAttributeValue(null, "date");
						int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
						int gotBonus = Integer.parseInt(parser.getAttributeValue(null, "got_bonus"));
						int gotYahtzee = Integer.parseInt(parser.getAttributeValue(null, "got_yahtzee"));
						result.add(new SixYahtzeeScore(date, score, gotBonus, gotYahtzee));
					}
				}
				eventType = parser.next();
			}
		}
		catch (XmlPullParserException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<BalutScore> parseBalutScores() {
		List<BalutScore> result = new ArrayList<>();
		try {
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new FileInputStream(file), "utf-8");
			String tagName;
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					tagName = parser.getName();
					if (tagName.equals("BalutScore")) {
						String date = parser.getAttributeValue(null, "date");
						int score = Integer.parseInt(parser.getAttributeValue(null, "score"));
						int gotBalut = Integer.parseInt(parser.getAttributeValue(null, "got_balut"));
						result.add(new BalutScore(date, score, gotBalut));
					}
				}
				eventType = parser.next();
			}
		}
		catch (XmlPullParserException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
