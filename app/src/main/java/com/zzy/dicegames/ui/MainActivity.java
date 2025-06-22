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
import com.zzy.dicegames.ui.highscores.HighScoresActivity;
import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.BalutScore;
import com.zzy.dicegames.data.entity.FarkleScore;
import com.zzy.dicegames.data.entity.FiveYahtzeeScore;
import com.zzy.dicegames.data.entity.SixYahtzeeScore;
import com.zzy.dicegames.ui.game.GameFragment;
import com.zzy.dicegames.ui.game.balut.BalutGameFragment;
import com.zzy.dicegames.ui.game.farkle.FarkleGameFragment;
import com.zzy.dicegames.ui.game.rolladice.RollADiceGameFragment;
import com.zzy.dicegames.ui.game.yahtzee.FiveYahtzeeGameFragment;
import com.zzy.dicegames.ui.game.yahtzee.SixYahtzeeGameFragment;
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

    private GameFragment<?> mGameFragment;

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
            mGameFragment = new FiveYahtzeeGameFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.gameFragment, mGameFragment)
                    .commit();
        }
        else
            mGameFragment = (GameFragment<?>) getSupportFragmentManager().findFragmentById(R.id.gameFragment);
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
        else if (itemId == R.id.menuHighScores) {
            intent = new Intent(this, HighScoresActivity.class);
            intent.putExtra(HighScoresActivity.GAME_TITLE, mGameFragment.getTitle());
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
        super.onBackPressed();
        long currentTime = new Date().getTime();
        if (currentTime - mLastPressTime < 1000) finish();
        else {
            mLastPressTime = currentTime;
            Toast.makeText(this, R.string.quitPrompt, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        ScoreDatabase.closeInstance();
        super.onDestroy();
    }

    private void changeGameType(String gameTitle) {
        if (mGameFragment.getTitle().equals(gameTitle))
            mGameFragment.startNewGame();
        else {
            GameFragment<?> newGameFragment = null;
            if (gameTitle.equals(getString(R.string.fiveYahtzee)))
                newGameFragment = new FiveYahtzeeGameFragment();
            else if (gameTitle.equals(getString(R.string.sixYahtzee)))
                newGameFragment = new SixYahtzeeGameFragment();
            else if (gameTitle.equals(getString(R.string.balut)))
                newGameFragment = new BalutGameFragment();
//            else if (gameTitle.equals(getString(R.string.liarDice)))
//                ;
            else if (gameTitle.equals(getString(R.string.rollADice)))
                newGameFragment = new RollADiceGameFragment();
            else if (gameTitle.equals(getString(R.string.farkle)))
                newGameFragment = new FarkleGameFragment();

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
                        .attribute(null, "date", fiveYahtzeeScore.getDate())
                        .attribute(null, "score", fiveYahtzeeScore.getScore().toString())
                        .attribute(null, "got_bonus", fiveYahtzeeScore.getGotBonus().toString())
                        .attribute(null, "got_yahtzee", fiveYahtzeeScore.getGotYahtzee().toString())
                        .endTag(null, "FiveYahtzeeScore");
            serializer.endTag(null, "FiveYahtzeeScores");

            // 6骰Yahtzee
            serializer.startTag(null, "SixYahtzeeScores");
            for (SixYahtzeeScore sixYahtzeeScore : scoreDatabase.sixYahtzeeScoreDao().findAll())
                serializer.startTag(null, "SixYahtzeeScore")
                        .attribute(null, "date", sixYahtzeeScore.getDate())
                        .attribute(null, "score", sixYahtzeeScore.getScore().toString())
                        .attribute(null, "got_bonus", sixYahtzeeScore.getGotBonus().toString())
                        .attribute(null, "got_yahtzee", sixYahtzeeScore.getGotYahtzee().toString())
                        .endTag(null, "SixYahtzeeScore");
            serializer.endTag(null, "SixYahtzeeScores");

            // Balut
            serializer.startTag(null, "BalutScores");
            for (BalutScore balutScore : scoreDatabase.balutScoreDao().findAll())
                serializer.startTag(null, "BalutScore")
                        .attribute(null, "date", balutScore.getDate())
                        .attribute(null, "score", balutScore.getScore().toString())
                        .attribute(null, "got_balut", balutScore.getGotBalut().toString())
                        .endTag(null, "BalutScore");
            serializer.endTag(null, "BalutScores");

            // Farkle
            serializer.startTag(null, "FarkleScores");
            for (FarkleScore farkleScore : scoreDatabase.farkleScoreDao().findAll())
                serializer.startTag(null, "FarkleScore")
                        .attribute(null, "date", farkleScore.getDate())
                        .attribute(null, "score", farkleScore.getScore().toString())
                        .attribute(null, "cpu_score", farkleScore.getCpuScore().toString())
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
