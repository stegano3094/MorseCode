package com.steganowork.morsecode;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.TreeMap;

public class InfoActivity extends AppCompatActivity {
    String TAG = "InfoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TreeMap<String, String> MorseEnglish;
        TreeMap<String, String> MorseKorean;
        TreeMap<String, String> MorseNumber;
        TreeMap<String, String> MorseSpecial;

        MorseCodeBook morseCodeBook = new MorseCodeBook();
        MorseEnglish = morseCodeBook.getMorseEnglish();  // 해시 코드값 넣기 (모스 데이터 삽입)
        MorseKorean = morseCodeBook.getMorseKorean();
        MorseNumber = morseCodeBook.getMorseNumber();
        MorseSpecial = morseCodeBook.getMorseSpecial();

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://ko.wikipedia.org/wiki/%EB%AA%A8%EC%8A%A4_%EB%B6%80%ED%98%B8";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        TextView mainText = (TextView) findViewById(R.id.textView8);
        TextView subText = (TextView) findViewById(R.id.textView9);
        mainText.setTextSize(16);
        mainText.setText("모스 부호(Morse code)는 짧은 발신 전류(・)와 긴 발신 전류(-)을 적절히 조합하여 알파벳과 숫자를 표기한 것으로 기본적인 형태는 국제적으로 비슷하다.\n" +
                "미국의 발명가 새뮤얼 핀리 브리즈 모스가 고안하였으며, 1844년 최초로 미국의 볼티모어와 워싱턴 D.C. 사이 전신 연락에 사용되었다.\n" +
                "\n" +
                "개발 및 역사\n" +
                "\n" +
                "1836년을 기점으로, 미국의 아티스트 새뮤얼 모스, 미국의 물리학자 조지프 헨리, 앨프리드 베일이 전신 시스템을 개발하였다. \n" +
                "모스 코드가 개발되어 조작자들이 종이 테이프에 적힌 자국을 텍스트 메시지로 해석할 수 있었다. 초기 코드에서 모스는 숫자만을 전송할 계획이었다. 그러나 이 코드는 앨프리드 베일이 일반 문자와 특수 문자를 포함, 확장시켰고 일반화되었다. \n" +
                "\n\n\n");

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\n\n\n영어\n\n");
        for (String key : MorseEnglish.keySet()) {
            stringBuffer.append(key + "     " + MorseEnglish.get(key) + "\n");
        }

        stringBuffer.append("\n\n\n한국어\n\n");
        for (String key : MorseKorean.keySet()) {
            stringBuffer.append(key + "     " + MorseKorean.get(key) + "\n");
        }

        stringBuffer.append("\n\n\n숫자\n\n");
        for (String key : MorseNumber.keySet()) {
            stringBuffer.append(key + "     " + MorseNumber.get(key) + "\n");
        }

        stringBuffer.append("\n\n\n특수문자\n\n");
        for (String key : MorseSpecial.keySet()) {
            stringBuffer.append(key + "     " + MorseSpecial.get(key) + "\n");
        }
        stringBuffer.append("\n\n\n");

        subText.setTextSize(16);
        subText.setText(stringBuffer);

        setupWindowAnimations();  // 트랜지션 함수
    }

    private void setupWindowAnimations() {  // 트랜지션 함수
        //Slide slide = new Slide();
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setExitTransition(fade);

        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setReenterTransition(slide);
    }
}
