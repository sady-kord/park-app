package com.eos.parkban;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.eos.parkban.adapters.PlateListAdapter;
import com.eos.parkban.databinding.ActivityListPlatesBinding;
import com.eos.parkban.dialogs.RecordsStatusDialog;
import com.eos.parkban.helper.GPSTracker;
import com.eos.parkban.helper.ImageLoadHelper;
import com.eos.parkban.persistence.ParkbanDatabase;
import com.eos.parkban.persistence.models.Car;
import com.eos.parkban.viewmodels.PlateListViewModel;

import java.io.Serializable;
import java.util.List;

public class ListPlatesActivity extends BaseActivity {

    private ActivityListPlatesBinding binding;
    private PlateListViewModel plateListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_list_plates);
        plateListViewModel = ViewModelProviders.of(this).get(PlateListViewModel.class);

        binding.setViewModel(plateListViewModel);

        plateListViewModel.init(this);

    }



}
