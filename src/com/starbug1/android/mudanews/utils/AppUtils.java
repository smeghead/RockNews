package com.starbug1.android.mudanews.utils;

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
			Log.e("NudaNewsActivity", "failed to retreive version info.");
		}
		return "";
	}


}
