package com.eos.parkban.core.anpr;

import android.graphics.Bitmap;

import com.eos.parkban.core.anpr.helpers.PlateDetectionState;
import com.eos.parkban.core.anpr.helpers.RidingType;

public interface onPlateDetectedCallback {
    void onPlateDetected(PlateDetectionState state, RidingType ridingType ,String plateNo, Bitmap plateImage , String p0 ,
                         String p1 , String p2 , String p3);


}
