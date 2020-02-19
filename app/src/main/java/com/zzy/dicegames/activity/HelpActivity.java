package com.zzy.dicegames.activity;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.zzy.dicegames.R;

import java.util.Arrays;
import java.util.List;

/**
 * 用于查看帮助信息的{@code Activity}<br>
 * 传入数据：<br>
 * <ul><li>{@link #GAME_TYPE_CODE}: 一个字符串，游戏类型代码</li></ul>
 * 返回结果：无
 *
 * @author 赵正阳
 */
public class HelpActivity extends Activity {
	/** 传入Intent参数：游戏类型代码 */
	public static final String GAME_TYPE_CODE = "gameTypeCode";

	/** 支持的语言列表 */
	private static final List<String> SUPPORTED_LANG = Arrays.asList("zh", "en");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		WebView webView = findViewById(R.id.wvHelp);

		String filename = getIntent().getStringExtra(GAME_TYPE_CODE);
		String lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
		if (!SUPPORTED_LANG.contains(lang))
			lang = "zh";

		webView.loadUrl(String.format("file:///android_asset/help/%s_%s.html", filename, lang));
	}

}
