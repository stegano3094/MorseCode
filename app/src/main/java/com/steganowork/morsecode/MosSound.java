package com.steganowork.morsecode;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

public class MosSound {
    String TAG = "MosSound";
    Context context;
    MediaPlayer mediaPlayer;
    int playNumber;

    MosSound(Context context) {
        this.context = context;
        mediaPlayer = null;
    }

    public void SoundSetting(int playNumber) {
        // 소리 세팅
        this.playNumber = playNumber;
        mediaPlayer = null;
        if (playNumber == 1) {
            mediaPlayer = MediaPlayer.create(context, R.raw.sound_beep_dash);
        } else if (playNumber == 2) {
            mediaPlayer = MediaPlayer.create(context, R.raw.sound_beep_dash);
        }
    }

    public void SoundPlay(long[] customTimings_sound, int[] customOnOff_sound, int TermTime) {
        Log.i(TAG, "playNumber : " + playNumber + ", TermTime : " + TermTime);

        // 소리 시작
        if (mediaPlayer != null) {
            try {
                Log.i(TAG, "mediaPlayer Start");
                int count = 0;
                for (long i : customTimings_sound) {
                    if (customOnOff_sound[count] == 1) {
                        mediaPlayer.start();
                        Thread.sleep(i);
                    } else if (customOnOff_sound[count] == 0) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                        }
                        Thread.sleep(i);
                    }
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.seekTo(0);
                        mediaPlayer.pause();
                    }
                    Thread.sleep(TermTime);
                    count++;
                }

                mediaPlayer.release();
                Log.i(TAG, "mediaPlayer Stop");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "mediaPlayer is null", Toast.LENGTH_SHORT).show();
        }
    }
}