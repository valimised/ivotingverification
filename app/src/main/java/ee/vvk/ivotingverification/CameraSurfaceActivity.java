package ee.vvk.ivotingverification;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import ee.vvk.ivotingverification.qr.CameraManager;
import ee.vvk.ivotingverification.util.Util;


public abstract class CameraSurfaceActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = CameraSurfaceActivity.class.getSimpleName();

    protected CameraManager cameraManager;
    private boolean hasSurface;

    protected int surfaceResource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        hasSurface = false;

    }

    @Override
    public void onResume() {
        super.onResume();

        cameraManager = new CameraManager(this);

        SurfaceView surfaceView = this.findViewById(surfaceResource);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
        }
    }

    @Override
    public void onPause() {
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = this.findViewById(surfaceResource);
            if (surfaceView != null) {
                SurfaceHolder surfaceHolder = surfaceView.getHolder();
                surfaceHolder.removeCallback(this);
            }
        }
        super.onPause();
    }

    protected void initCamera(SurfaceHolder surfaceHolder) {
        try {
            cameraManager.openDriver(surfaceHolder, false);
            cameraManager.startPreview();
        } catch (Exception ioe) {
            Util.logException(TAG, ioe);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Util.logError(TAG,"*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

}
