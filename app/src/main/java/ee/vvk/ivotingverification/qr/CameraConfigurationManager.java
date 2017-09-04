/**
 * This file incorporates work covered by the following copyright and  
 * permission notice:  
 * 
 * Copyright (C) 2008 ZXing authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/



package ee.vvk.ivotingverification.qr;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.Collection;

import ee.vvk.ivotingverification.util.Util;

/**
 * A class which deals with reading, parsing, and setting the camera parameters
 * which are used to configure the camera hardware.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */

final class CameraConfigurationManager {

	private static final String TAG = "CameraConfiguration";
	private static final int MIN_PREVIEW_PIXELS = 320 * 240; // small screen
	private final Context context;
	private Point screenResolution;
	private Point cameraResolution;

	CameraConfigurationManager(Context context) {
		this.context = context;
	}

	void initFromCameraParameters(Camera camera) {

		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		int width = 0;
		int height = 0;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			Point size = new Point();
			manager.getDefaultDisplay().getSize(size);
			width = size.x;
			height = size.y;
		} else {
			Display d = manager.getDefaultDisplay();
			width = d.getWidth();
			height = d.getHeight();
		}

		if (width < height) {
			int temp = width;
			width = height;
			height = temp;
		}

		screenResolution = new Point(width, height);

		if (Util.DEBUGGABLE) {
			Log.i(TAG, "Screen resolution: " + screenResolution);
		}

		cameraResolution = findBestPreviewSizeValue(parameters,
				screenResolution, false);

		if (Util.DEBUGGABLE) {
			Log.i(TAG, "Camera resolution: " + cameraResolution);
		}
	}

	void setDesiredCameraParameters(Camera camera, int cameraId, int screenRotation) {

		Camera.Parameters parameters = camera.getParameters();

		setCameraDisplayOrientation(camera, cameraId, screenRotation);

		if (parameters == null) {
			if (Util.DEBUGGABLE) {
				Log.w(TAG,
						"Device error: no camera parameters are available. Proceeding without configuration.");
			}
			return;
		}

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		initializeTorch(parameters, prefs);

		String focusMode = findSettableValue(
				parameters.getSupportedFocusModes(),
				Camera.Parameters.FOCUS_MODE_AUTO,
				Camera.Parameters.FOCUS_MODE_MACRO);

		if (focusMode != null) {
			parameters.setFocusMode(focusMode);
		}
		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		camera.setParameters(parameters);

	}

	Point getCameraResolution() {
		return cameraResolution;
	}

	Point getScreenResolution() {
		return screenResolution;
	}

	void setTorch(Camera camera, boolean newSetting) {
		Camera.Parameters parameters = camera.getParameters();

		doSetTorch(parameters, newSetting);
		camera.setParameters(parameters);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		boolean currentSetting = prefs.getBoolean(
				PreferencesActivity.KEY_FRONT_LIGHT, false);

		if (currentSetting != newSetting) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(PreferencesActivity.KEY_FRONT_LIGHT, newSetting);
			editor.commit();
		}
	}

	private static void initializeTorch(Camera.Parameters parameters,
			SharedPreferences prefs) {
		doSetTorch(parameters, false);
	}

	private static void doSetTorch(Camera.Parameters parameters,
			boolean newSetting) {
		String flashMode;

		if (newSetting) {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(),
					Camera.Parameters.FLASH_MODE_TORCH,
					Camera.Parameters.FLASH_MODE_ON);
		} else {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(),
					Camera.Parameters.FLASH_MODE_OFF);
		}
		if (flashMode != null) {
			parameters.setFlashMode(flashMode);
		}
	}

	private static Point findBestPreviewSizeValue(Camera.Parameters parameters,
			Point screenResolution, boolean portrait) {

		Point bestSize = null;

		double diff = Double.MAX_VALUE;

		for (Camera.Size supportedPreviewSize : parameters
				.getSupportedPreviewSizes()) {
			int pixels = supportedPreviewSize.height
					* supportedPreviewSize.width;

			if (pixels < MIN_PREVIEW_PIXELS || pixels > screenResolution.x * screenResolution.y * 1.20) {
				continue;
			}

			double supportedWidth = portrait ? supportedPreviewSize.height
					: supportedPreviewSize.width;
			double supportedHeight = portrait ? supportedPreviewSize.width
					: supportedPreviewSize.height;
			double newDiff = Math.abs(screenResolution.y / supportedHeight
					- screenResolution.x / supportedWidth);

			if (newDiff == 0) {
				bestSize = new Point((int)supportedWidth,(int) supportedHeight);
				break;
			}

			if (newDiff < diff) {
				bestSize = new Point((int)supportedWidth, (int)supportedHeight);
				diff = newDiff;
			}
		}

		if (bestSize == null) {
			Camera.Size defaultSize = parameters.getPreviewSize();
			bestSize = new Point(defaultSize.width, defaultSize.height);
		}
		return bestSize;
	}

	private static String findSettableValue(Collection<String> supportedValues,
			String... desiredValues) {

		if (Util.DEBUGGABLE) {
			Log.i(TAG, "Supported values: " + supportedValues);
		}
		String result = null;
		if (supportedValues != null) {
			for (String desiredValue : desiredValues) {
				if (supportedValues.contains(desiredValue)) {
					result = desiredValue;
					break;
				}
			}
		}

		if (Util.DEBUGGABLE) {
			Log.i(TAG, "Settable value: " + result);
		}
		return result;
	}

	private static void setCameraDisplayOrientation(Camera camera, int cameraId, int rotation) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);

		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0: degrees = 0; break;
			case Surface.ROTATION_90: degrees = 90; break;
			case Surface.ROTATION_180: degrees = 180; break;
			case Surface.ROTATION_270: degrees = 270; break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}
}
