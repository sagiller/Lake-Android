package com.capgemini.psclient.ui.customviews;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.capgemini.psclient.R;

public class ConfirmDialog extends DialogFragment{
	public static final String CONFIRM_TEXT = "confirm_text";
	public static final String LEFT_TEXT = "left_text";
	public static final String RIGHT_TEXT = "right_text";
	private View.OnClickListener mOnClickListener;
	private DialogClickListener mListener;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		View view = inflater.inflate(R.layout.confirm_dialog, container);
		Button leftBtn = (Button) view.findViewById(R.id.confirm_left);
		Button rightBtn = (Button) view.findViewById(R.id.confirm_right);
		if(mListener != null){
			initOnClickListener();
			leftBtn.setOnClickListener(mOnClickListener);
			rightBtn.setOnClickListener(mOnClickListener);
		}
		Bundle bundle = getArguments();
		if(bundle != null){
			String text = bundle.getString(CONFIRM_TEXT);
			String leftText = bundle.getString(LEFT_TEXT);
			String rightText = bundle.getString(RIGHT_TEXT);
			if(text != null){
				((TextView) view.findViewById(R.id.confirm_text)).setText(text);
			}
			if(leftText != null){
				leftBtn.setText(leftText);
			}
			if(rightText != null){
				rightBtn.setText(rightText);
			}
		}
		return view;
	}


	public void setDialogClickListener(DialogClickListener listener){
		this.mListener = listener;
	}


	private void initOnClickListener(){
		if(mOnClickListener == null){
			mOnClickListener = new View.OnClickListener(){
				@Override
				public void onClick(View v){
					switch(v.getId()){
						case R.id.confirm_left:
							mListener.onLeftClicked();
							break;
						case R.id.confirm_right:
							mListener.onRightClicked();
							break;
					}
				}
			};
		}
	}


	public interface DialogClickListener{
		public void onRightClicked();

		public void onLeftClicked();
	}
}
