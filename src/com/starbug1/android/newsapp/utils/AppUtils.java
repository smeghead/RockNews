package com.starbug1.android.newsapp.utils;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.starbug1.android.newsapp.AppException;

public class AppUtils {
	private static final String TAG = "AppUtils";
	
	public static String getVersionName(Context context) {
		PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(),
					PackageManager.GET_META_DATA);
			return "Version " + packageInfo.versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, "failed to retreive version info.");
		}
		return "";
	}

	public static boolean isServiceRunning(Activity activity) {
		final ActivityManager activityManager = (ActivityManager) activity
				.getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		String serviceName = AppUtils.getServiceClass(activity).getCanonicalName();
		for (RunningServiceInfo info : services) {
			if (serviceName.equals(info.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	public static Class<?> getServiceClass(Context context) {
		Class<?> serviceClass = null;
		try {
			serviceClass = Class.forName(context.getPackageName() + ".AppFetchFeedService");
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "no service " + e.toString());
			throw new AppException("failed to get service class.");
		}
		return serviceClass;
	}
}
