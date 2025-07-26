package com.zzy.dicegames.ui.game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.ui.dice.RollDiceFragment;

import androidx.fragment.app.Fragment;

/**
 * 游戏Fragment基类，有一个{@link RollDiceFragment 骰子窗口}和一个计分板
 *
 * @param <V> ViewModel类
 * @author 赵正阳
 */
public abstract class BaseGameFragment<V extends BaseGameViewModel> extends Fragment {
    /** 标题标签*/
    protected TextView mTitleTextView;

    /** 骰子窗口 */
    protected RollDiceFragment mRollDiceFragment;

    protected V mViewModel;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTitleTextView = view.findViewById(R.id.tvTitle);
        if (savedInstanceState == null) {
            mRollDiceFragment = createRollDiceFragment();
            getChildFragmentManager().beginTransaction()
                    .add(R.id.diceFragment, mRollDiceFragment)
                    .commit();
        }
        else {
            mRollDiceFragment = (RollDiceFragment) getChildFragmentManager().findFragmentById(R.id.diceFragment);
        }

        mViewModel = createViewModel();
    }

    /** 返回游戏标题 */
    public String getTitle() {
        return mTitleTextView.getText().toString();
    }

    /** 创建骰子窗口 */
    protected abstract RollDiceFragment createRollDiceFragment();

    /** 创建游戏ViewModel */
    protected abstract V createViewModel();

    /** 开始一次新游戏 */
    public void startNewGame() {
        mViewModel.reset();
        mRollDiceFragment.activate();
    }

    /**
     * 根据名次返回对应的文字“第rank名”
     *
     * @throws IllegalArgumentException 如果rank<=0
     */
    public String getRankText(int rank) {
        if (rank <= 0)
            throw new IllegalArgumentException("名次必须大于0");
        String lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
        String arg;
        if (lang.equals("en")) {
            if (rank % 10 == 1 && rank != 11)
                arg = String.format("%dst", rank);
            else if (rank % 10 == 2 && rank != 12)
                arg = String.format("%dnd", rank);
            else if (rank % 10 == 3 && rank != 13)
                arg = String.format("%drd", rank);
            else
                arg = String.format("%dth", rank);
        }
        else
            arg = String.valueOf(rank);
        return String.format(getString(R.string.nthPlace), arg);
    }

    /**
     * 弹出窗口，显示得分
     *
     * @param score 得分
     * @param rank  排名
     */
    public void showScore(int score, int rank) {
        String honor;
        if (rank == 1)
            honor = getString(R.string.newHighScore);
        else if (rank >= 2 && rank <= 10)
            honor = getRankText(rank);
        else
            honor = getString(R.string.score);
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.gameOver))
                .setMessage(String.format("%s: %d", honor, score))
                .setPositiveButton(R.string.ok, (dialog, which) -> startNewGame())
                .show();
    }

}
