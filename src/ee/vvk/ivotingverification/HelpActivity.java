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
 
package ee.vvk.ivotingverification;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import ee.vvk.ivotingverification.dialog.LoadingSpinner;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.Util;

/**
 * Help activity. Web view.
 * 
 * @version 13.05.2013
 */
public class HelpActivity extends Activity {

	private WebView wv;
	private AlertDialog alert;
	private LoadingSpinner mLoadingSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.help_activity);
		init(savedInstanceState);

		mLoadingSpinner = Util.startSpinner(HelpActivity.this, false);
	}

	private void init(Bundle savedInstanceState) {
		wv = (WebView) findViewById(R.id.webView);
		wv.setWebViewClient(new InsideWebViewClient());
		wv.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					final JsResult result) {
				alert = new AlertDialog.Builder(HelpActivity.this)
						.setTitle("VVK:")
						.setMessage(message)
						.setPositiveButton(android.R.string.ok,
								new AlertDialog.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.confirm();
									}
								})
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {
									public void onCancel(DialogInterface dialog) {
										result.cancel();
									}
								}).setCancelable(true).create();
				alert.show();

				return true;
			};

			@Override
			public boolean onJsConfirm(WebView view, String url,
					String message, final JsResult result) {
				alert = new AlertDialog.Builder(HelpActivity.this)
						.setTitle("VVK:")
						.setMessage(message)
						.setCancelable(true)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.confirm();
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.cancel();
									}
								})
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {
									public void onCancel(DialogInterface dialog) {
										result.cancel();
									}
								}).create();
				alert.show();

				return true;
			};

			@Override
			public void onGeolocationPermissionsShowPrompt(String origin,
					GeolocationPermissions.Callback callback) {
				callback.invoke(origin, false, false);
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message,
					String defaultValue, final JsPromptResult result) {

				alert = new AlertDialog.Builder(HelpActivity.this)
						.setTitle("VVK:")
						.setMessage(message)
						.setCancelable(true)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										result.confirm();
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										result.cancel();
									}
								})
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {
									public void onCancel(DialogInterface dialog) {
										result.cancel();
									}
								}).create();
				alert.show();

				return true;
			};

		});

		wv.requestFocus(View.FOCUS_DOWN);
		wv.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_UP:
					if (!v.hasFocus()) {
						v.requestFocus();
					}
					break;
				}
				return false;
			}
		});

		wv.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		WebSettings webSettings = wv.getSettings();

		wv.getSettings().setGeolocationEnabled(false);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		wv.getSettings().setSupportMultipleWindows(false);
		wv.getSettings().setAllowFileAccess(false);
		wv.getSettings().setPluginState(PluginState.ON_DEMAND);
		wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

		String uaString = webSettings.getUserAgentString();
		try {
			uaString += " VVK/"
					+ this.getPackageManager().getPackageInfo(
							this.getPackageName(), 0).versionName;
			webSettings.setUserAgentString(uaString);
		} catch (NameNotFoundException e) {
			if (Util.DEBUGGABLE) {
				Log.e("Error getting app version", "" + e.getMessage());
			}
		}
		if (C.helpURL.length() > 0) {
			wv.loadUrl(C.helpURL);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && wv.canGoBack()) {
			wv.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		wv.saveState(outState);
	}

	private class InsideWebViewClient extends WebViewClient {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			Util.stopSpinner(mLoadingSpinner);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
			handler.proceed();
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			if (url != null)
				view.loadUrl(url);

			return true;
		}
	}
}