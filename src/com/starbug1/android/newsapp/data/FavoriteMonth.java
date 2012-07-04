package com.starbug1.android.newsapp.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class FavoriteMonth implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Date month_;
	private List<NewsListItem> items_;
	
	public FavoriteMonth(Date month, List<NewsListItem> items) {
		month_ = month;
		items_ = items;
	}
	
	public Date getMonth() {
		return month_;
	}
	public void setMonth(Date month) {
		this.month_ = month;
	}
	public List<NewsListItem> getItems() {
		return items_;
	}
	public void setItems(List<NewsListItem> items) {
		this.items_ = items;
	}
	
}
