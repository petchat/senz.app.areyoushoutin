package com.areyoushouting;

import com.kiding.areyoushouting.R;

/** Interface that contains settings names and values, used in different Activities */
public interface AreYouShoutingInterface {
	
	//--- SharedPreferences settings names ---
	public static final String MY_PREFS = "my_prefs";
	public static final String BACKGROUND_COLOR = "background_color";
	public static final String TEXT_COLOR = "text_color";
	public static final String LOUD_VOICE_LEVEL = "loud_voice_level";
	public static final String DECIBEL_SENSIBILITY = "decibel_sensibility";
	public static final String DELETE_TEMP_FILES = "delete_temp_files";
	
	//--- Default settings values ---
	public static final int DEFAULT_BACKGROUND_COLOR = R.color.midnight_blue;
	public static final int DEFAULT_TEXT_COLOR = R.color.turquoise;
	public static final int DEFAULT_LOUD_VOICE_LEVEL = 80;
	public static final int DEFAULT_DECIBEL_SENSIBILITY = 3;
	public static final boolean DEFAULT_DELETE_TEMP_FILES = true;
	
	//--- NumberPicker MIN/MAX values ---
	public static final int LOUD_VOICE_LEVEL_MIN = 75;
	public static final int LOUD_VOICE_LEVEL_MAX = 90;
	public static final int DECIBEL_SENSIBILITY_MIN = 0;
	public static final int DECIBEL_SENSIBILITY_MAX = 10;
	
	//--- Recording parameters ---
	public static final String RECORDING_DIR_NAME = "/media/audio/";
	public static final String RECORDING_FILE_NAME = "AreYouShoutingRecording";
}
