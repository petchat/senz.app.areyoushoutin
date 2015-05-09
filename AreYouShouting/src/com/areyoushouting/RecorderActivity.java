package com.areyoushouting;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import utils.BooleanUtils;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kiding.areyoushouting.R;
import com.senz.data.collector.AlarmReceiver;

public class RecorderActivity extends Activity implements
		AreYouShoutingInterface {
	// --- Sound Recording ---
	public static final int SOUND_RATE = 16000;
	private AudioRecord audioRecorder;
	public static boolean isRecording = false;
	private int loudVoiceLevel;
	private int decibelSensibility;
	private File recorderFile;
	private short[] buffer;

	// --- Notification ---
	private static final int NOTIFICATION_ID = 777;
	private NotificationManager notificationManager;
	public Vibrator vibrator;
	private boolean isShowingToastAlertMessage = false;

	// --- Views ---
	private RelativeLayout parentLayout;
	private ImageButton micButton;
	private ProgressBar progressBar;
	private TextView soundVolumeTextView;
	private ImageButton lockButton;
	private boolean isUILocked;

	// --- Preferences ---
	private SharedPreferences settings;

	// --- Animations ---
	Animation alphaIn, alphaOut, scale;

	

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// --- Hide App's Title ---
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// --- Settings ---
		settings = getSharedPreferences(MY_PREFS, Activity.MODE_PRIVATE);
		loudVoiceLevel = settings.getInt(LOUD_VOICE_LEVEL,
				DEFAULT_LOUD_VOICE_LEVEL);
		decibelSensibility = settings.getInt(DECIBEL_SENSIBILITY,
				DEFAULT_DECIBEL_SENSIBILITY);

		// --- Create the Layout ---
		createLayout();

		// --- Update UI Icons and Colors ---
		updateUIColors();

		// --- Set the Animations ---
		alphaIn = AnimationUtils.loadAnimation(this, R.anim.alpha_fast_in);
		alphaOut = AnimationUtils.loadAnimation(this, R.anim.alpha_fast_out);
		scale = AnimationUtils.loadAnimation(this, R.anim.scale);

		// --- Init ---
		progressBar.setVisibility(View.INVISIBLE);
		soundVolumeTextView.setVisibility(View.INVISIBLE);
		progressBar.setMax(loudVoiceLevel);
		isUILocked = false;
		micButton.setClickable(!isUILocked); // here micButton is clickable
		initRecorder();

			}

	/** Method that creates the layout */
	private void createLayout() {
		// --- Set the layout ---
		setContentView(R.layout.recorder);

		// --- Views ---
		parentLayout = (RelativeLayout) findViewById(R.id.parent);
		micButton = (ImageButton) findViewById(R.id.micButton);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		soundVolumeTextView = (TextView) findViewById(R.id.soundVolumeTextView);
		lockButton = (ImageButton) findViewById(R.id.lockButton);

		// --- FONT ---
		Typeface font = Typeface.createFromAsset(getAssets(),
				"Roboto-Light.ttf");
		soundVolumeTextView.setTypeface(font);

		// --- micButton OnClickListener ---
		micButton.setOnClickListener(new MicButtonListener());

		// --- lockButton OnClickListener ---
		lockButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// --- Set UI as Locked or Unlocked (switch every time) ---
				isUILocked = !isUILocked;
				micButton.setClickable(!isUILocked);

				// --- Show an animation ---
				lockButton.startAnimation(scale);

				// --- Update UI Icons and Colors ---
				updateUIColors();
			}
		});
	}

	/**
	 * Method that updates Background and Text colors (stored in
	 * SharedPreferences) If not set, default colors are those set in
	 * 'AreYouShoutingInterface' interface
	 **/
	@SuppressWarnings("deprecation")
	private void updateUIColors() {
		// --- Background Color ---
		int color = settings.getInt(BACKGROUND_COLOR,
				getResources().getColor(DEFAULT_BACKGROUND_COLOR));
		parentLayout.setBackgroundColor(color);

		// --- Text/Icons Color ---
		color = settings.getInt(TEXT_COLOR,
				getResources().getColor(DEFAULT_TEXT_COLOR));

		// --- Create Drawables of specified color ---
		Drawable micDrawable = getResources().getDrawable(R.drawable.mic);
		Drawable lockDrawable = (isUILocked) ? getResources().getDrawable(
				R.drawable.lock) : getResources().getDrawable(R.drawable.lock2);
		micDrawable.setColorFilter(color, Mode.MULTIPLY);
		lockDrawable.setColorFilter(color, Mode.MULTIPLY);

		// --- Update UI Icons and TextView ---
		// setBackgroundDrawable() has been deprecated, I used it anyway for
		// SDKs compatibility reasons
		micButton.setBackgroundDrawable(micDrawable);
		lockButton.setBackgroundDrawable(lockDrawable);
		soundVolumeTextView.setTextColor(color);
	}

	/** OnClickListener for the Mic Button */
	private class MicButtonListener implements View.OnClickListener {
		@Override
		public void onClick(final View v) {
			if (!isRecording) {
				// --- START RECORDING ---

				// --- Set Animations ---
				progressBar.startAnimation(alphaIn);
				soundVolumeTextView.startAnimation(alphaIn);
				micButton.startAnimation(scale);

				// --- Set Views as VISIBLE ---
				progressBar.setVisibility(View.VISIBLE);
				soundVolumeTextView.setVisibility(View.VISIBLE);

				// Now start recording
				isRecording = true;
				audioRecorder.startRecording();
				recorderFile = getFile("raw");
				if (recorderFile != null && audioRecorder != null) {
					try {
						startBufferedWrite(recorderFile);
					} catch (Exception e) {
						Log.i("recorderactivity", e.toString());
					}
					// --- Show Notification in Status Bar ---
					showNotification(true);

				} else {
					// --- Can't store recording Temp file ==> finishActivity
					// ---
					finishActivity();
				}

			} else {
				// --- STOP RECORDING ---

				// --- Set Animations ---
				progressBar.startAnimation(alphaOut);
				soundVolumeTextView.startAnimation(alphaOut);
				micButton.startAnimation(scale);

				// --- Set Views as INVISIBLE ---
				progressBar.setVisibility(View.INVISIBLE);
				soundVolumeTextView.setVisibility(View.INVISIBLE);

				// Now stop recording
				isRecording = false;
				audioRecorder.stop();
				// Stop Showing the Notification in Action Bar ---
				showNotification(false);

				// --- Check if user wants to delete Temp Files ---
				if (!settings.getBoolean(DELETE_TEMP_FILES,
						DEFAULT_DELETE_TEMP_FILES)) {

					// --- Store Temp file on SD card ---
					File waveFile = getFile("wav");
					try {
						// --- Convert RAW recording to WAVE file ---
						rawToWave(recorderFile, waveFile);

						// --- Show a confirm message ---
						Toast.makeText(
								RecorderActivity.this,
								getResources().getString(R.string.recorded_to)
										+ RECORDING_DIR_NAME
										+ waveFile.getName(), Toast.LENGTH_LONG)
								.show();

					} catch (IOException e) {
						Toast.makeText(RecorderActivity.this, e.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	}

	/** Method that initialize the Recorder */
	private void initRecorder() {
		int bufferSize = AudioRecord.getMinBufferSize(SOUND_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		buffer = new short[bufferSize];
		audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				SOUND_RATE, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);
	}

	/**
	 * Method that starts writing the Buffer to Temp file It uses a Thread to
	 * reduce resources consuming
	 **/
	private synchronized void startBufferedWrite(final File file) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				DataOutputStream output = null;
				try {
					output = new DataOutputStream(new BufferedOutputStream(
							new FileOutputStream(file)));
					while (isRecording) {
						double sum = 0;
						int readSize = audioRecorder.read(buffer, 0,
								buffer.length);
						for (int i = 0; i < readSize; i++) {
							output.writeShort(buffer[i]);
							sum += buffer[i] * buffer[i];
						}

						if (readSize > 0) {
							final double powerRatio = sum / readSize;
							int decibel = (int) (10 * Math.log10(powerRatio));
							// int amplitudeRatio = (int) Math.sqrt(powerRatio);
							// //Not used

							// --- Update UI ---
							progressBar.setProgress(decibel);
							setSoundVolumeTextView(decibel);

							/**
							 * From ==> http://en.wikipedia.org/wiki/Decibel
							 * 
							 * Decibel = 10* Math.log10(powerRatio);
							 * AmplitudeRatio = Math.sqrt(powerRatio);
							 * 
							 * Use Decibels to update the ProgressBar [75 - 90]
							 * 
							 * Than check if the current sound level is > than
							 * the threshold set
							 * 
							 * Log.d("TEST","Power="+powerRatio+" - DECIBEL="+
							 * decibel+" - AMP="+amplitudeRatio);
							 */

							// --- Check if the loudVoiceLevel limit has been
							// exceeded ---
							if (decibel > loudVoiceLevel) {
								showToastAlertMessage();
							}
						}
					}

				} catch (IOException e) {
					Toast.makeText(RecorderActivity.this, e.getMessage(),
							Toast.LENGTH_SHORT).show();

				} finally {

					if (output != null) {
						try {
							output.flush();
						} catch (IOException e) {
							Toast.makeText(RecorderActivity.this,
									e.getMessage(), Toast.LENGTH_SHORT).show();
						} finally {
							try {
								output.close();
							} catch (IOException e) {
								Toast.makeText(RecorderActivity.this,
										e.getMessage(), Toast.LENGTH_SHORT)
										.show();
							}
						}
					}
				}
			}
		}).start();
	}

	/**
	 * Method called when the user is Shouting! Simply shows a Toast message and
	 * vibrates
	 **/
	private synchronized void showToastAlertMessage() {

		// --- Displays a message only if it is not already displayed ---
		if (!isShowingToastAlertMessage) {

			// --- Thread ACTIVE ---
			isShowingToastAlertMessage = true;
			this.runOnUiThread(new Thread() {
				public void run() {

					// --- Set the Vibrator and show output message ---
					vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					vibrator.vibrate(300);
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.volume_message),
							Toast.LENGTH_SHORT).show();

					try {
						// stop the thread for a while (for a better UX)
						sleep(600);
					} catch (InterruptedException e) {
					}

					// --- Thread UNACTIVE ---
					isShowingToastAlertMessage = false;
				}
			});
		}
	}

	/** Method that updates the soundVolumeTextView with current sound decibels */
	private synchronized void setSoundVolumeTextView(final int decibel) {

		// --- Update soundVolumeTextView only if current decibels differ from
		// old sound decibel by more than 'decibelSensibility' ---
		this.runOnUiThread(new Thread() {
			public void run() {
				Log.i("RecorderActivity",
						"==>"
								+ soundVolumeTextView.getText().toString()
										.substring(0, 2));
				// 要去除空格
				int oldDecibel = Integer.parseInt(soundVolumeTextView.getText()
						.toString().substring(0, 2).replace(" ", ""));

				if ((Math.abs(oldDecibel - decibel)) > decibelSensibility) {
					soundVolumeTextView.setText(decibel + " / "
							+ loudVoiceLevel + " dB");
				}
			}
		});
	}

	/**
	 * Method that saves audio from RAW file containing the Recording to a final
	 * Wave file
	 */
	private void rawToWave(final File rawFile, final File waveFile)
			throws IOException {

		byte[] rawData = new byte[(int) rawFile.length()];
		DataInputStream input = null;

		try {
			input = new DataInputStream(new FileInputStream(rawFile));
			input.read(rawData);

		} finally {
			if (input != null) {
				input.close();
			}
		}

		DataOutputStream output = null;
		try {
			output = new DataOutputStream(new FileOutputStream(waveFile));
			// WAVE header
			// see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
			writeString(output, "RIFF"); // chunk id
			writeInt(output, 36 + rawData.length); // chunk size
			writeString(output, "WAVE"); // format
			writeString(output, "fmt "); // subchunk 1 id
			writeInt(output, 16); // subchunk 1 size
			writeShort(output, (short) 1); // audio format (1 = PCM)
			writeShort(output, (short) 1); // number of channels
			writeInt(output, SOUND_RATE); // sample rate
			writeInt(output, SOUND_RATE * 2); // byte rate
			writeShort(output, (short) 2); // block align
			writeShort(output, (short) 16); // bits per sample
			writeString(output, "data"); // subchunk 2 id
			writeInt(output, rawData.length); // subchunk 2 size
			// Audio data (conversion big endian -> little endian)
			short[] shorts = new short[rawData.length / 2];
			ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN)
					.asShortBuffer().get(shorts);
			ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
			for (short s : shorts) {
				bytes.putShort(s);
			}
			output.write(bytes.array());
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}

	/**
	 * Method that returns the output file for the recording (raw or wav) Null
	 * if external storage is not available
	 **/
	private File getFile(final String suffix) {
		// --- Check if external storage is available for write ---
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// --- External storage available ---
			// --- Set the Folder and File names ---
			File sdCard = Environment.getExternalStorageDirectory();
			File dirName = new File(sdCard.getAbsolutePath()
					+ RECORDING_DIR_NAME);
			dirName.mkdirs();
			String newSoundFile = dirName.getAbsolutePath() + "/"
					+ RECORDING_FILE_NAME + "." + suffix;

			return new File(newSoundFile);

		} else {
			// --- External storage not available ---
			Toast.makeText(RecorderActivity.this,
					getResources().getString(R.string.external_storage_error),
					Toast.LENGTH_LONG).show();
			return null;
		}
	}

	/** Methods used for writing */
	private void writeInt(final DataOutputStream output, final int value)
			throws IOException {
		output.write(value >> 0);
		output.write(value >> 8);
		output.write(value >> 16);
		output.write(value >> 24);
	}

	private void writeShort(final DataOutputStream output, final short value)
			throws IOException {
		output.write(value >> 0);
		output.write(value >> 8);
	}

	private void writeString(final DataOutputStream output, final String value)
			throws IOException {
		for (int i = 0; i < value.length(); i++) {
			output.write(value.charAt(i));
		}
	}

	/** Method that shows or stops showing a Notification in Notification Bar */
	private void showNotification(boolean show) {
		if (show) {
			// --- Show Notification ---
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					this)
					.setOngoing(true)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(
							getResources().getString(
									R.string.notification_title))
					.setContentText(
							getResources()
									.getString(R.string.notification_text));

			// --- Intent: if user clicks on the Notification ==> Show current
			// Activity ---
			// PS: Please note the definition of "android:launchMode="singleTop"
			// in Manifest file
			Intent resultIntent = new Intent(this, RecorderActivity.class);
			PendingIntent resultPendingIntent = PendingIntent.getActivity(this,
					0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent);

			// --- Show the Notification ---
			notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, mBuilder.build());

		} else {
			// --- Stop showing the Notification ---
			try {
				notificationManager.cancel(NOTIFICATION_ID);
			} catch (NullPointerException e) {
				Log.d("Catched Exception", e.toString());
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing()) {
			// --- Stop showing the Notification ---
			showNotification(false);
		}
	}

	@Override
	public void onDestroy() {
		isRecording = false;
		audioRecorder.release();

		// 网上找的答案，不设置为null的话，会出现：
		// startRecording() called on an uninitialized AudioRecord
		audioRecorder = null;

		super.onDestroy();
	}

	/** Called when a key is pressed */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// --- Finish the Activity ---
			finishActivity();

			
			return true;
		}
		return false;
	}

	/**
	 * Method called when Activity has to be finished (set a Transition
	 * Animation)
	 */
	public void finishActivity() {
		finish();
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}
}