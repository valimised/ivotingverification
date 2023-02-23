package ee.vvk.ivotingverification;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import ee.vvk.ivotingverification.model.QRCodeContents;
import ee.vvk.ivotingverification.model.VoteContainerInfo;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.Util;


public class VoteReceivedActivity extends Activity {

    private VoteContainerInfo voteContainerInfo;
    private QRCodeContents qrCodeContents;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.vote_activity);

        Intent intent = getIntent();
        voteContainerInfo = intent.getParcelableExtra(Util.VOTE_CONTAINER_INFO);
        qrCodeContents = intent.getParcelableExtra(Util.QR_CODE_CONTENTS);

        TextView lblVote = findViewById(R.id.vote_label);
        lblVote.setText(C.lblVote);
        lblVote.setTypeface(C.typeFace);
        lblVote.setTextColor(Util.generateHexColorValue(C.lblForeground));
        lblVote.setBackgroundColor(Util.generateHexColorValue(C.lblBackground));

        View lblShadow = findViewById(R.id.vote_label_shadow);
        lblShadow.setBackgroundColor(Util.generateHexColorValue(C.lblShadow));

        TextView lblVoteTxt = findViewById(R.id.vote_txt_label);
        lblVoteTxt.setTypeface(C.typeFace);
        lblVoteTxt.setText(C.lblVoteTxt);
        lblVoteTxt.setTextColor(Util.generateHexColorValue(C.lblOuterContainerForeground));

        TextView lblVoteSigner = findViewById(R.id.vote_signer_label);
        lblVoteSigner.setTypeface(C.typeFace);
        lblVoteSigner.setText(
                getString(R.string.lblVoteSinger,C.lblVoteSigner,voteContainerInfo.getSignerCN()));
        lblVoteSigner.setTextColor(Util.generateHexColorValue(C.lblOuterContainerForeground));

        Button btnVoteVerify = findViewById(R.id.vote_btn_verify);
        btnVoteVerify.setTypeface(C.typeFace);
        btnVoteVerify.setText(C.btnVerify);
        btnVoteVerify.setTextColor(Util.generateHexColorValue(C.btnVerifyForeground));
        int[] colors = new int[3];
        colors[0] = Util.generateHexColorValue(C.btnVerifyBackgroundStart);
        colors[1] = Util.generateHexColorValue(C.btnVerifyBackgroundCenter);
        colors[2] = Util.generateHexColorValue(C.btnVerifyBackgroundEnd);

        GradientDrawable bgVoteVerifyShape = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, colors);
        bgVoteVerifyShape.setCornerRadius(5);
        btnVoteVerify.setBackground(bgVoteVerifyShape);
        btnVoteVerify.setOnClickListener(this::clickVerifyButton);
        btnVoteVerify.setClickable(true);
        btnVoteVerify.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    public void clickVerifyButton(View view) {
        Intent next_intent = new Intent(this, DecryptionActivity.class);
        next_intent.putExtra(Util.QR_CODE_CONTENTS, qrCodeContents);
        next_intent.putExtra(Util.VOTE_CONTAINER_INFO, voteContainerInfo);
        startActivity(next_intent);
        finish();
    }
}
