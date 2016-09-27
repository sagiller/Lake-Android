package com.capgemini.psclient.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.capgemini.psclient.R;
import com.capgemini.psclient.config.PSApplication;
import com.capgemini.psclient.model.PSService;

import java.util.List;

public class ServerDisplayListViewAdapter extends ArrayAdapter<PSService>{
	private List<PSService> mDataset;


	public ServerDisplayListViewAdapter(Context context, int resource, List<PSService> list){
		super(context, resource, list);
		mDataset = list;
	}


	@Override
	public int getCount(){
		if(mDataset == null){
			return 0;
		} else{
			if(mDataset.size() == 1){  // 特殊处理只有一个item的情况
				return 2;
			} else{
				return mDataset.size();
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View rowView = convertView;
		ServerDisplayViewHolder viewHolder = null;

		if(rowView == null){
			LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
			rowView = inflater.inflate(R.layout.item_server_display_listview, null);
			viewHolder = new ServerDisplayViewHolder(rowView);
			rowView.setTag(viewHolder);
		} else{
			viewHolder = (ServerDisplayViewHolder) rowView.getTag();
		}

		if(getItem(position) != null){
			String name = getItem(position).getName();
			if(name == null || name.length() == 0){
				viewHolder.getTextView().setText(getItem(position).getIP());
			} else{
				viewHolder.getTextView().setText(name);
			}
			if(viewHolder.getTextView().getBackground() == null){
				viewHolder.getTextView().setBackground(PSApplication.getInstance().getResources().getDrawable(R.drawable.item_psserver_listview_bg));
			}
		} else{
			viewHolder.getTextView().setText("");
			viewHolder.getTextView().setBackground(null);
		}

		return rowView;
	}

	@Override
	public PSService getItem(int position){
		if(mDataset.size() == 1){ // 特殊处理只有一个item的情况
			if(position == 0){
				return null;
			} else if(position == 1){
				return mDataset.get(0);
			}
		}

		return mDataset.get(position);
	}
}
