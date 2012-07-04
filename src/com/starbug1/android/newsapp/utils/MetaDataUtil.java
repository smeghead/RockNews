package com.starbug1.android.newsapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class MetaDataUtil {
	private static final String TAG = MetaDataUtil.class.getName();
	
    public static String getMetaData(Context context, String name, String defaultValue) {
        ApplicationInfo info = null;
		try {
			info = context.getPackageManager().getApplicationInfo(context.getPackageName(), 
					PackageManager.GET_META_DATA); 
		} catch (NameNotFoundException e) {
			Log.e(TAG, "failed to get meta-data. " + e.getMessage());
		}
		if (info == null || info.metaData == null) {
			Log.e(TAG, "ASB_APP_CODE is REQUIRED in AndroidManifest.xml.");
			return defaultValue;
		}
		if (info.metaData.containsKey(name)) {
			Object value = info.metaData.get(name);
			Log.d(TAG, "value:" + value);
	        return value == null ? defaultValue : value.toString();
		} else {
			return defaultValue;
		}
    }

    /**
     *  アプリケーションを起動するためのインテントを取得する。
     *  
     * @param context
     * @return アプリケーションを起動するためのインテント
     */
    public static Intent getLaunchIntent(Context context) {
		String packageName = context.getPackageName();
		return context.getPackageManager().getLaunchIntentForPackage(packageName);
    }
    
    public static Drawable getApplicationIcon(Context context) {
    	try {
    		String packageName = context.getPackageName();
    		PackageManager pm = context.getPackageManager();
        	ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
        	return appInfo.loadIcon(pm);
    	} catch (NameNotFoundException e) {
    		Log.e(TAG, "failed to get version code.");
    		return null;
    	}
    }

    public static int getApplicationIconId(Context context) {
    	try {
    		String packageName = context.getPackageName();
    		PackageManager pm = context.getPackageManager();
        	ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
        	return appInfo.icon;
    	} catch (NameNotFoundException e) {
    		Log.e(TAG, "failed to get version code.");
    		return 0;
    	}
    }
}
