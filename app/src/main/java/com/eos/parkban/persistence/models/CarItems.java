package com.eos.parkban.persistence.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.eos.parkban.helper.DateTimeHelper;
import com.eos.parkban.helper.ImageLoadHelper;

import java.io.File;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CarItems {

    private String part0, part1, part2, part3, carItemSize, lastTime = "" , state;
    private Bitmap imageFile;
    private List<String> timeList;
    private long carId;

    public CarItems(Context context, Car car) {

        if (car.getPlateNo() != null) {
            part0 = car.getPlateNo().substring(0, 2);
            part1 = car.getPlateNo().substring(2, 3);
            part2 = car.getPlateNo().substring(3, 6);
            part3 = car.getPlateNo().substring(6, 8);


            if (car.getCarPlates().size() > 0) {
                // String seenDate = car.getCarPlates().get(car.getCarPlates().size() - 1).getSeenDate();
                // lastTime = DateTimeHelper.parsTime(seenDate, false);

                Date date = new Date(car.getCarPlates().get(car.getCarPlates().size() - 1).getRecordDate().getTime());
                int hours = date.getHours();
                int minutes = date.getMinutes();

                lastTime = hours + ":" + minutes;

                File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File outFile = new File(storageDir, car.getCarPlates().get(car.getCarPlates().size() - 1).getPlateFileName());
                String absolutePath = outFile.getAbsolutePath();
                imageFile = ImageLoadHelper.getInstance().loadImage(context, absolutePath);

                carItemSize = String.valueOf(car.getCarPlates().size());

            }

            carId = car.getId();
        }
    }

    public long getCarId() {
        return carId;
    }

    public void setCarId(long carId) {
        this.carId = carId;
    }

    public String getPart0() {
        return part0;
    }

    public void setPart0(String part0) {
        this.part0 = part0;
    }

    public String getPart1() {
        return part1;
    }

    public void setPart1(String part1) {
        this.part1 = part1;
    }

    public String getPart2() {
        return part2;
    }

    public void setPart2(String part2) {
        this.part2 = part2;
    }

    public String getPart3() {
        return part3;
    }

    public void setPart3(String part3) {
        this.part3 = part3;
    }

    public String getCarItemSize() {
        return carItemSize;
    }

    public void setCarItemSize(String carItemSize) {
        this.carItemSize = carItemSize;
    }

    public Bitmap getImageFile() {
        return imageFile;
    }

    public void setImageFile(Bitmap imageFile) {
        this.imageFile = imageFile;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public List<String> getTimeList() {
        if (timeList == null)
            timeList = new ArrayList<>();
        return timeList;
    }

    public void setTimeList(List<String> timeList) {
        this.timeList = timeList;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
