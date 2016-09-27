package com.capgemini.psclient.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.capgemini.psclient.config.EnvironmentConfig;

public class RecordServerDB extends SQLiteOpenHelper{


	public RecordServerDB(Context context){
		super(context, "RecordServerDB", null, EnvironmentConfig.DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL("CREATE TABLE server (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT DEFAULT \"\", ip TEXT DEFAULT \"\", password TEXT DEFAULT \"\")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	}
}
