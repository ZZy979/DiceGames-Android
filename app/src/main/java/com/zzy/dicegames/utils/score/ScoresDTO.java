package com.zzy.dicegames.utils.score;

import com.zzy.dicegames.data.entity.balut.BalutScore;
import com.zzy.dicegames.data.entity.farkle.FarkleScore;
import com.zzy.dicegames.data.entity.liarsdice.LiarsDiceScore;
import com.zzy.dicegames.data.entity.yahtzee.YahtzeeScore;
import com.zzy.dicegames.data.entity.yatzy.MaxiYatzyScore;

import java.util.ArrayList;
import java.util.List;

/** 导入/导出得分数据的中间结果 */
public class ScoresDTO {
    public List<YahtzeeScore> yahtzeeScores;
    public List<MaxiYatzyScore> maxiYatzyScores;
    public List<BalutScore> balutScores;
    public List<LiarsDiceScore> liarsDiceScores;
    public List<FarkleScore> farkleScores;

    public ScoresDTO() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public ScoresDTO(
            List<YahtzeeScore> yahtzeeScores,
            List<MaxiYatzyScore> maxiYatzyScores,
            List<BalutScore> balutScores,
            List<LiarsDiceScore> liarsDiceScores,
            List<FarkleScore> farkleScores) {
        this.yahtzeeScores = yahtzeeScores;
        this.maxiYatzyScores = maxiYatzyScores;
        this.balutScores = balutScores;
        this.liarsDiceScores = liarsDiceScores;
        this.farkleScores = farkleScores;
    }
}
