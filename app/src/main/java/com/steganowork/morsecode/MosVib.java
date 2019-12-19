package com.steganowork.morsecode;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class MosVib {
    Vibrator vib;
    Boolean isStarted;
    Context context;

    MosVib(Context context){
        this.context = context;
        vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void VidStart(long[] Timings, int[] Amplitudes) {
        if (vib != null) {
            isStarted = false;
            vib.cancel();  // 이미 실행중이면 제거함
        }

        // after API LEVEL 28(26~27에서 사용시 진폭 사이사이에 0을 주어 패턴 변화를 주어야 함)
        isStarted = true;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {  // 오레오 버전 이상 실행
            vib.vibrate(VibrationEffect.createWaveform(Timings, Amplitudes, -1));  // -1은 반복하지 않음
            //Toast.makeText(getApplicationContext(), "상태 : " + state, Toast.LENGTH_SHORT).show();
        } else {  // 나머지 버전에서 실행
            //Toast.makeText(context.getApplicationContext(), "버전이 낮아서 실행할 수 없습니다", Toast.LENGTH_SHORT).show();
            vib.vibrate(Timings, -1);
        }
    }

    public void VidStop() {
        isStarted = false;
        if (vib != null) {
            vib.cancel();
        }
    }
}
