package com.zzy.dicegames.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.zzy.dicegames.R;
import com.zzy.dicegames.dice.DiceFragment;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 测试{@link DiceFragment 骰子窗口}的{@code Activity}
 *
 * @author 赵正阳
 */
public class TestActivity extends Activity {
	private DiceFragment mDiceFragment;

	private EditText etDiceNumbers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		if (savedInstanceState == null) {
			mDiceFragment = new DiceFragment();
			Bundle bundle = new Bundle();
			bundle.putInt("rollTimes", 2);
			mDiceFragment.setArguments(bundle);
			getFragmentManager().beginTransaction()
					.add(R.id.diceFragment, mDiceFragment)
					.commit();
		}

		findViewById(R.id.btnActivate).setOnClickListener(v -> mDiceFragment.activateRollButton());

		NumberPicker diceCountPicker = findViewById(R.id.diceCountPicker);
		diceCountPicker.setMinValue(1);
		diceCountPicker.setMaxValue(6);
		diceCountPicker.setValue(6);
		diceCountPicker.setOnValueChangedListener((picker, oldVal, newVal) -> mDiceFragment.setDiceCount(newVal));

		EditText etRollTimes = findViewById(R.id.etRollTimes);
		findViewById(R.id.btnOKRollTimes).setOnClickListener(v -> {
			try {
				mDiceFragment.setRollTimes(Integer.parseInt(etRollTimes.getText().toString()));
			}
			catch (NumberFormatException ignored) {
			}
		});

		etDiceNumbers = findViewById(R.id.etDiceNumbers);
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
				)
		);
	}

}
