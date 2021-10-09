package ee.vvk.ivotingverification;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

import java.util.Collection;

import ee.vvk.ivotingverification.qr.CaptureActivityHandler;
import ee.vvk.ivotingverification.qr.DecodeFormatManager;
import ee.vvk.ivotingverification.qr.InactivityTimer;
import ee.vvk.ivotingverification.qr.Intents;
import ee.vvk.ivotingverification.qr.ViewfinderView;
import ee.vvk.ivotingverification.util.Util;


public class QRScannerActivity extends CameraSurfaceActivity {

    private static final String TAG = QRScannerActivity.class.getSimpleName();

    private ViewfinderView viewfinderView;
    private InactivityTimer inactivityTimer;
    private CaptureActivityHandler handler;

    private Collection<BarcodeFormat> decodeFormats = null;
    private String characterSet = null;

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a,
                                 ResultPoint b) {
        canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        surfaceResource = R.id.preview_view;

        super.onCreate(savedInstanceState);
        if (Util.SpecialModels.contains(Util.getDeviceName())) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.qrscanner_activity);

        inactivityTimer = new InactivityTimer(this);

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }


    @Override
    public void onResume() {
        super.onResume();

        viewfinderView = this.findViewById(R.id.viewfinder_view);

        if (viewfinderView == null) {
            Intent i = this.getIntent();
            this.finish();
            startActivity(i);
            return;
        }

        viewfinderView.setCameraManager(getCameraManager());

        inactivityTimer.onResume();

        Intent intent = this.getIntent();

        if (intent != null) {

            String action = intent.getAction();

            if (Intents.Scan.ACTION.equals(action)) {
                decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);

                if (intent.hasExtra(Intents.Scan.WIDTH)
                        && intent.hasExtra(Intents.Scan.HEIGHT)) {
                    int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
                    int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
                    if (width > 0 && height > 0) {
                        getCameraManager().setManualFramingRect(width, height);
                    }
                }
            }
            characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
        }
    }

    @Override
    public void onPause() {
        inactivityTimer.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    protected void initCamera(SurfaceHolder surfaceHolder) {
        // no super, no preview, since handler
        try {
            cameraManager.openDriver(surfaceHolder, false);
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats, characterSet, cameraManager);
            }

        } catch (Exception ioe) {
            Util.logException(TAG, ioe);
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public void handleDecode(Result rawResult, Bitmap barcode) {
        inactivityTimer.onActivity();

        if (rawResult != null && barcode != null) {
            Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(Util.VIBRATE_DURATION);
            drawResultPoints(barcode, rawResult);

            Intent returnIntent = new Intent();
            returnIntent.putExtra(Util.RESULT, rawResult.getText());
            setResult(RESULT_OK, returnIntent);
        } else {
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
        }
        finish();
    }

    private void drawResultPoints(Bitmap barcode, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.result_points));
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
                drawLine(canvas, paint, points[0], points[1]);
            } else if (points.length == 4
                    && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult
                    .getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                drawLine(canvas, paint, points[0], points[1]);
                drawLine(canvas, paint, points[2], points[3]);
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    canvas.drawPoint(point.getX(), point.getY(), paint);
                }
            }
        }
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }
}
