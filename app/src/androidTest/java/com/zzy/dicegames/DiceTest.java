package com.zzy.dicegames;

import android.content.Context;

import com.zzy.dicegames.widget.Dice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link Dice 骰子组件}的插桩单元测试
 *
 * @author 赵正阳
 */
@RunWith(AndroidJUnit4.class)
public class DiceTest {
	private Dice dice;

	@Before
	public void setUp() {
		Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		dice = new Dice(context);
	}

	@Test
	public void setNumber() {
		assertEquals(6, dice.getNumber());
		assertFalse(dice.isLocked());
		for (int i = 1; i <= 6; ++i) {
			dice.setNumber(i);
			assertEquals(i, dice.getNumber());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void setNumberOutOfRange() {
		dice.setNumber(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setNumberOutOfRange2() {
		dice.setNumber(7);
	}

	/** 骰子被锁定时{@code setNumber}无效 */
	@Test
	public void setNumberWhenLocked() {
		dice.setLocked(true);
		dice.setNumber(4);
		assertEquals(6, dice.getNumber());
	}

	/** {@code forceSetNumber()}无视锁定状态 */
	@Test
	public void forceSetNumber() {
		dice.setLocked(true);
		dice.forceSetNumber(4);
		assertEquals(4, dice.getNumber());
	}

	/** 点击骰子改变锁定状态 */
	@Test
	public void changeLocked() {
		assertFalse(dice.isLocked());
		dice.callOnClick();
		assertTrue(dice.isLocked());
		dice.callOnClick();
		assertFalse(dice.isLocked());
	}

	/** {@code enabled == false}时点击无效 */
	@Test
	public void changeLockedWhenDisabled() {
		assertFalse(dice.isLocked());
		dice.setEnabled(false);
		dice.callOnClick();
		assertFalse(dice.isLocked());
	}

}
