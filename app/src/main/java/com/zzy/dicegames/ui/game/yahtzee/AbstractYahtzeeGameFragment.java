package com.zzy.dicegames.ui.game.yahtzee;

import com.zzy.dicegames.data.entity.AbstractYahtzeeScore;
import com.zzy.dicegames.ui.game.GameFragment;

/**
 * Yahtzee游戏Fragment
 *
 * @author 赵正阳
 */
public abstract class AbstractYahtzeeGameFragment extends GameFragment<AbstractYahtzeeScoreBoardFragment> {

    public AbstractYahtzeeGameFragment() {}

    @Override
    protected void setListeners() {
        mRollDiceFragment.setRollListener(mScoreBoardFragment::updateScores);
        mScoreBoardFragment.setSelectAction(mRollDiceFragment::activate);
        mScoreBoardFragment.setGameOverAction(this::onGameOver);
    }

    @Override
    public void startNewGame() {
        mScoreBoardFragment.reset();
        mRollDiceFragment.activate();
    }

    /**
     * 游戏结束时的回调函数，保存得分并开始新游戏<br>
     * 将该方法设置为计分板的监听器，游戏结束时计分板将以本局得分为参数调用该监听器
     */
    protected void onGameOver(AbstractYahtzeeScore score) {
        int rank = saveScore(score);
        showScore(score.getScore(), rank);
    }

    /** 保存得分，返回该得分在前10名中的名次，0表示不在前10名中 */
    protected abstract int saveScore(AbstractYahtzeeScore score);

}
