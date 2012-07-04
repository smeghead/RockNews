package com.starbug1.android.newsapp;

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

import com.starbug1.android.newsapp.data.DatabaseHelper;
import com.starbug1.android.newsapp.data.NewsListItem;
import com.starbug1.android.newsapp.utils.AppUtils;
import com.starbug1.android.newsapp.utils.ResourceProxy.R;

public class MainActivity extends AbstractActivity {
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
		bindService(new Intent(MainActivity.this, AppUtils.getServiceClass(this)),
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
		R.init(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.d(TAG, "setContentView");
		
		dbHelper_ = new DatabaseHelper(this);

		doBindService();
		Log.d(TAG, "bindService");

		page_ = 0; hasNextPage = true;
		items_ = new ArrayList<NewsListItem>();
		adapter_ = new NewsListAdapter(this, R.class);

		final String versionName = AppUtils.getVersionName(this);
		final TextView version = (TextView) this.findViewById(R.id.version);
		version.setText(versionName);

		final GridView grid = (GridView) this.findViewById(R.id.grid);
		grid.setOnItemClickListener(new NewsGridEvents.NewsItemClickListener(this, EntryActivity.class));

		grid.setOnItemLongClickListener(new NewsGridEvents.NewsItemLognClickListener(this, R.class));
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
						
						if (!MainActivity.this.gridUpdating && MainActivity.this.hasNextPage) {
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
							fetchFeeds(true);
						}
					});
				}
			}).start();
		}

		Log.d(TAG, "updateList start.");
		updateList(page_);
		Log.d(TAG, "updateList end.");

		final NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancelAll();
		
		parappa_ = new PaRappa(this);
	}

	private NewsCollectTask task_ = null;

	private int column_count_ = 1;
	private void setupGridColumns() {
		final WindowManager w = getWindowManager();
		final Display d = w.getDefaultDisplay();
		int width = d.getWidth();
		column_count_ = width / 160;
		final GridView grid = (GridView) this.findViewById(R.id.grid);
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
		final GridView grid = (GridView) this.findViewById(R.id.grid);
		task_ = new NewsCollectTask(this, grid, adapter_);
		task_.execute(String.valueOf(page));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_update_feeds) {
			fetchFeeds(false);
		} else if (item.getItemId() == R.id.menu_settings) {
			settings();
		} else if (item.getItemId() == R.id.menu_notify_all) {
			shareAll();
		} else if (item.getItemId() == R.id.menu_review) {
			parappa_.gotoMarket();
		} else if (item.getItemId() == R.id.menu_support) {
			parappa_.startSupportActivity();
		} else if (item.getItemId() == R.id.menu_favorites) {
			Intent intent = new Intent(this, FavoriteListActivity.class);
			this.startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	private void shareAll() {
		parappa_.shareString(getResources().getString(R.string.shareDescription) + " #" + getResources().getString(R.string.app_name), "紹介");
	}
		
	private void settings() {
		final Intent intent = new Intent(this, AppPrefActivity.class);
		startActivity(intent);
	}

	private void fetchFeeds(boolean isFirst) {
		final boolean first = isFirst;
		items_.clear();
		progressDialog_ = new ProgressDialog(this);
		progressDialog_.setMessage("読み込み中...");
		progressDialog_.show();
		new Thread() {
			@Override
			public void run() {
				final int count = fetchFeedService_.updateFeeds(first);
				handler_.post(new Runnable() {
					public void run() {
						final TextView initialMessage = (TextView) findViewById(R.id.initialMessage);
						initialMessage.setVisibility(TextView.GONE);

						progressDialog_.dismiss();
						page_ = 0; hasNextPage = true;
						items_.clear();
						updateList(page_);
						if (count == 0) {
							Toast.makeText(MainActivity.this, "新しい記事はありませんでした", Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(MainActivity.this, count + "件の記事を追加しました", Toast.LENGTH_LONG).show();
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
