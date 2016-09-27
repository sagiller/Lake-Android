package com.capgemini.psclient.ui.customviews.movescaleimageview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;


public class Viewport{
	private Scene mScene;
	private float mZoom = 1.0f, mOrigZoom = 1.0f; // 小于1时，表示图片被放大； 大于1时，表示图片被缩小。（和小图的情况相反）
	private Bitmap mViewportBitmap = null;
	private Rect mViewportWindow = new Rect(0, 0, 0, 0);


	public Viewport(Scene scene){
		this.mScene = scene;
	}


	public void draw(Canvas c){
		mScene.getCache().updateInnerViewport(this);
		synchronized(this){
			if(c != null && mViewportBitmap != null){
				c.drawBitmap(mViewportBitmap, 0F, 0F, null);
			}
		}
	}

	public void zoom(float factor, PointF screenFocus){
		if(factor != 1.0){
			if(mZoom * factor < (1.0 / 3.0) || mZoom * factor > mOrigZoom){
				return;
			}

			synchronized(this){
				float newZoom = mZoom * factor;
				RectF newRect = new RectF();
				PointF focusInScene = new PointF(mViewportWindow.left + (screenFocus.x / getViewportBitmapWidth()) * mViewportWindow.width(), mViewportWindow.top + (screenFocus.y / getViewportBitmapHeight()) * mViewportWindow.height());
				float newRectWidth = getViewportBitmapWidth() * newZoom;
				float newRectHeight = getViewportBitmapHeight() * newZoom;
				newRect.left = focusInScene.x - ((screenFocus.x / getViewportBitmapWidth()) * newRectWidth);
				newRect.top = focusInScene.y - ((screenFocus.y / getViewportBitmapHeight()) * newRectHeight);
				newRect.right = newRect.left + newRectWidth;
				newRect.bottom = newRect.top + newRectHeight;
				mViewportWindow.set((int) newRect.left, (int) newRect.top, (int) newRect.right, (int) newRect.bottom);
				mZoom = newZoom;
			}
		}
	}

	public void setPosition(int x, int y){
		synchronized(this){
			int w = mViewportWindow.width();
			int h = mViewportWindow.height();

			mViewportWindow.set(x, y, x + w, y + h);
		}
	}

	public void setViewportSize(int w, int h){
		synchronized(this){
			if(mViewportBitmap != null){
				mViewportBitmap.recycle();
				mViewportBitmap = null;
			}
			mViewportBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
			mViewportWindow.set(mViewportWindow.left, mViewportWindow.top, mViewportWindow.left + w, mViewportWindow.top + h);
		}
	}

	public void getViewportBitmapSize(Point p){
		synchronized(this){
			p.x = getViewportBitmapWidth();
			p.y = getViewportBitmapHeight();
		}
	}

	public int getViewportBitmapWidth(){
		return mViewportBitmap.getWidth();
	}

	public int getViewportBitmapHeight(){
		return mViewportBitmap.getHeight();
	}

	public float getZoom(){
		return mZoom;
	}

	public void setZoom(float newZoom){
		zoom(newZoom, new PointF(0, 0));
		float x = (mScene.getSceneSize().x - mViewportBitmap.getWidth() * mZoom) / 2;
		float y = (mScene.getSceneSize().y - mViewportBitmap.getHeight() * mZoom) / 2;
		setPosition((int) x, (int) y);
	}

	public void setOrigZoom(float newOrigZoom){
		mOrigZoom = newOrigZoom;
	}

	public float getOrigZoom(){
		return mOrigZoom;
	}

	public Rect getViewportWindow(){
		return mViewportWindow;
	}

	public Bitmap getViewportBitmap(){
		return mViewportBitmap;
	}
}