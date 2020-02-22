package com.zzy.dicegames.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.ScoreDatabase;
import com.zzy.dicegames.database.dao.BalutScoreDao;
import com.zzy.dicegames.database.dao.FiveYahtzeeScoreDao;
import com.zzy.dicegames.database.dao.SixYahtzeeScoreDao;
import com.zzy.dicegames.fragment.highscores.BalutHighScoresFragment;
import com.zzy.dicegames.fragment.highscores.YahtzeeHighScoresFragment;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 用于展示最高分和统计数据的{@code Activity}<br>
 * 传入数据：<br>
 * <ul><li>{@link #GAME_TITLE}：一个字符串，游戏标题</li></ul>
 * 返回结果：无
 *
 * @author 赵正阳
 */
public class HighScoresActivity extends Activity {
	/** 用于传入参数/保存和恢复状态：游戏标题 */
	public static final String GAME_TITLE = "gameTitle";

	/** 支持的游戏类型列表 */
	private List<String> mSupportedGameTypes;

	/** 当前展示的游戏类型在{@link #mSupportedGameTypes}中的索引 */
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
				getString(R.string.balut)
		);

		if (savedInstanceState == null)
			changeGameType(getIntent().getStringExtra(GAME_TITLE));
		else {
			mGameTitleIndex = savedInstanceState.getInt(GAME_TITLE);
			mHighScoresFragment = getFragmentManager().findFragmentById(R.id.highScoresFragment);
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
			if (gameTitle.equals(getString(R.string.fiveYahtzee))) {
				mHighScoresFragment = new YahtzeeHighScoresFragment();
				FiveYahtzeeScoreDao fiveYahtzeeScoreDao = scoreDatabase.fiveYahtzeeScoreDao();
				bundle.putSerializable(YahtzeeHighScoresFragment.TOP10_SCORE, (Serializable) fiveYahtzeeScoreDao.findTop10());
				bundle.putInt(YahtzeeHighScoresFragment.GAMES_PLAYED, fiveYahtzeeScoreDao.count());
				bundle.putInt(YahtzeeHighScoresFragment.MAX_SCORE, fiveYahtzeeScoreDao.maxScore());
				bundle.putInt(YahtzeeHighScoresFragment.MIN_SCORE, fiveYahtzeeScoreDao.minScore());
				bundle.putDouble(YahtzeeHighScoresFragment.AVERAGE_SCORE, fiveYahtzeeScoreDao.averageScore());
				bundle.putInt(YahtzeeHighScoresFragment.GOT_BONUS, fiveYahtzeeScoreDao.sumGotBonus());
				bundle.putInt(YahtzeeHighScoresFragment.GOT_YAHTZEE, fiveYahtzeeScoreDao.sumGotYahtzee());
			}
			else if (gameTitle.equals(getString(R.string.sixYahtzee))) {
				mHighScoresFragment = new YahtzeeHighScoresFragment();
				SixYahtzeeScoreDao sixYahtzeeScoreDao = scoreDatabase.sixYahtzeeScoreDao();
				bundle.putSerializable(YahtzeeHighScoresFragment.TOP10_SCORE, (Serializable) sixYahtzeeScoreDao.findTop10());
				bundle.putInt(YahtzeeHighScoresFragment.GAMES_PLAYED, sixYahtzeeScoreDao.count());
				bundle.putInt(YahtzeeHighScoresFragment.MAX_SCORE, sixYahtzeeScoreDao.maxScore());
				bundle.putInt(YahtzeeHighScoresFragment.MIN_SCORE, sixYahtzeeScoreDao.minScore());
				bundle.putDouble(YahtzeeHighScoresFragment.AVERAGE_SCORE, sixYahtzeeScoreDao.averageScore());
				bundle.putInt(YahtzeeHighScoresFragment.GOT_BONUS, sixYahtzeeScoreDao.sumGotBonus());
				bundle.putInt(YahtzeeHighScoresFragment.GOT_YAHTZEE, sixYahtzeeScoreDao.sumGotYahtzee());
			}
			else if (gameTitle.equals(getString(R.string.balut))) {
				mHighScoresFragment = new BalutHighScoresFragment();
				BalutScoreDao balutScoreDao = scoreDatabase.balutScoreDao();
				bundle.putSerializable(BalutHighScoresFragment.TOP10_SCORE, (Serializable) balutScoreDao.findTop10());
				bundle.putInt(BalutHighScoresFragment.GAMES_PLAYED, balutScoreDao.count());
				bundle.putInt(BalutHighScoresFragment.MAX_SCORE, balutScoreDao.maxScore());
				bundle.putInt(BalutHighScoresFragment.MIN_SCORE, balutScoreDao.minScore());
				bundle.putDouble(BalutHighScoresFragment.AVERAGE_SCORE, balutScoreDao.averageScore());
				bundle.putInt(BalutHighScoresFragment.GOT_BALUT, balutScoreDao.sumGotBalut());
			}

			mHighScoresFragment.setArguments(bundle);
			getFragmentManager().beginTransaction()
					.replace(R.id.highScoresFragment, mHighScoresFragment)
					.commit();
		}
	}

}
