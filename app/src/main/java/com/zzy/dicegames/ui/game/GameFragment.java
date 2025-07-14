package com.zzy.dicegames.ui.game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzy.dicegames.R;
import com.zzy.dicegames.ui.dice.RollDiceFragment;

import androidx.fragment.app.Fragment;

/**
 * 游戏Fragment基类，有一个{@link RollDiceFragment 骰子窗口}和一个计分板
 *
 * @param <T> 计分板Fragment类
 * @author 赵正阳
 */
public abstract class GameFragment<T extends Fragment> extends Fragment {
    /** 计分板 */
    protected T mScoreBoardFragment;

    /** 骰子窗口 */
    protected RollDiceFragment mRollDiceFragment;

    public GameFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);

        ((TextView) rootView.findViewById(R.id.tvTitle)).setText(getTitle());

        if (savedInstanceState == null) {
            mScoreBoardFragment = createScoreBoardFragment();
            getChildFragmentManager().beginTransaction()
                    .add(R.id.scoreBoardFragment, mScoreBoardFragment)
                    .commit();

            mRollDiceFragment = RollDiceFragment.newInstance(getDiceCount(), getRollTimes(), rollOnStart());
            getChildFragmentManager().beginTransaction()
                    .add(R.id.diceFragment, mRollDiceFragment)
                    .commit();
        }
        else {
            mScoreBoardFragment = (T) getChildFragmentManager().findFragmentById(R.id.scoreBoardFragment);
            mRollDiceFragment = (RollDiceFragment) getChildFragmentManager().findFragmentById(R.id.diceFragment);
        }

        setListeners();
        return rootView;
    }

    /** 创建一个新的计分板 */
    public abstract T createScoreBoardFragment();

    /** 设置计分板和骰子窗口相关的监听器 */
    protected abstract void setListeners();

    /** 返回游戏标题 */
    public abstract String getTitle();

    /** 返回游戏使用的骰子个数 */
    public abstract int getDiceCount();

    /** 返回游戏每轮掷骰子次数 */
    public abstract int getRollTimes();

    /** 初次创建骰子窗口后是否立即掷骰子 */
    public boolean rollOnStart() {
        return true;
    }

    /** 开始一次新游戏 */
    public void startNewGame() {
        // fixme 目前开始新游戏时无法正确更新得分，因为掷骰子时计分板还未初始化完成
        // TODO 计分板改为使用ViewModel
        mScoreBoardFragment = createScoreBoardFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.scoreBoardFragment, mScoreBoardFragment)
                .commit();
        setListeners();
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
