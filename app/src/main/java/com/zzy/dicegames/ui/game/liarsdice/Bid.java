package com.zzy.dicegames.ui.game.liarsdice;

/**
 * 叫数，由数量、点数和是否斋组成
 *
 * @param quantity 数量
 * @param face     点数，1~6
 * @param zhai     是否斋（1不作为万能）
 */
public record Bid(int quantity, int face, boolean zhai) {
}
