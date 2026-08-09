package com.zzy.dicegames.ui.help;

import android.os.Bundle;
import android.webkit.WebView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;

import java.util.List;

import androidx.annotation.NonNull;
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

    /** 用于保存和恢复WebView状态 */
    public static final String KEY_WEBVIEW_STATE = "webViewState";

    /** 支持的语言列表 */
    private static final List<String> SUPPORTED_LANG = List.of("en", "zh");

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        mWebView = findViewById(R.id.wvHelp);
        if (savedInstanceState == null) {
            loadPage((GameType) getIntent().getSerializableExtra(KEY_GAME_TYPE));
        }
        else {
            var bundle = savedInstanceState.getBundle(KEY_WEBVIEW_STATE);
            if (bundle != null)
                mWebView.restoreState(bundle);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        var bundle = new Bundle();
        mWebView.saveState(bundle);
        outState.putBundle(KEY_WEBVIEW_STATE, bundle);
    }

    private void loadPage(GameType gameType) {
        String lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
        if (!SUPPORTED_LANG.contains(lang))
            lang = SUPPORTED_LANG.get(0);

        String url = String.format("file:///android_asset/help/%s_%s.html", gameType.name().toLowerCase(), lang);
        mWebView.loadUrl(url);
    }

}
