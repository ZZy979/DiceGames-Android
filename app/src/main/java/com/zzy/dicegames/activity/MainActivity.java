package com.zzy.dicegames.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.zzy.dicegames.R;
import com.zzy.dicegames.gamefragment.BalutFragment;
import com.zzy.dicegames.gamefragment.FiveYahtzeeFragment;
import com.zzy.dicegames.gamefragment.GameFragment;
import com.zzy.dicegames.gamefragment.RollADiceFragment;
import com.zzy.dicegames.gamefragment.SixYahtzeeFragment;

import java.lang.ref.WeakReference;
import java.util.Date;

public class MainActivity extends Activity {
	private String[] mGameTypes;

	private GameFragment mGameFragment;

	/** 上次按返回键的时间 */
	private long mLastPressTime = 0;

	// -------------------------消息处理-------------------------
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
			switch (msg.what) {
			case MSG_SUBMIT_CMD:
				activity.parseCmd((String) msg.obj);
				break;
			}
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
			mGameFragment = (GameFragment) getFragmentManager().findFragmentById(R.id.gameFragment);

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
			intent.putExtra(HelpActivity.GAME_TYPE_CODE, mGameFragment.getGameTypeCode());
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
		return super.onOptionsItemSelected(item);
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

	private void changeGameType(String title) {
		if (mGameFragment.getTitle().equals(title))
			mGameFragment.startNewGame();
		else {
			GameFragment newGameFragment = null;
			if (title.equals(getString(R.string.fiveYahtzee)))
				newGameFragment = new FiveYahtzeeFragment();
			else if (title.equals(getString(R.string.sixYahtzee)))
				newGameFragment = new SixYahtzeeFragment();
			else if (title.equals(getString(R.string.balut)))
				newGameFragment = new BalutFragment();
			else if (title.equals(getString(R.string.liarDice)))
				;
			else if (title.equals(getString(R.string.rollADice)))
				newGameFragment = new RollADiceFragment();

			if (newGameFragment != null) {
				mGameFragment = newGameFragment;
				getFragmentManager().beginTransaction()
						.replace(R.id.gameFragment, mGameFragment)
						.commit();
			}
		}
	}

	/**
	 * 解析命令行并执行相应操作。命令列表：
	 * <table border="1">
	 * <tr><td>{@code Author}</td><td>显示作者</td></tr>
	 * <tr><td>{@code Test}</td><td>打开测试界面</td></tr>
	 * <tr><td>{@code Help}</td><td>显示命令列表</td></tr>
	 * </table>
	 * 其他由{@link GameFragment#executeCmd(String)}执行
	 */
	private void parseCmd(String cmd) {
		String parsedCmd = cmd.trim();
		if (parsedCmd.isEmpty())
			return;
		String[] words = parsedCmd.split("\\s+", 2);
		parsedCmd = words[0];
		String[] args = words.length > 1 ? words[1].split("\\s+") : new String[0];
		if (parsedCmd.equals("Author") && args.length == 0)
			Toast.makeText(this, getString(R.string.zzy), Toast.LENGTH_SHORT).show();
		else if (parsedCmd.equals("Test") && args.length == 0)
			startActivity(new Intent(this, TestActivity.class));
		else if (parsedCmd.equals("Help") && args.length == 0)
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.cmd)
					.setTitle(R.string.cmdList)
					.setItems(R.array.cmdList, null)
					.setPositiveButton(R.string.ok, null)
					.create().show();
		else
			mGameFragment.executeCmd(cmd);
	}

}
