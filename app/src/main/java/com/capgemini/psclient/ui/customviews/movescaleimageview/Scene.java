package com.capgemini.psclient.ui.customviews.movescaleimageview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

import com.capgemini.psclient.R;
import com.capgemini.psclient.config.PSApplication;

import java.io.IOException;


/*
 * +-------------------------------------------------------------------+
 * |                                        |                          |
 * |  +------------------------+            |                          |
 * |  |                        |            |                          |
 * |  |                        |            |                          |
 * |  |                        |            |                          |
 * |  |           Viewport     |            |                          |
 * |  +------------------------+            |                          |
 * |                                        |                          |
 * |                                        |                          |
 * |                                        |                          |
 * |                          Cache         |                          |
 * |----------------------------------------+                          |
 * |                                                                   |
 * |                                                                   |
 * |                                                                   |
 * |                                                                   |
 * |                               Scene                               |
 * |                               Entire bitmap -- too big for memory |
 * +-------------------------------------------------------------------+
 */

public class Scene{
	private MoveScaleImageView mView;
	private Viewport mViewport = new Viewport(this);
	private Cache mCache = new Cache(this);
	private Point mSceneSize = new Point();
	private BitmapFactory.Options mOptions;
	private BitmapRegionDecoder mDecoder;
	private int mPercent = 60;
	private int mInSampleSize = 1;
	private Bitmap mSampleBitmap;
	private int mImageViewBGColor = PSApplication.getInstance().getResources().getColor(R.color.imageview_bg_single);


	public Scene(MoveScaleImageView view, byte[] bytes, int offset, int length, int imageWidth, int imageHeight, int imageviewWidth, int imageviewHeight){
		try{
			this.mView = view;
			mOptions = new BitmapFactory.Options();
			mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
			this.mDecoder = BitmapRegionDecoder.newInstance(bytes, offset, length, false);
			mSceneSize.set(imageWidth, imageHeight);
			BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
			mInSampleSize = calculateInSampleSize(imageWidth, imageHeight, imageviewWidth, imageviewHeight);
			tmpOptions.inSampleSize = (mInSampleSize);
			mSampleBitmap = BitmapFactory.decodeByteArray(bytes, offset, length);
			mViewport.setPosition(0, 0);
			mViewport.setViewportSize(imageviewWidth, imageviewHeight);
			mViewport.setOrigZoom(calcuBigInitZoom(imageWidth, imageHeight, imageviewWidth, imageviewHeight));
			mViewport.setZoom(mViewport.getOrigZoom());
			if(mCache.getCacheState() == CacheState.UNINITIALIZED){
				synchronized(mCache){
					mCache.setCacheState(CacheState.INITIALIZED);
				}
			}
			mCache.start();
		} catch(IOException e){
			e.printStackTrace();
		}
	}


	public void draw(Canvas c){
		mViewport.draw(c);
	}

	public void reDrawInOtherThread(){
		mView.postInvalidate();
	}

	public Point getSceneSize(){
		return mSceneSize;
	}

	public Viewport getViewport(){
		return mViewport;
	}

	public Cache getCache(){
		return mCache;
	}

	public int getPercent(){
		return mPercent;
	}

	public void setPercent(int percent){
		mPercent = percent;
	}

	public Bitmap decodeRegionToGetCache(Rect rectOfCache){
		Bitmap bitmap = null;
		if(mDecoder != null)
			bitmap = mDecoder.decodeRegion(rectOfCache, mOptions);
		return bitmap;
	}

	public void fillCacheOutOfMemoryError(OutOfMemoryError error){
		if(mPercent > 3)
			mPercent -= 3;
	}

	public Rect calculateCacheWindow(Rect viewportRect){
		long bytesToUse = Runtime.getRuntime().maxMemory() * mPercent / 100;
		int viewportWidth = viewportRect.width();
		int viewportHeight = viewportRect.height();

		// Calculate the max size of the margins to fit in our memory budget
		int marginWidth = 0;
		int marginHeight = 0;
		while((viewportWidth + marginWidth) * (viewportHeight + marginHeight) * MoveScaleImageView.BYTES_PER_PIXEL / 2 < bytesToUse){ // Config.RGB_565是2byte
			marginWidth++;
			marginHeight++;
		}

		if(viewportWidth + marginWidth > mSceneSize.x)
			marginWidth = Math.max(0, mSceneSize.x - viewportWidth);
		if(viewportHeight + marginHeight > mSceneSize.y)
			marginHeight = Math.max(0, mSceneSize.y - viewportHeight);

		// Figure out the left & right based on the margin. We assume our viewportRect is <= our size. If that's not the case, then this logic breaks.
		int left = viewportRect.left - (marginWidth >> 1);
		int right = viewportRect.right + (marginWidth >> 1);
		if(left < 0){
			right = right - left;
			left = 0;
		}
		if(right > mSceneSize.x){
			left = left - (right - mSceneSize.x);
			right = mSceneSize.x;
		}

		// Figure out the top & bottom based on the margin. We assume our viewportRect is <= our size. If that's not the case, then this logic breaks.
		int top = viewportRect.top - (marginHeight >> 1);
		int bottom = viewportRect.bottom + (marginHeight >> 1);
		if(top < 0){
			bottom = bottom - top;
			top = 0;
		}
		if(bottom > mSceneSize.y){
			top = top - (bottom - mSceneSize.y);
			bottom = mSceneSize.y;
		}

		if(left < 0){
			left = 0;
		}
		if(top < 0){
			top = 0;
		}
		if(right > mSceneSize.x){
			right = mSceneSize.x;
		}
		if(bottom > mSceneSize.y){
			bottom = mSceneSize.y;
		}

		Rect cacheWindowRect = new Rect();
		cacheWindowRect.set(left, top, right, bottom);
		return cacheWindowRect;
	}

	public void drawSampleRectIntoBitmap(Bitmap viewportBitmap, Rect rectOfSample){
		if(viewportBitmap != null){
			Canvas c = new Canvas(viewportBitmap);

			int left = (rectOfSample.left / mInSampleSize);
			int top = (rectOfSample.top / mInSampleSize);
			int right = left + (rectOfSample.width() / mInSampleSize);
			int bottom = top + (rectOfSample.height() / mInSampleSize);

			Rect srcRect = new Rect(left, top, right, bottom);
			Rect identity = new Rect(0, 0, c.getWidth(), c.getHeight());
			c.drawColor(mImageViewBGColor);
			c.drawBitmap(mSampleBitmap, srcRect, identity, null);
		}
	}

	public void recycle(){
		mCache.stop();
		if(mSampleBitmap != null){
			mSampleBitmap.recycle();
		}
		if(mCache.getCacheBitmap() != null){
			mCache.getCacheBitmap().recycle();
		}
		if(mViewport.getViewportBitmap() != null){
			mViewport.getViewportBitmap().recycle();
		}
		if(mDecoder != null){
			mDecoder.recycle();
		}
	}


	private int calculateInSampleSize(int imageWidth, int imageHeight, int reqWidth, int reqHeight){
		int inSampleSize = 1;

		if(imageHeight > reqHeight || imageWidth > reqWidth){
			final int halfHeight = imageHeight / 2;
			final int halfWidth = imageWidth / 2;

			while((halfHeight / inSampleSize) >= reqHeight && (halfHeight / inSampleSize) >= reqWidth && (halfWidth / inSampleSize) >= reqWidth && (halfWidth / inSampleSize) >= reqHeight){
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	private float calcuBigInitZoom(int imageWidth, int imageHeight, int imageViewWidth, int imageViewHeight){
		float a = ((float) imageWidth) / ((float) imageViewWidth);
		float b = ((float) imageHeight) / ((float) imageViewHeight);

		float result = Math.max(a, b);
		if(result < 1){
			result = 1;
		}
		return result;
	}
}