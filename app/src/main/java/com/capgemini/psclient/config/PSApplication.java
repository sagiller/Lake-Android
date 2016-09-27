package com.capgemini.psclient.config;

import android.app.Application;

import com.capgemini.psclient.utils.CrashHandler;


public class PSApplication extends Application{
	private static PSApplication mInstance;


	public static PSApplication getInstance(){
		return mInstance;
	}


	@Override
	public void onCreate(){
		super.onCreate();
		mInstance = this;

		initCrashHandler();
	}


	private void initCrashHandler(){
		CrashHandler crashHandler = CrashHandler.getIntance();
		crashHandler.initCrashHandler();
	}
}
