package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.common.GameType;
import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.BaseStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

/**
 * 游戏统计数据Fragment基类
 *
 * @author 赵正阳
 */
public abstract class BaseStatsFragment extends Fragment {

    protected ScoreDatabase mScoreDatabase;

    /** 最高分列表 */
    protected ListView mHighScoresListView;

    /** 最高分为空时的占位符标签 */
    protected TextView mNothingTextView;

    /** 局数标签 */
    protected TextView mGamesPlayedTextView;

    /** 最高分标签 */
    protected TextView mHighestScoreTextView;

    /** 最低分标签 */
    protected TextView mLowestScoreTextView;

    /** 平均分标签 */
    protected TextView mAverageScoreTextView;

    /** 根据游戏类型创建统计数据Fragment */
    public static BaseStatsFragment createByGameType(GameType gameType) {
        return switch (gameType) {
            case YAHTZEE -> new YahtzeeStatsFragment();
            case MAXI_YATZY -> new MaxiYatzyStatsFragment();
            case BALUT -> new BalutStatsFragment();
            case FARKLE -> new FarkleStatsFragment();
            default -> null;
        };
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mScoreDatabase = ScoreDatabase.getInstance(getContext());
        initViews(view);
        setupObservers(getViewLifecycleOwner());
    }

    /** 初始化视图 */
    protected void initViews(View view) {
        mHighScoresListView = view.findViewById(R.id.lvHighScores);
        mNothingTextView = view.findViewById(R.id.tvNothing);
        mGamesPlayedTextView = view.findViewById(R.id.tvGamesPlayed);
        mHighestScoreTextView = view.findViewById(R.id.tvHighestScore);
        mLowestScoreTextView = view.findViewById(R.id.tvLowestScore);
        mAverageScoreTextView = view.findViewById(R.id.tvAverageScore);
    }

    /** 返回最高分列表 */
    protected abstract LiveData<? extends List<? extends BaseScore>> getHighScores();

    /** 返回统计数据 */
    protected abstract LiveData<? extends BaseStatistics> getStatistics();

    /** 设置LiveData观察者 */
    protected void setupObservers(LifecycleOwner owner) {
        getHighScores().observe(owner, this::onHighScoresChanged);
        getStatistics().observe(owner, this::onStatisticsChanged);
    }

    /** 最高分列表更新时的回调 */
    protected void onHighScoresChanged(List<? extends BaseScore> highScores) {
        if (highScores.isEmpty()) {
            mHighScoresListView.setVisibility(View.GONE);
            mNothingTextView.setVisibility(View.VISIBLE);
        }
        else {
            mHighScoresListView.setVisibility(View.VISIBLE);
            mNothingTextView.setVisibility(View.GONE);
            mHighScoresListView.setAdapter(createHighScoresListAdapter(highScores));
        }
    }

    /** 统计数据更新时的回调 */
    protected void onStatisticsChanged(BaseStatistics stats) {
        mGamesPlayedTextView.setText(Integer.toString(stats.count));
        mHighestScoreTextView.setText(Integer.toString(stats.maxScore));
        mLowestScoreTextView.setText(Integer.toString(stats.minScore));
        mAverageScoreTextView.setText(String.format("%.2f", stats.avgScore));
    }

    protected ListAdapter createHighScoresListAdapter(List<? extends BaseScore> highScores) {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(Map.of(
                "rank", getString(R.string.rank),
                "score", getString(R.string.score),
                "date", getString(R.string.date)));
        for (int i = 0; i < highScores.size(); ++i) {
            data.add(Map.of(
                    "rank", i + 1,
                    "score", highScores.get(i).score,
                    "date", highScores.get(i).date));
        }
        return new SimpleAdapter(
                getContext(), data, R.layout.high_score_item,
                new String[] {"rank", "score", "date"},
                new int[] {R.id.tvRank, R.id.tvScore, R.id.tvDate}
        );
    }
}
