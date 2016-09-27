package com.capgemini.psclient.ui.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.capgemini.psclient.utils.ScreenUtils;


// 正余弦函数： y = Asin(wx+b)
public class WaveView extends View{
	private Paint mWavePaint;
	private DrawFilter mDrawFilter;
	private Path mPath;
	private int mTopColor = 0x9919416e; // 波纹上部颜色
	private int mBottomColor = 0x990d2d50; // 波纹底部颜色
	private float mAmplitude = 12.25f; // 振幅(dp)
	private int mWave1Speed = 3; // 第一条水波移动速度(dp)
	private int mWave2Speed = 2; // 第二条水波移动速度(dp)
	private float mPhy = 0.0f; // 决定初始相位
	private float mOmega = 0.0f; // 决定周期
	private float mDistanceToTop, mOrigDistance;
	private int mTotalWidth, mTotalHeight;
	private float[] mYPositions;
	private int mWave1Offset, mWave2Offset;
	private boolean mIncrease = true;
	private float mIncreaseDelta = 0.5f;

	public WaveView(Context context){
		super(context);
		init();
	}

	public WaveView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}

	public WaveView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public WaveView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}


	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		canvas.setDrawFilter(mDrawFilter);

		mWave1Offset += mWave1Speed;
		mWave2Offset += mWave2Speed;
		if(mWave1Offset >= mTotalWidth){
			mWave1Offset = mWave1Offset - mTotalWidth;
		}
		if(mWave2Offset >= mTotalWidth){
			mWave2Offset = mWave2Offset - mTotalWidth;
		}

		// 1st wave
		calculateWavePath(mYPositions, mWave1Offset, mIncreaseDelta * 1.8f);
		canvas.drawPath(mPath, mWavePaint);

		// 2nd wave
		calculateWavePath(mYPositions, mWave2Offset, mIncreaseDelta);
		canvas.drawPath(mPath, mWavePaint);

		if(mIncrease){
			mIncreaseDelta = mIncreaseDelta + 0.002f;
			if(mIncreaseDelta >= 2.0f){
				mIncrease = false;
			}
		} else{
			mIncreaseDelta = mIncreaseDelta - 0.002f;
			if(mIncreaseDelta <= 0.1f){
				mIncrease = true;
			}
		}

		invalidate();
	}


	public void setDeltaDistanceToTop(float deltaDistance){
		this.mDistanceToTop = mOrigDistance - deltaDistance;
		updateShader();
	}

	private void init(){
		mWave1Speed = ScreenUtils.dp2px(mWave1Speed);
		mWave2Speed = ScreenUtils.dp2px(mWave2Speed);
		mAmplitude = ScreenUtils.dp2px((int) mAmplitude);

		mPath = new Path();
		mWavePaint = new Paint();
		mWavePaint.setAntiAlias(true);
		mWavePaint.setStyle(Paint.Style.FILL);
		mWavePaint.setColor(mTopColor);
		mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

		mTotalWidth = ScreenUtils.getScreenWidth();
		mTotalHeight = ScreenUtils.getScreenHeight();
		mOmega = (float) (2 * Math.PI / mTotalWidth); // 将周期定为view总宽度
		mOrigDistance = mTotalHeight * 2 / 3;
		mDistanceToTop = mOrigDistance;
		mYPositions = new float[mTotalWidth];
		updateYPosition();
		updateShader();
	}

	private void calculateWavePath(float[] yPositions, int waveOffset, float delta){
		mPath.reset();
		mPath.moveTo(0, mTotalHeight);
		for(int i = 0; i < mTotalWidth; i++){
			if((i + waveOffset) >= mTotalWidth){
				mPath.lineTo(i, yPositions[i + waveOffset - mTotalWidth] * delta + mDistanceToTop);
			} else{
				mPath.lineTo(i, yPositions[i + waveOffset] * delta + mDistanceToTop);
			}
		}
		mPath.lineTo(mTotalWidth, mTotalHeight);
	}

	private void updateYPosition(){
		if(mYPositions != null){
			for(int i = 0; i < mTotalWidth; i++){
				mYPositions[i] = (float) (mAmplitude * Math.sin(mOmega * i + mPhy));
			}
		}
	}

	private void updateShader(){
		float yStart = 0 + mDistanceToTop;
		float yEnd = yStart + mTotalHeight / 4;
		mWavePaint.setShader(new LinearGradient(0, yStart, 0, yEnd, new int[]{mTopColor, mBottomColor}, null, Shader.TileMode.CLAMP));
	}
}
