package com.starbug1.android.mudanews;

import java.util.ArrayList;
import java.util.List;

import me.parappa.sdk.PaRappa;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.starbug1.android.mudanews.data.DatabaseHelper;
import com.starbug1.android.mudanews.data.NewsListItem;
import com.starbug1.android.mudanews.utils.AppUtils;
import com.starbug1.android.rocknews.R;
import com.starbug1.android.rocknews.RockNewsFavoriteListActivity;
import com.starbug1.android.rocknews.RockNewsPrefActivity;

public class MudanewsActivity extends AbstractActivity {
	private static final String TAG = "MudanewsActivity";
	
	private List<NewsListItem> items_;
	private int page_ = 0;
	private ProgressDialog progressDialog_;
	private DatabaseHelper dbHelper_ = null;
	private NewsListAdapter adapter_;
	public boolean hasNextPage = true;
	public boolean gridUpdating = false;

	private FetchFeedService fetchFeedService_;
	private boolean isBound_;
	final Handler handler_ = new Handler();

	private ServiceConnection connection_ = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// サービスにはIBinder経由で#getService()してダイレクトにアクセス可能
			fetchFeedService_ = ((FetchFeedService.FetchFeedServiceLocalBinder) service)
					.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			fetchFeedService_ = null;
		}
	};

	void doBindService() {
		bindService(new Intent(MudanewsActivity.this, FetchFeedService.class),
				connection_, Context.BIND_AUTO_CREATE);
		isBound_ = true;
	}

	void doUnbindService() {
		if (isBound_) {
			unbindService(connection_);
			isBound_ = false;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.d(TAG, "setContentView");
		
		dbHelper_ = new DatabaseHelper(this);

		doBindService();
		Log.d(TAG, "bindService");

		page_ = 0; hasNextPage = true;
		items_ = new ArrayList<NewsListItem>();
		adapter_ = new NewsListAdapter(this);

		String versionName = AppUtils.getVersionName(this);
		TextView version = (TextView) this.findViewById(R.id.version);
		version.setText(versionName);

		final GridView grid = (GridView) this.findViewById(R.id.grid);
		grid.setOnItemClickListener(new NewsGridEvents.NewsItemClickListener(this));

		grid.setOnItemLongClickListener(new NewsGridEvents.NewsItemLognClickListener(this));
		Log.d(TAG, "grid setup");

		grid.setOnScrollListener(new OnScrollListener() {
			private boolean stayBottom = false;

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				// スクロールしていない
				case OnScrollListener.SCROLL_STATE_IDLE:
				case OnScrollListener.SCROLL_STATE_FLING:
					if (stayBottom) {
						Log.d(TAG, "scrollY: " + grid.getHeight());
						// load more.
						
						if (!MudanewsActivity.this.gridUpdating && MudanewsActivity.this.hasNextPage) {
							updateList(++page_);
						}
					}
					break;
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				stayBottom = (totalItemCount == firstVisibleItem
						+ visibleItemCount);
			}
		});
		Log.d(TAG, "scroll");

		// 初回起動なら、feed取得 ボタンを表示する
		if (dbHelper_.entryIsEmpty()) {
			final TextView initialMessage = (TextView) this.findViewById(R.id.initialMessage);
			initialMessage.setVisibility(Button.VISIBLE);
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < 10; i++) {
						try {
							Thread.sleep(500);
						} catch (Exception e) {}
						Log.d(TAG, "service:" + isBound_);
						if (isBound_) break;
					}
					handler_.post(new Runnable() {
						@Override
						public void run() {
							fetchFeeds();
						}
					});
				}
			}).start();
		}

		Log.d(TAG, "updateList start.");
		updateList(page_);
		Log.d(TAG, "updateList end.");

		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancelAll();
		
		// サービスが開始されていなかったら開始する
		if (!AppUtils.isServiceRunning(this)) {
			Intent intent = new Intent(this, FetchFeedService.class);
			this.startService(intent);
		}
		
		parappa_ = new PaRappa(this);
	}

	private NewsCollectTask task_ = null;

	private int column_count_ = 1;
	private void setupGridColumns() {
		WindowManager w = getWindowManager();
		Display d = w.getDefaultDisplay();
		int width = d.getWidth();
		column_count_ = width / 160;
		GridView grid = (GridView) this.findViewById(R.id.grid);
		grid.setNumColumns(column_count_);
	}
	
	public void resetGridInfo() {
		page_ = 0; hasNextPage = true;
		updateList(page_);
	}

	private void updateList(int page) {
		setupGridColumns();

		if (page_ == 0) {
			adapter_.clear();
		}
		GridView grid = (GridView) this.findViewById(R.id.grid);
		task_ = new NewsCollectTask(this, grid, adapter_);
		task_.execute(String.valueOf(page));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_update_feeds:
			fetchFeeds();
			break;
		case R.id.menu_settings:
			settings();
			break;
		case R.id.menu_notify_all:
			shareAll();
			break;
		case R.id.menu_review:
			parappa_.gotoMarket();
			break;
		case R.id.menu_support:
			parappa_.startSupportActivity();
			break;
		case R.id.menu_favorites:
			Intent intent = new Intent(this, RockNewsFavoriteListActivity.class);
			this.startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void shareAll() {
		parappa_.shareString(getResources().getString(R.string.shareDescription) + " #" + getResources().getString(R.string.app_name), "紹介");
	}
		
	private void settings() {
		Intent intent = new Intent(this, RockNewsPrefActivity.class);
		startActivity(intent);
	}

	private void fetchFeeds() {
		items_.clear();
		progressDialog_ = new ProgressDialog(MudanewsActivity.this);
		progressDialog_.setMessage("読み込み中...");
		progressDialog_.show();
		new Thread() {
			@Override
			public void run() {
				final int count = fetchFeedService_.updateFeeds();
				handler_.post(new Runnable() {
					public void run() {
						TextView initialMessage = (TextView) findViewById(R.id.initialMessage);
						initialMessage.setVisibility(TextView.GONE);

						progressDialog_.dismiss();
						page_ = 0; hasNextPage = true;
						items_.clear();
						updateList(page_);
						if (count == 0) {
							Toast.makeText(MudanewsActivity.this, "新しい記事はありませんでした", Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(MudanewsActivity.this, count + "件の記事を追加しました", Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		}.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	}

	@Override
	protected void onPause() {
		if (task_ != null) {
			task_.progresCancel();
		}
		super.onPause();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setupGridColumns();
	}

	public DatabaseHelper getDbHelper() {
		return dbHelper_;
	}

	@Override
	public int getGridColumnCount() {
		return this.column_count_;
	}
}
