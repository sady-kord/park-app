package com.eos.parkban;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.eos.parkban.controls.CardViewControl;
import com.eos.parkban.databinding.ActivityFunctionalityReportBinding;
import com.eos.parkban.helper.DateTimeHelper;
import com.eos.parkban.viewmodels.FunctionalityReportViewModel;

import java.util.Date;

public class FunctionalityReportActivity extends BaseActivity {

    private ActivityFunctionalityReportBinding binding;
    private Date BeginDate, EndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_functionality_report);
        final FunctionalityReportViewModel viewModel = ViewModelProviders.of(this).get(FunctionalityReportViewModel.class);

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        BeginDate = DateTimeHelper.getBeginCurrentMonth();
        EndDate = DateTimeHelper.getEndCurrentMonth();
        binding.fromDatePicker.setDisplayDate(BeginDate);
        binding.toDatePicker.setDisplayDate(EndDate);

        viewModel.init(this);


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

    public void hideResultLayout(boolean status){
       binding.totalLayout.setVisibility(status ? View.GONE : View.VISIBLE)  ;
    }

}
