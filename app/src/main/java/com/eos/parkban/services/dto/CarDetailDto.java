package com.eos.parkban.services.dto;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.Date;

public class CarDetailDto {
//
    @SerializedName("DataRow")
    @Expose
    private int[] imageByteArray;

    @SerializedName("ParkDateTime")
    @Expose
    private Date dateTime;

    @SerializedName("IsExit")
    @Expose
    private boolean IsExit;

    public int[] getImageByteArray() {
        return imageByteArray;
    }

    public void setImageByteArray(int[] imageByteArray) {
        this.imageByteArray = imageByteArray;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isExit() {
        return IsExit;
    }

    public void setExit(boolean exit) {
        IsExit = exit;
    }

}
