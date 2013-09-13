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
	private Context cx;
	private ImageView iv;
	private TextView tv;
	private boolean isWhite;

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
		iv = (ImageView) findViewById(R.id.spinner_img);
		iv.startAnimation(AnimationUtils.loadAnimation(cx, R.anim.spinner));

		tv = (TextView) findViewById(R.id.loading);
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
		params.height = LayoutParams.FILL_PARENT;
		params.width = LayoutParams.FILL_PARENT;
		getWindow().setAttributes(
				(android.view.WindowManager.LayoutParams) params);
	}
}