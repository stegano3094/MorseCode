package com.steganowork.morsecode;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView mainText = (TextView) findViewById(R.id.textView8);
        mainText.setTextSize(16);
        mainText.setText("모스 부호(Morse code)는 짧은 발신 전류(・)와 긴 발신 전류(-)을 적절히 조합하여 알파벳과 숫자를 표기한 것으로 기본적인 형태는 국제적으로 비슷하다.\n" +
                "미국의 발명가 새뮤얼 핀리 브리즈 모스가 고안하였으며, 1844년 최초로 미국의 볼티모어와 워싱턴 D.C. 사이 전신 연락에 사용되었다.\n" +
                "\n" +
                "개발 및 역사\n" +
                "\n" +
                "1836년을 기점으로, 미국의 아티스트 새뮤얼 모스, 미국의 물리학자 조지프 헨리, 앨프리드 베일이 전신 시스템을 개발하였다. \n" +
                "모스 코드가 개발되어 조작자들이 종이 테이프에 적힌 자국을 텍스트 메시지로 해석할 수 있었다. 초기 코드에서 모스는 숫자만을 전송할 계획이었다. 그러나 이 코드는 앨프리드 베일이 일반 문자와 특수 문자를 포함, 확장시켰고 일반화되었다. \n");
    }
}
