package com.capgemini.psclient.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;

import com.capgemini.psclient.config.EnvironmentConfig;
import com.capgemini.psclient.config.PSApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class CrashHandler implements Thread.UncaughtExceptionHandler{
	private static CrashHandler mInstance = new CrashHandler();
	private Thread.UncaughtExceptionHandler mDefaultCrashHandler;
	private Context mContext;


	private CrashHandler(){
	}

	public static CrashHandler getIntance(){
		return mInstance;
	}


	@Override
	public void uncaughtException(Thread thread, Throwable ex){
		try{
			saveCrashLogToSDCard(ex);
		} catch(IOException e){
			e.printStackTrace();
		}

		ex.printStackTrace();

		if(mDefaultCrashHandler != null){
			mDefaultCrashHandler.uncaughtException(thread, ex);
		} else{
			Process.killProcess(Process.myPid());
		}
	}


	public void initCrashHandler(){
		mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(mInstance);
		mContext = PSApplication.getInstance().getApplicationContext();
	}


	private void saveCrashLogToSDCard(Throwable ex) throws IOException{
		if(StoragePathUtils.getExternalSDCardPath() == null){
			return;
		}

		File path = new File(StoragePathUtils.getExternalFilePath(mContext, StoragePathUtils.FileType.NULL), EnvironmentConfig.CRASH_LOG_FOLDER_NAME);
		String fileName = TimeUtils.getFormatDate(TimeUtils.getCurrentDate());
		File dataFile = new File(path, "CrashLog_" + fileName + ".txt");
		PrintWriter pw = null;
		BufferedWriter bw = null;
		FileWriter fw = null;

		try{
			if(!path.exists()){
				path.mkdirs();
			}
			if(!dataFile.exists()){
				dataFile.createNewFile();
			}
			fw = new FileWriter(dataFile);
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);

			printPhoneInfoAndError(pw, ex);
			pw.flush();

		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			if(pw != null){
				pw.close();
			}
			if(bw != null){
				bw.close();
			}
			if(fw != null){
				fw.close();
			}
		}
	}

	private void printPhoneInfoAndError(PrintWriter pw, Throwable ex){
		pw.println("Device Info:");

		PackageManager pm = mContext.getPackageManager();
		PackageInfo packageInfo = null;
		try{
			packageInfo = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
			pw.println("App Version: " + packageInfo.versionName);
			pw.println("Version Code: " + packageInfo.versionCode);
		} catch(PackageManager.NameNotFoundException e){
			e.printStackTrace();
		}

		pw.println("OS Version: " + Build.VERSION.RELEASE);
		pw.println("SDK Version: " + Build.VERSION.SDK_INT);
		pw.println("Device Vendor: " + Build.MANUFACTURER);
		pw.println("Device Model: " + Build.MODEL);

		pw.println();

		ex.printStackTrace(pw);
	}
}