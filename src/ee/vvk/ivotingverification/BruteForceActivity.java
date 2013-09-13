/**
 * Copyright (C) 2013 Eesti Vabariigi Valimiskomisjon 
 * (Estonian National Electoral Committee), www.vvk.ee
 *
 * Written in 2013 by AS Finestmedia, www.finestmedia.ee
 * 
 * Vote-verification application for Estonian Internet voting system
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
 
package ee.vvk.ivotingverification;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import ee.vvk.ivotingverification.adapter.CandidatesListAdapter;
import ee.vvk.ivotingverification.dialog.LoadingSpinner;
import ee.vvk.ivotingverification.model.Vote;
import ee.vvk.ivotingverification.model.Vote.Candidate;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.Crypto;
import ee.vvk.ivotingverification.util.RegexMatcher;
import ee.vvk.ivotingverification.util.Util;

/**
 * Brute force analysis of the vote.
 * 
 * @version 28.05.2013
 */
public class BruteForceActivity extends Activity {

	private static final String TAG = BruteForceActivity.class.getSimpleName();

	private String qrCode;
	private String webResult;
	private String versionNumber;

	private String publicKey;
	private ListView list;
	private Vote vote;
	private TextView lblChoice;
	private View lblShadow;
	private TextView lblCloseTimeout;
	private View lblcloseTimeoutShadow;
	private CustomCountDownTimer countDownTimer;
	private LoadingSpinner mLoadingSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Security.removeProvider(ext.org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME);
		Security.addProvider(new ext.org.bouncycastle.jce.provider.BouncyCastleProvider());
		setContentView(R.layout.bruteforce_activity);

		countDownTimer = new CustomCountDownTimer(C.closeTimeout,
				C.closeInterval);

		Intent intent = getIntent();
		qrCode = intent.getStringExtra(Util.QR_CODE);
		webResult = intent.getStringExtra(Util.WEB_RESULT);
		versionNumber = intent.getStringExtra(Util.VERSION_NUMBER);

		list = (ListView) findViewById(R.id.list);

		lblChoice = (TextView) findViewById(R.id.choice_title_label);
		lblChoice.setText(C.lblChoice);
		lblChoice.setTypeface(C.typeFace);
		lblChoice.setTextColor(Util.generateHexColorValue(C.lblForeground));
		lblChoice.setBackgroundColor(Util
				.generateHexColorValue(C.lblBackground));

		lblShadow = (View) findViewById(R.id.choice_title_label_shadow);
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

		lblcloseTimeoutShadow = (View) findViewById(R.id.close_timeout_label_shadow);
		GradientDrawable bgCloseTimeoutShadowShape = (GradientDrawable) lblcloseTimeoutShadow
				.getBackground();
		bgCloseTimeoutShadowShape.setColor(Util
				.generateHexColorValue(C.lblCloseTimeoutShadow));
		bgCloseTimeoutShadowShape.setCornerRadius(5);

		lblChoice.setVisibility(View.INVISIBLE);
		lblShadow.setVisibility(View.INVISIBLE);
		lblCloseTimeout.setVisibility(View.INVISIBLE);
		lblcloseTimeoutShadow.setVisibility(View.INVISIBLE);

		publicKey = C.publicKey;
		doBruteForce();
	}

	abstract class GetBruteForceTask extends
			AsyncTask<Void, Void, ArrayList<Candidate>> {

		@Override
		protected void onPreExecute() {
			mLoadingSpinner = Util.startSpinner(BruteForceActivity.this, false);
		}

		@Override
		protected void onPostExecute(ArrayList<Candidate> candidates) {

			if (candidates != null && candidates.size() > 0) {

				CandidatesListAdapter adapter = new CandidatesListAdapter(
						getApplicationContext(), candidates, vote);
				list.setAdapter(adapter);

				Util.stopSpinner(mLoadingSpinner);
				sendNotification(C.notificationTitle, C.notificationMessage);

				lblChoice.setVisibility(View.VISIBLE);
				lblShadow.setVisibility(View.VISIBLE);
				lblCloseTimeout.setVisibility(View.VISIBLE);
				lblcloseTimeoutShadow.setVisibility(View.VISIBLE);

				countDownTimer.start();
			} else {
				Util.startErrorIntent(BruteForceActivity.this,
						C.badVerificationMessage, true);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
				.cancel(1000123);
		if (countDownTimer != null) {
			countDownTimer.cancel();
		}
	}

	private void doBruteForce() {
		new GetBruteForceTask() {

			@Override
			protected ArrayList<Candidate> doInBackground(Void... arg0) {
				try {
					if (Util.DEBUGGABLE) {
						Log.d("QR_CODE", qrCode);
					}

					ArrayList<Candidate> candidates = new ArrayList<Vote.Candidate>();

					vote = new Vote();
					vote.parseHeader(webResult);
					List<Candidate> cands = vote.parseBody(webResult);
					String newEnc = "";

					if (qrCode.split("\n").length > 1) {
						for (int i = 1; i < qrCode.split("\n").length; i++) {
							String hexControlCode = qrCode.split("\n")[i]
									.split("\t")[1].split("\n")[0];
							if (!RegexMatcher.IsFortyCharacters(hexControlCode)) {
								Util.startErrorIntent(BruteForceActivity.this,
										C.badServerResponseMessage, true);
							}
							String electionId = qrCode.split("\n")[i]
									.split("\t")[0];

							newEnc = vote.encBallots.get(electionId)
									.replaceAll("\n", "");

							if (Util.DEBUGGABLE) {
								Log.d(TAG, electionId);
								Log.d(TAG, hexControlCode);
								Log.d(TAG, newEnc);
							}

							for (Candidate c : cands) {

								String decodedVote = versionNumber + "\n"
										+ electionId + "\n" + c.number + "\n";

								String bruteenc = new String(
										Crypto.encrypt(decodedVote,
												hexControlCode, publicKey),
										Util.ENCODING);

								if (newEnc.equals(bruteenc)) {
									candidates.add(c);
								}
							}
						}
					}
					return candidates;

				} catch (Exception e) {
					if (Util.DEBUGGABLE) {
						Log.d(TAG, "Error: " + e.getMessage(), e);
					}
					return null;
				}
			}
		}.execute();
	}

	public class CustomCountDownTimer extends CountDownTimer {

		public CustomCountDownTimer(long startTime, long interval) {
			super(startTime, interval);
		}

		@Override
		public void onFinish() {
			Intent intent = new Intent(getApplicationContext(),
					MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(Util.EXIT, true);
			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
					.cancel(1000123);
			startActivity(intent);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			lblCloseTimeout.setText(C.lblCloseTimeout.replace("XX", String
					.valueOf(TimeUnit.MILLISECONDS
							.toSeconds(millisUntilFinished))));
		}
	}

	private void sendNotification(String notificationTitle,
			String notificationMessage) {
		Notification notification = new Notification(R.drawable.icon,
				C.notificationMessage, System.currentTimeMillis());

		Intent notificationIntent = new Intent(this, BruteForceActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_ONE_SHOT);

		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notification.setLatestEventInfo(BruteForceActivity.this,
				notificationTitle, notificationMessage, contentIntent);

		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(1000123, notification);
	}
}