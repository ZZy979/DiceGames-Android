package com.zzy.dicegames.ui.dice;

import org.jetbrains.annotations.NotNull;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class RollDiceViewModelFactory implements ViewModelProvider.Factory {
    private int diceCount;
    private int maxRolls;

    public RollDiceViewModelFactory(int diceCount, int maxRolls) {
        this.diceCount = diceCount;
        this.maxRolls = maxRolls;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RollDiceViewModel.class))
            return (T) new RollDiceViewModel(diceCount, maxRolls);
        throw new IllegalArgumentException("This factory can't create " + modelClass);
    }
}
