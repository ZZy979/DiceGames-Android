package com.zzy.dicegames.ui.game.yahtzee;

/** 得分项的保留方案：要保留的骰子及其优先级，优先级越大越值得保留 */
public record KeepPlan(boolean[] keep, int rank) {
}
