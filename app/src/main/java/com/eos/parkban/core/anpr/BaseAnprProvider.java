package com.eos.parkban.core.anpr;

import android.content.Context;
import android.graphics.Bitmap;

import com.eos.parkban.anpr.farsi_ocr_anpr.ANPR;

public interface BaseAnprProvider {

    void getPlate(Context context , Bitmap plateImage , onPlateDetectedCallback callback);

}
