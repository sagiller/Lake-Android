package com.capgemini.psclient.ui.customviews.movescaleimageview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.Toast;

import com.capgemini.psclient.config.EnvironmentConfig;
import com.capgemini.psclient.config.PSApplication;

public class MoveScaleImageView extends ImageView{
	/////////// public ///////////
	public static final int BYTES_PER_PIXEL = 4; // How many bytes does one pixel use
	private int minSlop;
	private boolean mIsLargeImage = false, mAsClick;
	private int mOriginalImageWidth, mOriginalImageheight, mImageViewWidth, mImageViewHeight;
	private float oriX = 0, oriY = 0, lastX, lastY, oriDistance, lastDistance, centerX, centerY;
	/////////// small ///////////
	private Bitmap mSmallImageBitmap;
	private float mSmallImageZoom = 1.0f, mSmallOrigZoom = 1.0f; // 小于1时，表示图片被缩小； 大于1时，表示图片被放大。（和大图的情况相反）
	private boolean mIsSmallFit = false;
	/////////// large ///////////
	private Scene mScene;
	private PointF mScreenFocus;


	public MoveScaleImageView(Context context){
		super(context);
		minSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		setScaleType(ScaleType.CENTER_INSIDE);
	}

	public MoveScaleImageView(Context context, AttributeSet attrs){
		super(context, attrs);
		minSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		setScaleType(ScaleType.CENTER_INSIDE);
	}

	public MoveScaleImageView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		minSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		setScaleType(ScaleType.CENTER_INSIDE);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public MoveScaleImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
		super(context, attrs, defStyleAttr, defStyleRes);
		minSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		setScaleType(ScaleType.CENTER_INSIDE);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(mIsLargeImage){
			if(event.getPointerCount() == 1){
				moveLargeImage(event);
			} else{
				scaleLargeImage(event);
			}
		} else{
			if(mIsSmallFit && getScaleType() != ScaleType.MATRIX){
				setScaleType(ScaleType.MATRIX);
			}
			if(event.getPointerCount() == 1){
				moveSmallImage(event);
			} else{
				scaleSmallImage(event);
			}
		}
		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		mImageViewWidth = getWidth();
		mImageViewHeight = getHeight();

		// TODO: 大小变动的时候，做一些处理
		if(mIsLargeImage){
			if(mScene != null){
				mScene.getViewport().setViewportSize(mImageViewWidth, mImageViewHeight);
				// 还有计算zoom、mInSampleSize、mSampleBitmap，保存位置等逻辑。
			}
		} else{
			if(mSmallImageBitmap != null){
				mSmallOrigZoom = calcuSmallInitZoom(mSmallImageBitmap.getWidth(), mSmallImageBitmap.getHeight());
				mSmallImageZoom = mSmallOrigZoom;
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas){
		if(mIsLargeImage){
			if(mScene != null){
				mScene.draw(canvas);
			}
		} else{
			super.onDraw(canvas);
		}
	}


	public void setImageBytes(byte[] bytes, int offset, int length){
		setScaleType(ScaleType.CENTER_INSIDE);
		setImageMatrix(new Matrix());

		BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
		tmpOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, offset, length, tmpOptions);
		mOriginalImageWidth = tmpOptions.outWidth;
		mOriginalImageheight = tmpOptions.outHeight;

		checkIsLargeImage();
		if(mIsLargeImage){
			Log.i(EnvironmentConfig.LOG_TAG, "Large Image Mode!!!");
			mScene = new Scene(this, bytes, offset, length, mOriginalImageWidth, mOriginalImageheight, mImageViewWidth, mImageViewHeight);
		} else{
			try{
				tmpOptions.inJustDecodeBounds = false;
				mSmallImageBitmap = BitmapFactory.decodeByteArray(bytes, offset, length);
				setImageBitmap(mSmallImageBitmap);
				mSmallOrigZoom = calcuSmallInitZoom(mSmallImageBitmap.getWidth(), mSmallImageBitmap.getHeight());
				mSmallImageZoom = mSmallOrigZoom;
			} catch(OutOfMemoryError e){
				mIsLargeImage = true;
				Toast.makeText(PSApplication.getInstance(), "被迫启用大图模式", Toast.LENGTH_SHORT).show();
				Log.i(EnvironmentConfig.LOG_TAG, "Force to Large Image Mode!!!");
				setImageBytes(bytes, offset, length);
				return;
			}
		}

		invalidate();
	}

	public void recycle(){
		if(mSmallImageBitmap != null){
			mSmallImageBitmap.recycle();
		}
		if(mScene != null){
			mScene.recycle();
		}
	}


	private void moveSmallImage(MotionEvent event){
		float deltaX = 0;
		float deltaY = 0;
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				mAsClick = true;
				oriX = event.getX();
				oriY = event.getY();
				lastX = oriX;
				lastY = oriY;
				break;
			case MotionEvent.ACTION_MOVE:
				deltaX = event.getX() - oriX;
				deltaY = event.getY() - oriY;
				if(Math.abs(deltaX) >= minSlop || Math.abs(deltaY) >= minSlop){
					mAsClick = false;
					Matrix matrix = getImageMatrix();
					matrix.postTranslate(event.getX() - lastX, event.getY() - lastY);
					setImageMatrix(matrix);
					invalidate();
				}
				lastX = event.getX();
				lastY = event.getY();
				break;
			case MotionEvent.ACTION_UP:
				if(mAsClick){
					performClick();
				}
		}
	}

	private void scaleSmallImage(MotionEvent event){
		switch(event.getActionMasked()){
			case MotionEvent.ACTION_POINTER_DOWN:
				mAsClick = false;
				oriDistance = getDistance(event);
				lastDistance = oriDistance;
				calculateCenterPoint(event);
				break;
			case MotionEvent.ACTION_MOVE:
				float endDistance = getDistance(event);
				if(Math.abs(endDistance - oriDistance) >= minSlop){
					float scale = endDistance / lastDistance;
					if(mSmallImageZoom * scale >= mSmallOrigZoom && mSmallImageZoom * scale <= 3){
						mSmallImageZoom = mSmallImageZoom * scale;
						Matrix matrix = getImageMatrix();
						matrix.postScale(scale, scale, centerX, centerY);
						setImageMatrix(matrix);
						invalidate();
					}
				}
				lastDistance = endDistance;
				break;
			case MotionEvent.ACTION_POINTER_UP:
				if(event.getPointerCount() == 2){
					if(event.getActionIndex() == 0){
						oriX = event.getX(1);
						oriY = event.getY(1);
					} else if(event.getActionIndex() == 1){
						oriX = event.getX(0);
						oriY = event.getY(0);
					}
					lastX = oriX;
					lastY = oriY;
				}
				break;
		}
	}

	private void moveLargeImage(MotionEvent event){
		float deltaX = 0;
		float deltaY = 0;
		float zoom = mScene.getViewport().getZoom();
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				mAsClick = true;
				oriX = event.getX();
				oriY = event.getY();
				lastX = oriX;
				lastY = oriY;
				break;
			case MotionEvent.ACTION_MOVE:
				deltaX = event.getX() - oriX;
				deltaY = event.getY() - oriY;
				if(Math.abs(deltaX) >= minSlop || Math.abs(deltaY) >= minSlop){
					mAsClick = false;
					deltaX = zoom * (event.getX() - lastX);
					deltaY = zoom * (event.getY() - lastY);
					mScene.getViewport().setPosition((int) (mScene.getViewport().getViewportWindow().left - deltaX), (int) (mScene.getViewport().getViewportWindow().top - deltaY));
					invalidate();
				}
				lastX = event.getX();
				lastY = event.getY();
				break;
			case MotionEvent.ACTION_UP:
				if(mAsClick){
					performClick();
				}
		}
	}

	private void scaleLargeImage(MotionEvent event){
		switch(event.getActionMasked()){
			case MotionEvent.ACTION_POINTER_DOWN:
				mAsClick = false;
				oriDistance = getDistance(event);
				lastDistance = oriDistance;
				calculateCenterPoint(event);
				mScreenFocus = new PointF(centerX, centerY);
				break;
			case MotionEvent.ACTION_MOVE:
				float endDistance = getDistance(event);
				if(Math.abs(endDistance - oriDistance) >= minSlop){
					float scale = endDistance / lastDistance;
					mScene.getViewport().zoom(1 / scale, mScreenFocus);
					invalidate();
				}
				lastDistance = endDistance;
				break;
			case MotionEvent.ACTION_POINTER_UP:
				if(event.getPointerCount() == 2){
					if(event.getActionIndex() == 0){
						oriX = event.getX(1);
						oriY = event.getY(1);
					} else if(event.getActionIndex() == 1){
						oriX = event.getX(0);
						oriY = event.getY(0);
					}
					lastX = oriX;
					lastY = oriY;
				}
				break;
		}
	}

	private float getDistance(MotionEvent event){
		float dx = event.getX(1) - event.getX(0);
		float dy = event.getY(1) - event.getY(0);
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	private void calculateCenterPoint(MotionEvent event){
		centerX = (float) ((event.getX(1) + event.getX(0)) / 2.0);
		centerY = (float) ((event.getY(1) + event.getY(0)) / 2.0);
	}


	private void checkIsLargeImage(){
		if(mIsLargeImage){
			return;
		}

		long maxMemory = Runtime.getRuntime().maxMemory();
		if(mOriginalImageheight * mOriginalImageWidth * BYTES_PER_PIXEL >= maxMemory * 0.75){
			mIsLargeImage = true;
			Toast.makeText(PSApplication.getInstance(), "启用大图模式", Toast.LENGTH_SHORT).show();
		} else{
			mIsLargeImage = false;
		}
	}

	private float calcuSmallInitZoom(int imageWidth, int imageHeight){
		float a = ((float) mImageViewWidth) / ((float) imageWidth);
		float b = ((float) mImageViewHeight) / ((float) imageHeight);

		if(a == 1 && b == 1){
			mIsSmallFit = true;
			return 1;
		} else{
			mIsSmallFit = false;
		}

		float result = Math.min(a, b);
		if(result > 1){
			result = 1;
		}
		return result;
	}
}
