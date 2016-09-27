package com.capgemini.psclient.ui.customviews.movescaleimageview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.capgemini.psclient.config.EnvironmentConfig;


public class Cache{
	private CacheState mCacheState = CacheState.UNINITIALIZED;
	private Scene mScene;
	private Bitmap mCacheBitmap = null;
	private CacheThread mCacheThread;
	private final Rect mCacheWindow = new Rect(0, 0, 0, 0);
	private final Rect mSrcRect = new Rect(0, 0, 0, 0);
	private final Rect mDstRect = new Rect(0, 0, 0, 0);
	private final Point mDstSize = new Point();


	public Cache(Scene scene){
		this.mScene = scene;
	}


	public void start(){
		if(mCacheThread != null){
			mCacheThread.setRunning(false);
			mCacheThread.interrupt();
			mCacheThread = null;
		}
		mCacheThread = new CacheThread(this);
		mCacheThread.setName("cacheThread");
		mCacheThread.start();
	}

	public void stop(){
		mCacheThread.running = false;
		mCacheThread.interrupt();

		boolean retry = true;
		while(retry){
			try{
				mCacheThread.join();
				retry = false;
			} catch(InterruptedException e){
			}
		}
		mCacheThread = null;
	}

	/**
	 * Fill the bitmap with the part of the scene referenced by the viewport Rect
	 */
	public void updateInnerViewport(Viewport viewport){
		Bitmap bitmap = null; // If this is null at the bottom, then load from the sample
		synchronized(this){
			switch(getCacheState()){
				case UNINITIALIZED: // nothing can be done -- should never get here
					return;
				case INITIALIZED: // time to cache some data
					setCacheState(CacheState.START_UPDATE);
					mCacheThread.interrupt();
					break;
				case START_UPDATE: // already told the thread to start
					break;
				case IN_UPDATE: // Already reading some data, just use the sample
					break;
				case SUSPEND: // Loading from cache suspended.
					break;
				case READY: // have some data to show
					if(mCacheBitmap == null){ // Start the cache off right
						setCacheState(CacheState.START_UPDATE);
						mCacheThread.interrupt();
					} else if(!mCacheWindow.contains(viewport.getViewportWindow())){
						setCacheState(CacheState.START_UPDATE);
						mCacheThread.interrupt();
					} else{ // Happy case -- the cache already contains the Viewport
						bitmap = mCacheBitmap;
					}
					break;
			}
		}
		if(bitmap == null)
			loadSampleIntoViewport();
		else
			loadBitmapIntoViewport(bitmap);
	}

	public void loadBitmapIntoViewport(Bitmap cacheBitmap){
		Log.i(EnvironmentConfig.LOG_TAG, "with cache");
		if(cacheBitmap != null){
			synchronized(mScene.getViewport()){
				int left = mScene.getViewport().getViewportWindow().left - mCacheWindow.left;
				int top = mScene.getViewport().getViewportWindow().top - mCacheWindow.top;
				int right = left + mScene.getViewport().getViewportWindow().width();
				int bottom = top + mScene.getViewport().getViewportWindow().height();
				mSrcRect.set(left, top, right, bottom);

				mScene.getViewport().getViewportBitmapSize(mDstSize);
				mDstRect.set(0, 0, mDstSize.x, mDstSize.y);

				Canvas c = new Canvas(mScene.getViewport().getViewportBitmap());
				c.drawBitmap(cacheBitmap, mSrcRect, mDstRect, null);
			}
		}
	}

	public void loadSampleIntoViewport(){
		Log.i(EnvironmentConfig.LOG_TAG, "no cache");
		synchronized(mScene.getViewport()){
			mScene.drawSampleRectIntoBitmap(mScene.getViewport().getViewportBitmap(), mScene.getViewport().getViewportWindow());
		}
	}

	public void setCacheState(CacheState newState){
		mCacheState = newState;
	}

	public CacheState getCacheState(){
		return mCacheState;
	}

	public Bitmap getCacheBitmap(){
		return mCacheBitmap;
	}


	/**
	 * <p>The CacheThread's job is to wait until the {@link Cache#getCacheState} is {@link CacheState#START_UPDATE} and then update the {@link Cache} given the current {@link Viewport#getViewportWindow}.
	 * It does not want to hold the cache lock during the call to {@link Scene#decodeRegionToGetCache(Rect)} because the call can take a long time. If we hold the lock, the user experience is very jumpy.</p>
	 * <p>The CacheThread and the {@link Cache} work hand in hand, both using the cache itself to synchronize on and using the {@link Cache#getCacheState}.
	 * The {@link Cache} is free to update any part of the cache object as long as it holds the lock.
	 * The CacheThread is careful to make sure that it is the {@link Cache#getCacheState} is {@link CacheState#IN_UPDATE} as it updates the {@link Cache}.
	 * It locks and unlocks the cache all along the way, but makes sure that the cache is not locked when it calls {@link Scene#decodeRegionToGetCache(Rect)}.
	 */
	private class CacheThread extends Thread{
		private final Cache cache;
		private boolean running = false;


		CacheThread(Cache cache){
			this.cache = cache;
		}


		@Override
		public void run(){
			running = true;
			while(running){
				while(running && cache.getCacheState() != CacheState.START_UPDATE){
					try{
						Thread.sleep(Integer.MAX_VALUE);
					} catch(InterruptedException ignored){
					}
				}
				if(!running){
					return;
				}
				boolean cont = false;
				synchronized(cache){
					if(cache.getCacheState() == CacheState.START_UPDATE){
						cache.setCacheState(CacheState.IN_UPDATE);
						if(cache.mCacheBitmap != null){
							cache.mCacheBitmap.recycle();
							cache.mCacheBitmap = null;
						}
						cont = true;
					}
				}
				if(cont){
					synchronized(cache){
						if(cache.getCacheState() == CacheState.IN_UPDATE)
							cache.mCacheWindow.set(mScene.calculateCacheWindow(mScene.getViewport().getViewportWindow()));
						else
							cont = false;
					}
					if(cont){
						try{
							Bitmap bitmap = mScene.decodeRegionToGetCache(cache.mCacheWindow);
							if(bitmap != null){
								synchronized(cache){
									if(cache.getCacheState() == CacheState.IN_UPDATE){
										cache.mCacheBitmap = bitmap;
										cache.setCacheState(CacheState.READY);
										if(mScene.getPercent() <= 60 - 6){
											mScene.setPercent(mScene.getPercent() + 6);
										}
										mScene.reDrawInOtherThread();
									}
								}
							}
						} catch(OutOfMemoryError e){
							synchronized(cache){
								mScene.fillCacheOutOfMemoryError(e);
								if(mScene.getPercent() <= 3){
									if(cache.getCacheState() == CacheState.IN_UPDATE){
										cache.setCacheState(CacheState.READY);
									}
								} else{
									if(cache.getCacheState() == CacheState.IN_UPDATE){
										cache.setCacheState(CacheState.START_UPDATE);
									}
								}
							}
						}
					}
				}
			}
		}


		public void setRunning(boolean value){
			running = value;
		}
	}
}
