package com.starbug1.android.mudanews;

import java.util.Timer;
import java.util.TimerTask;

import me.parappa.sdk.PaRappa;

import com.starbug1.android.mudanews.data.DatabaseHelper;
import com.starbug1.android.mudanews.data.NewsListItem;
import com.starbug1.android.mudanews.utils.AppUtils;
import com.starbug1.android.mudanews.utils.UrlUtils;
import com.starbug1.android.rocknews.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EntryActivity extends Activity {
	private static final String TAG = "EntryActivity";
	private ProgressDialog progressDialog_;
	final Handler handler_ = new Handler();
	private NewsListItem currentItem_ = null;
	private DatabaseHelper dbHelper_ = null;
	private PaRappa parappa_;
	private WebView webview_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entry);

		dbHelper_ = new DatabaseHelper(this);

		String versionName = AppUtils.getVersionName(this);
		TextView version = (TextView) this.findViewById(R.id.version);
		version.setText(versionName);

		Intent intent = getIntent();
		currentItem_ = (NewsListItem) intent.getSerializableExtra("item");
		

		webview_ = (WebView) this
				.findViewById(R.id.entryView);
		
		webview_.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url,
					Bitmap favicon) {
				final WebView v = view;
				Log.d("NewsDetailActivity", "onPageStarted url: " + url);
				final Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						Log.i(TAG, "timer taks");
						if (v.getContentHeight() > 0) {
							handler_.post(new Runnable() {
								public void run() {
									if (progressDialog_ != null) {
										progressDialog_.dismiss();
									}
								}
							});
							timer.cancel();
						}
					}
				}, 1000, 1000);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view,
					String url) {
				if (!url.startsWith("file") && !UrlUtils.isSameDomain(view.getOriginalUrl(), url)) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					startActivity(intent);
					return true;
				}
				Log.d("NewsDetailActivity",
						"shouldOverrideUrlLoading url: " + url);
				return super.shouldOverrideUrlLoading(view, url);
			}
		});
		ImageView share = (ImageView)findViewById(R.id.image_share);
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				parappa_.shareString(currentItem_.getTitle() + " " + currentItem_.getLink() + " #" + getResources().getString(R.string.app_name), "共有");
			}
		});
		
		WebSettings ws = webview_.getSettings();
		ws.setBuiltInZoomControls(true);
		ws.setLoadWithOverviewMode(true);
		ws.setPluginsEnabled(true);
		ws.setUseWideViewPort(true);
		ws.setJavaScriptEnabled(true);
		ws.setAppCacheMaxSize(1024 * 1024 * 64); //64MB
		ws.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		ws.setDomStorageEnabled(true);
		ws.setAppCacheEnabled(true);
		webview_.setVerticalScrollbarOverlay(true);
		webview_.loadUrl(UrlUtils.mobileUrl(currentItem_.getLink()));
		progressDialog_ = new ProgressDialog(this);
		progressDialog_.setMessage("読み込み中...");
		progressDialog_.show();

		parappa_ = new PaRappa(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.entrymenu, menu);
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_reload:
			WebView entryView = (WebView) this
			.findViewById(R.id.entryView);
			entryView.reload();
			break;
		case R.id.menu_share:
			share();
			break;
		case R.id.menu_notify_all:
			shareAll();
			break;
		case R.id.menu_review:
			parappa_.gotoMarket();
			break;
		case R.id.menu_favorite:
			favorite();
			break;
		case R.id.menu_support:
			parappa_.startSupportActivity();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void favorite() {
		if (currentItem_ == null) return;
		Log.d(TAG, "favorite id:" + currentItem_.getId());
		
		final SQLiteDatabase db = dbHelper_.getWritableDatabase();
		try {
			// お気に入り
			db.execSQL(
					"insert into favorites (feed_id, created_at) values (?, current_timestamp)",
					new String[] { String.valueOf(currentItem_.getId()) });
			currentItem_.setFavorite(true);
			Toast.makeText(this, currentItem_.getTitle() + "をお気に入りにしました", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Log.e(TAG, "failed to favorite.");
		} finally {
			if (db != null && db.isOpen())
				db.close();
		}
	}
	
	private void share() {
		if (currentItem_ == null) {
			return;
		}
		parappa_.shareString(currentItem_.getTitle() + " " + currentItem_.getLink() + " #" + getResources().getString(R.string.app_name), "共有");
	}
	
	private void shareAll() {
		parappa_.shareString(getResources().getString(R.string.shareDescription) + " #" + getResources().getString(R.string.app_name), "紹介");
	}		

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//
//			Log.d(TAG, "onKeyDown");
//			Log.d(TAG, "onKeyDown cGeanGoBack:" + webview_.canGoBack());
//			if (webview_.canGoBack()) {
//				webview_.goBack();
//				return true;
//			}
//		}
//		return false;
//	}

//	@Override
//	protected void onDestroy() {
//		Log.d(TAG, "onDestroy");
//		Log.d(TAG, "onDestroy cGeanGoBack:" + webview_.canGoBack());
//		if (webview_.canGoBack()) {
//			webview_.goBack();
//		} else {
//			super.onDestroy();
//		}
//	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				Log.d(TAG, "dispatchKeyEvent");
				Log.d(TAG, "dispatchKeyEvent cGeanGoBack:" + webview_.canGoBack());
				if (webview_.canGoBack()) {
					webview_.goBack();
					return true;
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}

}
