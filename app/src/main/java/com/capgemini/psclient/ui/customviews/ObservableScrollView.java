package com.capgemini.psclient.ui.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ObservableScrollView extends ScrollView{
	private OnCustomScrollListener mOnCustomScrollListener;


	public ObservableScrollView(Context context){
		super(context);
	}

	public ObservableScrollView(Context context, AttributeSet attrs){
		super(context, attrs);
	}

	public ObservableScrollView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ObservableScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt){
		super.onScrollChanged(l, t, oldl, oldt);
		if(mOnCustomScrollListener != null){
			mOnCustomScrollListener.onScrollChanged(this, l, t, oldl, oldt);
		}
	}


	public void setOnCustomScrollListener(OnCustomScrollListener listener){
		mOnCustomScrollListener = listener;
	}


	public interface OnCustomScrollListener{
		public void onScrollChanged(ObservableScrollView scrollView, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
	}

	@Override
	public void setVisibility(int visibility){
		super.setVisibility(visibility);
	}
}
