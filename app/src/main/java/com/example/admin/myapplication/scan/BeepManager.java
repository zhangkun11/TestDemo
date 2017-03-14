package com.example.admin.myapplication.scan;



import android.content.Context;
import android.jb.Preference;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

public class BeepManager {

	// 常量
	private final float BEEP_VOLUME = 0.3f;
	private final long VIBRATE_DURATION = 200L;

	// 变量
	private boolean playBeep = false;
	private boolean vibrate = false;

	// 控制器
	private Context mContext;
	// private MediaPlayer mMediaPlayer;
	private int loadId1;
	private SoundPool mSoundPool;
	private Vibrator mVibrator;

	public BeepManager(Context context, boolean playBeep, boolean vibrate) {
		super();
		this.mContext = context;
		this.playBeep = playBeep;
		this.vibrate = vibrate;

		initial();
	}

	public boolean isPlayBeep() {
		playBeep = Preference.getScanSound(mContext, true);
		return playBeep;
	}

	public void setPlayBeep(boolean playBeep) {
		Preference.setScanSound(mContext, playBeep);
		this.playBeep = playBeep;
	}

	public boolean isVibrate() {
		vibrate = Preference.getScanVibration(mContext, false);
		return vibrate;
	}

	public void setVibrate(boolean vibrate) {
		Preference.setScanVibration(mContext, vibrate);
		this.vibrate = vibrate;
	}

	private void initial() {
		// TODO Auto-generated method stub

		// initialMediaPlayer
		// 判断mMediaPlayer是否已生成，有则先释放，再创建
		// if (mMediaPlayer != null) {
		// mMediaPlayer.reset();
		// mMediaPlayer.release();
		// mMediaPlayer = null;
		// }
		// mMediaPlayer = new MediaPlayer();
		//
		// /* 监听播放是否完成 */
		// // mMediaPlayer.setOnCompletionListener(this);
		//
		// AssetFileDescriptor afd = mContext.getResources().openRawResourceFd(
		// R.raw.beep);
		// try {
		// mMediaPlayer.setDataSource(afd.getFileDescriptor(),
		// afd.getStartOffset(), afd.getDeclaredLength());
		// mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// mMediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
		// mMediaPlayer.prepare();
		// mMediaPlayer.setLooping(false);
		// afd.close();
		// } catch (IllegalArgumentException e) {
		// Log.e("playMusic", "Unable to play audio queue do to exception: "
		// + e.getMessage(), e);
		// } catch (IllegalStateException e) {
		// Log.e("playMusic", "Unable to play audio queue do to exception: "
		// + e.getMessage(), e);
		// } catch (IOException e) {
		// Log.e("playMusic", "Unable to play audio queue do to exception: "
		// + e.getMessage(), e);
		// }

		if (null == mSoundPool) {
			mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		}
		//loadId1 = mSoundPool.load(mContext, ResourceUtil.getRawResIDByName(mContext, "beep"), 1);

		// initialVibrator
		mVibrator = (Vibrator) mContext
				.getSystemService(Context.VIBRATOR_SERVICE);
	}

	public void play() {
		// playMusic
		// if (playBeep && !km.inKeyguardRestrictedInputMode()) {
		// mMediaPlayer.start();
		// }
		if (playBeep) {
			// mMediaPlayer.start();
			// 参数1：播放特效加载后的ID值
			// 参数2：左声道音量大小(range = 0.0 to 1.0)
			// 参数3：右声道音量大小(range = 0.0 to 1.0)
			// 参数4：特效音乐播放的优先级，因为可以同时播放多个特效音乐
			// 参数5：是否循环播放，0只播放一次(0 = no loop, -1 = loop forever)
			// 参数6：特效音乐播放的速度，1F为正常播放，范围 0.5 到 2.0
			//mSoundPool.play(loadId1, 0.5f, 0.5f, 1, 0, 1f);
		}
		// vibrate
		if (vibrate) {
			mVibrator.vibrate(VIBRATE_DURATION);
		}

	}

}
