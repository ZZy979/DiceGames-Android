package com.zzy.dicegames.parser;

import com.zzy.dicegames.database.entity.BalutScore;
import com.zzy.dicegames.database.entity.FiveYahtzeeScore;
import com.zzy.dicegames.database.entity.SixYahtzeeScore;

import java.util.List;

/**
 * 得分数据解析器接口
 *
 * @author 赵正阳
 */
public interface ScoresParser {

	List<FiveYahtzeeScore> parseFiveYahtzeeScores();

	List<SixYahtzeeScore> parseSixYahtzeeScores();

	List<BalutScore> parseBalutScores();

}
