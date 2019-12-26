package com.steganowork.morsecode;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    private BackPressCloseHandler backPressCloseHandler; // back key 객체 선언

    private static TreeMap<String, String> MorseKorean;
    private static TreeMap<String, String> MorseEnglish;
    private static TreeMap<String, String> MorseNumber;
    private static TreeMap<String, String> MorseSpecial;
    TextView resultText;
    EditText editText;
    MaterialSpinner spinner;

    FeedbackThread feedbackThread_vid = null;
    FeedbackThread feedbackThread_light = null;
    FeedbackThread feedbackThread_sound = null;

    boolean lightEnable = false;
    boolean vibeEnable = false;
    boolean soundEnable = false;
    boolean isVibStarted = false;
    boolean isLightStarted = false;
    boolean isSoundStarted = false;

    String noResultData;
    String languageState = "";
    String[] languageAvailable;
    int cameraVersion = 0;
    int speed = 5;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String savedLanguage;
    int savedPlayLength;
    boolean savedSave;

    private UpdateThread mUpdateThread;
    private AdView mAdView;  // 광고뷰

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupWindowAnimations();  // 트랜지션 함수
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   // 화면꺼짐 방지
        backPressCloseHandler = new BackPressCloseHandler(this);      // 뒤로가기 버튼

        languageAvailable = getResources().getStringArray(R.array.TranslationLanguage);  // 가능한 언어
        noResultData = getResources().getString(R.string.no_result_data);
        resultText = (TextView) findViewById(R.id.textView7);
        resultText.setText(noResultData);  // 초기화
        editText = (EditText) findViewById(R.id.editText);

        MorseEnglish = new TreeMap<String, String>();
        MorseKorean = new TreeMap<String, String>();
        MorseNumber = new TreeMap<String, String>();
        MorseSpecial = new TreeMap<String, String>();

        MorseCodeBook morseCodeBook = new MorseCodeBook();
        MorseEnglish = morseCodeBook.getMorseEnglish();  // 해시 코드값 넣기 (모스 데이터 삽입)
        MorseKorean = morseCodeBook.getMorseKorean();
        MorseNumber = morseCodeBook.getMorseNumber();
        MorseSpecial = morseCodeBook.getMorseSpecial();

        // 모드 변경 -------------------------------------------------------------------------------
        spinner = (MaterialSpinner) findViewById(R.id.spinner);  // 스피너
        spinner.setItems(getResources().getStringArray(R.array.TranslationLanguage));
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                ChangeMode(item);  // 모드 변경
            }
        });

        // 실시간 번역 -----------------------------------------------------------------------------
        mUpdateThread = new UpdateThread();  // 스레드 생성
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 입력하기 전에
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = charSequence.toString();
                Log.i(TAG, "===========================================================");
                Log.i(TAG, "Origin Data : " + str);
                mUpdateThread.run(str);  // 입력 값이 바뀔 때마다 UI 업데이트 시켜줌
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 입력이 끝났을 때
            }
        });

        // 피드백 (빛, 진동) -----------------------------------------------------------------------
        resultText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int runNum;

                if (!isVibStarted && !isLightStarted && !isSoundStarted) {
                    if (vibeEnable) {  // 진동 보내기
                        runNum = 1;
                        feedbackThread_vid = new FeedbackThread(runNum, speed);
                        feedbackThread_vid.start();
                    }
                    if (lightEnable) {  // 빛 보내기
                        runNum = 2;
                        feedbackThread_light = new FeedbackThread(runNum, speed);
                        feedbackThread_light.start();
                    }
                    if (soundEnable) {  // 소리 보내기
                        runNum = 3;
                        int soundTrackNum = 1;  // tkdnse1~2
                        feedbackThread_sound = new FeedbackThread(runNum, soundTrackNum, speed);
                        feedbackThread_sound.start();
                    }
                }
            }
        });

        // 광고 ------------------------------------------------------------------------------------
        Ad();
    }

    // 동시 수행을 위한 스레드 =====================================================================
    public class FeedbackThread extends Thread {
        private static final String TAG = "FeedbackThread";
        int Amp = 150;
        int LongTime = 50;
        int ShortTime = 25;
        int TermTime = 25;
        int runNum;
        int soundTrackNum;
        String morseString = resultText.getText().toString();  // 현재 결과 가져옴

        public FeedbackThread(int runNUm, int speed) {
            Log.i(TAG, "feedback Thread on");
            this.runNum = runNUm;
            this.LongTime = LongTime * speed;
            this.ShortTime = ShortTime * speed;
            this.TermTime = TermTime * speed;
        }

        public FeedbackThread(int runNUm, int soundTrackNum, int speed) {
            Log.i(TAG, "feedback Thread on");
            this.runNum = runNUm;
            this.soundTrackNum = soundTrackNum;
            this.LongTime = LongTime * speed;
            this.ShortTime = ShortTime * speed;
            this.TermTime = TermTime * speed;
        }

        public void run() {
            try {
                if (runNum == 1) {
                    isVibStarted = true;  // 시작 플레그
                    Log.i(TAG, "runNum == 1");
                    vibeMode(morseString, Amp, LongTime, ShortTime, TermTime);
                    isVibStarted = false;  // 종료 플레그
                } else if (runNum == 2) {
                    isLightStarted = true;
                    Log.i(TAG, "runNum == 2");
                    lightMode(morseString, LongTime, ShortTime, TermTime);
                    isLightStarted = false;
                } else if (runNum == 3) {
                    isSoundStarted = true;
                    Log.i(TAG, "runNum == 3");
                    soundMode(morseString, LongTime, ShortTime, TermTime, soundTrackNum);
                    isSoundStarted = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void vibeMode(String morseString, int Amp, int LongTime, int ShortTime, int TermTime) {
        //Log.i(TAG, "진동 On, morseString : " + morseString);
        MosVib mosVib = new MosVib(getApplicationContext());  // 진동을 사용할 수 있는 객체를 생성
        long[] customTimings_vib = new long[resultText.length() * 2];
        int[] customAmplitudes_vib = new int[resultText.length() * 2];
        int tempCount = 0;

        for (char temp : morseString.toCharArray()) {  // 모스 부호 분석해서 타이밍 맞추기
            //Log.i(TAG, "temp : " + temp);
            if (temp == '-') {
                //Log.i(TAG, "- 추가");
                customTimings_vib[tempCount] = LongTime;
                customAmplitudes_vib[tempCount] = Amp;  // 진동 범위 : 0~255
            } else if (temp == '*') {
                //Log.i(TAG, "* 추가");
                customTimings_vib[tempCount] = ShortTime;
                customAmplitudes_vib[tempCount] = Amp;
            } else {
                //Log.i(TAG, "쉼표 추가");
                customTimings_vib[tempCount] = ShortTime;
                customAmplitudes_vib[tempCount] = 0;
            }
            customTimings_vib[tempCount + 1] = TermTime;  // 파형으로 들어가야하므로 주기적으로 넣어줌
            customAmplitudes_vib[tempCount + 1] = 0;
            tempCount += 2;
        }
        //Log.i(TAG, "진동 피드백 On");
        mosVib.VidStart(customTimings_vib, customAmplitudes_vib);  // 진동 시작
        //Log.i(TAG, "진동 Off");
    }

    public void lightMode(String morseString, int LongTime, int ShortTime, int TermTime) {
        //Log.i(TAG, "플래시 On, morseString : " + morseString);
        MosLight mosLight = new MosLight(getApplicationContext(), cameraVersion);  // 빛을 사용할 수 있는 객체를 생성
        long[] customTimings_light = new long[resultText.length()];
        int[] customOnOff_light = new int[resultText.length()];  // 1 = on, 0 = off
        int tempCount = 0;

        for (char temp : morseString.toCharArray()) {  // 모스 부호 분석해서 타이밍 맞추기
            //Log.i(TAG, "temp : " + temp);
            if (temp == '-') {
                customTimings_light[tempCount] = LongTime;
                customOnOff_light[tempCount] = 1;
            } else if (temp == '*') {
                customTimings_light[tempCount] = ShortTime;
                customOnOff_light[tempCount] = 1;
            } else {
                customTimings_light[tempCount] = ShortTime;
                customOnOff_light[tempCount] = 0;
            }
            tempCount += 1;
        }
        //Log.i(TAG, "플래시 피드백 On");
        mosLight.LightStart(customTimings_light, customOnOff_light, TermTime);  // 플래시 시작
        //Log.i(TAG, "플래시 Off");
    }

    public void soundMode(String morseString, int LongTime, int ShortTime, int TermTime, int soundTrackNum) {
        //Log.i(TAG, "소리 On, morseString : " + morseString);
        MosSound mosSound = new MosSound(getApplicationContext());  // 소리를 사용할 수 있는 객체를 생성
        long[] customTimings_sound = new long[resultText.length()];
        int[] customOnOff_sound = new int[resultText.length()];  // 1 = on, 0 = off
        int tempCount = 0;

        for (char temp : morseString.toCharArray()) {  // 모스 부호 분석해서 타이밍 맞추기
            //Log.i(TAG, "temp : " + temp);
            if (temp == '-') {
                customTimings_sound[tempCount] = LongTime;
                customOnOff_sound[tempCount] = 1;
            } else if (temp == '*') {
                customTimings_sound[tempCount] = ShortTime;
                customOnOff_sound[tempCount] = 1;
            } else {
                customTimings_sound[tempCount] = ShortTime;
                customOnOff_sound[tempCount] = 0;
            }
            tempCount += 1;
        }

        mosSound.SoundSetting(soundTrackNum);
        mosSound.SoundPlay(customTimings_sound, customOnOff_sound, TermTime);
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

    // 결과 업데이트 함수 ==========================================================================
    private void UpdateUI(StringBuffer result) {
        resultText.setText(result);
        Log.i(TAG, "Update Data : " + result);
        Log.i(TAG, "===========================================================");
    }

    // 옵션 메뉴 ===================================================================================
    Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        this.menu = menu;

        // 설정값 세팅
        getPrefSettings();  // 메뉴가 생성될 때 설정값 세팅함

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.light:
                // 플래시 기능 확인
                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    //Log.i(TAG, "Light 기능 있음");
                } else {  // 플래시 기능이 없을 때 접근시 실행
                    Toast.makeText(getApplicationContext(), "라이트 기능이 없습니다.", Toast.LENGTH_SHORT).show();
                    //Log.i(TAG, "Light 기능 없음");
                    break;
                }

                // 카메라 권한 확인 (빛 이용을 위해)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    cameraVersion = 1;
                } else {
                    cameraVersion = 2;
                }
                String permission[] = {Manifest.permission.CAMERA};
                boolean lightGranted = false;

                if (cameraVersion == 1) {  // 21 버전 이하는 카메라 권한이 필요함 (카메라 1 API 사용)
                    lightGranted = (checkSelfPermission(permission[0]) == PackageManager.PERMISSION_GRANTED);
                    if (!lightGranted) {
                        requestPermissions(permission, 1);  // 권한 신청 (카메라)
                    }
                } else if(cameraVersion == 2){  // 그 이상은 카메라 권한이 필요없음 (카메라 2 API 사용)
                    lightGranted = true;
                }

                if (lightGranted) {  // 카메라 권한이 있을 경우
                    if (lightEnable) {
                        item.setIcon(R.drawable.ic_light_off);
                        lightEnable = false;
                    } else {
                        item.setIcon(R.drawable.ic_light_on);
                        lightEnable = true;
                    }
                } else {  // 권한이 없을 경우
                    item.setIcon(R.drawable.ic_light_off);
                    lightEnable = false;
                }
                break;
            case R.id.vib:
                if (vibeEnable) {
                    item.setIcon(R.drawable.ic_vibe_off);
                    vibeEnable = false;
                } else {
                    item.setIcon(R.drawable.ic_vibe_on);
                    vibeEnable = true;
                }
                break;
            case R.id.sound:
                if (soundEnable) {
                    item.setIcon(R.drawable.ic_sound_off);
                    soundEnable = false;
                } else {
                    item.setIcon(R.drawable.ic_sound_on);
                    soundEnable = true;
                }
                break;
            case R.id.new_info:
                intent = new Intent(this, InfoActivity.class);
                startActivity(intent);
                break;
            case R.id.Settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        return true;
    }

    // 사용자 설정 세팅 ============================================================================
    private void setPrefSettings() {  // 저장하기
        if (savedSave) {  // 진동, 소리, 라이트 등 설정
            editor = sharedPreferences.edit();
            editor.putBoolean("vibeEnable", vibeEnable);
            editor.putBoolean("lightEnable", lightEnable);
            editor.putInt("cameraVersion", cameraVersion);  // 라이트 사용 가능할 때 카메라 버전도 저장함
            editor.putBoolean("soundEnable", soundEnable);
            Log.i(TAG, "vibeEnable : " + vibeEnable + ", lightEnable : " + lightEnable + "(cameraVersion : " + cameraVersion + "), soundEnable : " + soundEnable);
            editor.commit();
        }
    }

    private void getPrefSettings() {  // 불러오기
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        savedLanguage = sharedPreferences.getString("language", "0");
        savedPlayLength = sharedPreferences.getInt("play_len", 1);
        savedSave = sharedPreferences.getBoolean("save", false);
        Log.i(TAG, "savedLanguage : " + savedLanguage +
                ", parseInt : " + Integer.parseInt(savedLanguage) + ", savedPlayLength : " + savedPlayLength + ", savedSave : " + savedSave);

        spinner.setSelectedIndex(Integer.parseInt(savedLanguage));  // 기본 언어 설정 (스피너 글씨만 세팅)
        languageState = languageAvailable[Integer.parseInt(savedLanguage)];  // 기본 언어 설정 (변환할 때 사용됨)
        speed = savedPlayLength + 3;  // 재생 길이 설정 (+는 시간 조금 더 늦추기 위함, 너무 빠르면 소리 재생이 안됌)

        if (savedSave) {  // 진동, 소리, 라이트 등 설정
            lightEnable = sharedPreferences.getBoolean("lightEnable", false);
            cameraVersion = sharedPreferences.getInt("cameraVersion", 0);  // 초기값 0번은 카메라 사용불가능한 값임
            setIcon(0, lightEnable);
            vibeEnable = sharedPreferences.getBoolean("vibeEnable", false);
            setIcon(1, vibeEnable);
            soundEnable = sharedPreferences.getBoolean("soundEnable", false);
            setIcon(2, soundEnable);
            Log.i(TAG, "vibeEnable : " + vibeEnable + ", lightEnable : " + lightEnable + "(cameraVersion : " + cameraVersion + "), soundEnable : " + soundEnable);
        }
    }

    private void setIcon(int setInt, boolean setBoolean) {  // 액션바 아이콘 세팅하기
        MenuItem[] item = new MenuItem[3];
        item[setInt] = menu.getItem(setInt);  // light = 0, vibe = 1, sound = 2

        if (setInt == 0) {
            if (setBoolean) {
                item[setInt].setIcon(R.drawable.ic_light_on);
            } else {
                item[setInt].setIcon(R.drawable.ic_light_off);
            }
        } else if (setInt == 1) {
            if (setBoolean) {
                item[setInt].setIcon(R.drawable.ic_vibe_on);
            } else {
                item[setInt].setIcon(R.drawable.ic_vibe_off);
            }
        } else if (setInt == 2) {
            if (setBoolean) {
                item[setInt].setIcon(R.drawable.ic_sound_on);
            } else {
                item[setInt].setIcon(R.drawable.ic_sound_off);
            }
        }
    }

    // 모드 변경 ===================================================================================
    private void ChangeMode(String getMode) {
        languageState = "";
        languageState = getMode;

        if (languageState.equals(languageAvailable[1]) || languageState.equals(languageAvailable[2]) ||
                languageState.equals(languageAvailable[3])) {  // 모스부호 -> 영어, 한국어, 숫자 등 으로 변환
            editText.setHint("Input Morse Data");
            editText.setText("");
            Toast.makeText(getApplicationContext(), "모스부호를 입력하세요.", Toast.LENGTH_SHORT).show();
        } else if (languageState.equals(languageAvailable[0])) {  // 영어, 한국어, 숫자 등 -> 모스부호로 변환
            editText.setHint("Input Data");
            editText.setText("");
            Toast.makeText(getApplicationContext(), "영어 또는 한국어를 입력하세요.", Toast.LENGTH_SHORT).show();
        }
        //Log.i(TAG, "languageState : " + languageState);
    }

    // Encoding ====================================================================================
    private StringBuffer EncodingDataToMorse(String str) {
        StringBuffer encode = new StringBuffer();
        String[] character = str.split("");  // 단어 단위로 분해

        if (str.equals("")) {
            Log.i(TAG, "EncodingDataToMorse : 데이터 없음");
            encode.append(noResultData);
            return encode;
        }

        for (int i = 0; i < character.length; i++) {
            Log.i(TAG, "character[" + i + "] : " + character[i]);  // 단어 단위 출력

            if(character[i].equals(" ")){  // 띄어쓰기일 경우
                encode.append(" ");
                continue;
            }

            boolean englishPattern = Pattern.matches("^[a-zA-Z]*$", character[i]);
            boolean koreanPattern = Pattern.matches("^[ㄱ-ㅎㅏ-ㅣ가-힣]*$", character[i]);
            boolean numberPattern = Pattern.matches("^[0-9]*$", character[i]);
            boolean specialCharacterPattern = Pattern.matches("^[.,?/\\-=:;()'\"!@+ ]*$", character[i]);
            Log.i(TAG, "englishPattern : " + englishPattern + ", koreanPattern : " + koreanPattern +
                    ", numberPattern : " + numberPattern + ", specialCharacterPattern : " + specialCharacterPattern);

            if (numberPattern) {  // 숫자 -> 모스부호 ----------------------------------------------
                for (String key : MorseNumber.keySet()) {
                    if (character[i].equals(key)) {
                        encode.append(MorseNumber.get(key)).append(" ");
                        Log.i(TAG, "number encode : " + encode);
                        break;
                    }
                }
            } else if (specialCharacterPattern) {  // 특수문자 -> 모스부호 -------------------------
                for (String key : MorseSpecial.keySet()) {
                    if (character[i].equals(key)) {
                        encode.append(MorseSpecial.get(key)).append(" ");
                        Log.i(TAG, "specialChar encode : " + encode);
                        break;
                    }
                }
            } else if (englishPattern) {  // 영어 -> 모스부호 --------------------------------------
                String engChar = character[i].toUpperCase();
                for (String key : MorseEnglish.keySet()) {
                    if (engChar.equals(key)) {
                        encode.append(MorseEnglish.get(key)).append(" ");
                        Log.i(TAG, "english encode : " + encode);
                        break;
                    }
                }
            } else if (koreanPattern) {  // 한글 -> 모스부호 ---------------------------------------
                char[] koreanChar = character[i].toCharArray();
                Log.i(TAG, "koreanChar : " + koreanChar[0]);

                int initialConsonantInt = ((koreanChar[0] - 0xAC00) / 28) / 21;  // 초성 인덱스 = ((한글 유니코드값 - 0xAC00) / 28) / 21
                int neuterConsonantInt = ((koreanChar[0] - 0xAC00) / 28) % 21;  // 중성 인덱스 = ((한글 유니코드값 - 0xAC00) / 28) % 21
                int finalConsonantInt = (koreanChar[0] - 0xAC00) % 28;  // 종성 인덱스 = (한글 유니코드값 - 0xAC00) % 28
                boolean koreanInitConsPattern = Pattern.matches("^[ㄱ-ㅎㅏ-ㅣ]*$", character[i]);
                Log.i(TAG, "initialConsonantInt : " + initialConsonantInt + ", neuterConsonantInt : " + neuterConsonantInt +
                        ", finalConsonantInt : " + finalConsonantInt + ", koreanInitConsPattern : " + koreanInitConsPattern);

                if (koreanInitConsPattern) {  // 초성 범위일 경우는 실행 (한글 범위를 말함) (미완성 한글일 때 : ㄱㄴㄷ)
                    for (String key : MorseKorean.keySet()) {
                        if (character[i].equals(key)) {
                            encode.append(MorseKorean.get(key)).append(" ");
                            Log.i(TAG, "korean encode : " + encode);
                            break;
                        }
                    }
                } else if (initialConsonantInt >= 0 && initialConsonantInt <= 18) {  // 초성 범위일 경우는 실행 (완성된 한글일 때 : 가나다)
                    String init = "" + Consonant(1, initialConsonantInt);
                    for (String key : MorseKorean.keySet()) {
                        if (init.equals(key)) {
                            encode.append(MorseKorean.get(key)).append(" ");
                            Log.i(TAG, "korean encode : " + encode);
                            break;
                        }
                    }
                } else {
                    encode.append("한글은 초성부터 입력해주세요.");
                    Log.i(TAG, "korean encode : " + encode);
                    break;
                }
                if (neuterConsonantInt >= 0 && neuterConsonantInt <= 20) {  // 중성 범위일 경우는 실행
                    String neu = "" + Consonant(2, neuterConsonantInt);
                    for (String key : MorseKorean.keySet()) {
                        if (neu.equals(key)) {
                            encode.append(MorseKorean.get(key)).append(" ");
                            Log.i(TAG, "korean encode : " + encode);
                            break;
                        }
                    }
                }
                if (finalConsonantInt >= 0 && finalConsonantInt <= 27) {  // 종성 범위일 경우는 실행
                    String fin = Consonant(3, finalConsonantInt);
                    for (String key : MorseKorean.keySet()) {
                        if (fin != null && fin.equals(key)) {
                            encode.append(MorseKorean.get(key)).append(" ");
                            Log.i(TAG, "korean encode : " + encode);
                            break;
                        }
                    }
                }
            } else {  // 모스부호로 바꿀 수 있는 매칭이(데이터) 없을 때 ----------------------------
                encode.delete(0, encode.length());
                encode.append("Not supported or not found in the data.");
                break;
            }
        }

        Log.i(TAG, "EncodingDataToMorse() -> Result Encode Value : " + encode);
        Log.i(TAG, "-----------------------------------------------------------");
        return encode;
    }

    // Decoding ====================================================================================
    private StringBuffer DecodingMorseToData(String str) {
        String[] txt = str.split("");
        String assembleString = "";
        String checkKorStr = "";  // 한국어 임시 보관용 (변환된 한국 초,중,종성을 보관하고있음)
        StringBuffer decode = new StringBuffer();  // 디코딩 결과 저장

        boolean morsePattern = false;

        // 모스패턴 검사 코드
        for (int i = 0; i < txt.length; i++) {
            //Log.e(TAG, "txt" + i + ": " + txt[i]);
            assembleString += txt[i];
            morsePattern = Pattern.matches("^[*-]*$", txt[i]);  // 하나씩 검사함 (모스패턴이면 true 아니면 false)

            if (txt[i].equals(" ")) {  // 띄어쓰기일 경우 띄어줌
                morsePattern = true;  // 띄어쓰기는 검사가 안되서 직접 바꿔줌
            } else if (!morsePattern) {  // 모스부호 패턴이 아니면 break
                break;
            }
        }
        Log.i(TAG, "assembleString : " + assembleString + ", morsePattern : " + morsePattern);

        if (!morsePattern) {  // '*', '-', ' ' 이외의 문자를 사용한 경우
            Log.i(TAG, "existMorsePattern : Out of Characters");
            decode.append("Please input only \'*\', \'-\' or \' \' characters.");
            return decode;
        } else if (assembleString.equals("")) {
            Log.i(TAG, "assembleString : 데이터 없어서 '-' 반환");
            decode.append(noResultData);
            return decode;
        }

        if (morsePattern) {  // 모스 패턴이면 실행
            String[] getSlice = assembleString.split(" ");

            for (int i = 0; i < getSlice.length; i++) {
                Log.i(TAG, "getSlice.length : " + getSlice.length + ", getSlice[" + i + "] : " + getSlice[i]);
                if (getSlice[i].equals("")) {  // 띄어쓰기는 지워지고 배열이 증가하여 다시 띄어쓰기를 넣어줌
                    decode.append(" ");
                    continue;
                }
                String KorValue = "";

                if (languageState.equals(languageAvailable[1])) {  // 모스부호 -> 영어 ===========================
                    Log.i(TAG, "languageState : 영어");
                    if (MorseEnglish.containsValue(getSlice[i])) {  // 영어일 경우 (영어에 값이 있을 경우, 키 말고 값임)
                        for (String key : MorseEnglish.keySet()) {
                            //Log.i(TAG, "key : " + key + ", MorseEnglish : " + MorseEnglish.get(key));
                            if (getSlice[i].equals(MorseEnglish.get(key))) {
                                Log.i(TAG, "MorseEnglish : " + key);
                                decode.append(key);
                                break;
                            }
                        }
                    } else if (MorseSpecial.containsValue(getSlice[i])) {  // 특수문자일 경우
                        for (String key : MorseSpecial.keySet()) {
                            if (getSlice[i].equals(MorseSpecial.get(key))) {
                                Log.i(TAG, "MorseSpecial : " + key);
                                decode.append(key);
                                break;
                            }
                        }
                    } else {  // * 또는 - 문자이지만 데이터에 없는 경우
                        decode.delete(0, decode.length());
                        decode.append("Not supported or not found in the data.");
                        break;
                    }
                } else if (languageState.equals(languageAvailable[2])) {  // 모스부호 -> 한국어 ================
                    Log.i(TAG, "languageState : 한국어");
                    if (MorseKorean.containsValue(getSlice[i])) {  // 한국어일 경우
                        for (String key : MorseKorean.keySet()) {
                            if (getSlice[i].equals(MorseKorean.get(key))) {
                                Log.i(TAG, "MorseKorean : " + key);
                                KorValue = key;
                                break;
                            }
                        }

                        // 초성+초성 (ㄱ + ㄱ => ㄲ), 중성+중성 (ㅑ + ㅣ => ㅒ), 종성+종성 (ㄱ + ㅅ => ㄳ) 조합
                        if ((i + 1) < getSlice.length) {  // 글자 조합을 위해서 하나 더 가져와서 조합 가능한지 확인함
                            for (String key : MorseKorean.keySet()) {
                                if ((getSlice[i] + " " + getSlice[i + 1]).equals(MorseKorean.get(key))) {  // 조합한 글자가 매칭되면
                                    Log.i(TAG, "MorseKorean : " + key);
                                    KorValue = key;
                                    i++;  // 2개를 합쳐서 읽고 값을 저장함, 2개를 읽었으니 +1 카운트함
                                    break;
                                }
                            }
                        }
                        Log.i(TAG, "KorValue(현재 값) : " + KorValue);
                        checkKorStr += KorValue;  // 현재 값도 저장함
                    } else if (MorseSpecial.containsValue(getSlice[i])) {  // 특수문자일 경우
                        for (String key : MorseSpecial.keySet()) {
                            if (getSlice[i].equals(MorseSpecial.get(key))) {
                                Log.i(TAG, "MorseSpecial : " + key);
                                checkKorStr += key;
                                break;
                            }
                        }
                    } else {  // * 또는 - 문자이지만 데이터에 없는 경우
                        decode.delete(0, decode.length());
                        decode.append("Not supported or not found in the data.");
                        break;
                    }

                    if ((i + 1) == (getSlice.length)) {  // 마지막 루프에서 한번에 문자 조합
                        Log.i(TAG, "checkKorStr(전체 문자) : " + checkKorStr);
                        Log.i(TAG, "AssembleConsonant() run");
                        decode.append(AssembleConsonant(checkKorStr));  // 초성 + 중성 + 종성 조합
                        Log.i(TAG, "AssembleConsonant() result : " + decode);
                    }
                } else if (languageState.equals(languageAvailable[3])) {  // 모스부호 -> 숫자 ================
                    Log.i(TAG, "languageState : 숫자");
                    if (MorseNumber.containsValue(getSlice[i])) {  // 숫자일 경우
                        for (String key : MorseNumber.keySet()) {
                            if (getSlice[i].equals(MorseNumber.get(key))) {
                                Log.i(TAG, "MorseNumber : " + key);
                                decode.append(key);
                                break;
                            }
                        }
                    } else if (MorseSpecial.containsValue(getSlice[i])) {  // 특수문자일 경우
                        for (String key : MorseSpecial.keySet()) {
                            if (getSlice[i].equals(MorseSpecial.get(key))) {
                                Log.i(TAG, "MorseSpecial : " + key);
                                decode.append(key);
                                break;
                            }
                        }
                    } else {  // * 또는 - 문자이지만 데이터에 없는 경우
                        decode.delete(0, decode.length());
                        decode.append("Not supported or not found in the data.");
                        break;
                    }
                }
            }
        }

        Log.i(TAG, "DecodingMorseToData() -> Result Decode Value : " + decode);
        Log.i(TAG, "-----------------------------------------------------------");
        return decode;
    }

    // 한글 조합 ===================================================================================
    private String AssembleConsonant(String recordTxt) {
        Log.i(TAG, "recordTxt : " + recordTxt);

        recordTxt = recordTxt + "    ";
        String[] checkStr = recordTxt.split("");  // 이전 값만 따로 저장해 놓음
        String[] tempTxt = {"", "", "", ""};
        String resultText = "";

        for (int numCount = 3; numCount < checkStr.length - 1; numCount++) {  // 글자를 하나씩 불러옴 (decodeEnd 안에 값이 한글임)
            tempTxt[0] = checkStr[numCount - 3];
            tempTxt[1] = checkStr[numCount - 2];
            tempTxt[2] = checkStr[numCount - 1];
            tempTxt[3] = checkStr[numCount];
            Log.i(TAG, "tempTxt[0] : " + tempTxt[0] + ", tempTxt[1] : " + tempTxt[1] +
                    ", tempTxt[2] : " + tempTxt[2] + ", tempTxt[3] : " + tempTxt[3] + ", numCount : " + numCount +
                    ", checkStr.length-1 : " + (checkStr.length - 1));

            int first = Consonant(1, tempTxt[0]);  // 초성
            int second = Consonant(2, tempTxt[1]);  // 중성
            int third = Consonant(3, tempTxt[2]);  // 종성
            boolean fourthBoolean = Pattern.matches("^[ㅏ-ㅣ]*$", tempTxt[3]);  // 4번째가 중성인가
            Log.i(TAG, "first : " + first + ", second : " + second + ", third : " + third + ", fourthBoolean : " + fourthBoolean);
            String tempText;  // 한국어 임시 보관용

            // 초성 범위 0~18, 총 19개, 중성 범위 0~20, 총 21개, 종성 범위 0~27, 총 28개
            // 초성으로 시작시 실행
            if (0 <= first && first <= 18) {  // 초성
                if (0 <= second && second <= 20) {  // 중성
                    if (0 <= third && third <= 27) {  // 종성
                        if (fourthBoolean) {  // 4번째 값이 중성이 올 예정
                            char getCon = (char) (0xAC00 + (first * 21 * 28) + (second * 28));
                            Log.i(TAG, "conChar -> (초+중+종) + 중 : " + getCon);
                            tempText = Character.toString(getCon);
                            numCount += 1;
                        } else {  // 4번째 값이 중성이 안 올 예정
                            char getCon = (char) (0xAC00 + (first * 21 * 28) + (second * 28) + third);
                            Log.i(TAG, "conChar -> (초+중+종) + 초 : " + getCon);
                            tempText = Character.toString(getCon);
                            numCount += 2;
                        }
                    } else {  // 3번째 값이 종성이 안 올 예정
                        if (third == 100) third = 0;
                        char getCon = (char) (0xAC00 + (first * 21 * 28) + (second * 28) + third);
                        Log.i(TAG, "conChar -> (초+중) + 중 : " + getCon);
                        tempText = Character.toString(getCon);
                        numCount += 1;
                    }
                } else {  // 초성 다음 중성이 안올 때
                    tempText = tempTxt[0];
                }
            } else {  // 시작이 초성이 아닐때 실행
                tempText = tempTxt[0];
            }
            resultText += tempText;  // 조합이 끝난 후 저장
        }

        Log.i(TAG, "resultText : " + resultText);  // 최종 결과 값
        return resultText;
    }

    // 한글
    static String[] initialConsonant = {"ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};
    static String[] neutralConsonant = {"ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"};
    static String[] finalConsonant = {"", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};

    // 한글 인덱스 (초성 19개, 중성21개, 종성28개) =================================================
    // where = 초성,중성,종성, index = 몇번째 배열 (인덱스를 통해서 문자를 찾음)
    private String Consonant(int where, int index) {
        switch (where) {
            case 1:
                return initialConsonant[index];
            case 2:
                return neutralConsonant[index];
            case 3:
                return finalConsonant[index];
            default:
                return null;
        }
    }

    // where = 초성,중성,종성, findTxt = 몇번째인지 찾을 문자 (문자를 통해서 인덱스 값 찾음)
    private int Consonant(int where, String findTxt) {
        switch (where) {
            case 1:
                for (int i = 0; i < initialConsonant.length; i++) {
                    if (findTxt.equals(initialConsonant[i])) {
                        return i;
                    }
                }
                break;
            case 2:
                for (int i = 0; i < neutralConsonant.length; i++) {
                    if (findTxt.equals(neutralConsonant[i])) {
                        return i;
                    }
                }
                break;
            case 3:
                for (int i = 0; i < finalConsonant.length; i++) {
                    if (findTxt.equals(finalConsonant[i])) {
                        return i;
                    }
                }
                break;
        }

        return 100;  // 배열에 없는 경우 없는 인덱스 번호를 넘겨줌
    }

    // UI 업데이트 스레드 ==========================================================================
    public class UpdateThread extends Thread {  // 다운로드 작업을 위한 스레드입니다.
        public void run(String str) {  // 스레드에서 동작
            try {
                StringBuffer resultDTM, resultMTD;

                if (languageState.equals(languageAvailable[1]) || languageState.equals(languageAvailable[2]) ||
                        languageState.equals(languageAvailable[3])) {
                    resultMTD = DecodingMorseToData(str);
                    UpdateUI(resultMTD);
                } else {
                    resultDTM = EncodingDataToMorse(str);
                    UpdateUI(resultDTM);
                }
            } catch (Exception e) {
                Log.i(TAG, "스레드 예외 발생");
                e.printStackTrace();
            }
        }
    }

    // 광고 ========================================================================================
    private void Ad(){
        final String adTag = "Ad()";
        MobileAds.initialize(this, getString(R.string.banner_ad_unit_id_for_test));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // 광고가 제대로 로드 되는지 테스트 하기 위한 코드
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i(adTag, "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.i(adTag, "onAdFailedToLoad " + errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });
    }

    // 뒤로가기 2번 누름 ===========================================================================
    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();  // Back Key를 2번 눌러서 앱 종료
    }

    private class BackPressCloseHandler {
        private long backKeyPressedTime = 0;
        private Toast toast;
        private Activity activity;

        private BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        private void onBackPressed() {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide_Default();

                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                activity.finish();
                toast.cancel();
            }
        }

        private void showGuide_Default() {
            toast = Toast.makeText(activity, "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // 생명주기 ====================================================================================
    @Override
    protected void onRestart() {
        super.onRestart();
        getPrefSettings();  // 세팅값 불러옴
    }

    @Override
    protected void onStop() {
        super.onStop();
        setPrefSettings();  // 세팅값 저장함
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
