/**
 * 
 */
package com.starbug1.android.newsapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.starbug1.android.newsapp.data.DatabaseHelper;
import com.starbug1.android.newsapp.data.FavoriteMonth;
import com.starbug1.android.newsapp.data.NewsListItem;
import com.starbug1.android.newsapp.utils.ResourceProxy.R;

/**
 * @author smeghead
 *
 */
public class FavoriteNewsCollectTask extends AsyncTask<String, Integer, List<FavoriteMonth>> {
	private final String TAG = "FavoriteNewsCollectTask";
	private final FavoriteListActivity activity_;
	public FavoriteNewsCollectTask(FavoriteListActivity activity, Class<?> resourceClass) {
		R.init(resourceClass);
		activity_ = activity;
		activity_.gridUpdating = true;
	}
	
	@Override
	protected void onPreExecute() {
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected List<FavoriteMonth> doInBackground(String... params) {
		final List<FavoriteMonth> result = new ArrayList<FavoriteMonth>();
		final List<Date> months = new ArrayList<Date>();

		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			final DatabaseHelper helper = new DatabaseHelper(activity_);
			db = helper.getWritableDatabase();

			try {
				c = db.rawQuery(
						"select strftime('%Y-%m', fav.created_at, 'localtime') as month, count(f.id) " +
						"from favorites as fav " +
						"inner join feeds as f on fav.feed_id = f.id " + 
						"where f.deleted = 0 " + 
						"group by month " +
						"order by month desc",
						new String[0]
				);
				c.moveToFirst();
				if (c.getCount() == 0) {
					return result;
				}
				for (int i = 0, len = c.getCount(); i < len; i++) {
					String month = c.getString(0);
					Log.d(TAG, month + ":" + c.getInt(1));
					String[] dates = month.split("-");
					Calendar cal = Calendar.getInstance();
					cal.set(Integer.parseInt(dates[0], 10), Integer.parseInt(dates[1], 10) - 1, 1, 0, 0, 0);
					months.add(cal.getTime());
					c.moveToNext();
				}
			} finally {
				c.close();
			}

			final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			
			//  月毎にお気に入りを取得する。
			for (Date begin : months) {
				final List<NewsListItem> items = new ArrayList<NewsListItem>();
				final Calendar cal = Calendar.getInstance();
				cal.setTime(begin);
				cal.add(Calendar.MONTH, 1);
				final Date end = new Date(cal.getTimeInMillis());
				Log.d(TAG, "between " + begin + " and " + end);
				
				try {
					c = db.rawQuery(
							"select f.id, f.title, f.description, f.link, f.source, count(v.id), fav.id " +
							"from feeds as f " +
							"left join view_logs as v on v.feed_id = f.id " +
							"left join favorites as fav on fav.feed_id = f.id " +
							"where f.deleted = 0 and fav.created_at between ? and ? " +
							"group by f.id " +
		 					"order by fav.created_at desc ", new String[]{
									String.valueOf(dateFormat.format(begin)), 
									String.valueOf(dateFormat.format(end))}
					);
					c.moveToFirst();
					if (c.getCount() == 0) {
						continue;
					}
					for (int i = 0, len = c.getCount(); i < len; i++) {
						final NewsListItem item = new NewsListItem();
						item.setId(Integer.parseInt(c.getString(0)));
						item.setTitle(c.getString(1));
						item.setDescription(c.getString(2));
						item.setLink(c.getString(3));
						item.setSource(c.getString(4));
						item.setViewCount(c.getInt(5));
						item.setFavorite(c.getInt(6) > 0);
						items.add(item);
						c.moveToNext();
					}
				} finally {
					c.close();
				}
				for (NewsListItem item : items) {
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
				result.add(new FavoriteMonth(begin, items));
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
	protected void onPostExecute(List<FavoriteMonth> result) {
		progresCancel();
		
		final ListView list = (ListView)activity_.findViewById(R.id.favorite_blocks);
		
		final FavoriteMonthListAdapter adapter = new FavoriteMonthListAdapter(activity_, R.class);
		for (FavoriteMonth month : result) {
			adapter.add(month);
		}
		list.setAdapter(adapter);
		
		activity_.findViewById(R.id.no_favorites_message).setVisibility(result.size() == 0 ? TextView.VISIBLE : TextView.GONE);

//		int addedCount = 0;
//		for (NewsListItem item : result) {
//			if (addedCount >= MAX_ENTRIES_PER_PAGE) {
//				break;
//			}
//			adapter_.add(item);
//			addedCount++;
//		}
//		if (page_ == 0) {
//			GridView view = (GridView) activity_.findViewById(R.id.grid);
//			view.setAdapter(adapter_);
//		}
//		activity_.gridUpdating = false;
	}
	
	public void progresCancel() {
	}
}
