package com.zzy.dicegames.ui.dice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * {@link DiceView} 的插桩测试
 */
@RunWith(AndroidJUnit4.class)
public class DiceViewTest {

    /** 测试用骰子边长（像素） */
    private static final int VIEW_SIZE = 100;

    private Context context;
    private DiceView diceView;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        diceView = new DiceView(context);
        sizeAndLayout(diceView);
    }

    /** 将 View 固定为 VIEW_SIZE×VIEW_SIZE 并完成 measure/layout，使其可以绘制 */
    private static void sizeAndLayout(View view) {
        int spec = View.MeasureSpec.makeMeasureSpec(VIEW_SIZE, View.MeasureSpec.EXACTLY);
        view.measure(spec, spec);
        view.layout(0, 0, VIEW_SIZE, VIEW_SIZE);
    }

    /** 将 DiceView 绘制到一张 VIEW_SIZE×VIEW_SIZE 的位图上并返回 */
    private static Bitmap drawDiceView(DiceView diceView) {
        Bitmap bitmap = Bitmap.createBitmap(VIEW_SIZE, VIEW_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        diceView.draw(canvas);
        return bitmap;
    }

    @Test
    public void testDrawDiceFace() {
        // 点数 1：骰面中央是红色点
        diceView.setNumber(1);
        Bitmap one = drawDiceView(diceView);
        assertEquals(Color.RED, one.getPixel(VIEW_SIZE / 2, VIEW_SIZE / 2));

        // 点数 6：中央没有点，应为白色骰面
        diceView.setNumber(6);
        Bitmap six = drawDiceView(diceView);
        assertEquals(Color.WHITE,six.getPixel(VIEW_SIZE / 2, VIEW_SIZE / 2));
    }

    @Test
    public void testDrawBorderWhenLocked() {
        // 边框矩形从 BORDER_WIDTH/2 处开始描边，取 (BORDER_WIDTH/2, 高度中点) 检查
        final int borderPixelX = DiceView.BORDER_WIDTH / 2;
        final int borderPixelY = VIEW_SIZE / 2;

        diceView.setLocked(false);
        Bitmap unlocked = drawDiceView(diceView);
        assertNotEquals(Color.RED, unlocked.getPixel(borderPixelX, borderPixelY));

        diceView.setLocked(true);
        Bitmap locked = drawDiceView(diceView);
        assertEquals(Color.RED, locked.getPixel(borderPixelX, borderPixelY));
    }
}
