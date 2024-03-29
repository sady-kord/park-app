package com.eos.parkban;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.eos.parkban.databinding.ActivityChargeReportBinding;
import com.eos.parkban.helper.DateTimeHelper;
import com.eos.parkban.viewmodels.ChargeReportViewModel;

import java.util.Date;

public class ChargeReportActivity extends BaseActivity {

    private ActivityChargeReportBinding binding;
    private Date BeginDate, EndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_charge_report);

        ChargeReportViewModel viewModel = ViewModelProviders.of(this).get(ChargeReportViewModel.class);

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        viewModel.init(this);

        BeginDate = DateTimeHelper.getBeginCurrentMonth();
        EndDate = DateTimeHelper.getEndCurrentMonth();
        binding.fromDatePicker.setDisplayDate(BeginDate);
        binding.toDatePicker.setDisplayDate(EndDate);

        viewModel.getProgress().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer value) {
                if (value > 0) {
                    showProgress(true);
                } else {
                    showProgress(false);
                }
            }
        });

    }

    public String getStartDate() {
        return binding.fromDatePicker.getDate();
    }

    public String getEndDate() {
        return binding.toDatePicker.getDate();
    }

}
