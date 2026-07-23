package com.zzy.dicegames.ui.highscores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.YahtzeeStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

/**
 * Yahtzee统计数据Fragment
 *
 * @author 赵正阳
 */
public abstract class BaseYahtzeeStatsFragment extends BaseStatsFragment {
    /** 最高分列表 */
    private ListView mHighScoresListView;

    /** 最高分为空时的占位符标签 */
    private TextView mNothingTextView;

    /** 局数标签 */
    private TextView mGamesPlayedTextView;

    /** 最高分标签 */
    private TextView mMaxScoreTextView;

    /** 最低分标签 */
    private TextView mMinScoreTextView;

    /** 平均分标签 */
    private TextView mAverageScoreTextView;

    /** 得到奖励分次数标签 */
    private TextView mGotBonusTextView;

    /** 得到Yahtzee次数标签 */
    private TextView mGotYahtzeeTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_yahtzee_stats, container, false);
    }

    @Override
    protected void initViews(View view) {
        mHighScoresListView = view.findViewById(R.id.lvHighScores);
        mNothingTextView = view.findViewById(R.id.tvNothing);
        mGamesPlayedTextView = view.findViewById(R.id.tvGamesPlayed);
        mMaxScoreTextView = view.findViewById(R.id.tvMaxScore);
        mMinScoreTextView = view.findViewById(R.id.tvMinScore);
        mAverageScoreTextView = view.findViewById(R.id.tvAverageScore);
        mGotBonusTextView = view.findViewById(R.id.tvGotBonus);
        mGotYahtzeeTextView = view.findViewById(R.id.tvGotYahtzee);
    }

    @Override
    protected void setupObservers(LifecycleOwner owner) {
        getHighScores().observe(owner, this::onHighScoresChanged);
        getStatistics().observe(owner, this::onStatisticsChanged);
    }

    /** 返回最高分列表 */
    protected abstract LiveData<? extends List<? extends BaseScore>> getHighScores();

    /** 返回统计数据 */
    protected abstract LiveData<YahtzeeStatistics> getStatistics();

    /** 最高分列表更新时的回调 */
    private void onHighScoresChanged(List<? extends BaseScore> highScores) {
        if (highScores.isEmpty()) {
            mHighScoresListView.setVisibility(View.GONE);
            mNothingTextView.setVisibility(View.VISIBLE);
        }
        else {
            mHighScoresListView.setVisibility(View.VISIBLE);
            mNothingTextView.setVisibility(View.GONE);

            List<Map<String, Object>> data = new ArrayList<>();
            for (int i = 0; i < highScores.size(); ++i) {
                Map<String, Object> map = new HashMap<>();
                map.put("rank", i + 1);
                map.put("score", highScores.get(i).score);
                map.put("date", highScores.get(i).date);
                data.add(map);
            }
            mHighScoresListView.setAdapter(new SimpleAdapter(
                    getContext(), data, R.layout.high_score_item,
                    new String[] {"rank", "score", "date"},
                    new int[] {R.id.tvRank, R.id.tvScore, R.id.tvDate}
            ));
        }
    }

    /** 统计数据更新时的回调 */
    private void onStatisticsChanged(YahtzeeStatistics stats) {
        mGamesPlayedTextView.setText(Integer.toString(stats.count));
        mMaxScoreTextView.setText(Integer.toString(stats.maxScore));
        mMinScoreTextView.setText(Integer.toString(stats.minScore));
        mAverageScoreTextView.setText(String.format("%.2f", stats.avgScore));
        mGotBonusTextView.setText(stats.count == 0 ? "-" : String.format(
                "%.2f%% (%d/%d)", (double) stats.numBonus / stats.count * 100, stats.numBonus, stats.count));
        mGotYahtzeeTextView.setText(stats.count == 0 ? "-" : String.format(
                "%.2f%% (%d/%d)", (double) stats.numYahtzee / stats.count * 100, stats.numYahtzee, stats.count));
    }
}
