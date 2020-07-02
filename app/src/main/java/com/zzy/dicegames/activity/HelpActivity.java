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
 * <ul><li>{@link #GAME_TITLE}: 一个字符串，游戏标题</li></ul>
 * 返回结果：无
 *
 * @author 赵正阳
 */
public class HelpActivity extends Activity {
	/** 传入Intent参数：游戏标题 */
	public static final String GAME_TITLE = "gameTitle";

	/** 支持的语言列表 */
	private static final List<String> SUPPORTED_LANG = Arrays.asList("zh", "en");

	/** 帮助文件名 */
	private String mFilename;

	/** 用于保存和恢复状态：骰子个数 */
	private static final String FILENAME = "filename";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		WebView webView = findViewById(R.id.wvHelp);

		// 注意Intent中的参数不会随系统语言的变化而变化
		if (savedInstanceState == null)
			mFilename = getFilename(getIntent().getStringExtra(GAME_TITLE));
		else
			mFilename = savedInstanceState.getString(FILENAME);

		String lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
		if (!SUPPORTED_LANG.contains(lang))
			lang = "zh";

		webView.loadUrl(String.format("file:///android_asset/help/%s_%s.html", mFilename, lang));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(FILENAME, mFilename);
		super.onSaveInstanceState(outState);
	}

	private String getFilename(String gameTitle) {
		if (gameTitle.equals(getString(R.string.fiveYahtzee)))
			return "five_yahtzee";
		else if (gameTitle.equals(getString(R.string.sixYahtzee)))
			return "six_yahtzee";
		else if (gameTitle.equals(getString(R.string.balut)))
			return "balut";
		else if (gameTitle.equals(getString(R.string.rollADice)))
			return "roll_a_dice";
		else if (gameTitle.equals(getString(R.string.farkle)))
			return "farkle";
		else
			return "";
	}

}
