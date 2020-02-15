package com.zzy.dicegames.dice;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.zzy.dicegames.R;

/**
 * 骰子控件，点数1~6<br>
 * 点击骰子可以将其锁定，周围显示红色边框，再次点击解除锁定
 *
 * @author 赵正阳
 */
public class Dice extends View {
	/** 边长 */
	private static final int SIDE_LENGTH = 140;

	/** 边框宽度 */
	private static final int BORDER_WIDTH = 10;

	/** 6个点数的图片 */
	private final Bitmap[] pics = new Bitmap[6];

	/** 绘制图片的区域 */
	private final Rect drawImageRect;

	/** 绘制边框的区域 */
	private final Rect borderRect;

	/** 绘制边框的画笔 */
	Paint mPaint;

	/** 骰子点数 */
	private int mNumber;

	/** 是否被锁定 */
	private boolean mLocked;

	public Dice(Context context) {
		this(context, null);
	}

	public Dice(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Dice(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public Dice(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

		pics[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.d1);
		pics[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.d2);
		pics[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.d3);
		pics[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.d4);
		pics[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.d5);
		pics[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.d6);

		drawImageRect = new Rect(getPaddingLeft() + BORDER_WIDTH,
				getPaddingTop() + BORDER_WIDTH,
				getPaddingLeft() + SIDE_LENGTH - BORDER_WIDTH,
				getPaddingTop() + SIDE_LENGTH - BORDER_WIDTH);

		borderRect = new Rect(getPaddingLeft(), getPaddingTop(),
				getPaddingLeft() + SIDE_LENGTH, getPaddingTop() + SIDE_LENGTH);

		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(2 * BORDER_WIDTH);

		// 获取自定义属性的值
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Dice, defStyleAttr, defStyleRes);
		mNumber = a.getInteger(R.styleable.Dice_number, 6);
		mLocked = a.getBoolean(R.styleable.Dice_locked, false);
		a.recycle();

		setOnClickListener(v -> {
			if (isEnabled())
				setLocked(!mLocked);
		});
	}

	/** 返回骰子点数 */
	public int getNumber() {
		return mNumber;
	}

	/**
	 * 设置骰子点数，骰子被锁定时无效
	 *
	 * @throws IllegalArgumentException 如果未锁定且设置的点数不在1~6之间
	 */
	public void setNumber(int number) {
		if (!mLocked)
			forceSetNumber(number);
	}

	/**
	 * 设置骰子点数，无视锁定状态
	 *
	 * @throws IllegalArgumentException 如果设置的点数不在1~6之间
	 */
	public void forceSetNumber(int number) {
		if (number < 1 || number > 6)
			throw new IllegalArgumentException(getContext().getString(R.string.wrongDiceNumber));
		mNumber = number;
		invalidate();
	}

	/** 返回骰子是否被锁定 */
	public boolean isLocked() {
		return mLocked;
	}

	/** 设置骰子是否被锁定 */
	public void setLocked(boolean locked) {
		mLocked = locked;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(pics[mNumber - 1], null, drawImageRect, null);
		if (mLocked)
			canvas.drawRect(borderRect, mPaint);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int width = widthMode == MeasureSpec.EXACTLY ? widthSize : getPaddingStart() + SIDE_LENGTH + getPaddingEnd();
		int height = heightMode == MeasureSpec.EXACTLY ? heightSize : getPaddingTop() + SIDE_LENGTH + getPaddingBottom();
		setMeasuredDimension(width, height);
	}

}
