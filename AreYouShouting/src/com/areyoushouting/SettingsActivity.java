/*
 * Copyright 2008 The Android Open Source Project
 * Copyright 2011-2012 Michael Novak <michael.novakjr@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.areyoushouting;

import com.michaelnovakjr.numberpicker.NumberPicker;
import com.michaelnovakjr.numberpicker.NumberPickerDialog;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity implements AreYouShoutingInterface{
	//--- Colors ---
	private final static int N_COLORS = 21;
	private int[] colorsArray;
	
	//--- Views ---
	private RelativeLayout parentLayout;
	private LinearLayout backgroundColorGallery, textIconsColorGallery;
	private TextView title, backgroundColorLabel, textIconsColorLabel, loudVoiceLevelLabel, decibelSensibilityLabel, deleteTempFilesLabel;
	private NumberPickerDialog loudVoiceLevelPickerDialog, decibelSensibilityPickerDialog;
	private Button loudVoiceLevelButton, decibelSensibilityButton;
	private CheckBox deleteTempFilesCheckBox;
	
	//--- Preferences ---	
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
		
		//--- Update UI Colors ---
		updateUIColors();
	}
	
	
	/** Method that creates the layout */
	private void createLayout(){
		//--- Set the layout ---
		setContentView(R.layout.settings);
		
		//--- Views ---
		parentLayout = (RelativeLayout)findViewById(R.id.parent);
		title = (TextView)findViewById(R.id.title);
		backgroundColorLabel = (TextView)findViewById(R.id.backgroundColorLabel);
		backgroundColorGallery = (LinearLayout)findViewById(R.id.backgroundColorGallery);
		textIconsColorLabel = (TextView)findViewById(R.id.textIconsColorLabel);
		textIconsColorGallery = (LinearLayout)findViewById(R.id.textIconsColorGallery);
		loudVoiceLevelLabel = (TextView)findViewById(R.id.loudVoiceLevelLabel);
		loudVoiceLevelButton = (Button)findViewById(R.id.loudVoiceLevelButton);
		decibelSensibilityLabel = (TextView)findViewById(R.id.decibelSensibilityLabel);
		decibelSensibilityButton = (Button)findViewById(R.id.decibelSensibilityButton);
		deleteTempFilesLabel = (TextView)findViewById(R.id.deleteTempFilesLabel);
		deleteTempFilesCheckBox = (CheckBox)findViewById(R.id.deleteTempFilesCheckBox);
		
		//--- FONT ---
    	Typeface font = Typeface.createFromAsset(getAssets(), "Chunkfive.otf");	
    	Typeface font2 = Typeface.createFromAsset (getAssets(), "Roboto-Light.ttf");
		title.setTypeface(font);
		backgroundColorLabel.setTypeface(font2);
		textIconsColorLabel.setTypeface(font2);
		loudVoiceLevelLabel.setTypeface(font2);
		decibelSensibilityLabel.setTypeface(font2);
		deleteTempFilesLabel.setTypeface(font2);

		//--- Colors Array ---
		colorsArray = new int[N_COLORS];
		colorsArray[0] = getResources().getColor(R.color.black);
		colorsArray[1] = getResources().getColor(R.color.midnight);
		colorsArray[2] = getResources().getColor(R.color.turquoise);
		colorsArray[3] = getResources().getColor(R.color.green_sea);
		colorsArray[4] = getResources().getColor(R.color.emerald);
		colorsArray[5] = getResources().getColor(R.color.nephritis);
		colorsArray[6] = getResources().getColor(R.color.peter_river);
		colorsArray[7] = getResources().getColor(R.color.belize_hole);
		colorsArray[8] = getResources().getColor(R.color.amethyst);
		colorsArray[9] = getResources().getColor(R.color.wisteria);
		colorsArray[10] = getResources().getColor(R.color.wet_asphalt);
		colorsArray[11] = getResources().getColor(R.color.midnight_blue);
		colorsArray[12] = getResources().getColor(R.color.sun_flower);
		colorsArray[13] = getResources().getColor(R.color.orange);
		colorsArray[14] = getResources().getColor(R.color.carrot);
		colorsArray[15] = getResources().getColor(R.color.pumpkin);
		colorsArray[16] = getResources().getColor(R.color.alizarin);
		colorsArray[17] = getResources().getColor(R.color.clouds);
		colorsArray[18] = getResources().getColor(R.color.silver);
		colorsArray[19] = getResources().getColor(R.color.concrete);
		colorsArray[20] = getResources().getColor(R.color.asbestos);
				
		//--- Populate Galleries ---
		for (int i=0; i<colorsArray.length; i++){
  			backgroundColorGallery.addView(insertColor(colorsArray[i], BACKGROUND_COLOR));
  			textIconsColorGallery.addView(insertColor(colorsArray[i], TEXT_COLOR));
  		}
		
		//--- Loud Voice Level Picker Dialog ---
		loudVoiceLevelPickerDialog = new NumberPickerDialog(
					this, 
					-1, 
					settings.getInt(LOUD_VOICE_LEVEL, DEFAULT_LOUD_VOICE_LEVEL), 
					getResources().getString(R.string.loud_voice_level),
					getResources().getString(R.string.number_picker_positive_button),
					getResources().getString(R.string.number_picker_negative_button));
		NumberPicker numPicker = (NumberPicker) loudVoiceLevelPickerDialog.getNumberPicker();
		numPicker.setRange(LOUD_VOICE_LEVEL_MIN, LOUD_VOICE_LEVEL_MAX);
		loudVoiceLevelPickerDialog.setOnNumberSetListener(new MyNumberSetListener(LOUD_VOICE_LEVEL));
		
		//--- Loud Voice Level Button ---
		loudVoiceLevelButton.setText(settings.getInt(LOUD_VOICE_LEVEL, DEFAULT_LOUD_VOICE_LEVEL)+" dB");
		loudVoiceLevelButton.setOnTouchListener(new MyOnTouchListener(R.drawable.button, R.drawable.button_pressed));
		loudVoiceLevelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				//--- Show the Dialog ---
				loudVoiceLevelPickerDialog.show();
			}
		});
		
		//--- Decibel Sensibility Picker Dialog ---
		decibelSensibilityPickerDialog = new NumberPickerDialog(
					this, 
					-1, 
					settings.getInt(DECIBEL_SENSIBILITY, DEFAULT_DECIBEL_SENSIBILITY), 
					getResources().getString(R.string.decibel_sensibility),
					getResources().getString(R.string.number_picker_positive_button),
					getResources().getString(R.string.number_picker_negative_button));
		NumberPicker numPicker2 = (NumberPicker) decibelSensibilityPickerDialog.getNumberPicker();
		numPicker2.setRange(DECIBEL_SENSIBILITY_MIN, DECIBEL_SENSIBILITY_MAX);
		decibelSensibilityPickerDialog.setOnNumberSetListener(new MyNumberSetListener(DECIBEL_SENSIBILITY));
		
		//--- Decibel Sensibility Button ---
		decibelSensibilityButton.setText(settings.getInt(DECIBEL_SENSIBILITY, DEFAULT_DECIBEL_SENSIBILITY)+" dB");
		decibelSensibilityButton.setOnTouchListener(new MyOnTouchListener(R.drawable.button, R.drawable.button_pressed));
		decibelSensibilityButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				//--- Show the Dialog ---
				decibelSensibilityPickerDialog.show();
			}
		});
		
		//--- Delete Temporary Files CheckBox ---
		deleteTempFilesCheckBox.setChecked(settings.getBoolean(DELETE_TEMP_FILES, DEFAULT_DELETE_TEMP_FILES));
		deleteTempFilesCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//--- Update the SharedPreferences ---
				SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(DELETE_TEMP_FILES, isChecked);
                editor.commit();
            }
		});
	}
	
	
	/** Method that returns a LinearLayout that contains a solid Flat color.
	 * This is used to populate the Galleries
	 **/
	private View insertColor(final int color, final String type){
		//--- Params ---
		int imageWidth = getResources().getDimensionPixelSize(R.dimen.big_element_size);
		int imageHeight = LayoutParams.MATCH_PARENT;
		int padding = getResources().getDimensionPixelSize(R.dimen.xsmall_margin);
		
		//--- Layout ---
	    LinearLayout layout = new LinearLayout(getApplicationContext());
	    layout.setLayoutParams(new LayoutParams(imageWidth, imageHeight));
	    layout.setGravity(Gravity.CENTER);
	     
	    //--- ImageView (Solid Flat Color) ---
	    ColorDrawable colorDrawable = new ColorDrawable(color);
	    ImageView imageView = new ImageView(getApplicationContext());
	    imageView.setLayoutParams(new LayoutParams(imageWidth-padding, imageHeight)); 
	    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	    imageView.setImageDrawable(colorDrawable);   
	    imageView.setOnClickListener(new View.OnClickListener(){	
		   @Override
		   public void onClick(View v) {
			   //--- Check if user chose the same colors for background and text ---
			   if (((type == BACKGROUND_COLOR) && (settings.getInt(TEXT_COLOR, getResources().getColor(DEFAULT_TEXT_COLOR))!=color))
				  ||
				  ((type == TEXT_COLOR) && (settings.getInt(BACKGROUND_COLOR, getResources().getColor(DEFAULT_BACKGROUND_COLOR))!=color))){
					
				  //--- Two different colors for Background and Text has been chose --- 
				  updateSharedPreferenceColor(type, color);
		   	    
			   } else {
				   Toast.makeText(getBaseContext(), getResources().getString(R.string.error_incompatible_colors), Toast.LENGTH_LONG).show();
			   }
		   }});

	    //--- Add the Solid color to the layout, and return it ---	  
	    layout.addView(imageView);  
	    return layout; 
	}
	
	
	/** Method that edits the SharedPreferences and saves the color that user chose */	
	private void updateSharedPreferenceColor(String type, int color){
	   	   
		//--- Update SP ---
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(type, color);
		editor.commit();
        
		//--- Update UI colors ---
		updateUIColors();  
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
		title.setTextColor(color);
		backgroundColorLabel.setTextColor(color);
		textIconsColorLabel.setTextColor(color);
		loudVoiceLevelLabel.setTextColor(color);
		decibelSensibilityLabel.setTextColor(color);
		deleteTempFilesLabel.setTextColor(color);
	}
	
	
	/** My custom Listener class */
	class MyNumberSetListener implements NumberPickerDialog.OnNumberSetListener {
		private String type;
		
		public MyNumberSetListener(String type){
			this.type = type;
		}
		
		@Override
	    public void onNumberSet(int number) {
			//--- Update Shared Preferences ---
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt(type, number);
				
			//--- Save changes ---
			editor.commit();	
			
			//--- Update Button Text ---
			if (type.matches(LOUD_VOICE_LEVEL)){
				loudVoiceLevelButton.setText(number+" dB");
			} else if (type.matches(DECIBEL_SENSIBILITY)){
				decibelSensibilityButton.setText(number+" dB");
			}
		}
   }

	
   /** Called when a key is pressed */
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event){
	    if (keyCode == KeyEvent.KEYCODE_BACK){
	    	
	    	//--- Finish the Activity ---
	    	finishActivity();

	    	return true;	
        } 
        return false;
     }    
	
   
	/** Method called when Activity has to be finished (set a Transition Animation) */
    public void finishActivity(){
    	finish();
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
    
    
    /** Custom OnTouchListener (changes the look of loudVoiceLevelButton) */
    class MyOnTouchListener implements View.OnTouchListener{
    	private int button1;
    	private int button2;
    	
    	public MyOnTouchListener(int button1, int button2){
    		this.button1 = button1;
    		this.button2 = button2;
    	}
    	
    	public boolean onTouch(View v, MotionEvent event) {
    		int action = event.getAction();
    		if (action == MotionEvent.ACTION_DOWN) 
    			v.setBackgroundResource(button2);
    		else if (action == MotionEvent.ACTION_UP)
    			v.setBackgroundResource(button1);
    		return false;  
    	}
    } 
}