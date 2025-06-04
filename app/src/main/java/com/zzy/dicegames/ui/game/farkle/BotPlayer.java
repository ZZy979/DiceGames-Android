package com.zzy.dicegames.ui.game.farkle;

import com.zzy.dicegames.ui.dice.DiceView;

import java.util.Arrays;

/**
 * 电脑玩家
 *
 * @author 赵正阳
 */
class BotPlayer extends Player {

    @Override
    public void onDiceRolled(Result result) {
        delay(1);
        for (int i : result.getScoringDiceIndices())
            mRollDiceFragment.getDice()[i].callOnClick();
        if (mCurrentTurnScoreSupplier.getAsInt() >= 500
                || Arrays.stream(mRollDiceFragment.getDice()).filter(DiceView::isLocked).count() >= 4)
            // 为了让锁定骰子的效果立即显示，在新线程中点击Bank或Roll按钮
            // 因此需要将这两个按钮的监听器设置为在UI线程中运行
            new Thread(() -> {
                delay(1);
                mBankScoreAction.run();
            }).start();
        else
            new Thread(() -> {
                delay(1);
                mRollDiceFragment.getRollButton().callOnClick();
            }).start();
    }

    @Override
    public void onHotDice() {
        new Thread(() -> {
            delay(2);
            mRollDiceFragment.getRollButton().callOnClick();
        }).start();
    }

    private void delay(int sec) {
        try {
            Thread.sleep(sec * 1000);
        }
        catch (InterruptedException ignored) {
        }
    }

}
