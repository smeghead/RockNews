package com.starbug1.android.newsapp;

import com.starbug1.android.newsapp.utils.AppUtils;
import com.starbug1.android.newsapp.utils.ResourceProxy.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.TextView;

public class AppPrefActivity extends PreferenceActivity {
	private ListPreference clowlIntervalsPref_;
	private SharedPreferences sharedPref_;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		R.init(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pref);
		addPreferencesFromResource(R.xml.pref);

		final String versionName = AppUtils.getVersionName(this);
		final TextView version = (TextView) this.findViewById(R.id.version);
		version.setText(versionName);

		sharedPref_ = PreferenceManager.getDefaultSharedPreferences(this);
		clowlIntervalsPref_ = (ListPreference) findPreference("clowl_intervals");

		clowlIntervalsPref_.setSummary(getStringByValue(clowlIntervalsPref_,
				sharedPref_.getString("clowl_intervals", "60")));
		clowlIntervalsPref_
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						preference.setSummary(getStringByValue(
								clowlIntervalsPref_, newValue.toString()));
						return true;
					}
				});
	}

	public String getStringByValue(ListPreference listPref, String value) {
		final CharSequence[] strings = listPref.getEntries();
		final CharSequence[] values = listPref.getEntryValues();
		int index = -1;
		for (int i = 0, len = values.length; i < len; i++) {
			if (value.equals(values[i])) {
				index = i;
				break;
			}
		}
		if (index == -1 || index > strings.length - 1) {
			return "";
		}
		return strings[index].toString();
	}

	@Override
	public Header onGetInitialHeader() {
		return super.onGetInitialHeader();
	}
}
