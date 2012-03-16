/**
 * 
 */
package com.starbug1.android.mudanews;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;

import com.starbug1.android.mudanews.data.DatabaseHelper;
import com.starbug1.android.mudanews.data.NewsListItem;
import com.starbug1.android.rocknews.R;

/**
 * @author smeghead
 *
 */
public class NewsParserTask extends AsyncTask<String, Integer, List<NewsListItem>> {
	private final int MAX_ENTRIES_PER_PAGE = 30;
	private final MudanewsActivity activity_;
	private final NewsListAdapter adapter_;
	private ProgressDialog progresDialog_;
	private int page_;

	public NewsParserTask(MudanewsActivity activity, NewsListAdapter adapter) {
		activity_ = activity;
		adapter_ = adapter;
	}
	
	@Override
	protected void onPreExecute() {
		progresDialog_ = new ProgressDialog(activity_);
		progresDialog_.setMessage("読み込み中...");
		progresDialog_.show();
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected List<NewsListItem> doInBackground(String... params) {
		List<NewsListItem> result = new ArrayList<NewsListItem>(30);
		page_ = Integer.parseInt(params[0]);
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			DatabaseHelper helper = new DatabaseHelper(activity_);
			db = helper.getWritableDatabase();

			c = db.rawQuery(
					"select f.id, f.title, f.description, f.link, f.source, count(v.id), fav.id " +
					"from feeds as f " +
					"left join view_logs as v on v.feed_id = f.id " +
					"left join favorites as fav on fav.feed_id = f.id " +
					"where f.deleted = 0 " +
					"group by f.id " +
 					"order by published_at desc " + 
					"limit ? " +
					"offset ?", new String[]{
							String.valueOf(MAX_ENTRIES_PER_PAGE + 1), 
							String.valueOf(page_ * MAX_ENTRIES_PER_PAGE)}
			);
			c.moveToFirst();
			for (int i = 0, len = c.getCount(); i < len; i++) {
				NewsListItem item = new NewsListItem();
				item.setId(Integer.parseInt(c.getString(0)));
				item.setTitle(c.getString(1));
				item.setDescription(c.getString(2));
				item.setLink(c.getString(3));
				item.setSource(c.getString(4));
				item.setViewCount(c.getInt(5));
				item.setFavorite(c.getInt(6) > 0);
				result.add(item);
				c.moveToNext();
			}
			for (NewsListItem item : result) {
				Cursor cu = null;
				try {
					cu = db.rawQuery("select image from images where feed_id = ?", new String[]{String.valueOf(item.getId())});
					cu.moveToFirst();
					if (cu.getCount() != 1) {
						Log.w("NewsParserTask", "no image.");
						continue;
					}
					item.setImage(cu.getBlob(0));
				} finally {
					if (cu != null) cu.close();
				}
			}
			return result;
		} catch(Exception e) {
			Log.e("NewsParserTask", e.getMessage());
			throw new AppException("failed to background task.", e);
		} finally {
			if (c != null) c.close();
			if (db != null) db.close();
		}
	}

	@Override
	protected void onPostExecute(List<NewsListItem> result) {
		progresCancel();
		
		boolean hasNextPages = result.size() > MAX_ENTRIES_PER_PAGE;
		int addedCount = 0;
		for (NewsListItem item : result) {
			if (addedCount >= MAX_ENTRIES_PER_PAGE) {
				break;
			}
			adapter_.add(item);
			addedCount++;
		}
		if (page_ == 0) {
			GridView view = (GridView) activity_.findViewById(R.id.grid);
			view.setAdapter(adapter_);
		}
		Button more = (Button) activity_.findViewById(R.id.more);
		more.setTag(Boolean.valueOf(hasNextPages));
		more.setVisibility(Button.GONE);
	}
	
	public void progresCancel() {
		if (progresDialog_ != null && progresDialog_.isShowing()) {
			progresDialog_.dismiss();
			progresDialog_ = null;
		}
	}
}
