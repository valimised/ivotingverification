package ee.vvk.ivotingverification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ee.vvk.ivotingverification.adapter.CandidatesListAdapter;
import ee.vvk.ivotingverification.dialog.LoadingSpinner;
import ee.vvk.ivotingverification.model.Candidate;
import ee.vvk.ivotingverification.model.QRCodeContents;
import ee.vvk.ivotingverification.model.Vote;
import ee.vvk.ivotingverification.model.VoteContainerInfo;
import ee.vvk.ivotingverification.tasks.AsyncTaskActivity;
import ee.vvk.ivotingverification.tasks.DecryptionTask;
import ee.vvk.ivotingverification.tasks.TaskRunner;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.ElGamalPub;
import ee.vvk.ivotingverification.util.Util;

public class DecryptionActivity extends Activity implements AsyncTaskActivity<ArrayList<Candidate>> {

    private static final String TAG = DecryptionActivity.class.getSimpleName();

    private final Activity thisActivity = this;
    private VoteContainerInfo voteContainerInfo;
    private ListView list;
    private TextView lblChoice;
    private View lblShadow;
    private TextView lblCloseTimeout;
    private View lblcloseTimeoutShadow;
    private View closeBtn;
    private CustomCountDownTimer countDownTimer;
    private LoadingSpinner mLoadingSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.bruteforce_activity);

        countDownTimer = new CustomCountDownTimer(C.closeTimeout, C.closeInterval);

        Intent intent = getIntent();
        voteContainerInfo = intent.getParcelableExtra(Util.VOTE_CONTAINER_INFO);
        QRCodeContents qrCodeContents = intent.getParcelableExtra(Util.QR_CODE_CONTENTS);

        list = findViewById(R.id.list);

        closeBtn = findViewById(R.id.close_view_btn);
        closeBtn.setOnClickListener(view -> exitActivity());

        lblChoice = findViewById(R.id.choice_title_label);
        lblChoice.setText(C.lblChoice);
        lblChoice.setTypeface(C.typeFace);
        lblChoice.setTextColor(Util.generateHexColorValue(C.lblForeground));
        lblChoice.setBackgroundColor(Util.generateHexColorValue(C.lblBackground));

        lblShadow = findViewById(R.id.choice_title_label_shadow);
        lblShadow.setBackgroundColor(Util.generateHexColorValue(C.lblShadow));

        lblCloseTimeout = findViewById(R.id.close_timeout_label);
        lblCloseTimeout.setTypeface(C.typeFace);
        lblCloseTimeout.setText(C.lblCloseTimeout);
        lblCloseTimeout.setTextColor(Util.generateHexColorValue(C.lblCloseTimeoutForeground));
        int[] colors = new int[3];
        colors[0] = Util.generateHexColorValue(C.lblCloseTimeoutBackgroundStart);
        colors[1] = Util.generateHexColorValue(C.lblCloseTimeoutBackgroundCenter);
        colors[2] = Util.generateHexColorValue(C.lblCloseTimeoutBackgroundEnd);

        GradientDrawable bgCloseTimeoutShape = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, colors);
        bgCloseTimeoutShape.setCornerRadius(5);
        lblCloseTimeout.setBackground(bgCloseTimeoutShape);

        lblcloseTimeoutShadow = findViewById(R.id.close_timeout_label_shadow);
        GradientDrawable bgCloseTimeoutShadowShape = (GradientDrawable) lblcloseTimeoutShadow
                .getBackground();
        bgCloseTimeoutShadowShape.setColor(Util.generateHexColorValue(C.lblCloseTimeoutShadow));
        bgCloseTimeoutShadowShape.setCornerRadius(5);

        closeBtn.setVisibility(View.INVISIBLE);
        lblChoice.setVisibility(View.INVISIBLE);
        lblShadow.setVisibility(View.INVISIBLE);
        lblCloseTimeout.setVisibility(View.INVISIBLE);
        lblcloseTimeoutShadow.setVisibility(View.INVISIBLE);

        ElGamalPub pub;
        try {
            pub = new ElGamalPub(C.publicKey);
        } catch (Exception e) {
            Util.logDebug(TAG, "Bad public key", e);
            Util.startErrorIntent(DecryptionActivity.this, C.badConfigMessage);
            return;
        }
        TaskRunner runner = new TaskRunner();
        runner.executeAsync(new DecryptionTask(this, voteContainerInfo.getVoteList(), pub, qrCodeContents.getRndSeed()));
    }

    public void onPreExecute() {
        mLoadingSpinner = Util.startSpinner(DecryptionActivity.this, false);
    }

    public void onPostExecute(ArrayList<Candidate> candidates) {
        Util.stopSpinner(mLoadingSpinner);
        if (candidates != null) {
            List<String> questionIds = new ArrayList<>();
            for (Vote vote : voteContainerInfo.getVoteList()) {
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
            Util.startErrorIntent(DecryptionActivity.this, C.badServerResponseMessage);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    private void exitActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
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
                    .valueOf(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished))));
        }
    }
}
