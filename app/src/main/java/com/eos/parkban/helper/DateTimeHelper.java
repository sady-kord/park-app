package com.eos.parkban.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeHelper {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_FORMAT_HHMM = "HH:mm";
    private static final String PERSIAN_DATE_FORMAT = "%04d/%02d/%02d";

    public static Date parseDate(String dateStr) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.US);
        try {
            return formatter.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parseDate(String dateStr, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
        try {
            return formatter.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String parsTime(String dateStr, boolean fullTime) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.US);
        String time = null;
        try {
            Date date = formatter.parse(dateStr);
            String hours = String.valueOf(date.getHours());
            String minutes = String.valueOf(date.getMinutes());
            String seconds = String.valueOf(date.getSeconds());
            if (String.valueOf(minutes).length() == 1)
                minutes = "0" + minutes;
            if (fullTime == true)
                time = hours + ":" + minutes + ":" + seconds;
            else
                time =  hours + ":" + minutes;
            return time;
        } catch (ParseException e) {
            return null;
        }
    }

    public static String DateToStr(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
        return formatter.format(date);
    }

    public static String DateToStr(Date date) {
        return DateToStr(date, DATE_TIME_FORMAT);
    }

    public static String TimeToStrHHMM(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT_HHMM, Locale.US);
        return formatter.format(date);
    }

    public static java.sql.Date convertUtilToSql(java.util.Date uDate) {
        java.sql.Date sDate = new java.sql.Date(uDate.getTime());
        return sDate;
    }

}
