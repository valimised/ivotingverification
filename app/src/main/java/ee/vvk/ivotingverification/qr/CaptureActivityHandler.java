/*
  This file incorporates work covered by the following copyright and
  permission notice:

  Copyright (C) 2008 ZXing authors

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package ee.vvk.ivotingverification.qr;

import java.util.Collection;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import ee.vvk.ivotingverification.QRScannerActivity;
import ee.vvk.ivotingverification.R;
import ee.vvk.ivotingverification.util.Util;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

	private static final String TAG = CaptureActivityHandler.class
			.getSimpleName();

	private final QRScannerActivity activity;
	private final DecodeThread decodeThread;
	private State state;
	private final CameraManager cameraManager;

	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	public CaptureActivityHandler(QRScannerActivity activity,
			Collection<BarcodeFormat> decodeFormats, String characterSet,
			CameraManager cameraManager) {
		this.activity = activity;
		decodeThread = new DecodeThread(activity, decodeFormats, characterSet,
				new ViewfinderResultPointCallback(activity.getViewfinderView()));
		decodeThread.start();
		state = State.SUCCESS;

		this.cameraManager = cameraManager;
		cameraManager.startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {

		if (message.what == R.id.auto_focus) {
			if (state == State.PREVIEW) {
				cameraManager.requestAutoFocus(this, R.id.auto_focus);
			}
		} else if (message.what == R.id.restart_preview) {
			Util.logDebug(TAG, "Got restart preview message");
			restartPreviewAndDecode();
		} else if (message.what == R.id.decode_succeeded) {
			Util.logDebug(TAG, "Got decode succeeded message");
			state = State.SUCCESS;
			Bundle bundle = message.getData();
			Bitmap barcode = bundle == null ? null : (Bitmap) bundle
					.getParcelable(DecodeThread.BARCODE_BITMAP);
			activity.handleDecode((Result) message.obj, barcode);
		} else if (message.what == R.id.decode_failed) {
			state = State.PREVIEW;
			cameraManager.requestPreviewFrame(decodeThread.getHandler(),
					R.id.decode);
		} else if (message.what == R.id.return_scan_result) {
			Util.logDebug(TAG, "Got return scan result message");
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		cameraManager.stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
		quit.sendToTarget();
		try {
			decodeThread.join(500L);
		} catch (InterruptedException e) {
		}

		removeMessages(R.id.decode_succeeded);
		removeMessages(R.id.decode_failed);
	}

	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			cameraManager.requestPreviewFrame(decodeThread.getHandler(),
					R.id.decode);
			cameraManager.requestAutoFocus(this, R.id.auto_focus);
			activity.drawViewfinder();
		}
	}
}
