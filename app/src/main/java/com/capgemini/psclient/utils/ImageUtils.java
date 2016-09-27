package com.capgemini.psclient.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils{
	private ImageUtils(){
		throw new UnsupportedOperationException("cannot be instantiated");
	}


	public static boolean saveImageToFileFromBitmap(File newFile, Bitmap bitmap, boolean needRecycle){
		FileOutputStream fileOutputStream = null;
		try{
			fileOutputStream = new FileOutputStream(newFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
			fileOutputStream.flush();
			return true;
		} catch(FileNotFoundException e){
			e.printStackTrace();
			return false;
		} catch(IOException e){
			e.printStackTrace();
			return false;
		} finally{
			if(needRecycle){
				bitmap.recycle();
			}
			try{
				if(fileOutputStream != null){
					fileOutputStream.close();
				}
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}


	public static int calculateInSampleSize(BitmapFactory.Options options, int requestWidth, int requestHeight){
		final int originalHeight = options.outHeight;
		final int originalWidth = options.outWidth;
		int inSampleSize = 1;
		if(requestWidth <= 0 || requestHeight <= 0){
			return inSampleSize;
		}
		if(originalHeight > requestHeight || originalWidth > requestWidth){
			final int heightRatio = Math.round((float) originalHeight / (float) requestHeight);
			final int widthRatio = Math.round((float) originalWidth / (float) requestWidth);
			inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}
}
