package com.starbug1.android.newsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.starbug1.android.newsapp.data.NewsListItem;
import com.starbug1.android.newsapp.utils.ResourceProxy.R;

public class NewsGridEvents {

	public static class NewsItemClickListener implements OnItemClickListener {

		private final AbstractActivity activity_;
		private final Class<?> entryClass_;
		
		public NewsItemClickListener(AbstractActivity activity, Class<?> entryClass) {
			activity_ = activity;
			entryClass_ = entryClass;
		}
		
		@Override
		public void onItemClick(AdapterView<?> adapter, View view,
				int position, long id) {
			final NewsListItem item = (NewsListItem)adapter.getItemAtPosition(position);

			final SQLiteDatabase db = activity_.getDbHelper().getWritableDatabase();
			db.execSQL(
					"insert into view_logs (feed_id, created_at) values (?, current_timestamp)",
					new String[] { String.valueOf(item.getId()) });
			db.close();

			item.setViewCount(item.getViewCount() + 1);
			final ImageView newIcon = (ImageView) view
					.findViewById(R.id.newEntry);
			newIcon.setVisibility(ImageView.GONE);

			final Intent entryIntent = new Intent(activity_, entryClass_);
			entryIntent.putExtra("item", item);
			activity_.startActivity(entryIntent);
			
		}
		
	}
	
	public static class NewsItemLognClickListener implements AdapterView.OnItemLongClickListener {
		private static final String TAG = "NewsItemLognClickListener";
		private final AbstractActivity activity_;

		public NewsItemLognClickListener(AbstractActivity activity, Class<?> resourceClass) {
			activity_ = activity;
			R.init(resourceClass);
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> adapter, View viewArg,
				int position, long arg3) {
			final View v = viewArg;
			final NewsListItem item = (NewsListItem)adapter.getItemAtPosition(position);
//		    Integer item_index = (Integer)v.getTag() - 1;
			final AlertDialog.Builder ad = new AlertDialog.Builder(activity_);
			ad.setTitle("記事のアクション");
			ad.setItems(R.array.arrays_entry_actions, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Log.d("NewsListAdapter", "longclickmenu selected id:" + item.getId());
					final String processName = activity_.getResources().getStringArray(R.array.arrays_entry_action_values)[which];
					final SQLiteDatabase db = activity_.getDbHelper().getWritableDatabase();
					try {
						if ("share".equals(processName)) {
							//共有
							activity_.parappa_.shareString(item.getTitle() + " " + item.getLink() + " #" + activity_.getResources().getString(R.string.app_name), "共有");
						} else if ("make_favorite".equals(processName)) {
							//お気に入り
							db.execSQL(
									"insert into favorites (feed_id, created_at) values (?, current_timestamp)",
									new String[] { String.valueOf(item.getId()) });
							item.setFavorite(true);
							final ImageView favorite = (ImageView) v
									.findViewById(R.id.favorite);
							favorite.setImageResource(android.R.drawable.btn_star_big_on);
							Toast.makeText(activity_, item.getTitle() + "をお気に入りにしました", Toast.LENGTH_LONG).show();
						} else if ("make_read".equals(processName)) {
							//既読にする
							db.execSQL(
									"insert into view_logs (feed_id, created_at) values (?, current_timestamp)",
									new String[] { String.valueOf(item.getId()) });
							item.setViewCount(item.getViewCount() + 1);
							final ImageView newIcon = (ImageView) v
									.findViewById(R.id.newEntry);
							newIcon.setVisibility(ImageView.GONE);
							Toast.makeText(activity_, item.getTitle() + "を既読にしました", Toast.LENGTH_LONG).show();
						} else if ("delete".equals(processName)) {
							//削除
							db.execSQL(
									"update feeds set deleted = 1 where id = ?",
									new String[] { String.valueOf(item.getId()) });
							final MainActivity a = (MainActivity)activity_;
							a.resetGridInfo();
							Toast.makeText(activity_, item.getTitle() + "を削除しました", Toast.LENGTH_LONG).show();
						}
					} catch (Exception e) {
						Log.e(TAG, "failed to update entry action.", e);
					} finally {
						db.close();
					}
				}
			});
			final AlertDialog alert = ad.create();
			alert.show();
			return true;
		}
	}
}
