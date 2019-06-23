package com.eos.parkban;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.eos.parkban.anpr.farsi_ocr_anpr.ANPR;
import com.eos.parkban.anpr.farsi_ocr_anpr.FarsiOcrAnprProvider;
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
import java.util.Calendar;



public class detectPlateActivity extends AppCompatActivity {

    private static final String TAG = "ANPR-CAM";
    final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    private ANPR anpr;

    ImageView imageView;
    private Uri imageUri;
    private Bitmap bitmap;
    private Mat imIn;
    private ANPR.Result res;
    private String p0, p1, p2, p3;
    private FarsiOcrAnprProvider anprProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_plate);

        imageView = findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        copyAssets();

        anprProvider = new FarsiOcrAnprProvider(this);



//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    copyAssets();
//                    anpr = new ANPR(getApplicationContext());
//                    anpr.Initialize("ParkbanEos");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);

        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void openCamera() {
        String fileName = "pelak.jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                } catch (IOException e) {
                    //ShowToast.makeText(SaveRecordActivity.this, getString(R.string.picture_io_exception), ShowToast.LENGTH_SHORT).show();
                }
            }

            imageView.setImageBitmap(bitmap);

//            if (imIn == null)
//                imIn = new Mat();
//            Utils.bitmapToMat(bitmap, imIn);

//            anprProvider.getPlate(this, bitmap, new onPlateDetectedCallback() {
//                @Override
//                public void onPlateDetected(PlateDetectionState state, String plateNo, Bitmap plateImage) {
//                    PlateDetectionState state1 = state;
//                }
//            });


//            new AsyncTask<Void, Void, String>() {
//                @Override
//                protected String doInBackground(Void... params) {
////                    mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
////                    Log.i("===>", "time is " + mydate);
//                    Log.i("====>", "go to recognize");
//                    res = anpr.Recognize(imIn, null);
////                    mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
////                    Log.i("===>", "time is " + mydate);
//                    return null;
//                }
//
//                @Override
//                protected void onPostExecute(String s) {
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    if (res.str.length() > 0) {
//                        Mat plt = imIn.submat(res.rc);
//                        if (res.str.length() >= 2)
//                            p0 = res.str.substring(0, Math.min(2, res.str.length()));
//
//                        if (res.str.length() >= 3)
//                            p1 = res.str.substring(2, Math.min(3, res.str.length()));
//
//                        if (res.str.length() >= 4)
//                            p2 = res.str.substring(3, Math.min(6, res.str.length()));
//
//                        if (res.str.length() > 6)
//                            p3 = res.str.substring(6, Math.min(8, res.str.length()));
//
//                    } else {
//                        p0 = "Not Found!";
//                    }
//                }
//            }.execute();

        } else if (resultCode == RESULT_CANCELED) {
            //CustomToast.showError(getBaseContext(), getString(R.string.picture_was_not_taken_or_selected));
        } else {
            // CustomToast.showError(getBaseContext(), getString(R.string.picture_was_not_taken_or_selected));
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    Mat plateMat = new Mat();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File dir = getExternalFilesDir(null);
                File outFile = new File(dir, filename);
                if (!outFile.exists()) {
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                }
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
