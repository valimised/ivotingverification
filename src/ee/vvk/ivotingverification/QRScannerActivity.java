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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import ee.vvk.ivotingverification.qr.*;
import ee.vvk.ivotingverification.util.Util;

import java.io.IOException;
import java.util.Collection;

/**
 * QR code decoder activity.
 * 
 * @version 13.05.2013
 */
public class QRScannerActivity extends Activity implements
		SurfaceHolder.Callback {

	private static final String TAG = QRScannerActivity.class.getSimpleName();

	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private TextView statusView;
	private boolean hasSurface;
	private Collection<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Util.SpecialModels.contains(Util.getDeviceName())){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.qrscanner_activity);

		hasSurface = false;
		if (this != null)
			inactivityTimer = new InactivityTimer(this);

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	public void onResume() {
		super.onResume();

		cameraManager = new CameraManager(this);

		if (cameraManager == null) {
			Intent i = this.getIntent();
			this.finish();
			startActivity(i);
			return;
		}

		viewfinderView = (ViewfinderView) this
				.findViewById(R.id.viewfinder_view);

		if (viewfinderView == null) {
			Intent i = this.getIntent();
			this.finish();
			startActivity(i);
			return;
		}

		viewfinderView.setCameraManager(cameraManager);

		handler = null;

		SurfaceView surfaceView = (SurfaceView) this
				.findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder, false);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		inactivityTimer.onResume();

		Intent intent = this.getIntent();

		decodeFormats = null;
		characterSet = null;

		if (intent != null) {
			String action = intent.getAction();

			if (Intents.Scan.ACTION.equals(action)) {
				decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);

				if (intent.hasExtra(Intents.Scan.WIDTH)
						&& intent.hasExtra(Intents.Scan.HEIGHT)) {
					int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
					int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
					if (width > 0 && height > 0) {
						cameraManager.setManualFramingRect(width, height);
					}
				}

				String customPromptMessage = intent
						.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
				if (customPromptMessage != null) {
					statusView.setText(customPromptMessage);
				}
			}
			characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
		}
	}

	@Override
	public void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) this
					.findViewById(R.id.preview_view);
			if (surfaceView != null) {
				SurfaceHolder surfaceHolder = surfaceView.getHolder();
				surfaceHolder.removeCallback(this);
			}
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG,
						"*** WARNING *** surfaceCreated() gave us a null surface!");
			}
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder, false);
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

	public void handleDecode(Result rawResult, Bitmap barcode) {
		inactivityTimer.onActivity();

		if (rawResult != null && barcode != null) {
			Vibrator vibrator = (Vibrator) this
					.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(Util.VIBRATE_DURATION);
			drawResultPoints(barcode, rawResult);

			Intent returnIntent = new Intent();
			returnIntent.putExtra(Util.RESULT, rawResult.getText());
			setResult(RESULT_OK, returnIntent);
			finish();
		} else {
			Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED, returnIntent);
			finish();
		}
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

	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a,
			ResultPoint b) {
		canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
	}

	private void initCamera(SurfaceHolder surfaceHolder, boolean flashlight) {
		try {
			cameraManager.openDriver(surfaceHolder, flashlight);
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						characterSet, cameraManager);
			}
		} catch (IOException ioe) {
			if (Util.DEBUGGABLE) {
				Log.e(TAG, "Error:" + ioe);
			}
		} catch (RuntimeException e) {
			if (Util.DEBUGGABLE) {
				Log.w(TAG, "Unexpected error initializing camera", e);
			}
		}
	}

	public void restartPreviewAfterDelay(long delayMS) {

		if (handler != null) {
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}

	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}
}