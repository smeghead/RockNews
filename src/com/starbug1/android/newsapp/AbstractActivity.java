package com.starbug1.android.newsapp;

import me.parappa.sdk.PaRappa;

import com.starbug1.android.newsapp.data.DatabaseHelper;

import android.app.Activity;

public abstract class AbstractActivity extends Activity {
	public abstract DatabaseHelper getDbHelper();
	public abstract int getGridColumnCount();
	public abstract void resetGridInfo();
	public PaRappa parappa_;
}
