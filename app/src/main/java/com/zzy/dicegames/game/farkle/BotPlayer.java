package com.zzy.dicegames.game.farkle;

import com.zzy.dicegames.widget.Dice;

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
			mDiceFragment.getDice()[i].callOnClick();
		if (mScoreSupplier.getAsInt() + mCurrentTurnScoreSupplier.getAsInt() >= 10000
				|| mCurrentTurnScoreSupplier.getAsInt() >= 500
				|| Arrays.stream(mDiceFragment.getDice()).filter(Dice::isLocked).count() >= 4)
			// 为了让锁定骰子的效果立即显示，在新线程中点击Bank或Roll按钮
			// 因此需要将这两个按钮的监听器设置为在UI线程中运行
			new Thread(() -> {
				delay(1);
				mBankScoreAction.run();
			}).start();
		else
			new Thread(() -> {
				delay(1);
				mDiceFragment.getRollButton().callOnClick();
			}).start();
	}

	@Override
	public void onHotDice() {
		new Thread(() -> {
			delay(2);
			mDiceFragment.getRollButton().callOnClick();
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
