package com.zzy.dicegames.ui.game.yahtzee;

import android.os.Handler;

import com.zzy.dicegames.data.entity.BaseScore;
import com.zzy.dicegames.data.entity.yahtzee.YahtzeeScore;
import com.zzy.dicegames.utils.ArrayUtil;
import com.zzy.dicegames.utils.score.ScoreUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.function.Consumer;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BaseYahtzeeGameViewModelTest {
   @Rule
   public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

   @Rule
   public MockitoRule mockitoRule = MockitoJUnit.rule();

   private BaseYahtzeeGameViewModel viewModel;
   private BaseYahtzeeGameViewModel spyViewModel;

   @Mock
   private Handler mockHandler;

   @Before
   public void setUp() {
      viewModel = new BaseYahtzeeGameViewModel(5, 3, 10, 20, 8) {
         @Override public int calculateScore(int category) { return sumOfDice; }
         @Override public BaseScore createScoreEntity() { return null; }
         @Override public int saveScoreToDatabase(BaseScore score) { return 0; }
      };
      viewModel.setHandler(mockHandler);
      spyViewModel = spy(viewModel);
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
   public void testIsAllSame() {
      viewModel.updateDiceNumbers(5, 5, 2, 4, 5);
      assertFalse(viewModel.isAllSame());

      viewModel.updateDiceNumbers(6, 6, 6, 6, 6);
      assertTrue(viewModel.isAllSame());
   }

   @Test
   public void testIsJoker() {
      viewModel.updateDiceNumbers(2, 4, 5, 6, 6);
      assertFalse(viewModel.isJoker());
      viewModel.select(5);

      viewModel.updateDiceNumbers(3, 3, 3, 3, 3);
      assertFalse(viewModel.isJoker());
      viewModel.select(viewModel.getNumCategories() - 1);

      viewModel.updateDiceNumbers(6, 6, 6, 6, 6);
      assertTrue(viewModel.isJoker());
   }

   @Test
   public void testSelect() {
      viewModel.updateDiceNumbers(1, 2, 3, 4, 5);
      viewModel.select(9);
      assertEquals(15, viewModel.getScores().getValue()[9]);
      assertTrue(viewModel.getSelected().getValue()[9]);
      assertEquals(1, viewModel.getNumSelected());
      assertEquals(0, viewModel.getUpperTotalScore().getValue().intValue());
      assertEquals(15, viewModel.getTotalScore().getValue().intValue());

      viewModel.updateDiceNumbers(2, 2, 2, 2, 2);
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

      viewModel.updateDiceNumbers(2, 2, 2, 2, 2);
      viewModel.select(1);

      verify(scoresObserver, atLeastOnce()).onChanged(argThat(a -> a[1] == 10));
      verify(selectedObserver, atLeastOnce()).onChanged(argThat(a -> a[1]));
      verify(totalScoreObserver).onChanged(10);
   }

   @Test
   public void testBonus() {
      viewModel.updateDiceNumbers(3, 3, 3, 3, 3);
      viewModel.select(2);
      assertEquals(15, viewModel.getUpperTotalScore().getValue().intValue());
      assertEquals(0, viewModel.getBonusScore().getValue().intValue());
      assertEquals(15, viewModel.getTotalScore().getValue().intValue());

      viewModel.updateDiceNumbers(2, 2, 2, 2, 2);
      viewModel.select(1);
      assertEquals(25, viewModel.getUpperTotalScore().getValue().intValue());
      assertEquals(8, viewModel.getBonusScore().getValue().intValue());
      assertEquals(33, viewModel.getTotalScore().getValue().intValue());

      viewModel.updateDiceNumbers(6, 6, 6, 6, 6);
      viewModel.select(5);
      assertEquals(55, viewModel.getUpperTotalScore().getValue().intValue());
      assertEquals(8, viewModel.getBonusScore().getValue().intValue());
      assertEquals(63, viewModel.getTotalScore().getValue().intValue());
   }

   @Test
   public void testSelectAll() {
      doNothing().when(spyViewModel).gameOver();
      for (int i = 0; i < spyViewModel.getNumCategories(); i++)
         spyViewModel.select(i);
      verify(spyViewModel).gameOver();
   }

   @Test
   public void testGameOver() {
      var score = new YahtzeeScore("2025-01-01", 300, true, false);
      doReturn(score).when(spyViewModel).createScoreEntity();
      doReturn(8).when(spyViewModel).saveScoreToDatabase(any());
      Consumer<Object[]> gameOverAction = mock(Consumer.class);
      spyViewModel.setGameOverAction(gameOverAction);

      spyViewModel.gameOver();
      assertTrue(ArrayUtil.all(spyViewModel.getDiceEnabled().getValue(), false));
      assertFalse(spyViewModel.getRollButtonEnabled().getValue());
      verify(spyViewModel).createScoreEntity();
      verify(spyViewModel).saveScoreToDatabase(argThat(s -> ScoreUtil.isEqual(score, (YahtzeeScore) s)));
      verify(gameOverAction).accept(argThat(args ->
              ScoreUtil.isEqual(score, (YahtzeeScore) args[0]) && (int) args[1] == 8));
   }

   @Test
   public void testReset() {
      viewModel.updateDiceNumbers(6, 6, 6, 6, 6);
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
