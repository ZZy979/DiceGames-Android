package com.zzy.dicegames.game.farkle;

import com.zzy.dicegames.fragment.dice.DiceFragment;

import java.util.function.IntSupplier;

/**
 * Farkle玩家
 *
 * @author 赵正阳
 */
abstract class Player {
	/** 骰子窗口 */
	protected DiceFragment mDiceFragment;

	/** “获取当前得分”动作 */
	protected IntSupplier mScoreSupplier;

	/** “获取本轮预览得分”动作 */
	protected IntSupplier mCurrentTurnScoreSupplier;

	/** “保存得分”动作 */
	protected Runnable mBankScoreAction;

	/** 掷骰子后的回调函数 */
	public abstract void onDiceRolled(Result result);

	/** Hot Dice后的回调函数 */
	public abstract void onHotDice();

	public void setDiceFragment(DiceFragment diceFragment) {
		mDiceFragment = diceFragment;
	}

	public void setScoreSupplier(IntSupplier scoreSupplier) {
		mScoreSupplier = scoreSupplier;
	}

	public void setCurrentTurnScoreSupplier(IntSupplier currentTurnScoreSupplier) {
		mCurrentTurnScoreSupplier = currentTurnScoreSupplier;
	}

	public void setBankScoreAction(Runnable bankScoreAction) {
		mBankScoreAction = bankScoreAction;
	}

}
