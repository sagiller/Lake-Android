package com.capgemini.psclient.socketconnect;

import android.os.Message;
import android.util.Log;

import com.capgemini.psclient.config.EnvironmentConfig;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class SocketConnectionThread extends Thread{
	public static boolean mIsThreadRunning = false;
	public static boolean mIsSocketConnect = false;
	private String mIP;
	private String mPassword;
	private Socket mSocket = null;
	private InputStream mInputStream = null;
	private DataInputStream mDInputStream = null;
	private OutputStream mOutputStream = null;
	private DataOutputStream mDOutputStream = null;
	private MainThreadHandler mHandler;
	private Message mMsg;
	private EncryptDecrypt mEncryptDecrypt = null;
	private int mTransactionID = 0; // Each message sent and received will have an ID. If you send more than one make sure your ID's match.
	private List<Integer> mTransactionsToDump = new ArrayList<Integer>();


	public SocketConnectionThread(MainThreadHandler handler){
		this.mHandler = handler;
	}


	@Override
	public void run(){
		Log.i(EnvironmentConfig.LOG_TAG, "Thread is running");
		mIsThreadRunning = true;

		try{
			mEncryptDecrypt = new EncryptDecrypt(mPassword);
			mSocket = new Socket();
			SocketAddress socketAddress = new InetSocketAddress(mIP, PSConstant.PS_PORT_NO);
			mSocket.connect(socketAddress, PSConstant.CONNECT_TIMEOUT);

			mOutputStream = mSocket.getOutputStream();
			mDOutputStream = new DataOutputStream(mOutputStream);
			mInputStream = mSocket.getInputStream();
			mDInputStream = new DataInputStream(mInputStream);
			mTransactionsToDump.clear();

			mIsSocketConnect = true;
			mMsg = mHandler.obtainMessage(PSConstant.CONNECT_SUCCESS, "Socket is connected.");
			mHandler.sendMessage(mMsg);

			final int bytesBeforeProcessing = PSConstant.LENGTH_LENGTH + PSConstant.COMM_LENGTH + PSConstant.PROTOCOL_LENGTH;
			while(mIsSocketConnect){
				readMessageFromServer(bytesBeforeProcessing);
			}
		} catch(UnknownHostException e){
			e.printStackTrace();
			mMsg = mHandler.obtainMessage(PSConstant.ERROR_UNKNOW_HOST, "UnknownHostException...");
			mHandler.sendMessage(mMsg);
		} catch(SocketTimeoutException e){
			e.printStackTrace();
			mMsg = mHandler.obtainMessage(PSConstant.ERROR_TIMEOUT, "Time out...");
			mHandler.sendMessage(mMsg);
		} catch(IOException e){
			e.printStackTrace();
			mMsg = mHandler.obtainMessage(PSConstant.ERROR_IOEXCEPTION, "IOException...");
			mHandler.sendMessage(mMsg);
		} catch(InvalidAlgorithmParameterException e){
			e.printStackTrace();
			mMsg = mHandler.obtainMessage(PSConstant.ERROR_INVALIDALGORITHMPARAM, "InvalidAlgorithmParameterException...");
			mHandler.sendMessage(mMsg);
		} catch(NoSuchAlgorithmException e){
			e.printStackTrace();
			mMsg = mHandler.obtainMessage(PSConstant.ERROR_NOSUCHALGORITHM, "NoSuchAlgorithmException...");
			mHandler.sendMessage(mMsg);
		} catch(NoSuchPaddingException e){
			e.printStackTrace();
			mMsg = mHandler.obtainMessage(PSConstant.ERROR_NOSUCHPADDING, "NoSuchPaddingException...");
			mHandler.sendMessage(mMsg);
		} catch(InvalidKeyException e){
			e.printStackTrace();
			mMsg = mHandler.obtainMessage(PSConstant.ERROR_INVALIDKEY, "InvalidKeyException...");
			mHandler.sendMessage(mMsg);
		} finally{
			closeThread();
		}
	}


	public void setIP(String mIP){
		this.mIP = mIP;
	}

	public void setPassword(String mPassword){
		this.mPassword = mPassword;
	}

	public void dumpTransaction(int inTransaction){
		mTransactionsToDump.add(inTransaction);
	}

	public void closeThread(){
		mIsSocketConnect = false;
		Log.i(EnvironmentConfig.LOG_TAG, "Disconnect successfully.");

		if(mDOutputStream != null){
			try{
				mDOutputStream.close();
				mDOutputStream = null;
			} catch(IOException e){
				e.printStackTrace();
			}
		}
		if(mOutputStream != null){
			try{
				mOutputStream.close();
				mOutputStream = null;
			} catch(IOException e){
				e.printStackTrace();
			}
		}
		if(mDInputStream != null){
			try{
				mDInputStream.close();
				mDInputStream = null;
			} catch(IOException e){
				e.printStackTrace();
			}
		}
		if(mInputStream != null){
			try{
				mInputStream.close();
				mInputStream = null;
			} catch(IOException e){
				e.printStackTrace();
			}
		}
		if(mSocket != null){
			try{
				mSocket.close();
				mSocket = null;
			} catch(IOException e){
				e.printStackTrace();
			}
		}
		mEncryptDecrypt = null;

		mIsThreadRunning = false;
		mMsg = mHandler.obtainMessage(PSConstant.DISCONNECT_SUCCESS, "Disconnect.");
		mHandler.sendMessage(mMsg);
		Log.i(EnvironmentConfig.LOG_TAG, "Thread is stopped");
	}

	public int sendJavaScriptToServer(String inJavaScript){
		if(inJavaScript == null || mSocket == null || mOutputStream == null || !mIsSocketConnect){
			return -1;
		}

		int returnID = mTransactionID++;
		try{
			byte[] strBytes = inJavaScript.getBytes(PSConstant.ENCODE);
			byte[] allBytes = new byte[PSConstant.PROTOCOL_LENGTH + strBytes.length + 1];
			int byteIndexer = 0;
			allBytes[byteIndexer++] = (byte) (PSConstant.PROTOCOL_VERSION >>> 24);
			allBytes[byteIndexer++] = (byte) (PSConstant.PROTOCOL_VERSION >>> 16);
			allBytes[byteIndexer++] = (byte) (PSConstant.PROTOCOL_VERSION >>> 8);
			allBytes[byteIndexer++] = (byte) (PSConstant.PROTOCOL_VERSION);

			int messageID = returnID;

			allBytes[byteIndexer++] = (byte) (messageID >>> 24);
			allBytes[byteIndexer++] = (byte) (messageID >>> 16);
			allBytes[byteIndexer++] = (byte) (messageID >>> 8);
			allBytes[byteIndexer++] = (byte) (messageID);

			allBytes[byteIndexer++] = (byte) (PSConstant.JAVASCRIPT_TYPE >>> 24);
			allBytes[byteIndexer++] = (byte) (PSConstant.JAVASCRIPT_TYPE >>> 16);
			allBytes[byteIndexer++] = (byte) (PSConstant.JAVASCRIPT_TYPE >>> 8);
			allBytes[byteIndexer++] = (byte) (PSConstant.JAVASCRIPT_TYPE);

			for(int i = 0; i < strBytes.length; i++){
				allBytes[byteIndexer++] = strBytes[i];
			}

			allBytes[byteIndexer++] = (byte) 0x0a; // this is \n
			byte[] encryptedBytes = mEncryptDecrypt.encrypt(allBytes);
			int messageLength = PSConstant.COMM_LENGTH + encryptedBytes.length; // the communication status is not encrypted, add that length

			mDOutputStream.writeInt(messageLength);
			mDOutputStream.writeInt(PSConstant.NO_COMM_ERROR);
			mDOutputStream.write(encryptedBytes, 0, encryptedBytes.length);
			mDOutputStream.flush();
		} catch(IOException e){
			e.printStackTrace();
			return -1;
		} catch(IllegalBlockSizeException e){
			e.printStackTrace();
			return -1;
		} catch(BadPaddingException e){
			e.printStackTrace();
			return -1;
		}
		return returnID;
	}

	private void readMessageFromServer(int bytesBeforeProcessing){
		try{
			//if(mDInputStream.available() >= bytesBeforeProcessing){
			int messageLength = mDInputStream.readInt();
			int comStatus = mDInputStream.readInt();

			if(messageLength < bytesBeforeProcessing || comStatus != 0){
				return;
			}

			int encryptedMessageLength = messageLength - PSConstant.COMM_LENGTH;

			byte[] messageBytes = new byte[encryptedMessageLength];
			int offSet = 0;
			int bytesToComplete = encryptedMessageLength;
			boolean done = false;
			while(!done){
				int available = mDInputStream.available();
				if(available > bytesToComplete){
					available = bytesToComplete;
				}
				if(available > 0){
					mDInputStream.read(messageBytes, offSet, available);
					offSet += available;
					bytesToComplete -= available;
					if(offSet >= encryptedMessageLength){
						done = true;
					}
				}
			}

			if(comStatus != PSConstant.NO_COMM_ERROR){
				readDecryptedMessage(messageBytes);
			} else{
				byte[] decryptedBytes = mEncryptDecrypt.decrypt(messageBytes);
				readDecryptedMessage(decryptedBytes);
			}
			//}
		} catch(IOException e){
			e.printStackTrace();
			mMsg = mHandler.obtainMessage(PSConstant.ERROR_IOEXCEPTION, "IOException...");
			mHandler.sendMessage(mMsg);
		} catch(IllegalBlockSizeException e){
			e.printStackTrace();
			Log.i(EnvironmentConfig.LOG_TAG, "IllegalBlockSizeException....");
		} catch(BadPaddingException e){
			e.printStackTrace();
			Log.i(EnvironmentConfig.LOG_TAG, "BadPaddingException....");
		}
	}

	private void readDecryptedMessage(byte[] messageBytes){
		try{
			int messageIndexer = 0;
			int messageVersion = (messageBytes[messageIndexer++] << 24) + ((messageBytes[messageIndexer++] & 0xFF) << 16) + ((messageBytes[messageIndexer++] & 0xFF) << 8) + (messageBytes[messageIndexer++] & 0xFF);
			if(messageVersion != PSConstant.PROTOCOL_VERSION){
				return;
			}

			int messageID = (messageBytes[messageIndexer++] << 24) + ((messageBytes[messageIndexer++] & 0xFF) << 16) + ((messageBytes[messageIndexer++] & 0xFF) << 8) + (messageBytes[messageIndexer++] & 0xFF);
			if(messageID == mTransactionID){
				return;
			}

			if(mTransactionsToDump.size() > 0){
				for(int i = 0; i < mTransactionsToDump.size(); i++){
					Integer id = mTransactionsToDump.get(i);
					if(id == messageID){
						mTransactionsToDump.remove(i);
						return;
					}
				}
			}

			int messageType = (messageBytes[messageIndexer++] << 24) + ((messageBytes[messageIndexer++] & 0xFF) << 16) + ((messageBytes[messageIndexer++] & 0xFF) << 8) + (messageBytes[messageIndexer++] & 0xFF);
			if(messageType == PSConstant.JAVASCRIPT_TYPE || messageType == PSConstant.ERRORSTRING_TYPE){
				Log.i(EnvironmentConfig.LOG_TAG, "Message return is JAVASCRIPT_TYPE || ERRORSTRING_TYPE");
				byte[] returnBytes = new byte[messageBytes.length - messageIndexer];
				for(int i = 0; i < returnBytes.length; i++){
					returnBytes[i] = messageBytes[messageIndexer++];
				}
				String messageResult = new String(returnBytes, "UTF-8");
				String[] splitter = messageResult.split("\r");
				if(splitter.length == 2){
					if(splitter[0].equals(PSConstant.DOCUMENT_ID_STR)){
						mMsg = mHandler.obtainMessage(PSConstant.DOCUMENT_ID, splitter[1]);
						mHandler.sendMessage(mMsg);
					} else if(splitter[0].equals(PSConstant.CLOSED_DOCUMENT_STR) || splitter[0].equals(PSConstant.NEW_DOCUMENT_VIEW_CREATED_STR) || splitter[0].equals(PSConstant.CURRENT_DOCUMENT_CHANGED_STR) || splitter[0].equals(PSConstant.ACTIVE_VIEW_CHANGED_STR) || splitter[0].equals(PSConstant.DOCUMENT_CHANGED_STR)){
						mMsg = mHandler.obtainMessage(PSConstant.EVENT_CHANGE, splitter[1]);
						mHandler.sendMessage(mMsg);
					}
				} else{
					return;
				}
			} else if(messageType == PSConstant.IMAGE_TYPE){
				Log.i(EnvironmentConfig.LOG_TAG, "Message return is IMAGE_TYPE");
				mMsg = mHandler.obtainMessage(PSConstant.GET_IMAGE, messageIndexer, 0, messageBytes); // arg1, arg2, obj
				mHandler.sendMessage(mMsg);
			}
		} catch(Exception e){
		}
	}
}