package ee.vvk.ivotingverification;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.InputStream;
import java.security.Security;
import java.util.Objects;

import ee.vvk.ivotingverification.dialog.LoadingSpinner;
import ee.vvk.ivotingverification.qr.CameraManager;
import ee.vvk.ivotingverification.qr.InactivityTimer;
import ee.vvk.ivotingverification.tasks.AsyncTaskActivity;
import ee.vvk.ivotingverification.tasks.GetConfigTask;
import ee.vvk.ivotingverification.tasks.TaskRunner;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.JSONParser;
import ee.vvk.ivotingverification.util.Util;

public class MainActivity extends AppCompatActivity implements AsyncTaskActivity<String> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private InactivityTimer inactivityTimer;
    private LoadingSpinner mLoadingSpinner;
    private Button buttonMore;
    private Button buttonNext;
    private PreviewView previewView;

    public static boolean deleteDir(File dir) {
        if (dir == null) {
            return true;
        }
        boolean all_deleted = true;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    all_deleted &= deleteDir(new File(dir, child));
                }
            }
        }
        if (all_deleted) {
            return dir.delete();
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();



        C.configURL = BuildConfig.CONFIG_FILE;
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        Util.DEBUGGABLE = (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
        if (getIntent().getBooleanExtra(Util.EXIT, false)) {
            finish();
        }

        inactivityTimer = new InactivityTimer(this);
        setContentView(R.layout.main_activity);
        previewView = findViewById(R.id.main_previewView);

        LinearLayout linearLayout = findViewById(R.id.target_window);
        GradientDrawable bgShape = (GradientDrawable) linearLayout.getBackground();
        bgShape.setColor(Color.GRAY);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            TaskRunner runner = new TaskRunner();
            InputStream truststorein = getResources().openRawResource(R.raw.mytruststore);
            runner.executeAsync(new GetConfigTask(this, truststorein, C.configURL));
        } else {
            Util.startNetworkErrorIntent(MainActivity.this, C.noNetworkMessage);
        }

        LinearLayout frameBg = findViewById(R.id.frame_bg);
        frameBg.setBackgroundColor(Util.generateHexColorValue(C.frameBackground));

        buttonNext = findViewById(R.id.btn_next);
        buttonNext.setText(C.btnNext);
        buttonNext.setTextColor(Util.generateHexColorValue(C.btnForeground));
        GradientDrawable bgNextShape = (GradientDrawable) buttonNext.getBackground();
        bgNextShape.setColor(Util.generateHexColorValue(C.btnBackground));
        buttonNext.setOnClickListener(this::clickNextButton);

        buttonMore = findViewById(R.id.btn_more);
        buttonMore.setText(C.btnMore);
        buttonMore.setTextColor(Util.generateHexColorValue(C.btnForeground));
        GradientDrawable bgMoreShape = (GradientDrawable) buttonMore.getBackground();
        bgMoreShape.setColor(Util.generateHexColorValue(C.btnBackground));
        buttonMore.setOnClickListener(this::clickMoreButton);
    }

    @Override
    public void onResume() {
        super.onResume();
        inactivityTimer.onResume();
        new CameraManager(this).cameraPreview(previewView);
    }

    @Override
    public void onPause() {
        inactivityTimer.onPause();
        super.onPause();
        new CameraManager(this).cameraPreview(previewView);
    }

    public void clickNextButton(View view) {
        Intent QRCodeDecoder = new Intent(this, QRScannerActivity.class);
        startActivity(QRCodeDecoder);
    }

    public void clickMoreButton(View view) {
        Intent showHelp = new Intent(Intent.ACTION_VIEW, Uri.parse(C.helpURL));
        try {
            startActivity(showHelp);
        } catch (ActivityNotFoundException e) {
            Util.logDebug(TAG, "Error while displaying help: ", e);
        }
    }


    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
        Util.stopSpinner(mLoadingSpinner);
        try {
            deleteDir(getApplicationContext().getCacheDir());
        } catch (Exception e) {
            Util.logDebug(TAG, "Error while cleaning app cache: ", e);
        }
    }

    private void initMainWindow() {

        LinearLayout linearLayout = findViewById(R.id.target_window);
        GradientDrawable bgShape = (GradientDrawable) linearLayout.getBackground();
        bgShape.setColor(Util.generateHexColorValue(C.mainWindow));

        linearLayout = findViewById(R.id.target_window_shadow);
        GradientDrawable bgShapeShadow = (GradientDrawable) linearLayout.getBackground();
        bgShapeShadow.setColor(Util.generateHexColorValue(C.mainWindowShadow));

        LinearLayout linearLayoutShadow = findViewById(R.id.window_shadow);
        linearLayoutShadow.setVisibility(View.VISIBLE);

        TextView textView = findViewById(R.id.text_message);
        textView.setTypeface(C.typeFace);
        textView.setText(C.welcomeMessage);
        textView.setVisibility(View.VISIBLE);
        textView.setTextColor(Util.generateHexColorValue(C.mainWindowForeground));

        buttonMore.setText(C.btnMore);
        buttonMore.setVisibility(View.VISIBLE);
        buttonMore.setTypeface(C.typeFace);

        buttonNext.setText(C.btnNext);
        buttonNext.setVisibility(View.VISIBLE);
        buttonNext.setTypeface(C.typeFace);

        ImageView frameImage = findViewById(R.id.frame_image);
        frameImage.setVisibility(View.GONE);
    }

    public void onPreExecute() {
        mLoadingSpinner = Util.startSpinner(this, true);
    }

    public void onPostExecute(String result) {
        try {
            if (result == null) {
                Util.startErrorIntent(MainActivity.this, C.getConfigMessage);
                return;
            }
            try {
                JSONParser.parseConfig(result);
            } catch (Exception e) {
                Util.logException(TAG, e);
                Util.startErrorIntent(MainActivity.this, C.badConfigMessage);
                return;
            }

            if (!isApplicationUpToDate(C.expectedVersion)) {
                Util.startVersionErrorIntent(MainActivity.this, C.badVersionMessage);
            }

            Util.stopSpinner(mLoadingSpinner);
            initMainWindow();
        } catch (Exception e) {
            Util.logException(TAG, e);
        }
    }

    private boolean isApplicationUpToDate(long expectedVersion)
            throws PackageManager.NameNotFoundException {
        PackageManager pm = getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
        return packageInfo.versionCode >= expectedVersion;
    }
}
