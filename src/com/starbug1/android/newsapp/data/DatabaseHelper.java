package com.starbug1.android.newsapp.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	public DatabaseHelper(Context context) {
		super(context, "mudanews.db", null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();
		try {
			db.execSQL("create table feeds ( " + "  id integer primary key,"
					+ "  source text not null," + "  title text not null,"
					+ "  link text not null," + "  description text not null,"
					+ "  category text," + "  published_at datetime,"
					+ "  deleted bool not null default 0,"
					+ "  created_at datetime not null" + ")");
			db.execSQL("create table images ( " + "  id integer primary key,"
					+ "  feed_id integer not null," + "  image text,"
					+ "  created_at datetime not null" + ")");
			db.execSQL("create table view_logs ( "
					+ "  id integer primary key,"
					+ "  feed_id integer not null,"
					+ "  created_at datetime not null" + ")");
			db.execSQL("create table favorites ( "
					+ "  id integer primary key,"
					+ "  feed_id integer not null,"
					+ "  created_at datetime not null" + ")");
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int currentVersion, int newVersion) {
		// debug
		db.beginTransaction();
		try {
			if (currentVersion < 2 && newVersion == 2) {
				db.execSQL("create table favorites ( "
						+ "  id integer primary key,"
						+ "  feed_id integer not null,"
						+ "  created_at datetime not null" + ")");
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public boolean entryIsEmpty() {
		SQLiteDatabase db = null;
		Cursor cu = null;
		try {
			db = getReadableDatabase();
			cu = db.rawQuery("select count(*) from feeds", new String[] {});
			cu.moveToFirst();
			if (cu.getInt(0) == 0) {
				Log.w("NewsParserTask", "no feed.");
				return true;
			}
			return false;
		} finally {
			if (cu != null)
				cu.close();
			if (db != null)
				db.close();
		}
	}
}
