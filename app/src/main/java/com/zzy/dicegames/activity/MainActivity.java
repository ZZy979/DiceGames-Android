package com.zzy.dicegames.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.ScoreDatabase;
import com.zzy.dicegames.database.entity.BalutScore;
import com.zzy.dicegames.database.entity.FiveYahtzeeScore;
import com.zzy.dicegames.database.entity.SixYahtzeeScore;
import com.zzy.dicegames.game.balut.BalutFragment;
import com.zzy.dicegames.game.farkle.FarkleFragment;
import com.zzy.dicegames.game.yahtzee.FiveYahtzeeFragment;
import com.zzy.dicegames.game.GameFragment;
import com.zzy.dicegames.game.rolladice.RollADiceFragment;
import com.zzy.dicegames.game.yahtzee.SixYahtzeeFragment;
import com.zzy.dicegames.parser.ScoresParser;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;

public class MainActivity extends Activity {
	private String[] mGameTypes;

	private GameFragment<?> mGameFragment;

	/** 上次按返回键的时间 */
	private long mLastPressTime = 0;

	/** 导入和导出得分数据的文件名 */
	private static final String SCORES_FILENAME = "scores.xml";

	// ----------消息处理----------
	/** 解析提交的命令行 */
	private static final int MSG_SUBMIT_CMD = 1;

	private MainHandler mHandler;

	/** 防止内存泄露的消息处理器 */
	private static class MainHandler extends Handler {
		private WeakReference<MainActivity> mWeakReference;

		public MainHandler(MainActivity reference) {
			mWeakReference = new WeakReference<>(reference);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity activity = mWeakReference.get();
			if (activity == null)
				return;
			if (msg.what == MSG_SUBMIT_CMD)
				activity.parseCmd((String) msg.obj);
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mGameTypes = getResources().getStringArray(R.array.gameTypes);
		if (savedInstanceState == null) {
			mGameFragment = new FiveYahtzeeFragment();
			getFragmentManager().beginTransaction()
					.add(R.id.gameFragment, mGameFragment)
					.commit();
		}
		else
			mGameFragment = (GameFragment<?>) getFragmentManager().findFragmentById(R.id.gameFragment);

		mHandler = new MainHandler(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.menuNewGame:
			mGameFragment.startNewGame();
			break;
		case R.id.menuGameType:
			new AlertDialog.Builder(this)
					.setIcon(R.mipmap.ic_launcher)
					.setTitle(R.string.selectGameType)
					.setItems(mGameTypes, (dialog, which) -> changeGameType(mGameTypes[which]))
					.create().show();
			break;
		case R.id.menuHelp:
			intent = new Intent(this, HelpActivity.class);
			intent.putExtra(HelpActivity.GAME_TITLE, mGameFragment.getTitle());
			startActivity(intent);
			break;
		case R.id.menuHighScores:
			intent = new Intent(this, HighScoresActivity.class);
			intent.putExtra(HighScoresActivity.GAME_TITLE, mGameFragment.getTitle());
			startActivity(intent);
			break;
		case R.id.menuCmd:
			final EditText editText = new EditText(this);
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.cmd)
					.setTitle(R.string.cmd)
					.setView(editText)
					.setPositiveButton(R.string.ok, (dialog, which) -> {
						Message message = mHandler.obtainMessage(MSG_SUBMIT_CMD, editText.getText().toString());
						mHandler.sendMessage(message);
					})
					.create().show();
			break;
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		long currentTime = new Date().getTime();
		if (currentTime - mLastPressTime < 1000) finish();
		else {
			mLastPressTime = currentTime;
			Toast.makeText(this, R.string.quitPrompt, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onDestroy() {
		ScoreDatabase.closeInstance();
		super.onDestroy();
	}

	private void changeGameType(String gameTitle) {
		if (mGameFragment.getTitle().equals(gameTitle))
			mGameFragment.startNewGame();
		else {
			GameFragment<?> newGameFragment = null;
			if (gameTitle.equals(getString(R.string.fiveYahtzee)))
				newGameFragment = new FiveYahtzeeFragment();
			else if (gameTitle.equals(getString(R.string.sixYahtzee)))
				newGameFragment = new SixYahtzeeFragment();
			else if (gameTitle.equals(getString(R.string.balut)))
				newGameFragment = new BalutFragment();
//			else if (gameTitle.equals(getString(R.string.liarDice)))
//				;
			else if (gameTitle.equals(getString(R.string.rollADice)))
				newGameFragment = new RollADiceFragment();
			else if (gameTitle.equals(getString(R.string.farkle)))
				newGameFragment = new FarkleFragment();

			if (newGameFragment != null) {
				mGameFragment = newGameFragment;
				getFragmentManager().beginTransaction()
						.replace(R.id.gameFragment, mGameFragment)
						.commit();
			}
		}
	}

	/** 从XML文件中导入得分数据，导入成功则返回{@code true}，否则返回{@code false} */
	private boolean importScores(File file) {
		ScoreDatabase scoreDatabase = ScoreDatabase.getInstance(this);
		try {
			ScoresParser parser = new ScoresParser(file);
			parser.parse();
			scoreDatabase.fiveYahtzeeScoreDao().insertAll(parser.getFiveYahtzeeScores());
			scoreDatabase.sixYahtzeeScoreDao().insertAll(parser.getSixYahtzeeScores());
			scoreDatabase.balutScoreDao().insertAll(parser.getBalutScores());
			return true;
		}
		catch (FileNotFoundException e) {
			return false;
		}
	}

	/** 将得分数据导出到XML文件，导出成功则返回{@code true}，否则返回{@code false} */
	private boolean exportScores(File file) {
		try {
			ScoreDatabase scoreDatabase = ScoreDatabase.getInstance(this);
			FileOutputStream fos = new FileOutputStream(file);
			XmlSerializer serializer = Xml.newSerializer();
			serializer.setOutput(fos, "utf-8");
			serializer.startDocument("utf-8", true);
			serializer.startTag(null, "scores");

			// 5骰Yahtzee
			serializer.startTag(null, "FiveYahtzeeScores");
			for (FiveYahtzeeScore fiveYahtzeeScore : scoreDatabase.fiveYahtzeeScoreDao().findAll())
				serializer.startTag(null, "FiveYahtzeeScore")
						.attribute(null, "date", fiveYahtzeeScore.getDate())
						.attribute(null, "score", fiveYahtzeeScore.getScore().toString())
						.attribute(null, "got_bonus", fiveYahtzeeScore.getGotBonus().toString())
						.attribute(null, "got_yahtzee", fiveYahtzeeScore.getGotYahtzee().toString())
						.endTag(null, "FiveYahtzeeScore");
			serializer.endTag(null, "FiveYahtzeeScores");

			// 6骰Yahtzee
			serializer.startTag(null, "SixYahtzeeScores");
			for (SixYahtzeeScore sixYahtzeeScore : scoreDatabase.sixYahtzeeScoreDao().findAll())
				serializer.startTag(null, "SixYahtzeeScore")
						.attribute(null, "date", sixYahtzeeScore.getDate())
						.attribute(null, "score", sixYahtzeeScore.getScore().toString())
						.attribute(null, "got_bonus", sixYahtzeeScore.getGotBonus().toString())
						.attribute(null, "got_yahtzee", sixYahtzeeScore.getGotYahtzee().toString())
						.endTag(null, "SixYahtzeeScore");
			serializer.endTag(null, "SixYahtzeeScores");

			// Balut
			serializer.startTag(null, "BalutScores");
			for (BalutScore balutScore : scoreDatabase.balutScoreDao().findAll())
				serializer.startTag(null, "BalutScore")
						.attribute(null, "date", balutScore.getDate())
						.attribute(null, "score", balutScore.getScore().toString())
						.attribute(null, "got_balut", balutScore.getGotBalut().toString())
						.endTag(null, "BalutScore");
			serializer.endTag(null, "BalutScores");

			serializer.endTag(null, "scores");
			serializer.endDocument();
			fos.close();
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 解析命令行并执行相应操作。命令列表：
	 * <table border="1">
	 * <tr><td>{@code ImportScores}</td><td>导入得分数据</td></tr>
	 * <tr><td>{@code ExportScores}</td><td>导出得分数据</td></tr>
	 * <tr><td>{@code ClearScores}</td><td>清除得分数据</td></tr>
	 * <tr><td>{@code Author}</td><td>显示作者</td></tr>
	 * <tr><td>{@code Test}</td><td>打开测试界面</td></tr>
	 * <tr><td>{@code Help}</td><td>显示命令列表</td></tr>
	 * </table>
	 * 其他由{@link GameFragment#executeCmd(String, String[])}执行
	 */
	private void parseCmd(String cmd) {
		if (cmd.trim().isEmpty())
			return;
		String[] words = cmd.trim().split("\\s+", 2);
		cmd = words[0];
		String[] args = words.length > 1 ? words[1].split("\\s+") : new String[0];
		if (cmd.equals("ImportScores") && args.length == 0) {
			File file = new File(getExternalFilesDir(null), SCORES_FILENAME);
			if (importScores(file))
				Toast.makeText(this, R.string.importScoresSuccess, Toast.LENGTH_SHORT).show();
		}
		else if (cmd.equals("ExportScores") && args.length == 0) {
			File file = new File(getExternalFilesDir(null), SCORES_FILENAME);
			if (exportScores(file))
				Toast.makeText(this,
						String.format(getString(R.string.exportScoresSuccess), file.getAbsolutePath()),
						Toast.LENGTH_LONG
				).show();
		}
		else if (cmd.equals("ClearScores") && args.length == 0) {
			ScoreDatabase scoreDatabase = ScoreDatabase.getInstance(this);
			scoreDatabase.fiveYahtzeeScoreDao().deleteAll(scoreDatabase.fiveYahtzeeScoreDao().findAll());
			scoreDatabase.sixYahtzeeScoreDao().deleteAll(scoreDatabase.sixYahtzeeScoreDao().findAll());
			scoreDatabase.balutScoreDao().deleteAll(scoreDatabase.balutScoreDao().findAll());
		}
		else if (cmd.equals("Author") && args.length == 0)
			Toast.makeText(this, getString(R.string.zzy), Toast.LENGTH_SHORT).show();
		else if (cmd.equals("Test") && args.length == 0)
			startActivity(new Intent(this, TestActivity.class));
		else if (cmd.equals("Help") && args.length == 0)
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.cmd)
					.setTitle(R.string.cmdList)
					.setItems(R.array.cmdList, null)
					.setPositiveButton(R.string.ok, null)
					.create().show();
		else
			mGameFragment.executeCmd(cmd, args);
	}

}
