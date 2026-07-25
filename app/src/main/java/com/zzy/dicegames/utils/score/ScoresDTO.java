package com.zzy.dicegames.utils.score;

import com.zzy.dicegames.data.entity.BalutScore;
import com.zzy.dicegames.data.entity.FarkleScore;
import com.zzy.dicegames.data.entity.FiveYahtzeeScore;
import com.zzy.dicegames.data.entity.SixYahtzeeScore;

import java.util.ArrayList;
import java.util.List;

/** 导入/导出得分数据的中间结果 */
public class ScoresDTO {
    public List<FiveYahtzeeScore> fiveYahtzeeScores;
    public List<SixYahtzeeScore> sixYahtzeeScores;
    public List<BalutScore> balutScores;
    public List<FarkleScore> farkleScores;

    public ScoresDTO() {
        this(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>())
        ;
    }

    public ScoresDTO(
            List<FiveYahtzeeScore> fiveYahtzeeScores,
            List<SixYahtzeeScore> sixYahtzeeScores,
            List<BalutScore> balutScores,
            List<FarkleScore> farkleScores) {
        this.fiveYahtzeeScores = fiveYahtzeeScores;
        this.sixYahtzeeScores = sixYahtzeeScores;
        this.balutScores = balutScores;
        this.farkleScores = farkleScores;
    }
}
