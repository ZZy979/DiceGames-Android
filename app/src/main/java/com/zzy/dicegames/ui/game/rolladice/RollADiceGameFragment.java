package com.zzy.dicegames.ui.game.rolladice;

import com.zzy.dicegames.R;
import com.zzy.dicegames.ui.dice.RollDiceViewModel;
import com.zzy.dicegames.ui.game.GameFragment;

/**
 * 掷骰子游戏Fragment
 *
 * @author 赵正阳
 */
public class RollADiceGameFragment extends GameFragment<RollADiceScoreBoardFragment> {

    public RollADiceGameFragment() {}

    @Override
    public RollADiceScoreBoardFragment createScoreBoardFragment() {
        return new RollADiceScoreBoardFragment();
    }

    @Override
    protected void setListeners() {
        // fixme 暂时无效
        mScoreBoardFragment.setActionOnChangingDiceCount(mRollDiceFragment::setDiceCount);
    }

    @Override
    public String getTitle() {
        return getString(R.string.rollADice);
    }

    @Override
    public int getDiceCount() {
        return 6;
    }

    @Override
    public int getRollTimes() {
        return RollDiceViewModel.UNLIMITED_ROLLS;
    }

    @Override
    public boolean rollOnStart() {
        return false;
    }

    @Override
    public void startNewGame() {
        super.startNewGame();
        mRollDiceFragment.activate();
    }

}
