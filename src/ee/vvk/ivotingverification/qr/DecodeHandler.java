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

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import ee.vvk.ivotingverification.QRScannerActivity;
import ee.vvk.ivotingverification.R;
import ee.vvk.ivotingverification.util.Util;

import java.util.Map;

final class DecodeHandler extends Handler {

	private static final String TAG = DecodeHandler.class.getSimpleName();

	private final QRScannerActivity activity;
	private final MultiFormatReader multiFormatReader;
	private boolean running = true;

	DecodeHandler(QRScannerActivity activity, Map<DecodeHintType, Object> hints) {
		multiFormatReader = new MultiFormatReader();
		multiFormatReader.setHints(hints);
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message message) {
		if (!running) {
			return;
		}

		if (message.what == R.id.decode) {
			decode((byte[]) message.obj, message.arg1, message.arg2);
		} else if (message.what == R.id.quit) {
			running = false;
			Looper.myLooper().quit();
		}
	}

	private void decode(byte[] data, int width, int height) {
		long start = System.currentTimeMillis();
		Result rawResult = null;
		PlanarYUVLuminanceSource source;

		if (! Util.SpecialModels.contains(Util.getDeviceName())){
			byte[] rotatedData = new byte[data.length];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++)
					rotatedData[x * height + height - y - 1] = data[x + y * width];
			}
			data = rotatedData;
			int tmp = width;
			width = height;
			height = tmp;

		}
		source = activity.getCameraManager().buildLuminanceSource(data,
				width, height);
		if (source != null) {
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			try {
				rawResult = multiFormatReader.decodeWithState(bitmap);
			} catch (ReaderException re) {
			} finally {
				multiFormatReader.reset();
			}
		}

		Handler handler;
		handler = activity.getHandler();
		if (rawResult != null) {
			long end = System.currentTimeMillis();
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Found barcode in " + (end - start) + " ms");
			}
			if (handler != null) {
				Message message = Message.obtain(handler,
						R.id.decode_succeeded, rawResult);
				Bundle bundle = new Bundle();
				bundle.putParcelable(DecodeThread.BARCODE_BITMAP,
						source.renderCroppedGreyscaleBitmap());
				message.setData(bundle);
				message.sendToTarget();
			}
		} else {
			if (handler != null) {
				Message message = Message.obtain(handler, R.id.decode_failed);
				message.sendToTarget();
			}
		}
	}
}
