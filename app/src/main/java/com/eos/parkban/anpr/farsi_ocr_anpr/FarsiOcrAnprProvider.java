package com.eos.parkban.anpr.farsi_ocr_anpr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.eos.parkban.core.anpr.BaseAnprProvider;
import com.eos.parkban.core.anpr.helpers.PlateDetectionState;
import com.eos.parkban.core.anpr.onPlateDetectedCallback;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FarsiOcrAnprProvider implements BaseAnprProvider {

    private static final String TAG = "ANPR-CAM";

    private Mat plateMat;
    private ANPR anpr1;
    private Context context;

    public FarsiOcrAnprProvider(final Context context) {

        this.context = context;

        openCVLoader();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    anpr1 = new ANPR(context);
                    anpr1.Initialize("ParkbanEos");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void openCVLoader() {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, context, mLoaderCallback);

        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    plateMat = new Mat();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @SuppressLint("StaticFieldLeak")
    @Override
    public void getPlate(final Context context, final Bitmap plateImage, final onPlateDetectedCallback callback) {

        this.context = context;

        if (plateMat == null)
            plateMat = new Mat();

        new AsyncTask<Void, Void, String>() {

            private ANPR.Result result;
            private String p0, p1, p2, p3;

            @Override
            protected String doInBackground(Void... results) {
                Utils.bitmapToMat(plateImage, plateMat);
                result = anpr1.Recognize(plateMat, null);
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                if (result.str.length() > 0) {
                    if (result.str.length() >= 2)
                        p0 = result.str.substring(0, Math.min(2, result.str.length()));
                    if (result.str.length() >= 3)
                        p1 = result.str.substring(2, Math.min(3, result.str.length()));
                    if (result.str.length() >= 4)
                        p2 = result.str.substring(3, Math.min(6, result.str.length()));
                    if (result.str.length() > 6)
                        p3 = result.str.substring(6, Math.min(8, result.str.length()));

                    String plate = p0 + p1 + p2 + p3;

                    callback.onPlateDetected(PlateDetectionState.DETECTED, plate, plateImage , p0 , p1 , p2 , p3);

                } else {
                    callback.onPlateDetected(PlateDetectionState.NOT_FOUND, "", plateImage, p0 , p1 , p2 , p3);
                }

            }
        }.execute();

    }
}
