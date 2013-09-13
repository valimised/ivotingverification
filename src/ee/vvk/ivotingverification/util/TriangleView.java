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
 
package ee.vvk.ivotingverification.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Triangle view.
 * 
 * @version 16.05.2013
 */
public class TriangleView extends LinearLayout {

	private Paint trianglePaint;
	private Path trianglePath;
	private int color;
	private String number;
	private Context context;

	public TriangleView(Context context, int color, String number) {
		super(context);
		this.color = color;
		this.number = number;
		this.context = context;
		commonConstructor(context);
	}

	public TriangleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		commonConstructor(context);
	}

	private void commonConstructor(Context context) {
		setBackgroundColor(Color.TRANSPARENT);

		trianglePaint = new Paint();
		trianglePaint.setStyle(Style.FILL);
		trianglePaint.setColor(color);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		trianglePath = getTriangle(super.getWidth());
		canvas.drawPath(trianglePath, trianglePaint);

		Paint mTextPaint = new Paint();
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD,
				Typeface.BOLD));
		mTextPaint.setTextSize((int) Util.convertPixelsToDp(18, context));
		mTextPaint.setColor(Util.generateHexColorValue(C.lblForeground));
		canvas.save();
		canvas.rotate(-45);
		canvas.drawText(number, (int) Util.convertPixelsToDp(-15, context),
				(int) Util.convertPixelsToDp(30, context), mTextPaint);
		canvas.restore();
	}

	private Path getTriangle(int l) {
		Point p1 = null, p2 = null, p3 = null;

		p1 = new Point(0, l);
		p2 = new Point(l, l);
		p3 = new Point(l, 0);

		Path path = new Path();
		path.moveTo(p1.x, p1.y);
		path.lineTo(p2.x, p2.y);
		path.lineTo(p3.x, p3.y);

		return path;
	}
}