package ee.vvk.ivotingverification;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

import ee.vvk.ivotingverification.qr.CameraManager;
import ee.vvk.ivotingverification.qr.CaptureActivityHandler;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.Util;

/**
 * Main activity. The first screen.
 * 
 * @version 16.05.2013
 */
public class ErrorActivity extends Activity implements SurfaceHolder.Callback {

	private static final String TAG = ErrorActivity.class.getSimpleName();

	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private boolean hasSurface;
	private SurfaceView surfaceView;
	private String errorMessage;
	private boolean networkStatus;
	private Button buttonWifi;
	private Button buttonOk;
	private ImageView buttonClose;
	private boolean connectionButtonIsClicked = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Intent intent = getIntent();
		errorMessage = intent.getStringExtra(Util.ERROR_MESSAGE);
		networkStatus = intent.getBooleanExtra(Util.NETWORK_STATUS, true);

		hasSurface = false;

		setContentView(R.layout.error_activity);

		if (Util.DEBUGGABLE) {
			Log.e(TAG, "Message:" + errorMessage);
		}

		initErrorWindow(errorMessage, networkStatus);
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

		if (connectionButtonIsClicked) {
			finish();
			Intent intent = new Intent(getApplicationContext(),
					MainActivity.class);
			startActivity(intent);
		}

		cameraManager = new CameraManager(this);

		if (cameraManager == null) {
			Intent i = this.getIntent();
			this.finish();
			startActivity(i);
			return;
		}

		surfaceView = (SurfaceView) this.findViewById(R.id.surface_error);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder, false);
		} else {
			surfaceHolder.addCallback(this);
		}
	}

	@Override
	public void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) this
					.findViewById(R.id.surface_error);
			if (surfaceView != null) {
				SurfaceHolder surfaceHolder = surfaceView.getHolder();
				surfaceHolder.removeCallback(this);
			}
		}
		super.onPause();
	}

	private void initCamera(SurfaceHolder surfaceHolder, boolean flashlight) {
		try {
			cameraManager.openDriver(surfaceHolder, flashlight);
			if (handler == null) {
				cameraManager.startPreview();
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

	private void initErrorWindow(String errorMessage, boolean isNetwork) {

		LinearLayout frameBg = (LinearLayout) findViewById(R.id.frame_bg_error);
		frameBg.setBackgroundColor(Util
				.generateHexColorValue(C.frameBackground));

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.target_window_error);
		GradientDrawable bgShape = (GradientDrawable) linearLayout
				.getBackground();
		bgShape.setColor(Util.generateHexColorValue(C.errorWindow));

		linearLayout = (LinearLayout) findViewById(R.id.target_window_error_shadow);
		GradientDrawable bgShapeShadow = (GradientDrawable) linearLayout
				.getBackground();
		bgShapeShadow.setColor(Util.generateHexColorValue(C.errorWindowShadow));

		TextView textViewTitle = (TextView) findViewById(R.id.text_title_error);
		TextView textViewMessage = (TextView) findViewById(R.id.text_message_error);
		textViewTitle.setTypeface(C.typeFace);
		textViewMessage.setTypeface(C.typeFace);
		textViewMessage.setText(errorMessage);
		textViewTitle.setVisibility(View.INVISIBLE);
		textViewTitle.setTextColor(Util
				.generateHexColorValue(C.errorWindowForeground));
		textViewMessage.setVisibility(View.VISIBLE);
		textViewMessage.setTextColor(Util
				.generateHexColorValue(C.errorWindowForeground));

		buttonWifi = (Button) findViewById(R.id.btn_wifi);
		buttonWifi.setTextColor(Util.generateHexColorValue(C.btnForeground));
		GradientDrawable bgWifiShape = (GradientDrawable) buttonWifi
				.getBackground();
		buttonWifi.setTypeface(C.typeFace);
		bgWifiShape.setColor(Util.generateHexColorValue(C.btnBackground));

		buttonOk = (Button) findViewById(R.id.btn_ok);
		buttonOk.setTextColor(Util.generateHexColorValue(C.btnForeground));
		GradientDrawable bgOkShape = (GradientDrawable) buttonOk
				.getBackground();
		buttonOk.setTypeface(C.typeFace);
		bgOkShape.setColor(Util.generateHexColorValue(C.btnBackground));

		if (!isNetwork) {
			buttonWifi.setText(C.btnWifi);
			buttonWifi.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			buttonWifi.setVisibility(View.VISIBLE);
			buttonWifi.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					clickWifiButton(v);
				}
			});

			buttonOk.setText(C.btnPacketData);
			buttonOk.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			buttonOk.setVisibility(View.VISIBLE);
			buttonOk.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					clickPacketDataButton(v);
				}
			});
		} else {
			buttonWifi.setText(C.btnMore);
			buttonWifi.setVisibility(View.VISIBLE);
			buttonWifi.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					clickMoreButton(v);
				}
			});

			buttonOk.setText(C.btnOk);
			buttonOk.setVisibility(View.VISIBLE);
			buttonOk.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}

		buttonClose = (ImageView) findViewById(R.id.close_error);
		buttonClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	public void clickMoreButton(View view) {
		Intent helpActivity = new Intent(this, HelpActivity.class);
		startActivity(helpActivity);
	}

	public void clickPacketDataButton(View view) {
		connectionButtonIsClicked = true;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			ErrorActivity.this.startActivity(new Intent(
					Settings.ACTION_SETTINGS));
		} else {
			try {
				ErrorActivity.this.startActivity(new Intent(
						Settings.ACTION_DATA_ROAMING_SETTINGS));
			} catch (Exception e) {
				ErrorActivity.this.startActivity(new Intent(
						Settings.ACTION_SETTINGS));
			}
		}
	}

	public void clickWifiButton(View view) {
		connectionButtonIsClicked = true;
		ErrorActivity.this.startActivity(new Intent(
				Settings.ACTION_WIFI_SETTINGS));
	}

}
