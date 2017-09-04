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

/**
 * This class provides the constants to use when sending an Intent to Barcode
 * Scanner. These strings are effectively API and cannot be changed.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class Intents {
	private Intents() {
	}

	public static final class Scan {
		public static final String ACTION = "com.google.zxing.client.android.SCAN";

		public static final String MODE = "SCAN_MODE";

		public static final String PRODUCT_MODE = "PRODUCT_MODE";

		public static final String ONE_D_MODE = "ONE_D_MODE";
		public static final String QR_CODE_MODE = "QR_CODE_MODE";
		public static final String DATA_MATRIX_MODE = "DATA_MATRIX_MODE";

		public static final String FORMATS = "SCAN_FORMATS";
		public static final String CHARACTER_SET = "CHARACTER_SET";

		public static final String WIDTH = "SCAN_WIDTH";
		public static final String HEIGHT = "SCAN_HEIGHT";

		public static final String RESULT_DISPLAY_DURATION_MS = "RESULT_DISPLAY_DURATION_MS";
		public static final String PROMPT_MESSAGE = "PROMPT_MESSAGE";

		public static final String RESULT = "SCAN_RESULT";

		public static final String RESULT_FORMAT = "SCAN_RESULT_FORMAT";

		public static final String RESULT_BYTES = "SCAN_RESULT_BYTES";

		public static final String RESULT_ORIENTATION = "SCAN_RESULT_ORIENTATION";

		public static final String RESULT_ERROR_CORRECTION_LEVEL = "SCAN_RESULT_ERROR_CORRECTION_LEVEL";

		public static final String RESULT_BYTE_SEGMENTS_PREFIX = "SCAN_RESULT_BYTE_SEGMENTS_";

		private Scan() {
		}
	}
}
