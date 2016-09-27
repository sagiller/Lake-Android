package com.capgemini.psclient.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.capgemini.psclient.R;
import com.capgemini.psclient.ui.adapter.GuideActivityPagerAdapter;
import com.capgemini.psclient.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends BaseActivity{
	private static final float X_OK_DISTANCE = ScreenUtils.dp2px(50);
	private static final float y_OK_DISTANCE = ScreenUtils.dp2px(200);
	private ViewPager mViewPager;
	private List<View> mViewList = null;
	private float mOldX, mOldY;
	private boolean mFirstTime = true;


	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);

		mFirstTime = true;
		mViewPager = (ViewPager) findViewById(R.id.guide_viewpager);

		LayoutInflater inflater = getLayoutInflater();
		View subView1 = inflater.inflate(R.layout.view_guide_1, null);
		View subView2 = inflater.inflate(R.layout.view_guide_2, null);

		mViewList = new ArrayList<View>();
		mViewList.add(subView1);
		mViewList.add(subView2);

		GuideActivityPagerAdapter adapter = new GuideActivityPagerAdapter(mViewList);
		mViewPager.setAdapter(adapter);

		mViewPager.setOnTouchListener(new View.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event){
				if(mViewPager.getCurrentItem() == 0){
					return false;
				} else{
					switch(event.getAction()){
						case MotionEvent.ACTION_DOWN:
							mOldX = event.getX();
							mOldY = event.getY();
							break;
						case MotionEvent.ACTION_MOVE:
							if((mOldX - event.getX()) > X_OK_DISTANCE && Math.abs(mOldY - event.getY()) < y_OK_DISTANCE){
								gotoMainActivity();
								return true;
							}
							break;
					}
				}
				return false;
			}
		});
	}


	private synchronized void gotoMainActivity(){
		if(mFirstTime){
			Intent i = new Intent(this, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			overridePendingTransition(R.anim.anim_left_in, R.anim.anim_left_out);
		}
		mFirstTime = false;
	}
}