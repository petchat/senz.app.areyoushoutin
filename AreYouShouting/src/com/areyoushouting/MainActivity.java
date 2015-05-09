package com.areyoushouting;

import utils.BooleanUtils;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kiding.areyoushouting.R;
import com.senz.data.collector.AlarmReceiver;
import com.senz.data.collector.SampleService;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends Activity implements AreYouShoutingInterface {
	// --- Title Animation ---
	private final static int ANIMATION_TIMER = 250;
	private Handler animHandler = new Handler();
	private long animDelay;
	private CharSequence animText;
	private int animIndex;

	// --- Views ---
	private RelativeLayout parentLayout;
	private TextView titleView, checkNowView, settingsView;

	// --- SharedPreferences ---
	private SharedPreferences settings;

	// 定时时钟
	public static AlarmManager am = null;
	public static PendingIntent sender = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		Log.i("MainActivity", "==>onCreate()");
		super.onCreate(savedInstanceState);

		// 在原有录音结束后，开启service
		// --- alarm manager by 帅---
		am = (AlarmManager) MainActivity.this
				.getSystemService(Context.ALARM_SERVICE);
		// 程序开启后，检测service是否运行，是，关掉
		if (am != null && sender != null)
			am.cancel(sender);
		if (isServiceRunning("com.senz.data.collector.SampleService")) {
			if (SampleService.recordManager != null) {
				try {
					SampleService.recordManager.stopRecord();
				} catch (Exception ex) {
					Log.i("recorde error", ex.toString());
				}
			}
			Intent intent = new Intent(MainActivity.this,
					com.senz.data.collector.SampleService.class);
			stopService(intent);
		}

		// --- Hide App's Title ---
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// --- Settings ---
		settings = getSharedPreferences(MY_PREFS, Activity.MODE_PRIVATE);

		// --- Create the Layout ---
		createLayout();

		// --- Title Animation ---
		animateText(getResources().getString(R.string.title));

	}

	/** Method that creates the layout */
	private void createLayout() {
		// --- Set the layout ---
		setContentView(R.layout.main);

		// --- Views ---
		parentLayout = (RelativeLayout) findViewById(R.id.parent);
		titleView = (TextView) findViewById(R.id.title);
		checkNowView = (TextView) findViewById(R.id.checkNow);
		settingsView = (TextView) findViewById(R.id.settings);

		// --- FONT ---
		Typeface font = Typeface.createFromAsset(getAssets(), "Chunkfive.otf");
		Typeface font2 = Typeface.createFromAsset(getAssets(), "Quicksand.otf");
		titleView.setTypeface(font);
		checkNowView.setTypeface(font2);
		settingsView.setTypeface(font2);

		// --- Background and Text Colors update ---
		// updateUIColors() called in onResume() method
	}

	/**
	 * Method that updates Background and Text colors (stored in
	 * SharedPreferences) If not set, default colors are those set in
	 * 'AreYouShoutingInterface' interface
	 **/
	private void updateUIColors() {
		// --- Background Color ---
		int color = settings.getInt(BACKGROUND_COLOR,
				getResources().getColor(DEFAULT_BACKGROUND_COLOR));
		parentLayout.setBackgroundColor(color);

		// --- Text Color ---
		color = settings.getInt(TEXT_COLOR,
				getResources().getColor(DEFAULT_TEXT_COLOR));
		titleView.setTextColor(color);
		checkNowView.setTextColor(color);
		settingsView.setTextColor(color);
	}

	/**
	 * Method called after title animation has finished. It shows the Choice
	 * Menu (checkNowView and settingsView)
	 **/
	private void showMenu() {

		// --- OnTouch Events => Change the look of checkNowView and
		// settingsView ---
		MyOnTouchListener customTouchListener = new MyOnTouchListener();
		checkNowView.setOnTouchListener(customTouchListener);
		settingsView.setOnTouchListener(customTouchListener);

		// --- checkNowView OnClickListener ==> Go to RecorderActivity ---
		checkNowView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(MainActivity.this,
						RecorderActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.push_right_in,
						R.anim.push_right_out);
			}
		});

		// --- settingsView OnClickListener ==> Go to SettingsActivity ---
		settingsView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						SettingsActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.push_right_in,
						R.anim.push_right_out);
			}
		});

		// --- Set Alpha Animation ---
		Animation alpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
		checkNowView.startAnimation(alpha);
		settingsView.startAnimation(alpha);

		// --- Set Views as VISIBLE ---
		checkNowView.setVisibility(View.VISIBLE);
		settingsView.setVisibility(View.VISIBLE);
	}

	/** TITLE ANIMATION (it starts when animateText() is called) */
	private Runnable characterAdder = new Runnable() {
		@Override
		public void run() {
			titleView.setText(animText.subSequence(0, animIndex++));
			if (animIndex <= animText.length())
				animHandler.postDelayed(characterAdder, animDelay);
			else
				showMenu();
		}
	};

	public void animateText(CharSequence text) {
		animText = text;
		animIndex = 0;

		titleView.setText("");

		// --- Start the Animation after animDelay time delay ---
		animDelay = ANIMATION_TIMER;
		animHandler.removeCallbacks(characterAdder);
		animHandler.postDelayed(characterAdder, animDelay);
	}

	/** TITLE ANIMATION */

	/** Custom OnTouchListener */
	class MyOnTouchListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			// --- Define the related TextView and Animation ---
			TextView textView = (TextView) v;
			Animation scale = AnimationUtils.loadAnimation(
					getApplicationContext(), R.anim.scale);

			int action = event.getAction();
			if (action == MotionEvent.ACTION_DOWN) {
				textView.startAnimation(scale);
			}

			return false;
		}
	}

	public boolean isServiceRunning(String serviceName) {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceName.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onResume() {
		Log.i("MainActivity", "==>onResume()");
		super.onResume();
		// umeng 统计
		MobclickAgent.onResume(this);
		// --- Update the look of the UI ---
		updateUIColors();

		// --- Cancel previous animations ---
		checkNowView.clearAnimation();
		settingsView.clearAnimation();

	}

	@Override
	protected void onPause() {
		super.onPause();
		//umeng统计
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.i("MainActivity", "==>onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("MainActivity", "==>onDestroy()");

		if (!BooleanUtils.isServiceRunning(
				"com.senz.data.collector.SampleService", this)) {
			Log.i("check service running ", "==>service is not running");

			Intent intent = new Intent(this, AlarmReceiver.class);
			intent.setAction("alarm.action");
			sender = PendingIntent.getBroadcast(this, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			long firstime = SystemClock.elapsedRealtime();
			// am = (AlarmManager) this
			// .getSystemService(Context.ALARM_SERVICE);
			// minutes 一个周期，不停的发送广播
			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime,
					15 * 1000 * 60, sender);

		} else {
			Log.i("check service running ", "==>service is  running");
		}

		super.onDestroy();
	}

	// @Override
	// public void onBackPressed() {
	// new AlertDialog.Builder(this)
	// .setIcon(android.R.drawable.ic_dialog_alert)
	// .setTitle("Close")
	// .setMessage("Are you sure you want to close ?")
	// .setPositiveButton("Yes",
	// new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog,
	// int which) {
	// android.os.Process
	// .killProcess(android.os.Process.myPid());
	// }
	//
	// }).setNegativeButton("No", null).show();
	// }

}
