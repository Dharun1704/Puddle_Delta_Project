package com.example.deltaproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.deltaproject.NewsModel.Article;

public class NewsDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "bookmark.db";
    public static final String TABLE_NAME = "bookmark_table";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_URLTOIMAGE = "urltoimage";
    public static final String COLUMN_PUBLISHEDAT = "publishedat";
    public static final String COLUMN_SOURCE = "source";

    public NewsDatabase(@Nullable Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_AUTHOR + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_URL + " TEXT, " +
                COLUMN_URLTOIMAGE + " TEXT, " +
                COLUMN_PUBLISHEDAT + " TEXT, " +
                COLUMN_SOURCE + " TEXT);";
            db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String author, String title, String description, String url, String urlToImage, String publishedAt, String source) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_AUTHOR, author);
        contentValues.put(COLUMN_TITLE, title);
        contentValues.put(COLUMN_DESCRIPTION, description);
        contentValues.put(COLUMN_URL, url);
        contentValues.put(COLUMN_URLTOIMAGE,urlToImage);
        contentValues.put(COLUMN_PUBLISHEDAT, publishedAt);
        contentValues.put(COLUMN_SOURCE, source);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        return db.rawQuery(query, null);
    }

    public int  deleteData(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "url = ?", new String[] {url});
    }
}
