package com.steganowork.morsecode;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.HashMap;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    private static HashMap<String, String> MorseKorean;
    private static HashMap<String, String> MorseEnglish;
    private static HashMap<String, String> MorseNumber;
    private static HashMap<String, String> MorseSpecial;
    TextView modeText;
    TextView resultText;
    EditText editText;

    Boolean changeLanguage = false;
    String languageState = "";
    String[] languageAvailable;

    private UpdateThread mUpdateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   // 화면꺼짐 방지

        // 필요한 변수 선언
        editText = (EditText) findViewById(R.id.editText);
        modeText = (TextView) findViewById(R.id.textView4);
        resultText = (TextView) findViewById(R.id.textView7);

        MorseKorean = new HashMap<String, String>();
        MorseEnglish = new HashMap<String, String>();
        MorseNumber = new HashMap<String, String>();
        MorseSpecial = new HashMap<String, String>();

        InputMorseCode();  // 해시 코드값 넣기 (모스 데이터 삽입)
        languageAvailable = getResources().getStringArray(R.array.LAN);  // 가능한 언어

        // 실시간 번역
        mUpdateThread = new UpdateThread();  // 스레드 생성
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 입력하기 전에
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = charSequence.toString();
                mUpdateThread.run(str);  // 입력 값이 바뀔 때마다 UI 업데이트 시켜줌
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 입력이 끝났을 때
            }
        });
    }

    // 결과 업데이트 함수
    private void UpdateUI(String result) {
        if (result.equals("")) {  // 데이터가 없을 때 기본 출력값 지정
            result = "-";
        }
        resultText.setText(result);
    }
    // 옵션 메뉴 ===================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.change:
                ChangeMode();  // 모드 변경
                break;
            case R.id.new_info:
                String url = "https://ko.wikipedia.org/wiki/%EB%AA%A8%EC%8A%A4_%EB%B6%80%ED%98%B8";
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                break;
            case R.id.Settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        return true;
    }

    // 모드 변경 ===================================================================================
    private void ChangeMode() {
        languageState = "";
        changeLanguage = !changeLanguage;

        if (changeLanguage) {  // 모스부호 -> 영어, 한국어 등 으로 변환
            modeText.setText("한영 결과");
            editText.setHint("Input Morse Data");
            editText.setText("");
            languageState = languageAvailable[0];

            // 클릭 시 모드 변경 가능하게
            modeText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("출력할 언어 선택");
                    builder.setItems(R.array.LAN, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int pos) {
                            String[] items = getResources().getStringArray(R.array.LAN);
                            Toast.makeText(getApplicationContext(), items[pos], Toast.LENGTH_SHORT).show();
                            languageState = items[pos];
                            modeText.setText(items[pos] + " 결과");
                            //Log.i(TAG, "languageState : " + languageState);
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });

            Toast.makeText(getApplicationContext(), "모스부호를 입력하세요.", Toast.LENGTH_SHORT).show();
        } else {  // 영어, 한국어 등 -> 모스부호로 변환
            modeText.setText("모스부호 결과");
            editText.setHint("Input Data");
            editText.setText("");

            Toast.makeText(getApplicationContext(), "영어 또는 한국어를 입력하세요.", Toast.LENGTH_SHORT).show();
        }
        //Log.i(TAG, "languageState : " + languageState);
    }

    // Encoding ====================================================================================
    private String EncodingDataToMorse(String str) {
        String encode = "";
        String[] txt = str.split("");

        for (int i = 0; i < txt.length; i++) {
            String[] temp = txt[i].split("");

            for (int j = 1; j < temp.length; j++) {  // temp[0] 배열에는 공백이 들어가있어서 1부터 시작함
                boolean englishPattern = Pattern.matches("^[a-zA-Z]*$", temp[j]);
                boolean koreanPattern = Pattern.matches("^[ㄱ-ㅎ가-힣]*$", temp[j]);
                boolean numberPattern = Pattern.matches("^[0-9]*$", temp[j]);
                boolean specialCharacterPattern = Pattern.matches("^[.,?/\\-=:;()'\"!@+ ]*$", temp[j]);

                Log.i(TAG, "char : " + temp[j]);
                Log.i(TAG, "englishPattern : " + englishPattern);
                Log.i(TAG, "koreanPattern : " + koreanPattern);
                Log.i(TAG, "numberPattern : " + numberPattern);
                Log.i(TAG, "specialCharacterPattern : " + specialCharacterPattern);

                if (englishPattern) {  // 영어 -> 모스부호
                    temp[j] = temp[j].toLowerCase();
                    for (String key : MorseEnglish.keySet()) {
                        if (temp[j].equals(MorseEnglish.get(key))) {
                            encode += key + " ";
                            Log.i(TAG, "english encode : " + encode);
                            break;
                        }
                    }
                } else if (koreanPattern) {  // 한글 -> 모스부호
                    char[] koreanChar = temp[j].toCharArray();
                    Log.i(TAG, "koreanChar : " + koreanChar[0]);

                    /*
                    초성 인덱스 = ((한글 유니코드값 - 0xAC00) / 28) / 21
                    중성 인덱스 = ((한글 유니코드값 - 0xAC00) / 28) % 21
                    종성 인덱스 = (한글 유니코드값 - 0xAC00) % 28
                    한글 유니코드 조합하기 = 0xAC00(처음 한글 시작값) + (초성 인덱스 x 21 x 28 ) + (중성 인덱스 x 28) + 종성 인덱스
                    */
                    int initialConsonantInt = ((koreanChar[0] - 0xAC00) / 28) / 21;
                    int neuterConsonantInt = ((koreanChar[0] - 0xAC00) / 28) % 21;
                    int finalConsonantInt = (koreanChar[0] - 0xAC00) % 28;
                    Log.i(TAG, "initialConsonantInt : " + initialConsonantInt);
                    Log.i(TAG, "neuterConsonantInt : " + neuterConsonantInt);
                    Log.i(TAG, "finalConsonantInt : " + finalConsonantInt);
                    //char conChar = (char)(0xAC00 + (initialConsonant * 21 * 28 ) + (neuterConsonant * 28) + finalConsonant);
                    //Log.i(TAG, "con Char : " + conChar);

                    boolean koreanInitConsPattern = Pattern.matches("^[ㄱ-ㅎ]*$", temp[j]);
                    Log.i(TAG, "koreanInitConsPattern : " + koreanInitConsPattern);

                    if (koreanInitConsPattern) {  // 초성 범위일 경우는 실행 (한글 범위를 말함) (미완성 한글일 때 : ㄱㄴㄷ)
                        for (String key : MorseKorean.keySet()) {
                            if (temp[j].equals(MorseKorean.get(key))) {
                                encode += key + " ";
                                Log.i(TAG, "korean encode : " + encode);
                                break;
                            }
                        }
                    } else if (initialConsonantInt >= 0 && initialConsonantInt <= 18) {  // 초성 범위일 경우는 실행 (완성된 한글일 때 : 가나다)
                        String init = "" + Consonant(1, initialConsonantInt);
                        for (String key : MorseKorean.keySet()) {
                            if (init.equals(MorseKorean.get(key))) {
                                encode += key + " ";
                                Log.i(TAG, "korean encode : " + encode);
                                break;
                            }
                        }
                    }
                    if (neuterConsonantInt >= 0 && neuterConsonantInt <= 20) {  // 중성 범위일 경우는 실행
                        String neu = "" + Consonant(2, neuterConsonantInt);
                        for (String key : MorseKorean.keySet()) {
                            if (neu.equals(MorseKorean.get(key))) {
                                encode += key + " ";
                                Log.i(TAG, "korean encode : " + encode);
                                break;
                            }
                        }
                    }
                    if (finalConsonantInt >= 0 && finalConsonantInt <= 27) {  // 종성 범위일 경우는 실행
                        String fin = Consonant(3, finalConsonantInt);
                        for (String key : MorseKorean.keySet()) {
                            if (fin.equals(MorseKorean.get(key))) {
                                encode += key + " ";
                                Log.i(TAG, "korean encode : " + encode);
                                break;
                            }
                        }
                    }
                } else if (numberPattern) {  // 숫자 -> 모스부호
                    for (String key : MorseNumber.keySet()) {
                        if (temp[j].equals(MorseNumber.get(key))) {
                            encode += key + " ";
                            Log.i(TAG, "number encode : " + encode);
                            break;
                        }
                    }
                } else if (specialCharacterPattern) {  // 특수문자 -> 모스부호
                    for (String key : MorseSpecial.keySet()) {
                        if (temp[j].equals(MorseSpecial.get(key))) {
                            encode += key + " ";
                            Log.i(TAG, "specialChar encode : " + encode);
                            break;
                        }
                    }
                } else {
                    encode = "Not supported or not found in the data.";
                }
            }
        }

        Log.i(TAG, "EncodingDataToMorse() -> Result Encode Value : " + encode);
        return encode;
    }

    // Decoding ====================================================================================
    private String DecodingMorseToData(String str) {
        String[] txt = str.split("");
        String assembleString = "";
        String recordTxt = "";  // 한국어 임시 보관용 (변환된 한국 초,중,종성을 보관하고있음)
        String decode = "";  // 디코딩 결과 저장

        boolean morsePattern = false;
        boolean existMorsePattern = false;

        // 모스패턴 검사 코드
        for (int i = 1; i < txt.length; i++) {
            //Log.e(TAG, "txt" + i + ": " + txt[i]);
            morsePattern = Pattern.matches("^[*-]*$", txt[i]);  // 하나씩 검사해서
            if (txt[i].equals(" ")) {
                assembleString += " ";
                morsePattern = true;  // 스페이스바는 검사가 안되서 직접 바꿔줌
            } else if (morsePattern) {
                assembleString += txt[i];
            } else {
                existMorsePattern = !morsePattern;
                break;  // 모스 패턴이 아니면 Decode 중단
            }
        }
        Log.i(TAG, "assembleString : " + assembleString + ", morsePattern : " + morsePattern);

        if (morsePattern) {  // 모스 패턴이면 실행
            String[] getSlice = assembleString.split(" ");

            for (int i = 0; i < getSlice.length; i++) {
                Log.i(TAG, "getSlice" + i + ": " + getSlice[i]);
                if (getSlice[i].equals("")) {  // 스페이스를 짤라서 넣기때문에 스페이스를 다시 넣어줌
                    //Log.i(TAG, "Insult SpaceBar");
                    getSlice[i] = " ";
                }

                String getNumValue = "" + MorseNumber.get(getSlice[i]);
                String getSpeValue = "" + MorseSpecial.get(getSlice[i]);
                String getEngValue = "" + MorseEnglish.get(getSlice[i]);  // 널을 막기위해서 ""로 지정함
                String getKorValue = "" + MorseKorean.get(getSlice[i]);  // 널을 막기위해서 ""로 지정함
                //Log.i(TAG, "getEngValue : " + getEngValue);
                //Log.i(TAG, "getSpeValue : " + getSpeValue);

                if (languageState.equals("영어")) {  // 모스부호 -> 영어
                    if (getEngValue.equals(MorseEnglish.get(getSlice[i]))) {  // 영어일 경우
                        Log.i(TAG, "getEngValue : " + getEngValue);
                        decode += getEngValue;
                    } else if (getNumValue.equals(MorseNumber.get(getSlice[i]))) {  // 숫자일 경우
                        Log.i(TAG, "getNumValue : " + getNumValue);
                        decode += getNumValue;
                    } else if (getSpeValue.equals(MorseSpecial.get(getSlice[i]))) {  // 특수문자일 경우
                        Log.i(TAG, "MorseSpecial : " + getSpeValue);
                        decode += getSpeValue;
                    } else {  // * 또는 - 문자이지만 데이터에 없는 경우
                        decode = "Not supported or not found in the data.";
                    }
                    Log.i(TAG, "decode : " + decode);
                } else if (languageState.equals("한국어")) {  // 모스부호 -> 한국어
                    if (getKorValue.equals(MorseKorean.get(getSlice[i]))) {  // 한국어일 경우
                        String getKorValueCom;

                        if ((i + 1) < getSlice.length) {  // 글자 조합을 위해서 하나 더 가져와서 조합 가능한지 확인함
                            // 초성+초성 (ㄱ + ㄱ => ㄲ), 중성+중성 (ㅑ + ㅣ => ㅒ), 종성+종성 (ㄱ + ㅅ => ㄳ) 조합
                            getKorValueCom = "" + MorseKorean.get((getSlice[i] + " " + getSlice[i + 1]));  // 두 성조를 조합해봄
                            if (getKorValueCom.equals(MorseKorean.get(getSlice[i] + " " + getSlice[i + 1]))) {  // 조합한 성조가 매칭이 되면
                                Log.i(TAG, "getKorValueCom : " + getKorValueCom);
                                getKorValue = getKorValueCom;  // 2개를 합쳐서 읽고 값을 저장함, 2개를 읽었으니 +1 카운트함
                                i++;
                            }
                        }

                        Log.i(TAG, "getKorValue(현재 값) : " + getKorValue);
                        recordTxt += getKorValue;  // 현재 값도 저장함
                    } else if (getNumValue.equals(MorseNumber.get(getSlice[i]))) {  // 숫자일 경우
                        Log.i(TAG, "getNumValue : " + getNumValue);
                        recordTxt += getNumValue;
                    } else if (getSpeValue.equals(MorseSpecial.get(getSlice[i]))) {  // 특수문자일 경우
                        Log.i(TAG, "MorseSpecial : " + getSpeValue);
                        recordTxt += getSpeValue;
                    } else {  // * 또는 - 문자이지만 데이터에 없는 경우
                        decode = "Not supported or not found in the data.";
                    }

                    if (i == (getSlice.length - 1)) {  // 마지막에만 한번에 조합
                        Log.i(TAG, "recordTxt(전체 문자) : " + recordTxt);
                        Log.i(TAG, "AssembleConsonant() run");
                        decode += AssembleConsonant(recordTxt);  // 초성 + 중성 + 종성 조합
                        Log.i(TAG, "AssembleConsonant() result : " + decode);
                    }
                }
            }
        } else if (existMorsePattern) {  // '*', '-', ' ' 이외의 문자를 사용한 경우
            Log.i(TAG, "Out of Characters");
            decode = "Please input only \"*\" or \"-\" characters.";
        }

        Log.i(TAG, "DecodingMorseToData() -> Result Decode Value : " + decode);
        Log.i(TAG, "===========================================================");
        return decode;
    }

    // 한글
    static String[] initialConsonant = {"ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};
    static String[] neutralConsonant = {"ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"};
    static String[] finalConsonant = {"", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};

    // 한글 조합 ===================================================================================
    private String AssembleConsonant(String recordTxt) {
        recordTxt += "    ";
        String[] decodeEnd = recordTxt.split("");  // 이전 값만 따로 저장해 놓음
        String[] tempTxt = {"", "", "", ""};
        String resultText = "";

        int numCount = decodeEnd.length - 1;  // 문자 개수
        for (int k = 4; k <= numCount; k++) {  // 글자를 하나씩 불러옴 (decodeEnd 안에 값이 한글임)
            tempTxt[0] = decodeEnd[k - 3];
            tempTxt[1] = decodeEnd[k - 2];
            tempTxt[2] = decodeEnd[k - 1];
            tempTxt[3] = decodeEnd[k];
            Log.i(TAG, "tempTxt[0] : " + tempTxt[0] + ", tempTxt[1] : " + tempTxt[1] +
                    ", tempTxt[2] : " + tempTxt[2] + ", tempTxt[3] : " + tempTxt[3] +
                    ", numCount : " + numCount + ", k : " + k);

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
                    if (fourthBoolean) {  // 4번째 값이 중성이 올 예정
                        char getCon = (char) (0xAC00 + (first * 21 * 28) + (second * 28));
                        Log.i(TAG, "conChar -> 초+중+종 + 중 : " + getCon);
                        tempText = Character.toString(getCon);
                        k += 1;
                    } else {  // 4번째 값이 중성이 안 올 예정
                        if (third == 100) third = 0;
                        char getCon = (char) (0xAC00 + (first * 21 * 28) + (second * 28) + third);
                        Log.i(TAG, "conChar -> 초+중+종 + 초 : " + getCon);
                        tempText = Character.toString(getCon);
                        k += 2;
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


    // 스레드 클래스 처리 ==========================================================================
    // 1번 스레드
    public class UpdateThread extends Thread {  // 다운로드 작업을 위한 스레드입니다.
        public void run(String str) {  // 스레드에서 동작
            try {
                String resultDTM, resultMTD;

                if (changeLanguage) {
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

    // 생명주기 ====================================================================================
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 모스부호 데이터 =============================================================================
    static private void InputMorseCode() {
        // 모스 부호 -> 한국어
        MorseKorean.put("*-**", "ㄱ");
        MorseKorean.put("*-** *-**", "ㄲ");
        MorseKorean.put("*-** --*", "ㄳ");
        MorseKorean.put("**-*", "ㄴ");
        MorseKorean.put("**-* *--*", "ㄵ");
        MorseKorean.put("**-* *---", "ㄶ");
        MorseKorean.put("-***", "ㄷ");
        MorseKorean.put("-*** -***", "ㄸ");
        MorseKorean.put("***-", "ㄹ");
        MorseKorean.put("***- *-**", "ㄺ");
        MorseKorean.put("***- --", "ㄻ");
        MorseKorean.put("***- *--", "ㄼ");
        MorseKorean.put("***- --*", "ㄽ");
        MorseKorean.put("***- --**", "ㄾ");
        MorseKorean.put("***- ---", "ㄿ");
        MorseKorean.put("***- *---", "ㅀ");
        MorseKorean.put("--", "ㅁ");
        MorseKorean.put("*--", "ㅂ");
        MorseKorean.put("*-- *--", "ㅃ");
        MorseKorean.put("*-- --*", "ㅄ");
        MorseKorean.put("--*", "ㅅ");
        MorseKorean.put("--* --*", "ㅆ");
        MorseKorean.put("-*-", "ㅇ");
        MorseKorean.put("*--*", "ㅈ");
        MorseKorean.put("*--* *--*", "ㅉ");
        MorseKorean.put("-*-*", "ㅊ");
        MorseKorean.put("-**-", "ㅋ");
        MorseKorean.put("--**", "ㅌ");
        MorseKorean.put("---", "ㅍ");
        MorseKorean.put("*---", "ㅎ");
        MorseKorean.put("*", "ㅏ");
        MorseKorean.put("**", "ㅑ");
        MorseKorean.put("-", "ㅓ");
        MorseKorean.put("***", "ㅕ");
        MorseKorean.put("*-", "ㅗ");
        MorseKorean.put("-*", "ㅛ");
        MorseKorean.put("****", "ㅜ");
        MorseKorean.put("*-*", "ㅠ");
        MorseKorean.put("-**", "ㅡ");
        MorseKorean.put("**-", "ㅣ");
        MorseKorean.put("-*--", "ㅔ");
        MorseKorean.put("--*-", "ㅐ");
        MorseKorean.put("*- *", "ㅘ");
        MorseKorean.put("*- --*-", "ㅙ");
        MorseKorean.put("*- **-", "ㅚ");
        MorseKorean.put("*** **-", "ㅖ");
        MorseKorean.put("** **-", "ㅒ");
        MorseKorean.put("**** -", "ㅝ");
        MorseKorean.put("**** -*--", "ㅞ");
        MorseKorean.put("**** **-", "ㅟ");
        MorseKorean.put("-** **-", "ㅢ");

        // 모스 부호 -> 영어
        MorseEnglish.put("*-", "a");
        MorseEnglish.put("-***", "b");
        MorseEnglish.put("-*-*", "c");
        MorseEnglish.put("-**", "d");
        MorseEnglish.put("*", "e");
        MorseEnglish.put("**-*", "f");
        MorseEnglish.put("--*", "g");
        MorseEnglish.put("****", "h");
        MorseEnglish.put("**", "i");
        MorseEnglish.put("*---", "j");
        MorseEnglish.put("-*-", "k");
        MorseEnglish.put("*-**", "l");
        MorseEnglish.put("--", "m");
        MorseEnglish.put("-*", "n");
        MorseEnglish.put("---", "o");
        MorseEnglish.put("*--*", "p");
        MorseEnglish.put("--*-", "q");
        MorseEnglish.put("*-*", "r");
        MorseEnglish.put("***", "s");
        MorseEnglish.put("-", "t");
        MorseEnglish.put("**-", "u");
        MorseEnglish.put("***-", "v");
        MorseEnglish.put("*--", "w");
        MorseEnglish.put("-**-", "x");
        MorseEnglish.put("-*--", "y");
        MorseEnglish.put("--**", "z");

        // 모스 부호 -> 숫자
        MorseNumber.put("*----", "1");
        MorseNumber.put("**---", "2");
        MorseNumber.put("***--", "3");
        MorseNumber.put("****-", "4");
        MorseNumber.put("*****", "5");
        MorseNumber.put("-****", "6");
        MorseNumber.put("--***", "7");
        MorseNumber.put("---**", "8");
        MorseNumber.put("----*", "9");
        MorseNumber.put("-----", "0");

        // 모스 부호 -> 특수문자
        MorseSpecial.put("*-*-*-", ".");
        MorseSpecial.put("--**--", ",");
        MorseSpecial.put("**--**", "?");
        MorseSpecial.put("-**-*", "/");
        MorseSpecial.put("-****-", "-");
        MorseSpecial.put("-***-", "=");
        MorseSpecial.put("---***", ":");
        MorseSpecial.put("-*-*-*", ";");
        MorseSpecial.put("-*--*", "(");
        MorseSpecial.put("-*--*-", ")");
        MorseSpecial.put("*----*", "'");
        MorseSpecial.put("*-**-*", "\"");
        MorseSpecial.put("*-***", "!");
        MorseSpecial.put("*--*-*", "@");
        MorseSpecial.put("*-*-*", "+");
        MorseSpecial.put(" ", " ");
    }
}
