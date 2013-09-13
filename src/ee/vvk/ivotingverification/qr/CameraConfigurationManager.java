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
 *
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

import java.util.Collection;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
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
	private static final int MAX_PREVIEW_PIXELS = 800 * 480; // large/HD screen
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

		Display display = manager.getDefaultDisplay();

		int width = display.getWidth();
		int height = display.getHeight();

		if (width < height) {
			if (Util.DEBUGGABLE) {
				Log.i(TAG,
						"Display reports portrait orientation; assuming this is incorrect");
			}

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

	void setDesiredCameraParameters(Camera camera) {

		Camera.Parameters parameters = camera.getParameters();
		camera.setDisplayOrientation(90);
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

		int diff = Integer.MAX_VALUE;

		for (Camera.Size supportedPreviewSize : parameters
				.getSupportedPreviewSizes()) {

			int pixels = supportedPreviewSize.height
					* supportedPreviewSize.width;

			if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
				continue;
			}

			int supportedWidth = portrait ? supportedPreviewSize.height
					: supportedPreviewSize.width;
			int supportedHeight = portrait ? supportedPreviewSize.width
					: supportedPreviewSize.height;
			int newDiff = Math.abs(screenResolution.x * supportedHeight
					- supportedWidth * screenResolution.y);

			if (newDiff == 0) {
				bestSize = new Point(supportedWidth, supportedHeight);
				break;
			}

			if (newDiff < diff) {
				bestSize = new Point(supportedWidth, supportedHeight);
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
}