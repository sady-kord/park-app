package com.eos.parkban;

import android.app.ActivityManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.IntentSender;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.eos.parkban.databinding.ActivityMainBinding;
import com.eos.parkban.helper.GPSTracker;
import com.eos.parkban.helper.LocationTracker;
import com.eos.parkban.helper.ShowToast;
import com.eos.parkban.viewmodels.MainViewModel;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.Iterator;
import java.util.List;

public class MainActivity extends BaseActivity {

    private MainViewModel viewModel;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        binding.setMainViewModel(viewModel);

        viewModel.init(this);

        BaseActivity.locationTracker.checkGpsAvailability(MainActivity.this);

       // GPSTracker gpsTracker = new GPSTracker(this);
       // gpsTracker.getLocation("main");

    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.init(this);
    }

    @Override
    public void onBackPressed() {

        viewModel.backPress(MainActivity.this);
    }


}
