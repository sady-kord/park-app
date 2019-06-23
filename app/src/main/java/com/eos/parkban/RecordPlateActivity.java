package com.eos.parkban;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.AssetManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.Log;
import com.eos.parkban.databinding.ActivityRecordPlateBinding;
import com.eos.parkban.dialogs.GPSDialog;
import com.eos.parkban.helper.GPSTracker;
import com.eos.parkban.helper.ImageLoadHelper;
import com.eos.parkban.persistence.models.CarPlate;
import com.eos.parkban.viewmodels.RecordPlateViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RecordPlateActivity extends BaseActivity {

    private ActivityRecordPlateBinding binding;
    private RecordPlateViewModel plateViewModel;
    private GPSTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_record_plate);

        plateViewModel = ViewModelProviders.of(this).get(RecordPlateViewModel.class);

        binding.setRecordViewModel(plateViewModel);
        binding.setLifecycleOwner(this);

        plateViewModel.init(this);

        copyAssets();
    }

    @Override
    protected void onResume() {
        super.onResume();
       // gpsTracker = new GPSTracker(this);

        plateViewModel.resetActivity(this);
        plateViewModel.checkGps(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
       // plateViewModel.onPostResumeActivity(this);
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
