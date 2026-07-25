package com.zzy.dicegames.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;
import com.zzy.dicegames.ui.help.HelpActivity;
import com.zzy.dicegames.ui.stats.StatisticsActivity;
import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.ui.game.BaseGameFragment;
import com.zzy.dicegames.utils.score.ScoreUtil;

import java.io.File;
import java.util.Arrays;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {
    private MainViewModel mViewModel;

    private BaseGameFragment<?> mGameFragment;

    private String[] mGameTypeNames;

    /** 上次按返回键的时间 */
    private long mLastPressBackTime = 0;

    /** 导入和导出得分数据的文件名 */
    private static final String SCORES_FILENAME = "scores.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        setupObservers(this);

        // 初次创建时会自动调用onGameTypeChanged()
        if (savedInstanceState != null)
            mGameFragment = (BaseGameFragment<?>) getSupportFragmentManager().findFragmentById(R.id.gameFragment);

        mGameTypeNames = Arrays.stream(GameType.values())
                .map(t -> getString(t.getNameResId()))
                .toArray(String[]::new);

        ScoreUtil.setScoreDatabase(ScoreDatabase.getInstance(this));

        // 返回键事件回调
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { quitOrPrompt(); }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menuNewGame) {
            mGameFragment.startNewGame();
        }
        else if (itemId == R.id.menuGameType) {
            selectGameType();
        }
        else if (itemId == R.id.menuHelp) {
            Intent intent = new Intent(this, HelpActivity.class);
            intent.putExtra(HelpActivity.KEY_GAME_TYPE, mGameFragment.getGameType());
            startActivity(intent);
        }
        else if (itemId == R.id.menuStatistics) {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra(StatisticsActivity.KEY_GAME_TYPE, mGameFragment.getGameType());
            startActivity(intent);
        }
        else if (itemId == R.id.menuImportScores) {
            importScores();
        }
        else if (itemId == R.id.menuExportScores) {
            exportScores();
        }
        return true;
    }

    private void quitOrPrompt() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastPressBackTime < 1000) {
            finish();
        }
        else {
            mLastPressBackTime = currentTime;
            Toast.makeText(this, R.string.quitPrompt, Toast.LENGTH_SHORT).show();
        }
    }

    /** 设置ViewModel观察者 */
    private void setupObservers(LifecycleOwner owner) {
        mViewModel.getGameType().observe(owner, this::onGameTypeChanged);
    }

    /** 选择游戏类型 */
    private void selectGameType() {
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.selectGameType)
                .setItems(mGameTypeNames, (dialog, which) -> mViewModel.changeGameType(which))
                .create().show();
    }

    /** 游戏类型更新时的回调 */
    private void onGameTypeChanged(GameType gameType) {
        if (mGameFragment != null && gameType == mGameFragment.getGameType())
            return;

        mGameFragment = BaseGameFragment.createByGameType(gameType);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.gameFragment, mGameFragment)
                .commit();
    }

    /** 返回导入/导出得分数据的文件 */
    private File getScoreDataFile() {
        return new File(getExternalFilesDir(null), SCORES_FILENAME);
    }

    /** 从文件中导入得分数据 */
    private void importScores() {
        try {
            ScoreUtil.importScores(getScoreDataFile());
            Toast.makeText(this, R.string.importScoresSuccess, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Toast.makeText(this, getString(R.string.importScoresFailed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    /** 将得分数据导出到文件 */
    private void exportScores() {
        try {
            File file = getScoreDataFile();
            ScoreUtil.exportScores(file);
            Toast.makeText(this,
                    getString(R.string.exportScoresSuccess, file.getAbsolutePath()), Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            Toast.makeText(this, getString(R.string.exportScoresFailed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

}
