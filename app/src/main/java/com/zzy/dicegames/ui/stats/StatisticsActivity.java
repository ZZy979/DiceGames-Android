package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 用于展示统计数据的{@code Activity}<br>
 * 传入数据：<br>
 * <ul><li>{@link #KEY_GAME_TYPE}：游戏类型</li></ul>
 * 返回结果：无
 *
 * @author 赵正阳
 */
public class StatisticsActivity extends AppCompatActivity {
    /** 传入参数：游戏类型 */
    public static final String KEY_GAME_TYPE = "gameType";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        GameType gameType = (GameType) getIntent().getSerializableExtra(KEY_GAME_TYPE);
        if (savedInstanceState == null)
            loadData(gameType);

        TextView titleTextView = findViewById(R.id.tvTitle);
        titleTextView.setText(getString(R.string.gameTypeStatistics, getString(gameType.getNameResId())));
    }

    private void loadData(GameType gameType) {
        var statsFragment = BaseStatsFragment.createByGameType(gameType);
        if (statsFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.statisticsFragment, statsFragment)
                    .commit();
        }
    }

}
