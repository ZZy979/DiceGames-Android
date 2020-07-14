package com.zzy.dicegames.fragment.highscores;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;

/**
 * 展示Farkle统计数据的Fragment<br>
 * 传入的参数：
 * <ul>
 *     <li>{@link #GAMES_PLAYED}：一个整数，局数</li>
 *     <li>{@link #WON_GAMES}：一个整数，获胜局数</li>
 * </ul>
 *
 * @author 赵正阳
 */
public class FarkleHighScoresFragment extends Fragment {
	// ----------传入参数----------
	/** 传入参数：局数 */
	public static final String GAMES_PLAYED = "gamesPlayed";

	/** 传入参数：获胜的局数 */
	public static final String WON_GAMES = "wonGames";

	public FarkleHighScoresFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_farkle_high_scores, container, false);
		Bundle bundle = getArguments();
		int gamesPlayed = bundle.getInt(GAMES_PLAYED, 0);
		int wonGames = bundle.getInt(WON_GAMES, 0);
		((TextView) rootView.findViewById(R.id.tvGamesPlayed)).setText(String.valueOf(gamesPlayed));
		((TextView) rootView.findViewById(R.id.tvWonGames)).setText(String.valueOf(wonGames));
		((TextView) rootView.findViewById(R.id.tvWinRate)).setText(
				gamesPlayed == 0 ? "-" : String.format("%.2f%%", (double) wonGames / gamesPlayed * 100.0));
		return rootView;
	}

}
