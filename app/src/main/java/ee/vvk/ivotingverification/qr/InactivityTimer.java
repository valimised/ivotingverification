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

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Finishes an activity after a period of inactivity if the device is on battery
 * power.
 */
public final class InactivityTimer {

	private static final int INACTIVITY_DELAY_SECONDS = 5 * 60;

	private final ScheduledExecutorService inactivityTimer = Executors
			.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
	private final Activity activity;
	private ScheduledFuture<?> inactivityFuture = null;
	private final BroadcastReceiver powerStatusReceiver = new PowerStatusReceiver();

	public InactivityTimer(Activity activity) {
		this.activity = activity;
		onActivity();
	}

	public void onActivity() {
		cancel();
		if (!inactivityTimer.isShutdown()) {
			try {
				inactivityFuture = inactivityTimer.schedule(new FinishListener(
						activity), INACTIVITY_DELAY_SECONDS, TimeUnit.SECONDS);
			} catch (RejectedExecutionException ree) {
			}
		}
	}

	public void onPause() {
		cancel();
		try {
			activity.unregisterReceiver(powerStatusReceiver);
		} catch (Exception e) {
		}
	}

	public void onResume() {
		activity.registerReceiver(powerStatusReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		onActivity();
	}

	private void cancel() {
		ScheduledFuture<?> future = inactivityFuture;
		if (future != null) {
			future.cancel(true);
			inactivityFuture = null;
		}
	}

	public void shutdown() {
		cancel();
		inactivityTimer.shutdown();
	}

	private static final class DaemonThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			return thread;
		}
	}

	private final class PowerStatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				int batteryPlugged = intent.getIntExtra("plugged", -1);
				if (batteryPlugged > 0) {
					InactivityTimer.this.cancel();
				}
			}
		}
	}
}
