package com.zzy.dicegames.ui.dice;

import org.jetbrains.annotations.NotNull;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class RollDiceViewModelFactory implements ViewModelProvider.Factory {
    private int numDice;
    private int maxRolls;

    public RollDiceViewModelFactory(int numDice, int maxRolls) {
        this.numDice = numDice;
        this.maxRolls = maxRolls;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RollDiceViewModel.class))
            return (T) new RollDiceViewModel(numDice, maxRolls);
        throw new IllegalArgumentException("This factory can't create " + modelClass);
    }
}
