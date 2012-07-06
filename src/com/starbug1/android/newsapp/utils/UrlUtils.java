package com.starbug1.android.newsapp.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {
	public static String mobileUrl(String url, String[] origins, String[] replaces) {
		String ret = url;
		for (int i = 0; i < origins.length; i++) {
			ret = ret.replaceAll(origins[i], replaces[i]);
		}
		return ret;
	}

	private static final Pattern domainPattern_ = Pattern.compile("https?://([^/]*)");
	private static String findDomain(String url) {
		if (url == null) return "";
		final Matcher m = domainPattern_.matcher(url);
		if (!m.find()) {
			return "";
		}
		return m.group(1);
	}
	
	public static boolean isSameDomain(String originalUrl, String url) {
		if (originalUrl == null) return true;
		return findDomain(originalUrl).equals(findDomain(url));
	}

	private static Pattern schemaDomainPattern_ = Pattern.compile("(https?://[^/]*)");
	public static String findSchemaDomain(String url) {
		Matcher m = schemaDomainPattern_.matcher(url);
		if (!m.find()) {
			return "";
		}
		return m.group(1);
	}
}
