package com.starbug1.android.mudanews;

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

import com.starbug1.android.mudanews.data.DatabaseHelper;
import com.starbug1.android.mudanews.utils.AppUtils;
import com.starbug1.android.rocknews.R;

public class FavoriteListActivity extends AbstractActivity {
	final Handler handler_ = new Handler();
	private DatabaseHelper dbHelper_ = null;
	public boolean gridUpdating = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorite_list);
		
		dbHelper_ = new DatabaseHelper(this);

		String versionName = AppUtils.getVersionName(this);
		TextView version = (TextView) this.findViewById(R.id.version);
		version.setText(versionName);
		
		setupGridColumns();
		FavoriteNewsCollectTask task = new FavoriteNewsCollectTask(this);
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
		WindowManager w = getWindowManager();
		Display d = w.getDefaultDisplay();
		int width = d.getWidth();
		column_count_ = width / 160;
		ListView list = (ListView) this.findViewById(R.id.favorite_blocks);
		for (int i = 0, len = list.getChildCount(); i < len; i++) {
			View child = list.getChildAt(i);
			GridView grid = (GridView) child.findViewById(R.id.grid);
			if (grid != null) {
				grid.setNumColumns(column_count_);
			}
		}
	}


}
