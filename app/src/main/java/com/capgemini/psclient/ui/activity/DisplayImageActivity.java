package com.capgemini.psclient.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.capgemini.psclient.R;
import com.capgemini.psclient.config.EnvironmentConfig;
import com.capgemini.psclient.socketconnect.MainThreadHandler;
import com.capgemini.psclient.socketconnect.PSConstant;
import com.capgemini.psclient.socketconnect.SocketConnectionThread;
import com.capgemini.psclient.ui.customviews.ConfirmDialog;
import com.capgemini.psclient.ui.customviews.movescaleimageview.MoveScaleImageView;
import com.capgemini.psclient.utils.ScreenUtils;


public class DisplayImageActivity extends BaseActivity implements View.OnClickListener{
	public static final String DOC_ID = "doc_id";
	private static final int IMAGE_WIDTH_HEIGHT = 10000;
	private MainThreadHandler mHandler;
	private MainThreadHandler.GetDocListener mGetDocListener;
	private MainThreadHandler.OnErrorListener mOnErrorListener;
	private String mActivityDocId;
	private MoveScaleImageView mImageView;
	private ImageView mBtnClose, mBtnTakePic, mBtnRefresh;
	private ConfirmDialog mConfirmDialog;
	private boolean mShouldExit = false;


	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_display_image);

		initViews();
		mHandler = MainThreadHandler.getMainHandler();
		setCallbackListeners();
		startGetImageUsingDocId(getIntent().getStringExtra(DOC_ID));
		// mHandler.subscribeEvents();
	}

	@Override
	protected void onStart(){
		super.onStart();
		mShouldExit = false;
	}

	@Override
	protected void onDestroy(){
		if(SocketConnectionThread.mIsThreadRunning){
			mHandler.stopSocketThread();
		}
		mImageView.recycle();
		super.onDestroy();
	}

	@Override
	public void onClick(View v){
		switch(v.getId()){
			case R.id.displayimage_image:
				displayHideBtns(false);
				break;
			case R.id.displayimage_close:
				onBackPressed();
				break;
			case R.id.displayimage_takepic:
				displayHideBtns(true);
				break;
			case R.id.displayimage_refresh:
				mHandler.getActiveDocID();
				break;
		}
	}

	@Override
	public void onBackPressed(){
		if(mShouldExit){
			mHandler.stopSocketThread();

			Intent i = new Intent(this, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.putExtra(MainActivity.NEED_MAIN, true);
			startActivity(i);
		} else{
			if(mConfirmDialog == null){
				mConfirmDialog = new ConfirmDialog();
				mConfirmDialog.setDialogClickListener(new ConfirmDialog.DialogClickListener(){
					@Override
					public void onRightClicked(){
						mShouldExit = true;
						mConfirmDialog.dismiss();
						onBackPressed();
					}

					@Override
					public void onLeftClicked(){
						mConfirmDialog.dismiss();
					}
				});
			}
			mConfirmDialog.show(getFragmentManager(), "ConfirmDialog");
		}
	}


	private void initViews(){
		mImageView = (MoveScaleImageView) findViewById(R.id.displayimage_image);
		mBtnClose = (ImageView) findViewById(R.id.displayimage_close);
		mBtnTakePic = (ImageView) findViewById(R.id.displayimage_takepic);
		mBtnRefresh = (ImageView) findViewById(R.id.displayimage_refresh);

		mImageView.setOnClickListener(this);
		mBtnClose.setOnClickListener(this);
		mBtnTakePic.setOnClickListener(this);
		mBtnRefresh.setOnClickListener(this);
	}

	private void setCallbackListeners(){
		if(mOnErrorListener == null){
			mOnErrorListener = new MainThreadHandler.OnErrorListener(){
				@Override
				public void onError(int errorIndex){
				}
			};
		}
		if(mGetDocListener == null){
			mGetDocListener = new MainThreadHandler.GetDocListener(){
				@Override
				public void onGetDocId(String documentId){
					startGetImageUsingDocId(documentId);
				}

				@Override
				public void onGetDocImage(byte[] inBytes, int inIndexer){
					parseImage(inBytes, inIndexer);
				}

				@Override
				public void onEventChanged(){
					Log.i(EnvironmentConfig.LOG_TAG, "onEventChanged");
					mHandler.getActiveDocID();
				}
			};
		}
		mHandler.setOnErrorListener(mOnErrorListener);
		mHandler.setGetDocListener(mGetDocListener);
	}

	private void startGetImageUsingDocId(String documentId){
		if(documentId == null){
			return;
		}
		mActivityDocId = documentId;
		if(!mActivityDocId.equals("0")){
			// 一般选false，用jpg的，文件更小. 宽高如果传的比原图大，就回过来原图的大小。这里传最大值，直接可以拿到原图
			mHandler.getPhotoshopImage(mActivityDocId, false, IMAGE_WIDTH_HEIGHT, IMAGE_WIDTH_HEIGHT);
		}
	}

	private void parseImage(byte[] inBytes, int inIndexer){
		byte format = inBytes[inIndexer++];
		if(format == Integer.valueOf(PSConstant.JPEG_TYPE_STR)){
			mImageView.setImageBytes(inBytes, inIndexer, inBytes.length - inIndexer);
		}
	}

	private void displayHideBtns(final boolean takePic){
		if(mBtnClose.getVisibility() == View.VISIBLE){
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);
			animation.setAnimationListener(new Animation.AnimationListener(){
				@Override
				public void onAnimationStart(Animation animation){
				}

				@Override
				public void onAnimationEnd(Animation animation){
					mBtnClose.setVisibility(View.INVISIBLE);
					mBtnTakePic.setVisibility(View.INVISIBLE);
					mBtnRefresh.setVisibility(View.INVISIBLE);
					if(takePic){
						ScreenUtils.takeScreenshotAndSave(DisplayImageActivity.this);
					}
				}

				@Override
				public void onAnimationRepeat(Animation animation){
				}
			});

			mBtnClose.startAnimation(animation);
			mBtnTakePic.startAnimation(animation);
			mBtnRefresh.startAnimation(animation);
		} else{
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
			mBtnClose.setVisibility(View.VISIBLE);
			mBtnTakePic.setVisibility(View.VISIBLE);
			mBtnRefresh.setVisibility(View.VISIBLE);
			mBtnClose.startAnimation(animation);
			mBtnTakePic.startAnimation(animation);
			mBtnRefresh.startAnimation(animation);
		}
	}
}
