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

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import ee.vvk.ivotingverification.util.Util;

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CameraManager {

	private static final String TAG = CameraManager.class.getSimpleName();
	private static final int MIN_FRAME_WIDTH = 240;
	private static final int MIN_FRAME_HEIGHT = 240;
	private static final int MAX_FRAME_WIDTH = 600;
	private static final int MAX_FRAME_HEIGHT = 400;
	private final Context context;
	private final CameraConfigurationManager configManager;
	private Camera camera;
	private Rect framingRect;
	private Rect framingRectInPreview;
	private boolean initialized;
	private boolean previewing;
	private boolean reverseImage;
	private int requestedFramingRectWidth;
	private int requestedFramingRectHeight;
	private final PreviewCallback previewCallback;
	private final AutoFocusCallback autoFocusCallback;

	public CameraManager(Context context) {

		this.context = context;
		this.configManager = new CameraConfigurationManager(context);
		previewCallback = new PreviewCallback(configManager);
		autoFocusCallback = new AutoFocusCallback();
	}

	public void openDriver(SurfaceHolder holder, boolean turnOnFlash)
			throws IOException {

		Camera theCamera = camera;
		if (theCamera == null) {
			theCamera = Camera.open();

			if (theCamera == null) {
				throw new IOException();
			}
			camera = theCamera;
		}
		theCamera.setPreviewDisplay(holder);

		if (!initialized) {

			initialized = true;
			configManager.initFromCameraParameters(theCamera);

			if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {

				setManualFramingRect(requestedFramingRectWidth,
						requestedFramingRectHeight);
				requestedFramingRectWidth = 0;
				requestedFramingRectHeight = 0;
			}
		}
		configManager.setDesiredCameraParameters(theCamera);

		if (turnOnFlash) {
			configManager.setTorch(theCamera, true);
		}

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		reverseImage = prefs.getBoolean(PreferencesActivity.KEY_REVERSE_IMAGE,
				false);
	}

	public void closeDriver() {

		if (camera != null) {
			camera.release();
			camera = null;
			framingRect = null;
			framingRectInPreview = null;
		}
	}

	public void startPreview() {

		Camera theCamera = camera;
		if (theCamera != null && !previewing) {
			theCamera.startPreview();
			previewing = true;
		}
	}

	public void stopPreview() {

		if (camera != null && previewing) {
			camera.stopPreview();
			previewCallback.setHandler(null, 0);
			autoFocusCallback.setHandler(null, 0);
			previewing = false;
		}
	}

	public void requestPreviewFrame(Handler handler, int message) {

		Camera theCamera = camera;
		if (theCamera != null && previewing) {
			previewCallback.setHandler(handler, message);
			theCamera.setOneShotPreviewCallback(previewCallback);
		}
	}

	public void requestAutoFocus(Handler handler, int message) {

		if (camera != null && previewing) {
			autoFocusCallback.setHandler(handler, message);
			try {
				camera.autoFocus(autoFocusCallback);
			} catch (RuntimeException re) {
				if (Util.DEBUGGABLE) {
					Log.w(TAG, "Unexpected exception while focusing", re);
				}
			}
		}
	}

	public Rect getFramingRect() {

		if (framingRect == null) {
			if (camera == null) {
				return null;
			}
			Point screenResolution = configManager.getScreenResolution();
			int width = screenResolution.x * 3 / 4;
			if (width < MIN_FRAME_WIDTH) {
				width = MIN_FRAME_WIDTH;
			} else if (width > MAX_FRAME_WIDTH) {
				width = MAX_FRAME_WIDTH;
			}
			int height = screenResolution.y * 3 / 4;
			if (height < MIN_FRAME_HEIGHT) {
				height = MIN_FRAME_HEIGHT;
			} else if (height > MAX_FRAME_HEIGHT) {
				height = MAX_FRAME_HEIGHT;
			}

			int leftOffset = (screenResolution.x - width) / 2;
			int topOffset = (screenResolution.y - height - 40) / 2;
			framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
					topOffset + height - 40);
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Calculated framing rect: " + framingRect);
			}
		}
		return framingRect;
	}

	public Rect getFramingRectInPreview() {
		if (framingRectInPreview == null) {

			Rect framingRect = getFramingRect();
			if (framingRect == null) {
				return null;
			}

			Rect rect = new Rect(framingRect);
			Point cameraResolution = configManager.getCameraResolution();
			Point screenResolution = configManager.getScreenResolution();
			rect.left = rect.left * cameraResolution.y / screenResolution.x;
			rect.right = rect.right * cameraResolution.y / screenResolution.x;
			rect.top = rect.top * cameraResolution.x / screenResolution.y;
			rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
			framingRectInPreview = rect;
		}
		return framingRectInPreview;
	}

	public void setManualFramingRect(int width, int height) {
		if (initialized) {

			Point screenResolution = configManager.getScreenResolution();
			if (width > screenResolution.x) {
				width = screenResolution.x;
			}
			if (height > screenResolution.y) {
				height = screenResolution.y;
			}
			int leftOffset = (screenResolution.x - width) / 2;
			int topOffset = (screenResolution.y - height) / 2;
			framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
					topOffset + height);
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Calculated manual framing rect: " + framingRect);
			}
			framingRectInPreview = null;
		} else {
			requestedFramingRectWidth = width;
			requestedFramingRectHeight = height;
		}
	}

	public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data,
			int width, int height) {

		Rect rect = getFramingRectInPreview();
		if (rect == null) {
			return null;
		}
		return new PlanarYUVLuminanceSource(data, width, height, rect.left,
				rect.top, rect.width(), rect.height(), reverseImage);
	}

	public void turnOnFlash(boolean turnOn) {

		if (camera != null) {
			try {
				if (turnOn) {
					Parameters p = camera.getParameters();
					p.setFlashMode(Parameters.FLASH_MODE_TORCH);
					camera.setParameters(p);
					stopPreview();
					startPreview();
				} else {
					camera.stopPreview();
					Parameters p = camera.getParameters();
					p.setFlashMode(Parameters.FLASH_MODE_OFF);
					camera.setParameters(p);
					stopPreview();
					startPreview();
				}
			} catch (Exception e) {
				if (Util.DEBUGGABLE) {
					Log.e("Error initiating or closing camera flash",
							"" + e.getMessage());
				}
			}
		}
	}
}