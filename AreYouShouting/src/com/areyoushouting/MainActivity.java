package com.areyoushouting;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements AreYouShoutingInterface{
	//--- Title Animation ---
	private final static int ANIMATION_TIMER = 150; 
	private Handler animHandler = new Handler();
	private long animDelay;
	private CharSequence animText;
	private int animIndex;
	
	//--- Views ---
	private RelativeLayout parentLayout;
	private TextView titleView, checkNowView, settingsView;

	//--- SharedPreferences ---	
	private SharedPreferences settings;
	
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//--- Hide App's Title ---
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //--- Settings ---
        settings = getSharedPreferences(MY_PREFS, Activity.MODE_PRIVATE);
		
        //--- Create the Layout ---
		createLayout();
		
		//--- Title Animation ---
	    animateText(getResources().getString(R.string.title));
	}
	
	
	/** Method that creates the layout */
	private void createLayout(){
		//--- Set the layout ---
		setContentView(R.layout.main);
		
		//--- Views ---
		parentLayout = (RelativeLayout)findViewById(R.id.parent);
		titleView = (TextView)findViewById(R.id.title);
		checkNowView = (TextView)findViewById(R.id.checkNow);
		settingsView = (TextView)findViewById(R.id.settings);
		
		//--- FONT ---
    	Typeface font = Typeface.createFromAsset (getAssets(), "Chunkfive.otf");
    	Typeface font2 = Typeface.createFromAsset (getAssets(), "Quicksand.otf"); 
        titleView.setTypeface(font);
        checkNowView.setTypeface(font2);
        settingsView.setTypeface(font2);
        
        //--- Background and Text Colors update ---
        //updateUIColors() called in onResume() method
	}
	
	
	/** Method that updates Background and Text colors (stored in SharedPreferences) 
	 * If not set, default colors are those set in 'AreYouShoutingInterface' interface
	 **/
	private void updateUIColors(){
		//--- Background Color ---
		int color = settings.getInt(BACKGROUND_COLOR, getResources().getColor(DEFAULT_BACKGROUND_COLOR));
        parentLayout.setBackgroundColor(color); 
		
        //--- Text Color ---
		color = settings.getInt(TEXT_COLOR, getResources().getColor(DEFAULT_TEXT_COLOR));	
		titleView.setTextColor(color);
		checkNowView.setTextColor(color);
		settingsView.setTextColor(color);
	}
	

	/** Method called after title animation has finished.
	 * It shows the Choice Menu (checkNowView and settingsView) 
	 **/
	private void showMenu(){
		
		//--- OnTouch Events => Change the look of checkNowView and settingsView ---
		MyOnTouchListener customTouchListener = new MyOnTouchListener();
		checkNowView.setOnTouchListener(customTouchListener);
		settingsView.setOnTouchListener(customTouchListener);

		//--- checkNowView OnClickListener ==> Go to RecorderActivity ---
		checkNowView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {			
				Intent intent = new Intent(MainActivity.this, RecorderActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}
		});
		
		//--- settingsView OnClickListener ==> Go to SettingsActivity ---
		settingsView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}
		});
		
		//--- Set Alpha Animation ---
        Animation alpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        checkNowView.startAnimation(alpha); 
        settingsView.startAnimation(alpha);
        
        //--- Set Views as VISIBLE ---
		checkNowView.setVisibility(View.VISIBLE);
		settingsView.setVisibility(View.VISIBLE);
	}
	
	
	/** TITLE ANIMATION (it starts when animateText() is called) */
		private Runnable characterAdder = new Runnable() {
			@Override
			public void run() {
			    titleView.setText(animText.subSequence(0, animIndex++));
			    if(animIndex <= animText.length())
			        animHandler.postDelayed(characterAdder, animDelay);
			    else
			    	showMenu();
			}
		};
		
		public void animateText(CharSequence text) {
			animText = text;
			animIndex = 0;
	
			titleView.setText("");
			
			//--- Start the Animation after animDelay time delay ---
			animDelay = ANIMATION_TIMER;
			animHandler.removeCallbacks(characterAdder);
			animHandler.postDelayed(characterAdder, animDelay);
		}
	/** TITLE ANIMATION */
		
	
	/** Custom OnTouchListener */
	class MyOnTouchListener implements View.OnTouchListener{		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			//--- Define the related TextView and Animation ---
			TextView textView = (TextView) v;
			Animation scale = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale);
			
			int action = event.getAction();
			if (action == MotionEvent.ACTION_DOWN){
		        textView.startAnimation(scale);
			}
			
			return false;  				
		}
	}
	
	
	@Override
	public void onResume(){
		super.onResume();
		
		//--- Update the look of the UI ---
		updateUIColors();
		
		//--- Cancel previous animations ---
		checkNowView.clearAnimation();
		settingsView.clearAnimation();
	}
}