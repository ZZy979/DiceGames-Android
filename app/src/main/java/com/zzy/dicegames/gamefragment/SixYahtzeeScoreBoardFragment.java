package com.zzy.dicegames.gamefragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zzy.dicegames.R;

/**
 * 6骰Yahtzee计分板Fragment
 *
 * @author 赵正阳
 */
public class SixYahtzeeScoreBoardFragment extends AbstractYahtzeeScoreBoardFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// AbstractYahtzeeFragment应当已设置过游戏参数，这里添加布局id参数
		Bundle bundle = getArguments();
		bundle.putInt(LAYOUT_ID, R.layout.fragment_six_yahtzee_score_board);
		setArguments(bundle);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	protected void initViews(View rootView) {
		mScoreButtons.add(rootView.findViewById(R.id.btn1));
		mScoreButtons.add(rootView.findViewById(R.id.btn2));
		mScoreButtons.add(rootView.findViewById(R.id.btn3));
		mScoreButtons.add(rootView.findViewById(R.id.btn4));
		mScoreButtons.add(rootView.findViewById(R.id.btn5));
		mScoreButtons.add(rootView.findViewById(R.id.btn6));
		mScoreButtons.add(rootView.findViewById(R.id.btn1p));
		mScoreButtons.add(rootView.findViewById(R.id.btn2p));
		mScoreButtons.add(rootView.findViewById(R.id.btn3p));
		mScoreButtons.add(rootView.findViewById(R.id.btn3e));
		mScoreButtons.add(rootView.findViewById(R.id.btn4e));
		mScoreButtons.add(rootView.findViewById(R.id.btn5e));
		mScoreButtons.add(rootView.findViewById(R.id.btnSmallStraight));
		mScoreButtons.add(rootView.findViewById(R.id.btnLargeStraight));
		mScoreButtons.add(rootView.findViewById(R.id.btnFullStraight));
		mScoreButtons.add(rootView.findViewById(R.id.btnHut));
		mScoreButtons.add(rootView.findViewById(R.id.btnHouse));
		mScoreButtons.add(rootView.findViewById(R.id.btnTower));
		mScoreButtons.add(rootView.findViewById(R.id.btnChance));
		mScoreButtons.add(rootView.findViewById(R.id.btnYahtzee));
		for (Button scoreButton : mScoreButtons)
			scoreButton.setOnClickListener(v -> choose(mScoreButtons.indexOf(v)));

		mScoreTextViews.add(rootView.findViewById(R.id.tv1));
		mScoreTextViews.add(rootView.findViewById(R.id.tv2));
		mScoreTextViews.add(rootView.findViewById(R.id.tv3));
		mScoreTextViews.add(rootView.findViewById(R.id.tv4));
		mScoreTextViews.add(rootView.findViewById(R.id.tv5));
		mScoreTextViews.add(rootView.findViewById(R.id.tv6));
		mScoreTextViews.add(rootView.findViewById(R.id.tv1p));
		mScoreTextViews.add(rootView.findViewById(R.id.tv2p));
		mScoreTextViews.add(rootView.findViewById(R.id.tv3p));
		mScoreTextViews.add(rootView.findViewById(R.id.tv3e));
		mScoreTextViews.add(rootView.findViewById(R.id.tv4e));
		mScoreTextViews.add(rootView.findViewById(R.id.tv5e));
		mScoreTextViews.add(rootView.findViewById(R.id.tvSmallStraight));
		mScoreTextViews.add(rootView.findViewById(R.id.tvLargeStraight));
		mScoreTextViews.add(rootView.findViewById(R.id.tvFullStraight));
		mScoreTextViews.add(rootView.findViewById(R.id.tvHut));
		mScoreTextViews.add(rootView.findViewById(R.id.tvHouse));
		mScoreTextViews.add(rootView.findViewById(R.id.tvTower));
		mScoreTextViews.add(rootView.findViewById(R.id.tvChance));
		mScoreTextViews.add(rootView.findViewById(R.id.tvYahtzee));

		mUpperTotalTextView = rootView.findViewById(R.id.tvUpperTotal);
		mBonusTextView = rootView.findViewById(R.id.tvBonus);
		mGameTotalTextView = rootView.findViewById(R.id.tvGameTotal);
	}

}
