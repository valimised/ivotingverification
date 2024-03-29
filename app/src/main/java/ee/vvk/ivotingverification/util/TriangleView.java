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
	private int color;
	private String number;
	private Context context;

	public TriangleView(Context context, int color, String number) {
		super(context);
		this.color = color;
		this.number = number;
		this.context = context;
		commonConstructor();
	}

	public TriangleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		commonConstructor();
	}

	private void commonConstructor() {
		setBackgroundColor(Color.TRANSPARENT);

		trianglePaint = new Paint();
		trianglePaint.setStyle(Style.FILL);
		trianglePaint.setColor(color);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Path trianglePath = getTriangle(super.getWidth());
		canvas.drawPath(trianglePath, trianglePaint);

		Paint mTextPaint = new Paint();
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD,
				Typeface.BOLD));
		mTextPaint.setTextSize((int) Util.convertDpToPixels(number.length() == 4 ? 16 : 18, context));
		mTextPaint.setColor(Util.generateHexColorValue(C.lblForeground));
		canvas.save();
		canvas.rotate(-45);
		canvas.drawText(number, (int) Util.convertDpToPixels(number.length() == 4 ? -20 : -15, context),
				(int) Util.convertDpToPixels(30, context), mTextPaint);
		canvas.restore();
	}

	private Path getTriangle(int l) {
		
		Point p1 = new Point(0, l);
		Point p2 = new Point(l, l);
		Point p3 = new Point(l, 0);

		Path path = new Path();
		path.moveTo(p1.x, p1.y);
		path.lineTo(p2.x, p2.y);
		path.lineTo(p3.x, p3.y);

		return path;
	}
}
