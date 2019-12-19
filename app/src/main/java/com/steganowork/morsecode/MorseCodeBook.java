package com.steganowork.morsecode;

import java.util.TreeMap;

public class MorseCodeBook {
    private TreeMap<String, String> MorseKorean;
    private TreeMap<String, String> MorseEnglish;
    private TreeMap<String, String> MorseNumber;
    private TreeMap<String, String> MorseSpecial;

    MorseCodeBook() {
        MorseKorean = new TreeMap<String, String>();
        MorseEnglish = new TreeMap<String, String>();
        MorseNumber = new TreeMap<String, String>();
        MorseSpecial = new TreeMap<String, String>();

        InputMorseCode();  // 해시 코드값 넣기 (모스 데이터 삽입)
    }

    public TreeMap<String, String> getMorseEnglish() {
        return MorseEnglish;
    }

    public TreeMap<String, String> getMorseKorean() {
        return MorseKorean;
    }

    public TreeMap<String, String> getMorseNumber() {
        return MorseNumber;
    }

    public TreeMap<String, String> getMorseSpecial() {
        return MorseSpecial;
    }

    // 모스부호 데이터 =============================================================================
    // 테스트 사이트 : https://jinh.kr/morse/
    private void InputMorseCode() {

        // 모스 부호 -> 한국어
        MorseKorean.put("ㄱ", "*-**");
        MorseKorean.put("ㄲ", "*-** *-**");
        MorseKorean.put("ㄳ", "*-** --*");
        MorseKorean.put("ㄴ", "**-*");
        MorseKorean.put("ㄵ", "**-* *--*");
        MorseKorean.put("ㄶ", "**-* *---");
        MorseKorean.put("ㄷ", "-***");
        MorseKorean.put("ㄸ", "-*** -***");
        MorseKorean.put("ㄹ", "***-");
        MorseKorean.put("ㄺ", "***- *-**");
        MorseKorean.put("ㄻ", "***- --");
        MorseKorean.put("ㄼ", "***- *--");
        MorseKorean.put("ㄽ", "***- --*");
        MorseKorean.put("ㄾ", "***- --**");
        MorseKorean.put("ㄿ", "***- ---");
        MorseKorean.put("ㅀ", "***- *---");
        MorseKorean.put("ㅁ", "--");
        MorseKorean.put("ㅂ", "*--");
        MorseKorean.put("ㅃ", "*-- *--");
        MorseKorean.put("ㅄ", "*-- --*");
        MorseKorean.put("ㅅ", "--*");
        MorseKorean.put("ㅆ", "--* --*");
        MorseKorean.put("ㅇ", "-*-");
        MorseKorean.put("ㅈ", "*--*");
        MorseKorean.put("ㅉ", "*--* *--*");
        MorseKorean.put("ㅊ", "-*-*");
        MorseKorean.put("ㅋ", "-**-");
        MorseKorean.put("ㅌ", "--**");
        MorseKorean.put("ㅍ", "---");
        MorseKorean.put("ㅎ", "*---");
        MorseKorean.put("ㅏ", "*");
        MorseKorean.put("ㅑ", "**");
        MorseKorean.put("ㅓ", "-");
        MorseKorean.put("ㅕ", "***");
        MorseKorean.put("ㅗ", "*-");
        MorseKorean.put("ㅛ", "-*");
        MorseKorean.put("ㅜ", "****");
        MorseKorean.put("ㅠ", "*-*");
        MorseKorean.put("ㅡ", "-**");
        MorseKorean.put("ㅣ", "**-");
        MorseKorean.put("ㅔ", "-*--");
        MorseKorean.put("ㅐ", "--*-");
        MorseKorean.put("ㅘ", "*- *");
        MorseKorean.put("ㅙ", "*- --*-");
        MorseKorean.put("ㅚ", "*- **-");
        MorseKorean.put("ㅖ", "*** **-");
        MorseKorean.put("ㅒ", "****-");
        MorseKorean.put("ㅝ", "**** -");
        MorseKorean.put("ㅞ", "**** -*--");
        MorseKorean.put("ㅟ", "**** **-");
        MorseKorean.put("ㅢ", "-** **-");

        // 모스 부호 -> 영어
        MorseEnglish.put("A", "*-");
        MorseEnglish.put("B", "-***");
        MorseEnglish.put("C", "-*-*");
        MorseEnglish.put("D", "-**");
        MorseEnglish.put("E", "*");
        MorseEnglish.put("F", "**-*");
        MorseEnglish.put("G", "--*");
        MorseEnglish.put("H", "****");
        MorseEnglish.put("I", "**");
        MorseEnglish.put("J", "*---");
        MorseEnglish.put("K", "-*-");
        MorseEnglish.put("L", "*-**");
        MorseEnglish.put("M", "--");
        MorseEnglish.put("N", "-*");
        MorseEnglish.put("O", "---");
        MorseEnglish.put("P", "*--*");
        MorseEnglish.put("Q", "--*-");
        MorseEnglish.put("R", "*-*");
        MorseEnglish.put("S", "***");
        MorseEnglish.put("T", "-");
        MorseEnglish.put("U", "**-");
        MorseEnglish.put("V", "***-");
        MorseEnglish.put("W", "*--");
        MorseEnglish.put("X", "-**-");
        MorseEnglish.put("Y", "-*--");
        MorseEnglish.put("Z", "--**");

        // 모스 부호 -> 숫자
        MorseNumber.put("1", "*----");
        MorseNumber.put("2", "**---");
        MorseNumber.put("3", "***--");
        MorseNumber.put("4", "****-");
        MorseNumber.put("5", "*****");
        MorseNumber.put("6", "-****");
        MorseNumber.put("7", "--***");
        MorseNumber.put("8", "---**");
        MorseNumber.put("9", "----*");
        MorseNumber.put("0", "-----");

        // 모스 부호 -> 특수문자
        MorseSpecial.put(".", "*-*-*-");
        MorseSpecial.put(",", "--**--");
        MorseSpecial.put("?", "**--**");
        MorseSpecial.put("/", "-**-*");
        MorseSpecial.put("-", "-****-");
        MorseSpecial.put("=", "-***-");
        MorseSpecial.put(":", "---***");
        MorseSpecial.put(";", "-*-*-*");
        MorseSpecial.put("(", "-*--*");
        MorseSpecial.put(")", "-*--*-");
        MorseSpecial.put("'", "*----*");
        MorseSpecial.put("\"", "*-**-*");
        MorseSpecial.put("!", "*-***");
        MorseSpecial.put("@", "*--*-*");
        MorseSpecial.put("+", "*-*-*");
        MorseSpecial.put(" ", " ");
    }
}