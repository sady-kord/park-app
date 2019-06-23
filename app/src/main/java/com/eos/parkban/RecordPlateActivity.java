package com.eos.parkban;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.AssetManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.widget.Toast;

import com.eos.parkban.databinding.ActivityRecordPlateBinding;
import com.eos.parkban.dialogs.GPSDialog;
import com.eos.parkban.helper.GPSTracker;
import com.eos.parkban.helper.ImageLoadHelper;
import com.eos.parkban.helper.Preferences;
import com.eos.parkban.helper.ShowToast;
import com.eos.parkban.persistence.models.CarPlate;
import com.eos.parkban.viewmodels.RecordPlateViewModel;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static ir.shahaabco.ANPRNDK.anpr_about;
import static ir.shahaabco.ANPRNDK.anpr_create;

public class RecordPlateActivity extends BaseActivity {

    private ActivityRecordPlateBinding binding;
    private RecordPlateViewModel plateViewModel;
    private GPSTracker gpsTracker;
    private int failedLoadLib = 0;

    private static final String TAG = "ANPR";

    static {

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
        }

        System.loadLibrary("anpr_ndk");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_record_plate);

        copyAssets();
        CreateANPR();

        plateViewModel = ViewModelProviders.of(this).get(RecordPlateViewModel.class);

        binding.setRecordViewModel(plateViewModel);
        binding.setLifecycleOwner(this);

        plateViewModel.init(this);

        plateViewModel.getProgress().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer value) {
                if (value > 0 ) {
                    showProgress(true);
                }else {
                    showProgress(false);
                }
            }
        });


    }

    private void CreateANPR() {
        try {
            File dir = getApplicationContext().getExternalFilesDir(null);
            String mcs_path = dir.getPath();// + "/IRP.mcs";
            short result = anpr_create((byte) 0, mcs_path, "09361392929", (byte) 0);
            if (result >= 0)
                Log.i(TAG, "ANPR Lib Initialized Correctly");
            else {
                failedLoadLib += 1;
                System.loadLibrary("anpr_ndk");
                CreateANPR();
                if (failedLoadLib > 3) {
                    ShowToast.getInstance().showError(this, R.string.anpr_not_load);
                }
            }
        }catch (Exception e){
            Log.i(TAG,e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //plateViewModel.revokeFileReadPermission(this);
        plateViewModel.resetActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        plateViewModel.cancelCrouton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        plateViewModel.processActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        plateViewModel.processOnSaveInstance(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        plateViewModel.processOnRestoreInstance(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

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
