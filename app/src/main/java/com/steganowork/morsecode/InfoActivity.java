package com.steganowork.morsecode;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.TreeMap;

public class InfoActivity extends AppCompatActivity {
    String TAG = "InfoActivity";
    TreeMap<String, String> MorseEnglish;
    TreeMap<String, String> MorseKorean;
    TreeMap<String, String> MorseNumber;
    TreeMap<String, String> MorseSpecial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setupWindowAnimations();  // 트랜지션 함수

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        MorseCodeBook morseCodeBook = new MorseCodeBook();
        MorseEnglish = morseCodeBook.getMorseEnglish();  // 해시 코드값 넣기 (모스 데이터 삽입)
        MorseKorean = morseCodeBook.getMorseKorean();
        MorseNumber = morseCodeBook.getMorseNumber();
        MorseSpecial = morseCodeBook.getMorseSpecial();

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://ko.wikipedia.org/wiki/%EB%AA%A8%EC%8A%A4_%EB%B6%80%ED%98%B8";  // 위키백과 URL
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        // 모스부호 설명 및 코드북 셋팅
        TextView mainText = (TextView) findViewById(R.id.textView8);
        mainText.setText("모스 부호(Morse code)는 짧은 발신 전류(・)와 긴 발신 전류(-)을 적절히 조합하여 알파벳과 숫자를 표기한 것으로 기본적인 형태는 국제적으로 비슷하다.\n" +
                "미국의 발명가 새뮤얼 핀리 브리즈 모스가 고안하였으며, 1844년 최초로 미국의 볼티모어와 워싱턴 D.C. 사이 전신 연락에 사용되었다.\n" +
                "\n\n\n");
        CodeBookSetting();  // 코드북 셋팅
    }

    private void CodeBookSetting(){
        String[] MorseEnglishKey = new String[MorseEnglish.size()];
        String[] MorseEnglishValue = new String[MorseEnglish.size()];

        // 영어
        int i = 0;
        for (String key : MorseEnglish.keySet()) {
            MorseEnglishKey[i] = key;
            MorseEnglishValue[i] =  MorseEnglish.get(key);
            i++;
        }

        GridView gridView = (GridView) findViewById(R.id.gridView);
        TextItemAdapter textItemAdapter = new TextItemAdapter(this, MorseEnglishKey, MorseEnglishValue);
        gridView.setAdapter(textItemAdapter);
        gridView.setNumColumns(3);

        int expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, View.MeasureSpec.AT_MOST);  // 그리드뷰 리사이즈 함
        gridView.measure(0, expandSpec);
        gridView.getLayoutParams().height = gridView.getMeasuredHeight();


        // 한국어
        i = 0;
        String[] MorseKoreanKey = new String[MorseKorean.size()];
        String[] MorseKoreanValue = new String[MorseKorean.size()];
        for (String key : MorseKorean.keySet()) {
            MorseKoreanKey[i] = key;
            MorseKoreanValue[i] =  MorseKorean.get(key);
            i++;
        }

        GridView gridView2 = (GridView) findViewById(R.id.gridView2);
        TextItemAdapter textItemAdapter2 = new TextItemAdapter(this, MorseKoreanKey, MorseKoreanValue);
        gridView2.setAdapter(textItemAdapter2);
        gridView2.setNumColumns(3);

        gridView2.measure(0, expandSpec);  // 그리드뷰 리사이즈 함
        gridView2.getLayoutParams().height = gridView2.getMeasuredHeight();

        // 숫자
        i = 0;
        String[] MorseNumberKey = new String[MorseNumber.size()];
        String[] MorseNumberValue = new String[MorseNumber.size()];
        for (String key : MorseNumber.keySet()) {
            MorseNumberKey[i] = key;
            MorseNumberValue[i] =  MorseNumber.get(key);
            i++;
        }

        GridView gridView3 = (GridView) findViewById(R.id.gridView3);
        TextItemAdapter textItemAdapter3 = new TextItemAdapter(this, MorseNumberKey, MorseNumberValue);
        gridView3.setAdapter(textItemAdapter3);
        gridView3.setNumColumns(3);

        gridView3.measure(0, expandSpec);
        gridView3.getLayoutParams().height = gridView3.getMeasuredHeight();

        // 특수문자
        i = 0;
        String[] MorseSpecialKey = new String[MorseSpecial.size()];
        String[] MorseSpecialValue = new String[MorseSpecial.size()];
        for (String key : MorseSpecial.keySet()) {
            MorseSpecialKey[i] = key;
            MorseSpecialValue[i] = MorseSpecial.get(key);
            i++;
        }

        GridView gridView4 = (GridView) findViewById(R.id.gridView4);
        TextItemAdapter textItemAdapter4 = new TextItemAdapter(this, MorseSpecialKey, MorseSpecialValue);
        gridView4.setAdapter(textItemAdapter4);
        gridView4.setNumColumns(3);

        gridView4.measure(0, expandSpec);
        gridView4.getLayoutParams().height = gridView4.getMeasuredHeight();
    }

    // 트랜지션 함수 ===============================================================================
    private void setupWindowAnimations() {
        //Slide slide = new Slide();
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setExitTransition(fade);

        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setReenterTransition(slide);
    }

    // 액션 바 =====================================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:  // 뒤로가기 아이콘
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
