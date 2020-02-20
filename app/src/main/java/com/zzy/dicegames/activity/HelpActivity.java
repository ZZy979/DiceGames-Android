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

	/** 支持的游戏类型列表 */
	private List<String> mSupportedGameTypes;

	/** 支持的语言列表 */
	private static final List<String> SUPPORTED_LANG = Arrays.asList("zh", "en");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		mSupportedGameTypes = Arrays.asList(
				getString(R.string.fiveYahtzee),
				getString(R.string.sixYahtzee),
				getString(R.string.balut),
				getString(R.string.rollADice)
		);

		WebView webView = findViewById(R.id.wvHelp);

		String filename = getFilename(getIntent().getStringExtra(GAME_TITLE));
		String lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
		if (!SUPPORTED_LANG.contains(lang))
			lang = "zh";

		webView.loadUrl(String.format("file:///android_asset/help/%s_%s.html", filename, lang));
	}

	private String getFilename(String gameTitle) {
		if (gameTitle.equals(mSupportedGameTypes.get(0)))
			return "five_yahtzee";
		else if (gameTitle.equals(mSupportedGameTypes.get(1)))
			return "six_yahtzee";
		else if (gameTitle.equals(mSupportedGameTypes.get(2)))
			return "balut";
		else if (gameTitle.equals(mSupportedGameTypes.get(3)))
			return "roll_a_dice";
		else
			return "";
	}

}
