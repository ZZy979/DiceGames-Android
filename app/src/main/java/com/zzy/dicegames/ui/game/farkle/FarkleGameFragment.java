package com.zzy.dicegames.ui.game.farkle;

import android.os.Bundle;

import com.zzy.dicegames.R;
import com.zzy.dicegames.data.ScoreDatabase;
import com.zzy.dicegames.data.entity.FarkleScore;
import com.zzy.dicegames.ui.dice.RollDiceFragment;
import com.zzy.dicegames.ui.game.BaseGameFragment;
import com.zzy.dicegames.ui.game.BaseGameViewModel;

/**
 * Farkle游戏Fragment
 *
 * @author 赵正阳
 */
// fixme 暂时不可用，实现一个FarkleRollDiceFragment类
public class FarkleGameFragment extends BaseGameFragment<BaseGameViewModel> {

    public FarkleGameFragment() {}

    // 该方法被调用时DiceFragment.onCreateView()已被调用
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null)
            mRollDiceFragment.setLeftRollTimes(0);
    }

//    @Override
    public FarkleScoreBoardFragment createScoreBoardFragment() {
        return new FarkleScoreBoardFragment();
    }

//    @Override
    protected void setListeners() {
//        mScoreBoardFragment.setDiceFragment(mRollDiceFragment);
//        mScoreBoardFragment.setGameOverAction(this::onGameOver);
//        mRollDiceFragment.setRollListener(mScoreBoardFragment::onDiceRolled);
    }

    @Override
    protected RollDiceFragment createRollDiceFragment() {
        return null;
    }

    @Override
    protected BaseGameViewModel createViewModel() {
        return null;
    }

    //    @Override
    public int getDiceCount() {
        return 6;
    }

//    @Override
    public int getRollTimes() {
        return 0;
    }

    @Override
    public void startNewGame() {
        super.startNewGame();
        mRollDiceFragment.activate();
        mRollDiceFragment.setLeftRollTimes(0);
    }

    /** 游戏结束时的回调函数 */
    private void onGameOver(FarkleScore score) {
        ScoreDatabase.getInstance(getContext()).farkleScoreDao().insert(score);
    }

}
