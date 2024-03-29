package com.starbug1.android.rocknews;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.starbug1.android.newsapp.FetchFeedService;
import com.starbug1.android.newsapp.data.NewsListItem;
import com.starbug1.android.newsapp.utils.UrlUtils;

public class AppFetchFeedService extends FetchFeedService {
	private static final String TAG = "AppFetchFeedService";

	private final Pattern imageUrl_ = Pattern.compile(
			"<img.*?src=\"([^\"]*)\"", Pattern.MULTILINE);
	private final Pattern skreamContent_ = Pattern.compile(
			"id=\"news-area\"(.*)id=\"sub-news\"", Pattern.DOTALL);
	private final Pattern ro69Content_ = Pattern
			.compile(
					"(article_body|detail_cap)(.*)(// div.article_body|id=\"detail_btn_bn\")",
					Pattern.DOTALL);

	@Override
	protected List<Feed> getFeeds() {
		List<Feed> feeds = new ArrayList<Feed>();

		feeds.add(new Feed("RO69", "http://sp.ro69.jp/rss.xml") {

			@Override
			public String getImageUrl(String content, NewsListItem item) {
				Matcher m = ro69Content_.matcher(content);
				if (!m.find()) {
					Log.w(TAG, "ro69Content_ not match");
					return null;
				}
				String mainPart = m.group(2);
				Log.d(TAG, mainPart);
				m = imageUrl_.matcher(mainPart);
				if (!m.find()) {
					Log.w(TAG, "imageUrl_ not match");
					return null;
				}
				String imageUrl = m.group(1);
				if (imageUrl != null && imageUrl.startsWith("/")) {
					imageUrl = UrlUtils.findSchemaDomain(item.getLink())
							+ imageUrl;
				}
				return imageUrl;
			}

		});
		feeds.add(new Feed("Skream!", "http://skream.jp/news/index.xml") {

			@Override
			public String getImageUrl(String content, NewsListItem item) {

				Matcher m = skreamContent_.matcher(content);
				if (!m.find()) {
					return null;
				}
				String mainPart = m.group(1);
				m = imageUrl_.matcher(mainPart);
				if (!m.find()) {
					return null;
				}
				return m.group(1);
			}

		});

		return feeds;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected boolean isValidItem(NewsListItem item) {
		if (item.getLink().toString().indexOf("/blog/") != -1) {
			return false;
		}
		return super.isValidItem(item);
	}

}
