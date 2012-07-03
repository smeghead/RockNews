/**
 * 
 */
package com.starbug1.android.mudanews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.starbug1.android.mudanews.data.FavoriteMonth;
import com.starbug1.android.mudanews.data.NewsListItem;
import com.starbug1.android.rocknews.R;

/**
 * @author smeghead
 * 
 */
public class FavoriteMonthListAdapter extends ArrayAdapter<FavoriteMonth> {
	private LayoutInflater inflater_;
	private FavoriteListActivity context_;
	private final SimpleDateFormat monthDateFormat = new SimpleDateFormat("yyyy年 M月"); 

	public FavoriteMonthListAdapter(Context context) {
		super(context, 0, new ArrayList<FavoriteMonth>());
		context_ = (FavoriteListActivity)context;
		inflater_ = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;

		if (view == null) {
			view = inflater_.inflate(R.layout.favorite_block, null);
		}

		if (this.getCount() < position + 1) {
			Log.w("FavoriteMonthListAdapter", "position invalid!");
			return null;
		}
		final FavoriteMonth month = this.getItem(position);
		if (month != null) {
			view.setTag(month);
			
			final List<NewsListItem> items = month.getItems();
			
			TextView monthLabel = (TextView)view.findViewById(R.id.month);
			monthLabel.setText(monthDateFormat.format(month.getMonth()));
			
			NewsListAdapter adapter = new NewsListAdapter(context_);
			for (NewsListItem item : items) {
				adapter.add(item);
			}
			GridView grid = (GridView) view.findViewById(R.id.grid);
			grid.setNumColumns(context_.getGridColumnCount());
			grid.setAdapter(adapter);
			int height = (int) (Math.ceil(Double.valueOf(month.getItems().size()) / context_.getGridColumnCount()) * 160 + 40);
			view.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, height));
			
			grid.setOnItemClickListener(new NewsGridEvents.NewsItemClickListener(context_));

			grid.setOnItemLongClickListener(new NewsGridEvents.NewsItemLognClickListener(context_));

		}
		return view;
	}

}
