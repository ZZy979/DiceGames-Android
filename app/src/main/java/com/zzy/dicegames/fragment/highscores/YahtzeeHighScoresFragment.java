package com.zzy.dicegames.fragment.highscores;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.database.entity.BaseScore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 展示5骰Yahtzee/6骰Yahtzee最高分和统计数据的Fragment<br>
 * 传入的参数：
 * <ul>
 *     <li>{@link #TOP10_SCORE}：一个{@code List<BaseScore>}，前10名得分</li>
 *     <li>{@link #GAMES_PLAYED}：一个整数，局数</li>
 *     <li>{@link #MAX_SCORE}：一个整数，最高分</li>
 *     <li>{@link #MIN_SCORE}：一个整数，最低分</li>
 *     <li>{@link #AVERAGE_SCORE}：一个double值，平均分</li>
 *     <li>{@link #GOT_BONUS}：一个整数，得到奖励分的次数</li>
 *     <li>{@link #GOT_YAHTZEE}：一个整数，得到Yahtzee的次数</li>
 * </ul>
 *
 * @author 赵正阳
 */
public class YahtzeeHighScoresFragment extends Fragment {
	// ----------传入参数----------
	/** 传入参数：前10名得分 */
	public static final String TOP10_SCORE = "top10Score";

	/** 传入参数：局数 */
	public static final String GAMES_PLAYED = "gamesPlayed";

	/** 传入参数：最高分 */
	public static final String MAX_SCORE = "maxScore";

	/** 传入参数：最低分 */
	public static final String MIN_SCORE = "minScore";

	/** 传入参数：平均分 */
	public static final String AVERAGE_SCORE = "averageScore";

	/** 传入参数：得到奖励分的次数 */
	public static final String GOT_BONUS = "gotBonus";

	/** 传入参数：得到Yahtzee的次数 */
	public static final String GOT_YAHTZEE = "gotYahtzee";

	public YahtzeeHighScoresFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_yahtzee_high_scores, container, false);
		Bundle bundle = getArguments();

		List<BaseScore> highScores = (List<BaseScore>) bundle.getSerializable(TOP10_SCORE);
		if (highScores != null && !highScores.isEmpty()) {
			List<Map<String, Object>> data = new ArrayList<>();
			for (int i = 0; i < highScores.size(); ++i) {
				Map<String, Object> map = new HashMap<>();
				map.put("rank", i + 1);
				map.put("score", highScores.get(i).getScore());
				map.put("date", highScores.get(i).getDate());
				data.add(map);
			}
			((ListView) rootView.findViewById(R.id.lvHighScores)).setAdapter(new SimpleAdapter(
					getContext(), data, R.layout.high_score_item,
					new String[] {"rank", "score", "date"},
					new int[] {R.id.tvRank, R.id.tvScore, R.id.tvDate}
			));
			rootView.findViewById(R.id.tvNothing).setVisibility(View.GONE);
		}
		else
			rootView.findViewById(R.id.lvHighScores).setVisibility(View.GONE);

		int gamesPlayed = bundle.getInt(GAMES_PLAYED, 0);
		((TextView) rootView.findViewById(R.id.tvGamesPlayed)).setText(String.valueOf(gamesPlayed));
		((TextView) rootView.findViewById(R.id.tvMaxScore)).setText(String.valueOf(bundle.getInt(MAX_SCORE, 0)));
		((TextView) rootView.findViewById(R.id.tvMinScore)).setText(String.valueOf(bundle.getInt(MIN_SCORE, 0)));
		((TextView) rootView.findViewById(R.id.tvAverageScore)).setText(String.format("%.2f", bundle.getDouble(AVERAGE_SCORE, 0.00)));

		int gotBonus = bundle.getInt(GOT_BONUS, 0);
		((TextView) rootView.findViewById(R.id.tvGotBonus)).setText(
				gamesPlayed == 0 ? "-"
						: String.format("%.2f%% (%d/%d)", (double) gotBonus / gamesPlayed * 100.0, gotBonus, gamesPlayed)
		);

		int gotYahtzee = bundle.getInt(GOT_YAHTZEE, 0);
		((TextView) rootView.findViewById(R.id.tvGotYahtzee)).setText(
				gamesPlayed == 0 ? "-"
						: String.format("%.2f%% (%d/%d)", (double) gotYahtzee / gamesPlayed * 100.0, gotYahtzee, gamesPlayed)
		);

		return rootView;
	}

}
