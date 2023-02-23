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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import ee.vvk.ivotingverification.R;
import ee.vvk.ivotingverification.util.Util;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192,
            128, 64};
    private static final long ANIMATION_DELAY = 80L;
    private static final int POINT_SIZE = 6;

    private final Paint paint;
    private final int maskColor;
    private final int laserColor;
    private int scannerAlpha;
    private final Context context;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskColor = ContextCompat.getColor(context, R.color.viewfinder_mask);
        laserColor = ContextCompat.getColor(context, R.color.viewfinder_laser);
        scannerAlpha = 0;
    }

    @Override
    public void onDraw(Canvas canvas) {

        int width = getWidth();
        int height = getHeight();
        int frameSize = (int) Util.convertDpToPixels(235, context);

        int xTopLeft = (width - frameSize) / 2;
        int yTopLeft = (height - frameSize) / 2;
        int xBottomRight = xTopLeft + frameSize;
        int yBottomRight = yTopLeft + frameSize;

        Rect frame = new Rect(xTopLeft, yTopLeft, xBottomRight, yBottomRight);

        paint.setColor(maskColor);
        paint.setColor(0xAA444444);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
                paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        paint.setColor(laserColor);
        paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
        scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
        int middle = frame.height() / 2 + frame.top;
        canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1,
                middle + 2, paint);

        postInvalidateDelayed(ANIMATION_DELAY, frame.left - POINT_SIZE,
                frame.top - POINT_SIZE, frame.right + POINT_SIZE,
                frame.bottom + POINT_SIZE);
    }
}
