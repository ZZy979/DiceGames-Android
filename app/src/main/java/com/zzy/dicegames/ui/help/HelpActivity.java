package com.zzy.dicegames.ui.help;

import android.os.Bundle;
import android.webkit.WebView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 用于查看帮助信息的{@code Activity}<br>
 * 传入数据：<br>
 * <ul><li>{@link #KEY_GAME_TYPE}: 游戏类型</li></ul>
 * 返回结果：无
 *
 * @author 赵正阳
 */
public class HelpActivity extends AppCompatActivity {
    /** 传入参数：游戏类型 */
    public static final String KEY_GAME_TYPE = "gameType";

    /** 支持的语言列表 */
    private static final List<String> SUPPORTED_LANG = List.of("en", "zh");

    /** 当前游戏类型 */
    private GameType mGameType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        if (savedInstanceState == null)
            mGameType = (GameType) getIntent().getSerializableExtra(KEY_GAME_TYPE);
        else
            mGameType = (GameType) savedInstanceState.getSerializable(KEY_GAME_TYPE);

        loadPage();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_GAME_TYPE, mGameType);
        super.onSaveInstanceState(outState);
    }

    private void loadPage() {
        String lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
        if (!SUPPORTED_LANG.contains(lang))
            lang = SUPPORTED_LANG.get(0);

        WebView webView = findViewById(R.id.wvHelp);
        String url = String.format("file:///android_asset/help/%s_%s.html", mGameType.name().toLowerCase(), lang);
        webView.loadUrl(url);
    }

}
