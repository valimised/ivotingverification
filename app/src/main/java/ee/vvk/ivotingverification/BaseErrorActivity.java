package ee.vvk.ivotingverification;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import java.util.Objects;

import ee.vvk.ivotingverification.qr.CameraManager;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.Util;

public abstract class BaseErrorActivity extends AppCompatActivity {

    private static final String TAG = BaseErrorActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        Intent intent = getIntent();
        String errorMessage = intent.getStringExtra(Util.ERROR_MESSAGE);
        setContentView(R.layout.error_activity);

        PreviewView previewView = findViewById(R.id.error_previewView);
        new CameraManager(this).cameraPreview(previewView);

        Util.logError(TAG, "Message:" + errorMessage);
        initErrorWindow(errorMessage);
    }

    protected void initErrorWindow(String errorMessage) {

        LinearLayout frameBg = findViewById(R.id.frame_bg_error);
        frameBg.setBackgroundColor(Util.generateHexColorValue(C.frameBackground));

        LinearLayout linearLayout = findViewById(R.id.target_window_error);
        GradientDrawable bgShape = (GradientDrawable) linearLayout.getBackground();
        bgShape.setColor(Util.generateHexColorValue(C.errorWindow));

        linearLayout = findViewById(R.id.target_window_error_shadow);
        GradientDrawable bgShapeShadow = (GradientDrawable) linearLayout.getBackground();
        bgShapeShadow.setColor(Util.generateHexColorValue(C.errorWindowShadow));

        TextView textViewTitle = findViewById(R.id.text_title_error);
        TextView textViewMessage = findViewById(R.id.text_message_error);
        textViewTitle.setTypeface(C.typeFace);
        textViewMessage.setTypeface(C.typeFace);
        textViewMessage.setText(errorMessage);
        textViewTitle.setVisibility(View.INVISIBLE);
        textViewTitle.setTextColor(Util.generateHexColorValue(C.errorWindowForeground));
        textViewMessage.setVisibility(View.VISIBLE);
        textViewMessage.setTextColor(Util.generateHexColorValue(C.errorWindowForeground));

        Button buttonWifi = findViewById(R.id.btn_wifi);
        buttonWifi.setTextColor(Util.generateHexColorValue(C.btnForeground));
        GradientDrawable bgWifiShape = (GradientDrawable) buttonWifi.getBackground();
        buttonWifi.setTypeface(C.typeFace);
        bgWifiShape.setColor(Util.generateHexColorValue(C.btnBackground));

        Button buttonOk = findViewById(R.id.btn_ok);
        buttonOk.setTextColor(Util.generateHexColorValue(C.btnForeground));
        GradientDrawable bgOkShape = (GradientDrawable) buttonOk.getBackground();
        buttonOk.setTypeface(C.typeFace);
        bgOkShape.setColor(Util.generateHexColorValue(C.btnBackground));

        ImageView buttonClose = findViewById(R.id.close_error);
        buttonClose.setOnClickListener(v -> finish());
    }

}
