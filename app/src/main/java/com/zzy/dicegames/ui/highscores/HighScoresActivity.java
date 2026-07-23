package com.zzy.dicegames.ui.highscores;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.ScoreDatabase;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * 用于展示最高分和统计数据的{@code Activity}<br>
 * 传入数据：<br>
 * <ul><li>{@link #GAME_TITLE}：一个字符串，游戏标题</li></ul>
 * 返回结果：无
 *
 * @author 赵正阳
 */
public class HighScoresActivity extends AppCompatActivity {
    /** 用于传入参数/保存和恢复状态：游戏标题 */
    public static final String GAME_TITLE = "gameTitle";

    /** 支持的游戏类型列表 */
    private List<String> mSupportedGameTypes;

    /** 当前展示的游戏类型在{@link #mSupportedGameTypes}中的索引 */
    // TODO 替换为ViewModel
    private int mGameTitleIndex = -1;

    /** 展示区域 */
    private Fragment mHighScoresFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        mSupportedGameTypes = Arrays.asList(
                getString(R.string.fiveYahtzee),
                getString(R.string.sixYahtzee),
                getString(R.string.balut),
                getString(R.string.farkle)
        );

        if (savedInstanceState == null)
            changeGameType(getIntent().getStringExtra(GAME_TITLE));
        else {
            mGameTitleIndex = savedInstanceState.getInt(GAME_TITLE);
            mHighScoresFragment = getSupportFragmentManager().findFragmentById(R.id.highScoresFragment);
        }

        Spinner spnGameType = findViewById(R.id.spnGameType);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mSupportedGameTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnGameType.setAdapter(adapter);
        spnGameType.setSelection(mGameTitleIndex);
        spnGameType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeGameType(mSupportedGameTypes.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(GAME_TITLE, mGameTitleIndex);
        super.onSaveInstanceState(outState);
    }

    private void changeGameType(String gameTitle) {
        if (!mSupportedGameTypes.contains(gameTitle))
            gameTitle = mSupportedGameTypes.get(0);
        if (mGameTitleIndex < 0 || !gameTitle.equals(mSupportedGameTypes.get(mGameTitleIndex))) {
            mGameTitleIndex = mSupportedGameTypes.indexOf(gameTitle);
            ScoreDatabase scoreDatabase = ScoreDatabase.getInstance(this);
            Bundle bundle = new Bundle();
            if (gameTitle.equals(getString(R.string.fiveYahtzee)))
                mHighScoresFragment = new FiveYahtzeeStatsFragment();
            else if (gameTitle.equals(getString(R.string.sixYahtzee)))
                mHighScoresFragment = new SixYahtzeeStatsFragment();
            else if (gameTitle.equals(getString(R.string.balut))) {
                mHighScoresFragment = new BalutHighScoresFragment();
                var dao = scoreDatabase.balutScoreDao();
                var stats = dao.statistics();
                bundle.putSerializable(BalutHighScoresFragment.TOP10_SCORE, (Serializable) dao.findTop(10));
                bundle.putInt(BalutHighScoresFragment.GAMES_PLAYED, stats.count);
                bundle.putInt(BalutHighScoresFragment.MAX_SCORE, stats.maxScore);
                bundle.putInt(BalutHighScoresFragment.MIN_SCORE, stats.minScore);
                bundle.putDouble(BalutHighScoresFragment.AVERAGE_SCORE, stats.avgScore);
                bundle.putInt(BalutHighScoresFragment.GOT_BALUT, stats.numBalut);
            }
            else if (gameTitle.equals(getString(R.string.farkle))) {
                mHighScoresFragment = new FarkleHighScoresFragment();
                var dao = scoreDatabase.farkleScoreDao();
                var stats = dao.statistics();
                bundle.putInt(FarkleHighScoresFragment.GAMES_PLAYED, stats.count);
                bundle.putInt(FarkleHighScoresFragment.WON_GAMES, stats.winCount);
            }

            mHighScoresFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.highScoresFragment, mHighScoresFragment)
                    .commit();
        }
    }

}
