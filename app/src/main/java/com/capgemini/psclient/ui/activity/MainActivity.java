package com.capgemini.psclient.ui.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.capgemini.psclient.R;
import com.capgemini.psclient.config.PSApplication;
import com.capgemini.psclient.model.PSService;
import com.capgemini.psclient.socketconnect.MainThreadHandler;
import com.capgemini.psclient.socketconnect.PSConstant;
import com.capgemini.psclient.socketconnect.SocketConnectionThread;
import com.capgemini.psclient.sqlite.RecordServerDB;
import com.capgemini.psclient.ui.adapter.ServerDisplayListViewAdapter;
import com.capgemini.psclient.ui.customviews.ListViewInScrollView;
import com.capgemini.psclient.ui.customviews.ObservableScrollView;
import com.capgemini.psclient.ui.customviews.WaveView;
import com.capgemini.psclient.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements View.OnClickListener{
	public static final String NEED_MAIN = "need_display_main";
	private static final String SERVICE_TYPE = "_photoshopserver._tcp.";
	private static final int SCROLL_TIME = 500;
	private MainThreadHandler mHandler;
	private MainThreadHandler.OnConnectListener mOnConnectListener;
	private MainThreadHandler.GetDocListener mGetDocListener;
	private MainThreadHandler.OnErrorListener mOnErrorListener;
	private NsdManager mNsdManager;
	private NsdManager.DiscoveryListener mDiscoveryListener;
	private List<PSService> mPSServerDisplayList;
	private ObservableScrollView mRootView;
	private View mNewView, mJoinView;
	private TextView mJoinServerName;
	private EditText mNewIp, mNewPassword, mJoinPassword;
	private ListViewInScrollView mServerDisplayListView;
	private WaveView mWaveView;
	private String mTempName, mTempIp, mTempPassword;
	private long mLastManiTime = 0;


	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		mHandler = MainThreadHandler.getMainHandler();

		if(needGuidePage()){
			Intent i = new Intent(this, GuideActivity.class);
			startActivity(i);
		}
	}

	@Override
	protected void onStart(){
		super.onStart();
		startSearch();
		setCallbackListeners();
		if(getIntent().getBooleanExtra(NEED_MAIN, false)){
			mRootView.setVisibility(View.VISIBLE);
			mJoinView.setVisibility(View.GONE);
			mNewView.setVisibility(View.GONE);
			mTempIp = null;
			mTempPassword = null;
			mTempName = null;
		}
	}

	@Override
	protected void onStop(){
		stopSearch();
		super.onStop();
	}

	@Override
	protected void onDestroy(){
		if(SocketConnectionThread.mIsThreadRunning){
			mHandler.stopSocketThread();
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v){
		switch(v.getId()){
			case R.id.main_btn_setting:
				break;
			case R.id.main_btn_add_new:
				gotoNewView();
				break;
			case R.id.main_new_back:
				backtoMainView();
				break;
			case R.id.main_new_btn_connect:
				if(mNewIp.getText().toString().isEmpty()){
					Toast.makeText(this, PSApplication.getInstance().getText(R.string.please_input_ip), Toast.LENGTH_SHORT).show();
					return;
				}
				if(mNewPassword.getText().toString().isEmpty()){
					Toast.makeText(this, PSApplication.getInstance().getText(R.string.please_input_password), Toast.LENGTH_SHORT).show();
					return;
				}
				mTempIp = mNewIp.getText().toString();
				mTempPassword = mNewPassword.getText().toString();
				mHandler.stopSocketThread();
				mHandler.startSocketThread(mTempIp, mTempPassword);
				break;
			case R.id.main_join_back:
				backtoMainView();
				break;
			case R.id.main_join_btn_connect:
				if(mJoinPassword.getText().toString().isEmpty()){
					Toast.makeText(this, PSApplication.getInstance().getText(R.string.please_input_password), Toast.LENGTH_SHORT).show();
					return;
				}
				mTempPassword = mJoinPassword.getText().toString();
				mHandler.stopSocketThread();
				mHandler.startSocketThread(mTempIp, mTempPassword);
				break;
		}
	}

	@Override
	public void onBackPressed(){
		if(mRootView.getVisibility() != View.VISIBLE){
			backtoMainView();
		} else{
			super.onBackPressed();
		}
	}


	private void initViews(){
		mRootView = (ObservableScrollView) findViewById(R.id.main_root);
		mNewView = findViewById(R.id.main_new);
		mJoinView = findViewById(R.id.main_join);
		mNewView.setVisibility(View.GONE);
		mJoinView.setVisibility(View.GONE);
		mWaveView = (WaveView) findViewById(R.id.main_waveview);

		mRootView.findViewById(R.id.main_btn_setting).setOnClickListener(this);
		mRootView.findViewById(R.id.main_btn_add_new).setOnClickListener(this);
		Typeface fontFace = Typeface.createFromAsset(getAssets(), "fonts/centurygothic.ttf");
		((TextView) mRootView.findViewById(R.id.SE)).setTypeface(fontFace);
		((TextView) mRootView.findViewById(R.id.LECT)).setTypeface(fontFace);
		((TextView) mRootView.findViewById(R.id.A)).setTypeface(fontFace);
		((TextView) mRootView.findViewById(R.id.CONNEC)).setTypeface(fontFace);
		((TextView) mRootView.findViewById(R.id.T)).setTypeface(fontFace);
		((TextView) mRootView.findViewById(R.id.ION)).setTypeface(fontFace);

		mNewView.findViewById(R.id.main_new_back).setOnClickListener(this);
		mNewIp = (EditText) mNewView.findViewById(R.id.main_new_et_ip);
		mNewPassword = (EditText) mNewView.findViewById(R.id.main_new_et_password);
		mNewView.findViewById(R.id.main_new_btn_connect).setOnClickListener(this);
		((TextView) mNewView.findViewById(R.id.main_new_tv_new)).setTypeface(fontFace);

		mJoinView.findViewById(R.id.main_join_back).setOnClickListener(this);
		mJoinServerName = (TextView) mJoinView.findViewById(R.id.main_new_tv_name);
		mJoinPassword = (EditText) mJoinView.findViewById(R.id.main_join_et_password);
		mJoinView.findViewById(R.id.main_join_btn_connect).setOnClickListener(this);
		((TextView) mJoinView.findViewById(R.id.main_new_tv_join)).setTypeface(fontFace);

		mServerDisplayListView = (ListViewInScrollView) mRootView.findViewById(R.id.main_listview);
		mPSServerDisplayList = new ArrayList<PSService>();
		ServerDisplayListViewAdapter adapter = new ServerDisplayListViewAdapter(this, android.R.layout.simple_list_item_1, mPSServerDisplayList);
		mServerDisplayListView.setAdapter(adapter);
		mServerDisplayListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				PSService tempServer = ((ServerDisplayListViewAdapter) mServerDisplayListView.getAdapter()).getItem(position);
				if(tempServer != null){
					if(tempServer.getPassword() == null || tempServer.getPassword().length() <= 1){
						gotoJoinView();
						mTempIp = tempServer.getIP();
						mTempName = tempServer.getName();
						mJoinServerName.setText(tempServer.getName());
					} else{
						mTempIp = tempServer.getIP();
						mTempName = tempServer.getName();
						mHandler.startSocketThread(tempServer.getIP(), tempServer.getPassword());
					}
				}
			}
		});

		mRootView.setOnCustomScrollListener(new ObservableScrollView.OnCustomScrollListener(){
			@Override
			public void onScrollChanged(ObservableScrollView scrollView, int scrollX, int scrollY, int oldScrollX, int oldScrollY){
				mWaveView.setDeltaDistanceToTop(scrollY);
			}
		});

		mRootView.post(new Runnable(){
			@Override
			public void run(){
				Handler mainHanler = new Handler(Looper.getMainLooper());
				mainHanler.post(new Runnable(){
					@Override
					public void run(){
						mRootView.scrollBy(0, -10000);
					}
				});
			}
		});
	}

	private void setCallbackListeners(){
		if(mOnConnectListener == null){
			mOnConnectListener = new MainThreadHandler.OnConnectListener(){
				@Override
				public void onSocketConnected(){
					mHandler.getActiveDocID();
				}

				@Override
				public void onDisconnect(){
				}
			};
		}
		if(mGetDocListener == null){
			mGetDocListener = new MainThreadHandler.GetDocListener(){
				@Override
				public void onGetDocId(String documentId){
					if(mTempPassword != null){
						PSService tempService = new PSService(mTempName, mTempIp, mTempPassword);
						writeToDatabase(tempService);
					}

					Intent i = new Intent(MainActivity.this, DisplayImageActivity.class);
					i.putExtra(DisplayImageActivity.DOC_ID, documentId);
					startActivity(i);
				}

				@Override
				public void onGetDocImage(byte[] inBytes, int inIndexer){
				}

				@Override
				public void onEventChanged(){
				}
			};
		}
		if(mOnErrorListener == null){
			mOnErrorListener = new MainThreadHandler.OnErrorListener(){
				@Override
				public void onError(int errorIndex){
					long currentTime = TimeUtils.getCurrentTimeMillis();
					switch(errorIndex){
						case PSConstant.ERROR_INVALIDALGORITHMPARAM:
						case PSConstant.ERROR_NOSUCHALGORITHM:
						case PSConstant.ERROR_NOSUCHPADDING:
						case PSConstant.ERROR_INVALIDKEY:
							break;
						case PSConstant.ERROR_TIMEOUT:
						case PSConstant.ERROR_UNKNOW_HOST:
							if(currentTime - mLastManiTime > 5000){
								Toast.makeText(PSApplication.getInstance(), PSApplication.getInstance().getString(R.string.please_verify_ip), Toast.LENGTH_SHORT).show();
							}
							break;
						case PSConstant.ERROR_IOEXCEPTION:
							if(currentTime - mLastManiTime > 5000){
								if(mRootView.getVisibility() == View.VISIBLE){
									gotoJoinView();
									if(mTempName != null){
										mJoinServerName.setText(mTempName);
									} else{
										mJoinServerName.setText(mTempIp);
									}
								} else{
									Toast.makeText(PSApplication.getInstance(), PSApplication.getInstance().getString(R.string.please_verify_ip_password), Toast.LENGTH_SHORT).show();
								}
							}
							break;
					}
					mHandler.stopSocketThread();
					mLastManiTime = currentTime;
				}
			};
		}

		mHandler.setOnErrorListener(mOnErrorListener);
		mHandler.setGetDocListener(mGetDocListener);
		mHandler.setOnConnectListener(mOnConnectListener);
	}

	private void startSearch(){
		if(mNsdManager == null){
			mNsdManager = (NsdManager) PSApplication.getInstance().getSystemService(NSD_SERVICE);
		}
		mPSServerDisplayList.clear();
		readFromDatabase();
		((ServerDisplayListViewAdapter) mServerDisplayListView.getAdapter()).notifyDataSetChanged();
		if(mDiscoveryListener == null){
			mDiscoveryListener = new NsdManager.DiscoveryListener(){
				@Override
				public void onStartDiscoveryFailed(String s, int i){
				}

				@Override
				public void onStopDiscoveryFailed(String s, int i){
				}

				@Override
				public void onDiscoveryStarted(String s){
				}

				@Override
				public void onDiscoveryStopped(String s){
				}

				@Override
				public void onServiceFound(NsdServiceInfo nsdServiceInfo){
					mNsdManager.resolveService(nsdServiceInfo, new NsdManager.ResolveListener(){
						@Override
						public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i){
						}

						@Override
						public void onServiceResolved(NsdServiceInfo nsdServiceInfo){
							PSService newPSService = new PSService(nsdServiceInfo.getServiceName(), nsdServiceInfo.getHost().getHostAddress(), null);
							if(!mPSServerDisplayList.contains(newPSService)){
								mPSServerDisplayList.add(newPSService);
								runOnUiThread(new Runnable(){
									@Override
									public void run(){
										((ServerDisplayListViewAdapter) mServerDisplayListView.getAdapter()).notifyDataSetChanged();
									}
								});
							}
						}
					});
				}

				@Override
				public void onServiceLost(NsdServiceInfo nsdServiceInfo){
				}
			};
		}

		mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
	}

	private void stopSearch(){
		if(mDiscoveryListener != null){
			mNsdManager.stopServiceDiscovery(mDiscoveryListener);
		}
	}

	private void gotoNewView(){
		mNewIp.setText("");
		mNewPassword.setText("");
		if(mRootView.getScrollY() == 0){
			startNewViewAnimation();
		} else{
			ObjectAnimator animator = ObjectAnimator.ofInt(mRootView, "scrollY", 0);
			animator.addListener(new Animator.AnimatorListener(){
				@Override
				public void onAnimationStart(Animator animation){
				}

				@Override
				public void onAnimationEnd(Animator animation){
					startNewViewAnimation();
				}

				@Override
				public void onAnimationCancel(Animator animation){
				}

				@Override
				public void onAnimationRepeat(Animator animation){
				}
			});
			animator.setDuration(SCROLL_TIME).start();
		}
	}

	private void gotoJoinView(){
		mJoinPassword.setText("");
		if(mRootView.getScrollY() == 0){
			startJoinViewAnimation();
		} else{
			ObjectAnimator animator = ObjectAnimator.ofInt(mRootView, "scrollY", 0);
			animator.addListener(new Animator.AnimatorListener(){
				@Override
				public void onAnimationStart(Animator animation){
				}

				@Override
				public void onAnimationEnd(Animator animation){
					startJoinViewAnimation();
				}

				@Override
				public void onAnimationCancel(Animator animation){
				}

				@Override
				public void onAnimationRepeat(Animator animation){
				}
			});
			animator.setDuration(SCROLL_TIME).start();
		}
	}

	private void backtoMainView(){
		mTempIp = null;
		mTempPassword = null;
		mTempName = null;
		mRootView.setVisibility(View.VISIBLE);
		Animation animation0 = AnimationUtils.loadAnimation(this, R.anim.anim_right_in);
		animation0.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation){
			}

			@Override
			public void onAnimationEnd(Animation animation){
				mRootView.scrollBy(0, -10000);
			}

			@Override
			public void onAnimationRepeat(Animation animation){
			}
		});
		mRootView.startAnimation(animation0);

		if(mNewView.getVisibility() == View.VISIBLE){
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_right_out);
			animation.setAnimationListener(new Animation.AnimationListener(){
				@Override
				public void onAnimationStart(Animation animation){
				}

				@Override
				public void onAnimationEnd(Animation animation){
					mNewView.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation){
				}
			});
			mNewView.startAnimation(animation);
		}
		if(mJoinView.getVisibility() == View.VISIBLE){
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_right_out);
			animation.setAnimationListener(new Animation.AnimationListener(){
				@Override
				public void onAnimationStart(Animation animation){
				}

				@Override
				public void onAnimationEnd(Animation animation){
					mJoinView.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation){
				}
			});
			mJoinView.startAnimation(animation);
		}
	}

	private void startNewViewAnimation(){
		mJoinView.setVisibility(View.GONE);
		mNewView.setVisibility(View.VISIBLE);
		mNewView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_left_in));
		Animation animation2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_left_out);
		animation2.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation){
			}

			@Override
			public void onAnimationEnd(Animation animation){
				mRootView.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation){
			}
		});
		mRootView.startAnimation(animation2);
	}

	private void startJoinViewAnimation(){
		mNewView.setVisibility(View.GONE);
		mJoinView.setVisibility(View.VISIBLE);
		mJoinView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_left_in));
		Animation animation2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_left_out);
		animation2.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation){
			}

			@Override
			public void onAnimationEnd(Animation animation){
				mRootView.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation){
			}
		});
		mRootView.startAnimation(animation2);
	}

	private boolean needGuidePage(){
		SharedPreferences sp = getSharedPreferences("needguidepage", Context.MODE_PRIVATE);
		boolean result = sp.getBoolean("needguidepage", true);

		SharedPreferences.Editor e = sp.edit();
		e.putBoolean("needguidepage", false);
		e.commit();
		return result;
	}

	private void readFromDatabase(){
		RecordServerDB db = new RecordServerDB(this);
		SQLiteDatabase dbReader = db.getReadableDatabase();
		Cursor c = dbReader.query("server", null, null, null, null, null, null);
		while(c.moveToNext()){
			String name = c.getString(c.getColumnIndex("name"));
			String ip = c.getString(c.getColumnIndex("ip"));
			String password = c.getString(c.getColumnIndex("password"));
			mPSServerDisplayList.add(new PSService(name, ip, password));
		}
		c.close();
		dbReader.close();
		db.close();
	}

	private void writeToDatabase(PSService service){
		RecordServerDB db = new RecordServerDB(this);
		SQLiteDatabase dbWriter = db.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("name", service.getName());
		cv.put("ip", service.getIP());
		cv.put("password", service.getPassword());
		if(dbWriter.update("server", cv, "ip=?", new String[]{service.getIP()}) == 0){
			dbWriter.insert("server", null, cv);
		}
		dbWriter.close();
		db.close();
	}

	private void deleteFromDatabase(PSService service){
		RecordServerDB db = new RecordServerDB(this);
		SQLiteDatabase dbWriter = db.getWritableDatabase();
		dbWriter.delete("server", "ip=?", new String[]{service.getIP()});
		dbWriter.close();
		db.close();
	}

	private String getNetStatus(){
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if(networkInfo != null){
			return networkInfo.getTypeName() + ": " + networkInfo.getExtraInfo();
		}
		return "No Network";
	}
}