package com.zzy.dicegames.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.zzy.dicegames.R;
import com.zzy.dicegames.ui.help.HelpActivity;
import com.zzy.dicegames.ui.stats.StatisticsActivity;
import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.BalutScore;
import com.zzy.dicegames.data.entity.FarkleScore;
import com.zzy.dicegames.data.entity.FiveYahtzeeScore;
import com.zzy.dicegames.data.entity.SixYahtzeeScore;
import com.zzy.dicegames.ui.game.BaseGameFragment;
import com.zzy.dicegames.ui.game.balut.BalutFragment;
import com.zzy.dicegames.ui.game.farkle.FarkleFragment;
import com.zzy.dicegames.ui.game.rolladice.RollADiceFragment;
import com.zzy.dicegames.ui.game.yahtzee.FiveYahtzeeFragment;
import com.zzy.dicegames.ui.game.yahtzee.SixYahtzeeFragment;
import com.zzy.dicegames.utils.ScoresParser;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private String[] mGameTypes;

    private BaseGameFragment<?> mGameFragment;

    /** 上次按返回键的时间 */
    private long mLastPressTime = 0;

    /** 导入和导出得分数据的文件名 */
    private static final String SCORES_FILENAME = "scores.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGameTypes = getResources().getStringArray(R.array.gameTypes);
        if (savedInstanceState == null) {
            mGameFragment = new FiveYahtzeeFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.gameFragment, mGameFragment)
                    .commit();
        }
        else
            mGameFragment = (BaseGameFragment<?>) getSupportFragmentManager().findFragmentById(R.id.gameFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        int itemId = item.getItemId();
        if (itemId == R.id.menuNewGame) {
            mGameFragment.startNewGame();
        }
        else if (itemId == R.id.menuGameType) {
            new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.selectGameType)
                    .setItems(mGameTypes, (dialog, which) -> changeGameType(mGameTypes[which]))
                    .create().show();
        }
        else if (itemId == R.id.menuHelp) {
            intent = new Intent(this, HelpActivity.class);
            intent.putExtra(HelpActivity.GAME_TITLE, mGameFragment.getTitle());
            startActivity(intent);
        }
        else if (itemId == R.id.menuStatistics) {
            intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra(StatisticsActivity.GAME_TITLE, mGameFragment.getTitle());
            startActivity(intent);
        }
        else if (itemId == R.id.menuImportScores) {
            File file = new File(getExternalFilesDir(null), SCORES_FILENAME);
            if (importScores(file))
                Toast.makeText(this, R.string.importScoresSuccess, Toast.LENGTH_SHORT).show();
        }
        else if (itemId == R.id.menuExportScores) {
            File file = new File(getExternalFilesDir(null), SCORES_FILENAME);
            if (exportScores(file))
                Toast.makeText(this,
                        String.format(getString(R.string.exportScoresSuccess), file.getAbsolutePath()),
                        Toast.LENGTH_LONG
                ).show();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        // TODO fix
        super.onBackPressed();
        long currentTime = new Date().getTime();
        if (currentTime - mLastPressTime < 1000) finish();
        else {
            mLastPressTime = currentTime;
            Toast.makeText(this, R.string.quitPrompt, Toast.LENGTH_SHORT).show();
        }
    }

    private void changeGameType(String gameTitle) {
        if (mGameFragment.getTitle().equals(gameTitle))
            mGameFragment.startNewGame();
        else {
            // TODO createGameFragmentByGameType
            BaseGameFragment<?> newGameFragment = null;
            if (gameTitle.equals(getString(R.string.fiveYahtzee)))
                newGameFragment = new FiveYahtzeeFragment();
            else if (gameTitle.equals(getString(R.string.sixYahtzee)))
                newGameFragment = new SixYahtzeeFragment();
            else if (gameTitle.equals(getString(R.string.balut)))
                newGameFragment = new BalutFragment();
//            else if (gameTitle.equals(getString(R.string.liarDice)))
//                ;
            else if (gameTitle.equals(getString(R.string.rollADice)))
                newGameFragment = new RollADiceFragment();
            else if (gameTitle.equals(getString(R.string.farkle)))
                newGameFragment = new FarkleFragment();

            if (newGameFragment != null) {
                mGameFragment = newGameFragment;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.gameFragment, mGameFragment)
                        .commit();
            }
        }
    }

    /** 从XML文件中导入得分数据，导入成功则返回{@code true}，否则返回{@code false} */
    private boolean importScores(File file) {
        ScoreDatabase scoreDatabase = ScoreDatabase.getInstance(this);
        try {
            ScoresParser parser = new ScoresParser(file);
            parser.parse();
            scoreDatabase.fiveYahtzeeScoreDao().insertAll(parser.getFiveYahtzeeScores());
            scoreDatabase.sixYahtzeeScoreDao().insertAll(parser.getSixYahtzeeScores());
            scoreDatabase.balutScoreDao().insertAll(parser.getBalutScores());
            scoreDatabase.farkleScoreDao().insertAll(parser.getFarkleScores());
            return true;
        }
        catch (FileNotFoundException e) {
            return false;
        }
    }

    /** 将得分数据导出到XML文件，导出成功则返回{@code true}，否则返回{@code false} */
    private boolean exportScores(File file) {
        try {
            ScoreDatabase scoreDatabase = ScoreDatabase.getInstance(this);
            FileOutputStream fos = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "utf-8");
            serializer.startDocument("utf-8", true);
            serializer.startTag(null, "scores");

            // 5骰Yahtzee
            serializer.startTag(null, "FiveYahtzeeScores");
            for (FiveYahtzeeScore fiveYahtzeeScore : scoreDatabase.fiveYahtzeeScoreDao().findAll())
                serializer.startTag(null, "FiveYahtzeeScore")
                        .attribute(null, "date", fiveYahtzeeScore.date)
                        .attribute(null, "score", Integer.toString(fiveYahtzeeScore.score))
                        .attribute(null, "has_bonus", Boolean.toString(fiveYahtzeeScore.hasBonus))
                        .attribute(null, "has_yahtzee", Boolean.toString(fiveYahtzeeScore.hasYahtzee))
                        .endTag(null, "FiveYahtzeeScore");
            serializer.endTag(null, "FiveYahtzeeScores");

            // 6骰Yahtzee
            serializer.startTag(null, "SixYahtzeeScores");
            for (SixYahtzeeScore sixYahtzeeScore : scoreDatabase.sixYahtzeeScoreDao().findAll())
                serializer.startTag(null, "SixYahtzeeScore")
                        .attribute(null, "date", sixYahtzeeScore.date)
                        .attribute(null, "score", Integer.toString(sixYahtzeeScore.score))
                        .attribute(null, "has_bonus", Boolean.toString(sixYahtzeeScore.hasBonus))
                        .attribute(null, "has_yahtzee", Boolean.toString(sixYahtzeeScore.hasYahtzee))
                        .endTag(null, "SixYahtzeeScore");
            serializer.endTag(null, "SixYahtzeeScores");

            // Balut
            serializer.startTag(null, "BalutScores");
            for (BalutScore balutScore : scoreDatabase.balutScoreDao().findAll())
                serializer.startTag(null, "BalutScore")
                        .attribute(null, "date", balutScore.date)
                        .attribute(null, "score", Integer.toString(balutScore.score))
                        .attribute(null, "num_balut", Integer.toString(balutScore.numBalut))
                        .endTag(null, "BalutScore");
            serializer.endTag(null, "BalutScores");

            // Farkle
            serializer.startTag(null, "FarkleScores");
            for (FarkleScore farkleScore : scoreDatabase.farkleScoreDao().findAll())
                serializer.startTag(null, "FarkleScore")
                        .attribute(null, "date", farkleScore.date)
                        .attribute(null, "score", Integer.toString(farkleScore.score))
                        .attribute(null, "computer_score", Integer.toString(farkleScore.computerScore))
                        .endTag(null, "FarkleScore");
            serializer.endTag(null, "FarkleScores");

            serializer.endTag(null, "scores");
            serializer.endDocument();
            fos.close();
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
