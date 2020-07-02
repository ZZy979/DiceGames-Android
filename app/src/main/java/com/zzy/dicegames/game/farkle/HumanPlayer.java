package com.zzy.dicegames.game.farkle;

/**
 * 人类玩家，只需等待操作
 *
 * @author 赵正阳
 */
class HumanPlayer extends Player {

	@Override
	public void onDiceRolled(Result result) {}

	@Override
	public void onHotDice() {}

}
