package com.zzy.dicegames.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.ScoreDatabase;
import com.zzy.dicegames.database.entity.BaseScore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于展示最高分的{@code Activity}<br>
 * 传入数据：<br>
 * <ul><li>{@link #GAME_TITLE}：一个字符串，游戏标题</li></ul>
 * 返回结果：无
 *
 * @author 赵正阳
 */
public class HighScoresActivity extends Activity {
	/** 传入Intent参数：游戏标题 */
	public static final String GAME_TITLE = "gameTitle";

	/** 支持的游戏类型列表 */
	private List<String> mSupportedGameTypes;

	private ListView lvHighScores;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_high_scores);

		mSupportedGameTypes = Arrays.asList(
				getString(R.string.fiveYahtzee),
				getString(R.string.sixYahtzee),
				getString(R.string.balut)
		);

		lvHighScores = findViewById(R.id.lvHighScores);

		String gameTitle = getIntent().getStringExtra(GAME_TITLE);
		if (!mSupportedGameTypes.contains(gameTitle))
			gameTitle = mSupportedGameTypes.get(0);

		Spinner spnGameType = findViewById(R.id.spnGameType);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mSupportedGameTypes);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnGameType.setAdapter(adapter);
		spnGameType.setSelection(mSupportedGameTypes.indexOf(gameTitle));
		spnGameType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				loadHighScores(mSupportedGameTypes.get(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
	}

	private void loadHighScores(String gameTitle) {
		ScoreDatabase scoreDatabase = ScoreDatabase.getInstance(this);
		List<BaseScore> highScores = new ArrayList<>();
		if (gameTitle.equals(mSupportedGameTypes.get(0)))
			highScores.addAll(scoreDatabase.fiveYahtzeeScoreDao().findTop10());
		else if (gameTitle.equals(mSupportedGameTypes.get(1)))
			highScores.addAll(scoreDatabase.sixYahtzeeScoreDao().findTop10());
		else if (gameTitle.equals(mSupportedGameTypes.get(2)))
			highScores.addAll(scoreDatabase.balutScoreDao().findTop10());

		List<Map<String, Object>> data = new ArrayList<>();
		for (int i = 0; i < highScores.size(); ++i) {
			Map<String, Object> map = new HashMap<>();
			map.put("rank", i + 1);
			map.put("score", highScores.get(i).getScore());
			map.put("date", highScores.get(i).getDate());
			data.add(map);
		}
		lvHighScores.setAdapter(new SimpleAdapter(
				this, data, R.layout.high_score_item,
				new String[] {"rank", "score", "date"},
				new int[] {R.id.tvRank, R.id.tvScore, R.id.tvDate}
		));
	}

}
