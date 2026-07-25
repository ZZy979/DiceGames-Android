package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * 用于展示统计数据的{@code Activity}<br>
 * 传入数据：<br>
 * <ul><li>{@link #KEY_GAME_TYPE}：游戏类型</li></ul>
 * 返回结果：无
 *
 * @author 赵正阳
 */
public class StatisticsActivity extends AppCompatActivity {
    /** 用于传入参数/保存和恢复状态：游戏类型 */
    public static final String KEY_GAME_TYPE = "gameType";

    /** 支持的游戏类型列表 */
    private static final List<GameType> SUPPORTED_GAME_TYPES = List.of(
            GameType.FIVE_YAHTZEE,
            GameType.SIX_YAHTZEE,
            GameType.BALUT,
            GameType.FARKLE
    );

    private String[] mGameTypeNames;

    /** 当前游戏类型 */
    private GameType mGameType;

    /** 展示区域 */
    private Fragment mStatisticsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mGameTypeNames = SUPPORTED_GAME_TYPES.stream()
                .map(t -> getString(t.getNameResId()))
                .toArray(String[]::new);

        if (savedInstanceState == null) {
            changeGameType((GameType) getIntent().getSerializableExtra(KEY_GAME_TYPE));
        }
        else {
            mGameType = (GameType) savedInstanceState.getSerializable(KEY_GAME_TYPE);
            mStatisticsFragment = getSupportFragmentManager().findFragmentById(R.id.statisticsFragment);
        }

        initViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_GAME_TYPE, mGameType);
        super.onSaveInstanceState(outState);
    }

    private void initViews() {
        Spinner spnGameType = findViewById(R.id.spnGameType);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mGameTypeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnGameType.setAdapter(adapter);
        spnGameType.setSelection(SUPPORTED_GAME_TYPES.indexOf(mGameType));
        spnGameType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeGameType(SUPPORTED_GAME_TYPES.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void changeGameType(GameType gameType) {
        if (!SUPPORTED_GAME_TYPES.contains(gameType))
            gameType = SUPPORTED_GAME_TYPES.get(0);
        if (gameType == mGameType)
            return;

        mGameType = gameType;
        mStatisticsFragment = BaseStatsFragment.createByGameType(gameType);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.statisticsFragment, mStatisticsFragment)
                .commit();
    }

}
