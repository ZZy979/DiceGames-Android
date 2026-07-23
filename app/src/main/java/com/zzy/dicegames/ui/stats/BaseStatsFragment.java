package com.zzy.dicegames.ui.stats;

import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.BaseScore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

/**
 * 游戏统计数据Fragment基类
 *
 * @author 赵正阳
 */
public abstract class BaseStatsFragment extends Fragment {
    protected ScoreDatabase mScoreDatabase;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mScoreDatabase = ScoreDatabase.getInstance(getContext());
        initViews(view);
        setupObservers(getViewLifecycleOwner());
    }

    /** 初始化视图 */
    protected abstract void initViews(View view);

    /** 设置LiveData观察者 */
    protected abstract void setupObservers(LifecycleOwner owner);

    protected ListAdapter createHighScoresListAdapter(List<? extends BaseScore> highScores) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < highScores.size(); ++i) {
            Map<String, Object> map = new HashMap<>();
            map.put("rank", i + 1);
            map.put("score", highScores.get(i).score);
            map.put("date", highScores.get(i).date);
            data.add(map);
        }
        return new SimpleAdapter(
                getContext(), data, R.layout.high_score_item,
                new String[] {"rank", "score", "date"},
                new int[] {R.id.tvRank, R.id.tvScore, R.id.tvDate}
        );
    }
}
