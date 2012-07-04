package com.starbug1.android.newsapp;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.starbug1.android.newsapp.utils.AppUtils;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Class<?> serviceClass = AppUtils.getServiceClass(context);
		final Intent i = new Intent(context, serviceClass);
		context.startService(i);
	}

}
