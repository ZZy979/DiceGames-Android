package com.zzy.dicegames.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.zzy.dicegames.R;
import com.zzy.dicegames.fragment.dice.DiceFragment;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 测试{@link DiceFragment 骰子窗口}的{@code Activity}
 *
 * @author 赵正阳
 */
public class TestActivity extends Activity {
	/** 骰子窗口 */
	private DiceFragment mDiceFragment;

	/** 骰子个数选择器 */
	private NumberPicker mDiceCountPicker;

	/** 用于保存和恢复状态：骰子个数 */
	private static final String DICE_COUNT = "diceCount";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		if (savedInstanceState == null) {
			mDiceFragment = new DiceFragment();
			Bundle bundle = new Bundle();
			bundle.putInt(DiceFragment.ROLL_TIMES, 2);
			mDiceFragment.setArguments(bundle);
			getFragmentManager().beginTransaction()
					.add(R.id.diceFragment, mDiceFragment)
					.commit();
		}
		else
			mDiceFragment = (DiceFragment) getFragmentManager().findFragmentById(R.id.diceFragment);

		findViewById(R.id.btnActivate).setOnClickListener(v -> mDiceFragment.activateRollButton());

		mDiceCountPicker = findViewById(R.id.diceCountPicker);
		mDiceCountPicker.setMinValue(DiceFragment.MIN_DICE_NUM);
		mDiceCountPicker.setMaxValue(DiceFragment.MAX_DICE_NUM);
		if (savedInstanceState == null)
			mDiceCountPicker.setValue(DiceFragment.MAX_DICE_NUM);
		else
			mDiceCountPicker.setValue(savedInstanceState.getInt(DICE_COUNT));
		mDiceCountPicker.setOnValueChangedListener((picker, oldVal, newVal) -> mDiceFragment.setDiceCount(newVal));

		EditText etRollTimes = findViewById(R.id.etRollTimes);
		findViewById(R.id.btnOKRollTimes).setOnClickListener(v -> {
			try {
				mDiceFragment.setRollTimes(Integer.parseInt(etRollTimes.getText().toString()));
				mDiceFragment.activateRollButton();
			}
			catch (NumberFormatException ignored) {
			}
		});

		EditText etDiceNumbers = findViewById(R.id.etDiceNumbers);
		findViewById(R.id.btnOKDiceNumbers).setOnClickListener(v -> {
			try {
				mDiceFragment.setDiceNumbers(Arrays.stream(etDiceNumbers.getText().toString().split(","))
						.mapToInt(Integer::parseInt)
						.toArray());
			}
			catch (NumberFormatException e) {
				Toast.makeText(this, getString(R.string.wrongFormat), Toast.LENGTH_SHORT).show();
			}
			catch (IllegalArgumentException e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});

		mDiceFragment.setRollListener(diceNumbers -> etDiceNumbers.setText(
				Arrays.stream(diceNumbers)
						.mapToObj(String::valueOf)
						.collect(Collectors.joining(","))
		));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(DICE_COUNT, mDiceCountPicker.getValue());
		super.onSaveInstanceState(outState);
	}

}
