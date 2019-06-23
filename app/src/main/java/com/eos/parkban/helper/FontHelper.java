package com.eos.parkban.helper;

import android.content.Context;
import android.graphics.Typeface;

public class FontHelper {
    private static FontHelper instance;
    private static Typeface persianTypeface;

    private FontHelper(Context context) {
        persianTypeface = Typeface.createFromAsset(context.getAssets(), "Font/irsans.ttf");
    }

    public static synchronized FontHelper getInstance(Context context){
        if (instance == null){
            instance = new FontHelper(context);
        }
        return instance;
    }

    public Typeface getPersianTextTypeface() {
        return persianTypeface;
    }

    private static String[] persianNumbers = new String[]{"۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹"};

    public static String toPersianNumber(String text) {
        if (text.isEmpty())
            return "";
        String out = "";
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if ('0' <= c && c <= '9') {
                int number = Integer.parseInt(String.valueOf(c));
                out += persianNumbers[number];
            } else if (c == '٫') {
                out += '،';
            } else {
                out += c;
            }

        }
        return out;
    }

    public static String toEnglishNumber(String text) {
        if (text.isEmpty() || text == null)
            return "";
        String out = "";
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (1176 <= c && c <= 1785) {
                int number = (c-1776);
                out += number;
            } else if (c == '٫') {
                out += '،';
            } else {
                out += c;
            }

        }
        return out;
    }

    public static String RemoveAllSpaceAndEnterAndTab(String str){
        return str.replace(" ","").replace("\t", "").replace("\n", "").trim();
    }

    public static String removeEnter(String str){
        return str.replace("\n","");
    }
}
