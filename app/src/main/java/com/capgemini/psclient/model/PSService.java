package com.capgemini.psclient.model;

public class PSService{
	private String mName;
	private String mIP;
	private String mPassword;


	public PSService(String name, String ip, String password){
		mName = name;
		mIP = ip;
		mPassword = password;
	}


	@Override
	public boolean equals(Object obj){
		if(obj instanceof PSService){
			if(((PSService) obj).getName() == null){
				return false;
			}
			if(((PSService) obj).getName().equals(mName) && ((PSService) obj).getIP().equals(mIP)){
				return true;
			} else{
				return false;
			}
		}
		return false;
	}

	@Override
	public String toString(){
		return mName;
	}

	public String getName(){
		return mName;
	}

	public void setName(String mName){
		this.mName = mName;
	}

	public String getIP(){
		return mIP;
	}

	public void setIP(String mIP){
		this.mIP = mIP;
	}

	public String getPassword(){
		return mPassword;
	}

	public void setPassword(String mPassword){
		this.mPassword = mPassword;
	}
}
