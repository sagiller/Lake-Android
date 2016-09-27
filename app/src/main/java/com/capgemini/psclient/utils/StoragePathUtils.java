package com.capgemini.psclient.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class StoragePathUtils{
	// cannot be instantiated
	private StoragePathUtils(){
		throw new UnsupportedOperationException("cannot be instantiated");
	}


	// 外部储存(SD card)根目录.   /storage/emulated/0
	public static File getExternalSDCardPath(){
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory() : null;
	}


	// 外部储存文件目录. 应用被卸载，里面的内容都会被移除  /storage/emulated/0/Android/data/com.xxx.xxx/files/Music...
	// 如果要得到File文件夹，不是具体内部的music之类的，可以传StoragePathUtils.FileType.NULL
	public static File getExternalFilePath(Context getApplicationContext, FileType fileType){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			switch(fileType){
				case DIRECTORY_MUSIC:
					return getApplicationContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
				case DIRECTORY_PODCASTS:
					return getApplicationContext.getExternalFilesDir(Environment.DIRECTORY_PODCASTS);
				case DIRECTORY_RINGTONES:
					return getApplicationContext.getExternalFilesDir(Environment.DIRECTORY_RINGTONES);
				case DIRECTORY_ALARMS:
					return getApplicationContext.getExternalFilesDir(Environment.DIRECTORY_ALARMS);
				case DIRECTORY_NOTIFICATIONS:
					return getApplicationContext.getExternalFilesDir(Environment.DIRECTORY_NOTIFICATIONS);
				case DIRECTORY_PICTURES:
					return getApplicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
				case DIRECTORY_MOVIES:
					return getApplicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
				case NULL:
					return getApplicationContext.getExternalFilesDir(null);
				default:
					return null;
			}
		} else{
			return null;
		}
	}


	public enum FileType{
		NULL, DIRECTORY_MUSIC, DIRECTORY_PODCASTS, DIRECTORY_RINGTONES, DIRECTORY_ALARMS, DIRECTORY_NOTIFICATIONS, DIRECTORY_PICTURES, DIRECTORY_MOVIES
	}
}
