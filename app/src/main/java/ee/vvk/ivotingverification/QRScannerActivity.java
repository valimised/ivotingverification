package ee.vvk.ivotingverification;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import java.util.Objects;

import ee.vvk.ivotingverification.model.QRCodeContents;
import ee.vvk.ivotingverification.qr.CameraManager;
import ee.vvk.ivotingverification.qr.InactivityTimer;
import ee.vvk.ivotingverification.qr.QRAnalyzer;
import ee.vvk.ivotingverification.util.Util;


public class QRScannerActivity extends AppCompatActivity {

    private InactivityTimer inactivityTimer;
    public PreviewView previewView;
    private QRAnalyzer QRAnalyzer;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrscanner_activity);
        Objects.requireNonNull(getSupportActionBar()).hide();

        if (Util.SpecialModels.contains(Util.getDeviceName())) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        inactivityTimer = new InactivityTimer(this);
        previewView = findViewById(R.id.qr_previewView);
        QRAnalyzer = new QRAnalyzer(this);
    }

    public void qrCodePassed(QRCodeContents qrCodeContents) {
        Intent intent = new Intent(this, VoteDownloadActivity.class);
        intent.putExtra(Util.QR_CODE_CONTENTS, qrCodeContents);
        startActivity(intent);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        new CameraManager(this)
                .cameraPreviewScanner(previewView, QRAnalyzer, false);
        inactivityTimer.onResume();
    }

    @Override
    public void onPause() {
        new CameraManager(this)
                .cameraPreviewScanner(previewView, QRAnalyzer, false);
        inactivityTimer.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent mainActivity = new Intent(this, MainActivity.class);
        startActivity(mainActivity);
        finish();
    }
}
