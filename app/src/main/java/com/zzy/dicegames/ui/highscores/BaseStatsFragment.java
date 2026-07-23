package com.zzy.dicegames.ui.highscores;

import android.os.Bundle;
import android.view.View;

import com.zzy.dicegames.data.ScoreDatabase;

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
}
