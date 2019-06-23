package com.eos.parkban.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static String User_Name = "username";
    private static String Password = "password";

    private static SharedPreferences preferences;

    private static SharedPreferences getSharedPreferences(Context context) {
        String PREFS_NAME = "Setting";
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void setPassword(String pass, Context context) {

        getSharedPreferences(context).edit()
                .putString(Password, pass)
                .apply();
    }

    public static String getPassword(Context context) {

        SharedPreferences pref = getSharedPreferences(context);

        String pass = pref.getString(Password, null);

        return (pass != null && !pass.isEmpty())
                ? pass
                : "";
    }

    public static void setUserName(String text, Context context) {

        getSharedPreferences(context).edit()
                .putString(User_Name, text)
                .apply();
    }

    public static String getUserName(Context context) {
        SharedPreferences pref = getSharedPreferences(context);

        String userName = pref.getString(User_Name, null);

        return (userName != null && !userName.isEmpty())
                ? userName
                : "";
    }
}
