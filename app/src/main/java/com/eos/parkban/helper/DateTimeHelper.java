package com.eos.parkban.helper;

import android.text.format.Time;

import com.eos.parkban.persistence.models.Language;
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateTimeHelper {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_FORMAT_HHMM = "HH:mm";
    private static final String PERSIAN_DATE_FORMAT = "%04d/%02d/%02d";

    public static int getHour(Date date) {
        return dateToCalendar(date).get(Calendar.HOUR_OF_DAY);
    }

    public static Date setHour(Date date, int hour) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        return cal.getTime();
    }

    public static Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static String DateToString(Date date) {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", GetLocal(Language.en));
        String formatted = format1.format(date);
        return formatted;
    }

    public static Locale GetLocal(Language lan) {
        Locale locale = new Locale(lan.toString());
        return locale;

    }

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
                time = hours + ":" + minutes;
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

    public static long getDateDifferent(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public static long getCurrentTimeForDB() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long time = calendar.getTime().getTime();
        Time timeFormat = new Time();
        timeFormat.set(time - TimeZone.getDefault().getOffset(time));
        return timeFormat.toMillis(true);
    }

    public static int getTimeMinute() {
        int minuteTime = 0;
        try {
            SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm:ss");
            String time = localDateFormat.format(new Date());

            String hour = time.substring(0, 2);
            String min = time.substring(3, 5);

            minuteTime = Integer.parseInt(hour) * 60 + Integer.parseInt(min);

        } catch (Exception e) {

        }
        return minuteTime;
    }

    public static boolean checkShiftTime(int start, int end) {

        boolean isShift = true;
        int now = getTimeMinute();

        if (start <= now && now <= end)
            isShift = true;
        else
            isShift = false;

        return isShift;
    }

    public static Date getBeginCurrentMonth() {
        PersianCalendar p = new PersianCalendar();
        p.setTime(Calendar.getInstance().getTime());
        p.setPersianDate(p.getPersianYear(), p.getPersianMonth(), 1);
        return p.getTime();
    }

    public static Date getEndCurrentMonth() {
        PersianCalendar p = new PersianCalendar();
        p.setTime(Calendar.getInstance().getTime());
        p.setPersianDate(p.getPersianYear(), p.getPersianMonth(), 1);

        int lastDay;

        if (p.getPersianMonth() >= 0 && p.getPersianMonth() <= 5)
            lastDay = 31;
        else if (p.getPersianMonth() >= 6 && p.getPersianMonth() <= 10)
            lastDay = 30;
        else if (p.isLeapYear(p.getPersianYear()))
            lastDay = 30;
        else
            lastDay = 29;

        p.setPersianDate(p.getPersianYear(), p.getPersianMonth(), lastDay);
        return p.getTime();
    }

}
