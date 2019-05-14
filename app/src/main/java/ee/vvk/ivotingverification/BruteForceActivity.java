package ee.vvk.ivotingverification;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Sequence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ee.vvk.ivotingverification.adapter.CandidatesListAdapter;
import ee.vvk.ivotingverification.dialog.LoadingSpinner;
import ee.vvk.ivotingverification.model.Candidate;
import ee.vvk.ivotingverification.model.Vote;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.ElGamalPub;
import ee.vvk.ivotingverification.util.Util;

/**
 * Brute force analysis of the vote.
 * 
 * @version 28.05.2013
 */
public class BruteForceActivity extends Activity {

	private static final String TAG = BruteForceActivity.class.getSimpleName();

	private Activity thisActivity = this;
	private ArrayList<Vote> voteList;
	private ElGamalPub pub;
	private ListView list;
	private TextView lblChoice;
	private View lblShadow;
	private TextView lblCloseTimeout;
	private View lblcloseTimeoutShadow;
	private View closeBtn;
	private CustomCountDownTimer countDownTimer;
	private LoadingSpinner mLoadingSpinner;
	private byte[] rnd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.bruteforce_activity);

		countDownTimer = new CustomCountDownTimer(C.closeTimeout,
				C.closeInterval);

		Intent intent = getIntent();
		voteList = intent.getParcelableArrayListExtra(Util.VOTE_ARRAY);
		rnd = intent.getByteArrayExtra(Util.RANDOM_SEED);

		list = (ListView) findViewById(R.id.list);

		closeBtn = findViewById(R.id.close_view_btn);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				exitActivity();
			}
		});

		lblChoice = (TextView) findViewById(R.id.choice_title_label);
		lblChoice.setText(C.lblChoice);
		lblChoice.setTypeface(C.typeFace);
		lblChoice.setTextColor(Util.generateHexColorValue(C.lblForeground));
		lblChoice.setBackgroundColor(Util
				.generateHexColorValue(C.lblBackground));

		lblShadow = findViewById(R.id.choice_title_label_shadow);
		lblShadow.setBackgroundColor(Util.generateHexColorValue(C.lblShadow));

		lblCloseTimeout = (TextView) findViewById(R.id.close_timeout_label);
		lblCloseTimeout.setTypeface(C.typeFace);
		lblCloseTimeout.setText(C.lblCloseTimeout);
		lblCloseTimeout.setTextColor(Util
				.generateHexColorValue(C.lblCloseTimeoutForeground));
		int colors[] = new int[3];
		colors[0] = Util
				.generateHexColorValue(C.lblCloseTimeoutBackgroundStart);
		colors[1] = Util
				.generateHexColorValue(C.lblCloseTimeoutBackgroundCenter);
		colors[2] = Util.generateHexColorValue(C.lblCloseTimeoutBackgroundEnd);

		GradientDrawable bgCloseTimeoutShape = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, colors);
		bgCloseTimeoutShape.setCornerRadius(5);
		lblCloseTimeout.setBackgroundDrawable(bgCloseTimeoutShape);

		lblcloseTimeoutShadow = findViewById(R.id.close_timeout_label_shadow);
		GradientDrawable bgCloseTimeoutShadowShape = (GradientDrawable) lblcloseTimeoutShadow
				.getBackground();
		bgCloseTimeoutShadowShape.setColor(Util
				.generateHexColorValue(C.lblCloseTimeoutShadow));
		bgCloseTimeoutShadowShape.setCornerRadius(5);

		closeBtn.setVisibility(View.INVISIBLE);
		lblChoice.setVisibility(View.INVISIBLE);
		lblShadow.setVisibility(View.INVISIBLE);
		lblCloseTimeout.setVisibility(View.INVISIBLE);
		lblcloseTimeoutShadow.setVisibility(View.INVISIBLE);

		try {
			pub = new ElGamalPub(C.publicKey);
		} catch (Exception e) {
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Bad public key: " + e.getMessage(), e);
			}
			Util.startErrorIntent(BruteForceActivity.this,
					C.badConfigMessage, true);
			return;
		}
		new BruteForceTask().execute();
	}

	@SuppressLint("StaticFieldLeak")
	private class BruteForceTask extends AsyncTask<Void, Void, ArrayList<Candidate>> {

		@Override
		protected void onPreExecute() {
			mLoadingSpinner = Util.startSpinner(BruteForceActivity.this, false);
		}

		@Override
		protected ArrayList<Candidate> doInBackground(Void... arg0) {
			try {
				ArrayList<Candidate> result = new ArrayList<>();
				for (Vote vote: voteList) {
					BigInteger choice = getChoice(vote.vote);

					BigInteger factor = pub.key.modPow(new BigInteger(1, rnd), pub.p);
					BigInteger factorInverse = factor.modInverse(pub.p);
					BigInteger s = factorInverse.multiply(choice).mod(pub.p);
					if (!s.modPow(pub.q, pub.p).equals(BigInteger.ONE)) {
						if (Util.DEBUGGABLE) {
							Log.d(TAG, "Error: Plaintext is not quadratic residue (s.modPow(q,p) != 1)");
						}
						return null;
					}
					BigInteger m = s.compareTo(pub.q) == 1 ? pub.p.subtract(s) : s;
					String decChoice = stripPadding(m.toByteArray());

					if (decChoice.equals("")) {
						result.add(Candidate.NO_CHOICE);
					} else {
						result.add(new Candidate(decChoice));
					}
				}
				return result;
			} catch (Exception e) {
				if (Util.DEBUGGABLE) {
					Log.d(TAG, "Error: " + e.getMessage(), e);
				}
				return null;
			}
		}

		private BigInteger getChoice(byte[] in) throws IOException {
			ASN1InputStream bIn = new ASN1InputStream(new ByteArrayInputStream(in));
			ASN1Sequence parentObj = (ASN1Sequence) ((ASN1Sequence) bIn.readObject()).getObjectAt(1);
			String c2 = parentObj.getObjectAt(1).toString();
			return new BigInteger(c2, 10);
		}

		private String stripPadding(byte[] in) throws Exception {
			if (in.length < 2) {
				throw new Exception("Source message can not contain padding");
			}
			// As the plaintext byte array is obtained from BigInteger, leading 0 is omitted
			if (in.length + 1 != pub.p.bitLength() / 8) {
				throw new Exception("Incorrect plaintext length");
			}
			if (in[0] != 0x01) {
				throw new Exception("Incorrect padding head");
			}
			for (int i = 1; i < in.length; i++) {
				switch (in[i]) {
					case 0:
						// found padding end
						return new String(Arrays.copyOfRange(in, i + 1, in.length));
					case (byte) 0xff:
						continue;
					default:
						// incorrect padding byte
						throw new Exception("Incorrect padding byte");
				}
			}
			throw new Exception("Padding unexpected");
		}

		@Override
		protected void onPostExecute(ArrayList<Candidate> candidates) {
			Util.stopSpinner(mLoadingSpinner);
			if (candidates != null) {
				List<String> questionIds = new ArrayList<>();
				for (Vote vote: voteList) {
					questionIds.add(vote.questionId);
				}
				CandidatesListAdapter adapter = new CandidatesListAdapter(
						thisActivity, candidates, questionIds);
				list.setAdapter(adapter);

				((Vibrator) thisActivity.getSystemService(Context.VIBRATOR_SERVICE))
						.vibrate(Util.VIBRATE_DURATION);

				closeBtn.setVisibility(View.VISIBLE);
				lblChoice.setVisibility(View.VISIBLE);
				lblShadow.setVisibility(View.VISIBLE);
				lblCloseTimeout.setVisibility(View.VISIBLE);
				lblcloseTimeoutShadow.setVisibility(View.VISIBLE);

				countDownTimer.start();
			} else {
				Util.startErrorIntent(BruteForceActivity.this,
						C.badServerResponseMessage, true);
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}

	private void exitActivity() {
		Intent intent = new Intent(getApplicationContext(),
		MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(Util.EXIT, true);
		startActivity(intent);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
        Util.stopSpinner(mLoadingSpinner);
		if (countDownTimer != null) {
			countDownTimer.cancel();
		}
	}

	public class CustomCountDownTimer extends CountDownTimer {

		public CustomCountDownTimer(long startTime, long interval) {
			super(startTime, interval);
		}

		@Override
		public void onFinish() {
			exitActivity();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			lblCloseTimeout.setText(C.lblCloseTimeout.replace("XX", String
					.valueOf(TimeUnit.MILLISECONDS
							.toSeconds(millisUntilFinished))));
		}
	}
}
