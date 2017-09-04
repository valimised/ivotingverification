package ee.vvk.ivotingverification;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import ee.vvk.ivotingverification.model.Vote;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.Util;

/**
 * Vote activity.
 * 
 * @version 16.05.2013
 */
public class VoteActivity extends Activity {

	private Button btnVoteVerify;

	private static final String TAG = VoteActivity.class.getSimpleName();

	private ArrayList<Vote> voteList;
	private byte[] rndSeed;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.vote_activity);

		Intent intent = getIntent();
		String signerCN = intent.getStringExtra(Util.SIGNER_CN);
		voteList = intent.getParcelableArrayListExtra(Util.VOTE_ARRAY);
		rndSeed = intent.getByteArrayExtra(Util.RANDOM_SEED);

		TextView lblVote = (TextView) findViewById(R.id.vote_label);
		lblVote.setText(C.lblVote);
		lblVote.setTypeface(C.typeFace);
		lblVote.setTextColor(Util.generateHexColorValue(C.lblForeground));
		lblVote.setBackgroundColor(Util.generateHexColorValue(C.lblBackground));

		View lblShadow = findViewById(R.id.vote_label_shadow);
		lblShadow.setBackgroundColor(Util.generateHexColorValue(C.lblShadow));

		TextView lblVoteTxt = (TextView) findViewById(R.id.vote_txt_label);
		lblVoteTxt.setTypeface(C.typeFace);
		lblVoteTxt.setText(C.lblVoteTxt);
		lblVoteTxt.setTextColor(Util
				.generateHexColorValue(C.lblOuterContainerForeground));

		TextView lblVoteSigner = (TextView) findViewById(R.id.vote_signer_label);
		lblVoteSigner.setTypeface(C.typeFace);
		lblVoteSigner.setText(C.lblVoteSigner + signerCN);
		lblVoteSigner.setTextColor(Util
				.generateHexColorValue(C.lblOuterContainerForeground));

		btnVoteVerify = (Button) findViewById(R.id.vote_btn_verify);
		btnVoteVerify.setTypeface(C.typeFace);
		btnVoteVerify.setText(C.btnVerify);
		btnVoteVerify.setTextColor(Util
				.generateHexColorValue(C.btnVerifyForeground));
		int colors[] = new int[3];
		colors[0] = Util.generateHexColorValue(C.btnVerifyBackgroundStart);
		colors[1] = Util.generateHexColorValue(C.btnVerifyBackgroundCenter);
		colors[2] = Util.generateHexColorValue(C.btnVerifyBackgroundEnd);

		GradientDrawable bgVoteVerifyShape = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, colors);
		bgVoteVerifyShape.setCornerRadius(5);
		btnVoteVerify.setBackgroundDrawable(bgVoteVerifyShape);
		btnVoteVerify.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clickVerifyButton(v);
			}
		});
		try {
			btnVoteVerify.setClickable(true);
			btnVoteVerify.setVisibility(android.view.View.VISIBLE);
		} catch (Exception e) {
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Parser exception. Vote could not be parsed.");
			}
			Util.startErrorIntent(VoteActivity.this,
					C.badServerResponseMessage, true);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}

	public void clickVerifyButton(View view) {
		Intent next_intent = new Intent(this, BruteForceActivity.class);
		next_intent.putExtra(Util.RANDOM_SEED, rndSeed);
		next_intent.putParcelableArrayListExtra(Util.VOTE_ARRAY, voteList);
		startActivity(next_intent);
		finish();
	}
}
