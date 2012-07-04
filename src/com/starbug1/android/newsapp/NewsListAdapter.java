/**
 * 
 */
package com.starbug1.android.newsapp;

import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.starbug1.android.newsapp.data.NewsListItem;
import com.starbug1.android.newsapp.utils.ResourceProxy.R;

/**
 * @author smeghead
 * 
 */
public class NewsListAdapter extends ArrayAdapter<NewsListItem> {
	private final LayoutInflater inflater_;
	private TextView title_;
	private final AbstractActivity context_;

	public NewsListAdapter(Context context, Class<?> resourceClass) {
		super(context, 0, new ArrayList<NewsListItem>());
		R.init(resourceClass);
		context_ = (AbstractActivity)context;
		inflater_ = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;

		if (view == null) {
			view = inflater_.inflate(R.layout.item_row, null);
		}

		if (this.getCount() < position + 1) {
			Log.w("NewsListAdapter", "position invalid!");
			return null;
		}
		final NewsListItem item = this.getItem(position);
		if (item != null) {
			view.setTag(item);
			
			final String title = item.getTitle().toString();
			title_ = (TextView) view.findViewById(R.id.item_title);
			title_.setText(title);
			final ImageView newEntry = (ImageView) view.findViewById(R.id.newEntry);
			newEntry.setVisibility(item.getViewCount() > 0 ? ImageView.GONE : ImageView.VISIBLE);
			title_.setTextColor(Color.argb(item.getViewCount() > 0 ? 168 : 230, 255, 255, 255));
			final ImageView isFavorite = (ImageView) view.findViewById(R.id.favorite);
			isFavorite.setImageResource(item.isFavorite()
					? android.R.drawable.btn_star_big_on
					: android.R.drawable.btn_star_big_off);
			isFavorite.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final boolean add = !item.isFavorite();
					//TODO locate right place.
					//お気に入り
					final SQLiteDatabase db = context_.getDbHelper().getWritableDatabase();
					try {
						
						db.execSQL(
								add
									? "insert into favorites (feed_id, created_at) values (?, current_timestamp)"
									: "delete from favorites where feed_id = ?",
								new String[] { String.valueOf(item.getId()) });
						item.setFavorite(add);
						final ImageView favorite = (ImageView) v
								.findViewById(R.id.favorite);
						favorite.setImageResource(add
								? android.R.drawable.btn_star_big_on
								: android.R.drawable.btn_star_big_off);
						if (add) {
							Toast.makeText(context_, item.getTitle() + "をお気に入りにしました", Toast.LENGTH_LONG).show();
						}
					} catch (Exception e) {
						Log.e("MudanewsActivity", "failed to update entry action.");
					} finally {
						db.close();
					}
				}
			});
			final WindowManager w = context_.getWindowManager();
			final Display d = w.getDefaultDisplay();

			final int size = d.getWidth() / context_.getGridColumnCount();
			if (item.getImage() != null) {
				Bitmap bOrg = item.getImageBitmap();
				if (bOrg == null) {
					final byte[] data = item.getImage();
					try {
						bOrg = BitmapFactory.decodeByteArray(data, 0, data.length);
					} catch (OutOfMemoryError e) {
						Log.e("NewsListAdapter", e.getMessage());
					}
				}
				// サイズ調整
				Bitmap b = bOrg;
				if (size != 160) {
					b = Bitmap.createScaledBitmap(bOrg, size, size, false);
				}
				final ImageView image = (ImageView) view
						.findViewById(R.id.item_image);
				image.setImageDrawable(null);
				image.setImageBitmap(b);
				image.setVisibility(ImageView.VISIBLE);
			} else {
				Log.d("NewsListAdapter", "item more? id:" + item.getId());
				final ImageView image = (ImageView) view
						.findViewById(R.id.item_image);
				image.setImageResource(R.drawable.no_image);
				image.setVisibility(ImageView.VISIBLE);
			}
		}
		return view;
	}

	@Override
	public void remove(NewsListItem object) {
		Log.d("NewsListAdapter", "remove");
		super.remove(object);
	}

}
