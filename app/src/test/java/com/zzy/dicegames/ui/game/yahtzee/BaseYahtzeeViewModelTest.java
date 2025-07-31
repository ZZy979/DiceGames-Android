package com.zzy.dicegames.ui.game.yahtzee;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BaseYahtzeeViewModelTest {
   @Rule
   public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

   private BaseYahtzeeViewModel viewModel;

   @Before
   public void setUp() {
      viewModel = new BaseYahtzeeViewModel(5, 3, 10, 20, 8) {
         @Override
         public int calculateScore(int category) {
            return sumOfDice;
         }
      };
   }

   @Test
   public void testInitialization() {
      assertEquals(10, viewModel.getNumCategories());
      assertEquals(20, viewModel.getBonusThreshold());
      assertEquals(8, viewModel.getBonusValue());
      assertArrayEquals(new int[10], viewModel.getScores().getValue());
      assertArrayEquals(new boolean[10], viewModel.getSelected().getValue());
      assertEquals(0, viewModel.getNumSelected());
      assertEquals(0, viewModel.getUpperTotalScore().getValue().intValue());
      assertEquals(0, viewModel.getBonusScore().getValue().intValue());
      assertEquals(0, viewModel.getTotalScore().getValue().intValue());
   }

   @Test
   public void testIsYahtzee() {
      viewModel.setDiceNumbers(5, 5, 2, 4, 5);
      assertFalse(viewModel.isYahtzee());

      viewModel.setDiceNumbers(6, 6, 6, 6, 6);
      assertTrue(viewModel.isYahtzee());
   }

   @Test
   public void testIsJoker() {
      viewModel.setDiceNumbers(5, 5, 2, 4, 5);
      assertFalse(viewModel.isJoker());

      viewModel.setDiceNumbers(6, 6, 6, 6, 6);
      assertFalse(viewModel.isJoker());

      viewModel.select(viewModel.getNumCategories() - 1);
      assertFalse(viewModel.isJoker());

      viewModel.select(5);
      assertTrue(viewModel.isJoker());
   }

   @Test
   public void testSelect() {
      viewModel.setDiceNumbers(1, 2, 3, 4, 5);
      viewModel.select(9);
      assertEquals(15, viewModel.getScores().getValue()[9]);
      assertTrue(viewModel.getSelected().getValue()[9]);
      assertEquals(1, viewModel.getNumSelected());
      assertEquals(0, viewModel.getUpperTotalScore().getValue().intValue());
      assertEquals(15, viewModel.getTotalScore().getValue().intValue());

      viewModel.setDiceNumbers(2, 2, 2, 2, 2);
      viewModel.select(1);
      assertEquals(10, viewModel.getScores().getValue()[1]);
      assertTrue(viewModel.getSelected().getValue()[1]);
      assertEquals(2, viewModel.getNumSelected());
      assertEquals(10, viewModel.getUpperTotalScore().getValue().intValue());
      assertEquals(25, viewModel.getTotalScore().getValue().intValue());

      // 重复选择
      viewModel.select(1);
      assertEquals(10, viewModel.getScores().getValue()[1]);
      assertTrue(viewModel.getSelected().getValue()[1]);
      assertEquals(2, viewModel.getNumSelected());
      assertEquals(10, viewModel.getUpperTotalScore().getValue().intValue());
      assertEquals(25, viewModel.getTotalScore().getValue().intValue());
   }

   @Test
   public void testSelectObserver() {
      Observer<int[]> scoresObserver = mock(Observer.class);
      Observer<boolean[]> selectedObserver = mock(Observer.class);
      Observer<Integer> totalScoreObserver = mock(Observer.class);
      viewModel.getScores().observeForever(scoresObserver);
      viewModel.getSelected().observeForever(selectedObserver);
      viewModel.getTotalScore().observeForever(totalScoreObserver);

      viewModel.setDiceNumbers(2, 2, 2, 2, 2);
      viewModel.select(1);

      verify(scoresObserver, atLeastOnce()).onChanged(argThat(a -> a[1] == 10));
      verify(selectedObserver, atLeastOnce()).onChanged(argThat(a -> a[1]));
      verify(totalScoreObserver).onChanged(10);
   }

   @Test
   public void testBonus() {
      viewModel.setDiceNumbers(3, 3, 3, 3, 3);
      viewModel.select(2);
      assertEquals(15, viewModel.getUpperTotalScore().getValue().intValue());
      assertEquals(0, viewModel.getBonusScore().getValue().intValue());
      assertEquals(15, viewModel.getTotalScore().getValue().intValue());

      viewModel.setDiceNumbers(2, 2, 2, 2, 2);
      viewModel.select(1);
      assertEquals(25, viewModel.getUpperTotalScore().getValue().intValue());
      assertEquals(8, viewModel.getBonusScore().getValue().intValue());
      assertEquals(33, viewModel.getTotalScore().getValue().intValue());

      viewModel.setDiceNumbers(6, 6, 6, 6, 6);
      viewModel.select(5);
      assertEquals(55, viewModel.getUpperTotalScore().getValue().intValue());
      assertEquals(8, viewModel.getBonusScore().getValue().intValue());
      assertEquals(63, viewModel.getTotalScore().getValue().intValue());
   }

   @Test
   public void testReset() {
      viewModel.setDiceNumbers(6, 6, 6, 6, 6);
      viewModel.select(5);
      assertEquals(30, viewModel.getScores().getValue()[5]);
      assertTrue(viewModel.getSelected().getValue()[5]);
      assertEquals(1, viewModel.getNumSelected());
      assertEquals(30, viewModel.getUpperTotalScore().getValue().intValue());
      assertEquals(8, viewModel.getBonusScore().getValue().intValue());
      assertEquals(38, viewModel.getTotalScore().getValue().intValue());

      viewModel.reset();
      assertArrayEquals(new int[10], viewModel.getScores().getValue());
      assertArrayEquals(new boolean[10], viewModel.getSelected().getValue());
      assertEquals(0, viewModel.getNumSelected());
      assertEquals(0, viewModel.getUpperTotalScore().getValue().intValue());
      assertEquals(0, viewModel.getBonusScore().getValue().intValue());
      assertEquals(0, viewModel.getTotalScore().getValue().intValue());
   }
}
