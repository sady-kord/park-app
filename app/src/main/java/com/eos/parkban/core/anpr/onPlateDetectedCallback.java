package com.eos.parkban.core.anpr;

import android.graphics.Bitmap;

import com.eos.parkban.anpr.farsi_ocr_anpr.ANPR;
import com.eos.parkban.core.anpr.helpers.PlateDetectionState;

public interface onPlateDetectedCallback {
    void onPlateDetected(PlateDetectionState state, String plateNo, Bitmap plateImage , String p0 ,
                         String p1 , String p2 , String p3);
}
