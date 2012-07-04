package com.starbug1.android.newsapp.data;

import java.io.Serializable;

import android.graphics.Bitmap;

/**
 * 記事一覧に表示するリストビュー
 * 
 * @author smeghead
 */
public class NewsListItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id_;
	private String title_;
	private String description_;
	private String category_;
	private Long publishedAt_;
	private String link_;
	private String source_;
	private String imageUrl_;
	private byte[] image_;
	private Bitmap imageBitmap_;
	private int viewCount_;
	private boolean favorite_;
	
	public NewsListItem() {
		title_ = "";
		description_ = "";
		category_ = "";
		publishedAt_ = 0l;
		link_ = "";
	}

	public String getTitle() {
		return title_;
	}

	public void setTitle(String title) {
		title_ = title;
	}

	public String getDescription() {
		return description_;
	}

	public void setDescription(String description) {
		description_ = description;
	}

	public String getCategory() {
		return category_;
	}

	public void setCategory(String category) {
		category_ = category;
	}

	public long getPublishedAt() {
		return publishedAt_;
	}

	public void setPublishedAt(long publishedAt) {
		publishedAt_ = publishedAt;
	}

	public String getLink() {
		return link_;
	}

	public void setLink(String link) {
		link_ = link;
	}

	public String getSource() {
		return source_;
	}

	public void setSource(String source) {
		source_ = source;
	}

	public String getImageUrl() {
		return imageUrl_;
	}

	public void setImageUrl(String imageUrl) {
		imageUrl_ = imageUrl;
	}

	public void setPublishedAt(Long publishedAt) {
		publishedAt_ = publishedAt;
	}

	public byte[] getImage() {
		return image_;
	}

	public void setImage(byte[] image) {
		image_ = image;
	}

	public int getId() {
		return id_;
	}

	public void setId(int id) {
		id_ = id;
	}

	public int getViewCount() {
		return viewCount_;
	}

	public void setViewCount(int viewCount) {
		viewCount_ = viewCount;
	}

	public Bitmap getImageBitmap() {
		return imageBitmap_;
	}

	public void setImageBitmap(Bitmap imageBitmap) {
		imageBitmap_ = imageBitmap;
	}

	public boolean isFavorite() {
		return favorite_;
	}

	public void setFavorite(boolean favorite) {
		favorite_ = favorite;
	}

	
}
