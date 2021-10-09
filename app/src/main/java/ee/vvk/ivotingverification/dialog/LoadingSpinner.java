package ee.vvk.ivotingverification.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import ee.vvk.ivotingverification.R;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.Util;

/**
 * Loading spinner dialog.
 * 
 * @version 21.05.2013
 */
public class LoadingSpinner extends Dialog {
	private final Context cx;
	private final boolean isWhite;

	public LoadingSpinner(Context context, boolean isWhite) {
		super(context, R.style.SpinnerDialog);
		this.cx = context;
		this.isWhite = isWhite;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isWhite) {
			setContentView(R.layout.dialog_loading);
		} else {
			setContentView(R.layout.dialog_loading_black);
		}
		setCancelable(false);
		ImageView iv = findViewById(R.id.spinner_img);
		iv.startAnimation(AnimationUtils.loadAnimation(cx, R.anim.spinner));

		TextView tv = findViewById(R.id.loading);
		if (C.typeFace != null) {
			tv.setTypeface(C.typeFace);
		}
		tv.setText(C.loading);
		if (isWhite) {
			tv.setTextColor(Util
					.generateHexColorValue(C.loadingWindowForeground));
		} else {
			tv.setTextColor(Color.BLACK);
		}
		LayoutParams params = getWindow().getAttributes();
		params.height = LayoutParams.MATCH_PARENT;
		params.width = LayoutParams.MATCH_PARENT;
		getWindow().setAttributes(
				params);
	}
}
