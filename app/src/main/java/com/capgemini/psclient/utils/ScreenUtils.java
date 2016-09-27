package com.capgemini.psclient.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.capgemini.psclient.R;
import com.capgemini.psclient.config.EnvironmentConfig;
import com.capgemini.psclient.config.PSApplication;

import java.io.File;

public class ScreenUtils{
	// cannot be instantiated
	private ScreenUtils(){
		throw new UnsupportedOperationException("cannot be instantiated");
	}


	public static int getScreenWidth(){
		return getScreenWidth(PSApplication.getInstance().getApplicationContext());
	}

	public static int getScreenWidth(Context getApplicationContext){
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) getApplicationContext.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}


	public static int getScreenHeight(){
		return getScreenHeight(PSApplication.getInstance().getApplicationContext());
	}

	public static int getScreenHeight(Context getApplicationContext){
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) getApplicationContext.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}


	public static int dp2px(int dp){
		final float scale = PSApplication.getInstance().getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}


	public static Bitmap takeScreenshotWithStatusBar(Activity activity){
		View view = activity.getWindow().getDecorView(); // 获取windows中最顶层的view
		boolean originalCache = view.isDrawingCacheEnabled();
		view.setDrawingCacheEnabled(true); // 允许当前窗口保存缓存信息
		Bitmap bmp = view.getDrawingCache();  // 要获取它的cache先要通过setDrawingCacheEnable方法把cache开启，就可以获得view的cache图片了。buildDrawingCache方法可以不用调用，因为调用getDrawingCache方法时，若cache没有建立，系统会自动调用buildDrawingCache

		int width = getScreenWidth(activity);
		int height = getScreenHeight(activity);
		Bitmap result = Bitmap.createBitmap(bmp, 0, 0, width, height);
		view.destroyDrawingCache();  // 销毁缓存信息
		if(!originalCache){
			view.setDrawingCacheEnabled(originalCache);
		}
		return result;
	}


	public static void takeScreenshotAndSave(Activity activity){
		if(StoragePathUtils.getExternalSDCardPath() == null){
			Toast.makeText(activity, activity.getString(R.string.image_save_success), Toast.LENGTH_SHORT).show();
			return;
		}
		File path = new File(StoragePathUtils.getExternalSDCardPath(), EnvironmentConfig.FOLDER_NAME);
		if(!path.exists()){
			path.mkdirs();
		}
		String name = TimeUtils.getFormatCurrentDate() + ".jpg";
		File file = new File(path, name);
		ImageUtils.saveImageToFileFromBitmap(file, takeScreenshotWithStatusBar(activity), true);
		Toast.makeText(activity, activity.getString(R.string.image_save_success), Toast.LENGTH_SHORT).show();
	}
}