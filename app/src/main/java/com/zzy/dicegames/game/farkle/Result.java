package com.zzy.dicegames.game.farkle;

import java.util.List;

/**
 * {@link FarkleScoreBoardFragment#calcScore 计算得分的方法}返回的结果<br>
 * <ul>
 *     <li>{@code score}：最大可能的得分</li>
 *     <li>{@code scoringDiceIndices}：得分的骰子下标列表</li>
 * </ul>
 *
 * @author 赵正阳
 */
public class Result {
	private int score;

	private List<Integer> scoringDiceIndices;

	public Result(int score, List<Integer> scoringDiceIndices) {
		this.score = score;
		this.scoringDiceIndices = scoringDiceIndices;
	}

	public int getScore() {
		return score;
	}

	public List<Integer> getScoringDiceIndices() {
		return scoringDiceIndices;
	}

}
