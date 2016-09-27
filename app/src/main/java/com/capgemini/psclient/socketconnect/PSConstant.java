package com.capgemini.psclient.socketconnect;

public class PSConstant{
	public static final String ENCODE = "UTF-8";
	public static final int PS_PORT_NO = 49494;
	public static final int CONNECT_TIMEOUT = 10000;

	public static final int LENGTH_LENGTH = 4; // Length of message
	public static final int COMM_LENGTH = 4; // Communication status
	public static final int PROTOCOL_LENGTH = 4 + 4 + 4; // number of bytes for the message header: protocol version + transaction ID + content type
	public static final int NO_COMM_ERROR = 0; // No communication error
	public static final int PROTOCOL_VERSION = 1; // Current version of the protocol used by Photoshop

	public static final int ERRORSTRING_TYPE = 1;
	public static final int JAVASCRIPT_TYPE = 2;
	public static final int IMAGE_TYPE = 3;

	// PS Event
	public static final String CLOSED_DOCUMENT_STR = "closedDocument";
	public static final String NEW_DOCUMENT_VIEW_CREATED_STR = "newDocumentViewCreated";
	public static final String CURRENT_DOCUMENT_CHANGED_STR = "currentDocumentChanged";
	public static final String ACTIVE_VIEW_CHANGED_STR = "activeViewChanged";
	public static final String DOCUMENT_CHANGED_STR = "documentChanged";
	public static final String SUBSCRIBE_SUCCESS_STR = "subscribeSuccess";
	public static final String DOCUMENT_ID_STR = "documentID";
	public static final String JPEG_TYPE_STR = "1";
	public static final String PIXMAP_TYPE_STR = "2";

	// Message Code
	public static final int CONNECT_SUCCESS = 1001;
	public static final int DISCONNECT_SUCCESS = 1002;
	public static final int ERROR_UNKNOW_HOST = 1003;
	public static final int ERROR_TIMEOUT = 1004;
	public static final int ERROR_IOEXCEPTION = 1005;
	public static final int ERROR_INVALIDALGORITHMPARAM = 1006;
	public static final int ERROR_NOSUCHALGORITHM = 1007;
	public static final int ERROR_NOSUCHPADDING = 1008;
	public static final int ERROR_INVALIDKEY = 1009;
	public static final int NORMAL_CONTENT = 7777;
	public static final int DOCUMENT_ID = 7778;
	public static final int GET_IMAGE = 7779;
	public static final int EVENT_CHANGE = 7780;


	private PSConstant(){
		throw new UnsupportedOperationException("Can not create instance.");
	}
}
