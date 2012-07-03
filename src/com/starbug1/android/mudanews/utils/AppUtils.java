package com.starbug1.android.mudanews.utils;

import java.util.List;

import com.starbug1.android.mudanews.FetchFeedService;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class AppUtils {
	public static String getVersionName(Context context) {
		PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(
					context.getClass().getPackage().getName(),
					PackageManager.GET_META_DATA);
			return "Version " + packageInfo.versionName;
		} catch (NameNotFoundException e) {
			Log.e("AppUtils", "failed to retreive version info.");
		}
		return "";
	}

	private static final String mServiceName = FetchFeedService.class
			.getCanonicalName();

	public static boolean isServiceRunning(Activity activity) {
		ActivityManager activityManager = (ActivityManager) activity
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		for (RunningServiceInfo info : services) {
			if (mServiceName.equals(info.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
