package com.capgemini.psclient.ui.adapter;

import android.view.View;
import android.widget.TextView;

import com.capgemini.psclient.R;

public class ServerDisplayViewHolder{
	private View rootView;

	public ServerDisplayViewHolder(View rootView){
		this.rootView = rootView;
	}


	public TextView getTextView(){
		return (TextView) rootView.findViewById(R.id.item_server_display_name);
	}
}
