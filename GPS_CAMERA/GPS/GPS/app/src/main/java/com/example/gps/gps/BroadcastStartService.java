package com.example.gps.gps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastStartService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("Hieu", "onReceive() was called");
		Intent service = new Intent(context, DLsTracking.class);
		context.startService(service);
		if (intent.getAction().equals("android.intent.action.PACKAGE_INSTALL")) {

		}
		// if(intent.getAction() != null)
		// {
		// if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
		// intent.getAction().equals(Intent.ACTION_USER_PRESENT))
		// {
		// context.startService(new Intent(context,
		// WeatherStatusService.class));
		// }
		// }
	}
}
