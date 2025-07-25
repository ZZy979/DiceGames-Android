package com.zzy.dicegames.ui.game;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

/** 计分板Fragment基类 */
public abstract class BaseScoreBoardFragment<V extends BaseScoreBoardViewModel> extends Fragment {
    protected V mViewModel;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = createViewModel();
        initViews(view);
        setObservers();
    }

    protected abstract V createViewModel();

    protected abstract void setObservers();

    protected abstract void initViews(View rootView);

    /** 重置计分板 */
    public void reset() {
        mViewModel.reset();
    }
}
