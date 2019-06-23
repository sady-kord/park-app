package com.eos.parkban.persistence;

import android.arch.persistence.room.TypeConverter;

import com.eos.parkban.persistence.models.ParkingSpace;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Date;
import java.util.List;


public class CustomTypeConverter {

    @TypeConverter
    public static Date dateFromTimeStamp(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long dateToTimeStamp(Date date) {
        return date == null ? null : date.getTime();
    }

}
