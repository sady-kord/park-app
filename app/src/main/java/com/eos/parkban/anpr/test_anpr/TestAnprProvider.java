package com.eos.parkban.anpr.test_anpr;

import android.content.Context;
import android.graphics.Bitmap;

import com.eos.parkban.anpr.farsi_ocr_anpr.ANPR;
import com.eos.parkban.core.anpr.BaseAnprProvider;
import com.eos.parkban.core.anpr.helpers.PlateDetectionState;
import com.eos.parkban.core.anpr.onPlateDetectedCallback;

public class TestAnprProvider implements BaseAnprProvider {

    @Override
    public void getPlate(Context context, Bitmap plateImage, onPlateDetectedCallback callback) {
        callback.onPlateDetected(PlateDetectionState.DETECTED, "55د761", plateImage,"","","","");
    }
}
