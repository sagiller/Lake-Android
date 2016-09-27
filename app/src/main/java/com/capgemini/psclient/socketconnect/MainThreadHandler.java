package com.capgemini.psclient.socketconnect;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;


public class MainThreadHandler extends Handler{
	private static MainThreadHandler mInstance = new MainThreadHandler();
	private SocketConnectionThread mSocketConnectionThread;
	private List<Integer> mOutStandingImageTransactionIDs = new ArrayList<Integer>();
	private OnConnectListener mOnConnectListener = null;
	private GetDocListener mGetDocListener = null;
	private OnErrorListener mOnErrorListener = null;


	private MainThreadHandler(){
		super(Looper.getMainLooper());
	}

	public static MainThreadHandler getMainHandler(){
		return mInstance;
	}


	@Override
	public void handleMessage(Message msg){
		switch(msg.what){
			case PSConstant.CONNECT_SUCCESS:
				if(mOnConnectListener != null){
					mOnConnectListener.onSocketConnected();
				}
				break;
			case PSConstant.DISCONNECT_SUCCESS:
				if(mOnConnectListener != null){
					mOnConnectListener.onDisconnect();
				}
				break;
			case PSConstant.NORMAL_CONTENT:
				break;
			case PSConstant.EVENT_CHANGE:
				if(mGetDocListener != null){
					mGetDocListener.onEventChanged();
				}
				break;
			case PSConstant.DOCUMENT_ID:
				if(mGetDocListener != null){
					mGetDocListener.onGetDocId((String) msg.obj);
				}
				break;
			case PSConstant.GET_IMAGE:
				if(mGetDocListener != null){
					mGetDocListener.onGetDocImage((byte[]) msg.obj, msg.arg1);
				}
				break;
			case PSConstant.ERROR_IOEXCEPTION:
			case PSConstant.ERROR_TIMEOUT:
			case PSConstant.ERROR_UNKNOW_HOST:
			case PSConstant.ERROR_INVALIDALGORITHMPARAM:
			case PSConstant.ERROR_NOSUCHALGORITHM:
			case PSConstant.ERROR_NOSUCHPADDING:
			case PSConstant.ERROR_INVALIDKEY:
				if(mOnErrorListener != null){
					mOnErrorListener.onError(msg.what);
				}
				break;
		}
	}


	public void startSocketThread(String ip, String password){
		if(mSocketConnectionThread != null){
			stopSocketThread();
		}
		mSocketConnectionThread = new SocketConnectionThread(mInstance);
		mSocketConnectionThread.setIP(ip);
		mSocketConnectionThread.setPassword(password);
		mSocketConnectionThread.start();
	}

	public void stopSocketThread(){
		if(SocketConnectionThread.mIsThreadRunning){
			mSocketConnectionThread.closeThread();
			mOutStandingImageTransactionIDs.clear();
		}
	}

	public void getActiveDocID(){
		StringBuffer s = new StringBuffer();
		s.append("var docID = '0';");
		s.append("if (app.documents.length)");
		s.append("{");
		s.append("  docID = activeDocument.id;");
		s.append("}");
		s.append("'" + PSConstant.DOCUMENT_ID_STR + "' + String.fromCharCode(13) + docID;");
		sendJStoPS(s.toString());
	}

	public void subscribeEvents(){
		subscribeEvent(PSConstant.CLOSED_DOCUMENT_STR);
		subscribeEvent(PSConstant.NEW_DOCUMENT_VIEW_CREATED_STR);
		subscribeEvent(PSConstant.CURRENT_DOCUMENT_CHANGED_STR);
		subscribeEvent(PSConstant.ACTIVE_VIEW_CHANGED_STR);
		subscribeEvent(PSConstant.DOCUMENT_CHANGED_STR);
	}

	/**
	 * Request image data from Photoshop. JPEG (1) or Pixmap (2)
	 *
	 * @param inImageID  unique ID of image given to me from Photoshop
	 * @param wantPixmap whether get pixmap
	 * @param w          width
	 * @param h          height
	 */
	public void getPhotoshopImage(String inImageID, boolean wantPixmap, int w, int h){
		if(w <= 1 || h <= 1 || inImageID == null || inImageID.length() == 0){
			return;
		}
		StringBuffer s = new StringBuffer();
		s.append("if (documents.length) {");
		s.append("var idNS = stringIDToTypeID( 'sendDocumentThumbnailToNetworkClient' );");
		s.append("var desc1 = new ActionDescriptor();");
		s.append("desc1.putInteger( stringIDToTypeID( 'documentID' )," + inImageID + ");");
		s.append("desc1.putInteger( stringIDToTypeID( 'width' )," + w + ");");
		s.append("desc1.putInteger( stringIDToTypeID( 'height' )," + h + ");");
		s.append("desc1.putInteger( stringIDToTypeID( 'format' )," + (wantPixmap ? PSConstant.PIXMAP_TYPE_STR : PSConstant.JPEG_TYPE_STR) + ");");
		s.append("executeAction( idNS, desc1, DialogModes.NO );");
		s.append("'Image Request Sent';");
		s.append("}");
		if(SocketConnectionThread.mIsSocketConnect){
			int id = sendJStoPS(s.toString());
			dumpAllCurrentImageRequests();
			mOutStandingImageTransactionIDs.add(id);
		}
	}


	private void subscribeEvent(String eventName){
		StringBuffer s = new StringBuffer();
		s.append("var idNS = stringIDToTypeID( 'networkEventSubscribe' );");
		s.append("var desc1 = new ActionDescriptor();");
		s.append("desc1.putClass( stringIDToTypeID( 'eventIDAttr' ), stringIDToTypeID( '" + eventName + "' ) );");
		s.append("executeAction( idNS, desc1, DialogModes.NO );");
		s.append("'" + PSConstant.SUBSCRIBE_SUCCESS_STR + "' + String.fromCharCode(13) + 'YES';");
		sendJStoPS(s.toString());
	}

	private void dumpAllCurrentImageRequests(){
		if(mOutStandingImageTransactionIDs.size() > 0){
			for(int i = mOutStandingImageTransactionIDs.size() - 1; mOutStandingImageTransactionIDs.size() > 0; i++){
				Integer id = mOutStandingImageTransactionIDs.get(i);
				mSocketConnectionThread.dumpTransaction(id);
				mOutStandingImageTransactionIDs.remove(i);
			}
		}
	}

	private int sendJStoPS(String jsConent){
		if(SocketConnectionThread.mIsSocketConnect){
			return mSocketConnectionThread.sendJavaScriptToServer(jsConent);
		}
		return -1;
	}


	//////////////////// Listeners ////////////////////
	public interface OnConnectListener{
		public void onSocketConnected();

		public void onDisconnect();
	}

	public void setOnConnectListener(OnConnectListener onConnectListener){
		this.mOnConnectListener = onConnectListener;
	}

	public interface GetDocListener{
		public void onGetDocId(String documentId);

		public void onGetDocImage(byte[] inBytes, int inIndexer);

		public void onEventChanged();
	}

	public void setGetDocListener(GetDocListener getDocListener){
		this.mGetDocListener = getDocListener;
	}

	public interface OnErrorListener{
		public void onError(int errorIndex);
	}

	public void setOnErrorListener(OnErrorListener errorListener){
		mOnErrorListener = errorListener;
	}
}