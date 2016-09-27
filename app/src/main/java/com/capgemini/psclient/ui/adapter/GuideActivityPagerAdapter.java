package com.capgemini.psclient.ui.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class GuideActivityPagerAdapter extends PagerAdapter{
	private List<View> viewList = null;


	public GuideActivityPagerAdapter(List<View> views){
		viewList = views;
	}


	@Override
	public int getCount(){
		if(viewList != null){
			return viewList.size();
		}
		return 0;
	}

	@Override
	public boolean isViewFromObject(View view, Object object){
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position){
		container.addView(viewList.get(position));
		return viewList.get(position);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object){
		container.removeView(viewList.get(position));
	}
}
