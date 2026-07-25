package com.zzy.dicegames.ui;

import com.zzy.dicegames.common.GameType;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    /** 当前游戏类型 */
    private final MutableLiveData<GameType> gameType = new MutableLiveData<>(GameType.FIVE_YAHTZEE);

    public LiveData<GameType> getGameType() {
        return gameType;
    }

    public void changeGameType(int index) {
        if (index < 0 || index >= GameType.values().length)
            return;
        gameType.setValue(GameType.values()[index]);
    }
}
