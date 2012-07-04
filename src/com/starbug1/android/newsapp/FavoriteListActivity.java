package com.starbug1.android.newsapp;

import me.parappa.sdk.PaRappa;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.starbug1.android.newsapp.data.DatabaseHelper;
import com.starbug1.android.newsapp.utils.AppUtils;
import com.starbug1.android.newsapp.utils.ResourceProxy.R;

public class FavoriteListActivity extends AbstractActivity {
	final Handler handler_ = new Handler();
	private DatabaseHelper dbHelper_ = null;
	public boolean gridUpdating = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		R.init(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorite_list);
		
		dbHelper_ = new DatabaseHelper(this);

		final String versionName = AppUtils.getVersionName(this);
		final TextView version = (TextView) this.findViewById(R.id.version);
		version.setText(versionName);
		
		setupGridColumns();
		FavoriteNewsCollectTask task = new FavoriteNewsCollectTask(this, R.class);
		task.execute();

		parappa_ = new PaRappa(this);
	}

	@Override
	public DatabaseHelper getDbHelper() {
		return dbHelper_;
	}

	@Override
	public int getGridColumnCount() {
		return column_count_;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setupGridColumns();
	}

	private int column_count_ = 1;
	private void setupGridColumns() {
		final WindowManager w = getWindowManager();
		final Display d = w.getDefaultDisplay();
		final int width = d.getWidth();
		column_count_ = width / 160;
		final ListView list = (ListView) this.findViewById(R.id.favorite_blocks);
		for (int i = 0, len = list.getChildCount(); i < len; i++) {
			final View child = list.getChildAt(i);
			final GridView grid = (GridView) child.findViewById(R.id.grid);
			if (grid != null) {
				grid.setNumColumns(column_count_);
			}
		}
	}

	@Override
	public void resetGridInfo() {
		// TODO Auto-generated method stub
		
	}


}
