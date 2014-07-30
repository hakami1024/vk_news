package com.hakami1024.utils;

import java.io.IOException;

import android.media.MediaPlayer;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class VKAudioPlayer {

	private String audioUrl;
	private static final VKAudioPlayer vkAudioPlayer = new VKAudioPlayer();
	//private static String TAG = "VKAudioPlayer";
	private MediaPlayer mediaPlayer = null;
	private SeekBar seekBar = null;
	private ImageButton button = null;

	private VKAudioPlayer() {
	}

	public static VKAudioPlayer getInstance() {
		return vkAudioPlayer;
	}

	public void play(String url, SeekBar seekBar, ImageButton button) {

		if (url.equals(audioUrl)) {
			if (mediaPlayer != null) {
				if (mediaPlayer.isPlaying()) {
					// Log.d(TAG, "already is playing");
					return;
				}

				mediaPlayer.start();
				this.seekBar.setProgress(mediaPlayer.getCurrentPosition());
				return;
			}
		}

		if (this.seekBar != null && !this.seekBar.equals(seekBar))
			stop();

		this.seekBar = seekBar;
		this.button = button;
		this.button.setSelected(true);

		audioUrl = url;
		final SeekBar curBar = seekBar;
		mediaPlayer = new MediaPlayer() {
			@Override
			public void start() {
				super.start();
				// Log.d(TAG, "starting mediaPlayer");
				new Thread(new Runnable() {

					@Override
					public void run() {
						while (mediaPlayer != null && mediaPlayer.isPlaying()) {
							// if( mediaPlayer.isPlaying() )
							// {
							try {
								curBar.setProgress(mediaPlayer
										.getCurrentPosition());
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// }
							// else
						}
						Thread.currentThread().interrupt();

					}
				}).start();
			}
		};
		try {
			mediaPlayer.setDataSource("http" + audioUrl.substring(5));
			mediaPlayer.prepare();
			mediaPlayer.start();
			seekBar.setVisibility(SeekBar.VISIBLE);
			// Log.d(TAG, "player started");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			// Log.d(TAG, "player error1");
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			// Log.d(TAG, "player error2");
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			// Log.d(TAG, "player error3");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// Log.d(TAG,
			// "player error4: "+e.getMessage()+" "+"http"+audioUrl.substring(5));
			e.printStackTrace();
		}

		/*
		 * mediaPlayer.setOnPreparedListener( new OnPreparedListener() {
		 * 
		 * @Override public void onPrepared(MediaPlayer mp) { final Handler
		 * handler = new Handler(); handler.post(new Runnable() { public void
		 * run() { controller.setEnabled(true); controller.show(); } }); } });
		 */

	}

	public void stop() {
		pause();
		if (mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	public void pause() {
		if (mediaPlayer != null)
			mediaPlayer.pause();
		if (seekBar != null) {
			seekBar.setVisibility(SeekBar.GONE);
			if (button.isSelected()) {
				button.setSelected(false);
				// Log.d(TAG, "last button stopped");
			}
		}
	}

	public int getDuration() {
		return mediaPlayer == null ? 0 : mediaPlayer.getDuration();
	}

	public int getCurrentPosition() {
		return mediaPlayer == null ? 0 : mediaPlayer.getCurrentPosition();
	}

	public void seekTo(int i) {
		if (mediaPlayer == null)
			return;
		mediaPlayer.seekTo(i);
	}

	public boolean isPlaying() {
		return (mediaPlayer != null && mediaPlayer.isPlaying());
	}
}
