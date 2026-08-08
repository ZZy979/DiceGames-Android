package com.zzy.dicegames.utils.score;

import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.farkle.FarkleScore;
import com.zzy.dicegames.data.entity.yahtzee.YahtzeeScore;
import com.zzy.dicegames.data.entity.yatzy.MaxiYatzyScore;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** 得分数据辅助类 */
public class ScoreUtil {
    private static ScoreDatabase scoreDatabase;

    public static void setScoreDatabase(ScoreDatabase scoreDatabase) {
        ScoreUtil.scoreDatabase = scoreDatabase;
    }

    public static void importScores(File file) throws IOException, XmlPullParserException {
        try (var inputStream = new FileInputStream(file)) {
            importScores(inputStream);
        }
    }

    public static void importScores(InputStream inputStream) throws IOException, XmlPullParserException {
        var parser = new ScoreParser(inputStream);
        ScoresDTO result = parser.parse();
        scoreDatabase.yahtzeeScoreDao().insertAll(result.yahtzeeScores);
        scoreDatabase.maxiYatzyScoreDao().insertAll(result.maxiYatzyScores);
        scoreDatabase.balutScoreDao().insertAll(result.balutScores);
        scoreDatabase.farkleScoreDao().insertAll(result.farkleScores);
    }

    public static void exportScores(File file) throws IOException {
        try (var outputStream = new FileOutputStream(file)) {
            exportScores(outputStream);
        }
    }

    public static void exportScores(OutputStream outputStream) throws IOException {
        var scoresDTO = new ScoresDTO(
                 scoreDatabase.yahtzeeScoreDao().findAll(),
                 scoreDatabase.maxiYatzyScoreDao().findAll(),
                 scoreDatabase.balutScoreDao().findAll(),
                 scoreDatabase.farkleScoreDao().findAll()
        );
        var serializer = new ScoreSerializer(outputStream, scoresDTO);
        serializer.serialize();
    }

    private static boolean isEqual(BaseScore a, BaseScore b) {
        return a.date.equals(b.date) && a.score == b.score;
    }

    public static boolean isEqual(YahtzeeScore a, YahtzeeScore b) {
        return isEqual((BaseScore) a, b) && a.hasBonus == b.hasBonus && a.hasYahtzee == b.hasYahtzee;
    }

    public static boolean isEqual(MaxiYatzyScore a, MaxiYatzyScore b) {
        return isEqual((BaseScore) a, b) && a.hasBonus == b.hasBonus && a.hasYatzy == b.hasYatzy;
    }

    public static boolean isEqual(BalutScore a, BalutScore b) {
        return isEqual((BaseScore) a, b) && a.points == b.points && a.numBalut == b.numBalut;
    }

    public static boolean isEqual(FarkleScore a, FarkleScore b) {
        return isEqual((BaseScore) a, b) && a.computerScore == b.computerScore;
    }
}
