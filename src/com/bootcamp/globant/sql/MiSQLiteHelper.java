package com.bootcamp.globant.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MiSQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_TWEET = "tweets";
	public static final String TWEET_ID = "_id";
	public static final String TWEET_COLUMNA_FROM = "from_tweet";
	public static final String TWEET_COLUMNA_TWEET = "tweet";
	public static final String TWEET_COLUMNA_IMAGEN = "imagen";
	
	public static final String DATABASE_NOMBRE = "twitter.db";
	public static final int DATABASE_VERSION = 1;
	
	public MiSQLiteHelper(Context context) {
		super(context, DATABASE_NOMBRE, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) { // Si la base no existe.
		db.execSQL("create table " + TABLE_TWEET + "(" + TWEET_ID + " integer primary key autoincrement, " + 
														 TWEET_COLUMNA_FROM + " text not null, " + 
														 TWEET_COLUMNA_TWEET + " text not null, " +
														 TWEET_COLUMNA_IMAGEN + " text not null ); "); 
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) { // Actualizar el schema.
		Log.e("INFO", "DB Actualizada.");
		
		db.execSQL("drop table id exists " + TABLE_TWEET);
		onCreate(db);
	}

}
