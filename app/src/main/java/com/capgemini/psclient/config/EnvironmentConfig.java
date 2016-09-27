package com.capgemini.psclient.config;

public class EnvironmentConfig{
	private EnvironmentConfig(){
		throw new UnsupportedOperationException("cannot be instantiated");
	}


	public static final String LOG_TAG = "lgz-debug";
	public static final String FOLDER_NAME	= "Lake";
	public static final String CRASH_LOG_FOLDER_NAME = "CrashLog";
	public static final int DB_VERSION = 1;
}
