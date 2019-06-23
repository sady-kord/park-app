package com.eos.parkban;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.eos.parkban.databinding.ActivityDriverChargeBinding;
import com.eos.parkban.viewmodels.DriverChargeViewModel;

public class DriverChargeActivity extends BaseActivity {

    DriverChargeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivityDriverChargeBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_driver_charge);

        viewModel = ViewModelProviders.of(this).get(DriverChargeViewModel.class);

        binding.setViewModel(viewModel);
        viewModel.init(this);

        binding.setLifecycleOwner(this);

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.setPlate1(String.valueOf(binding.spinner.getSelectedItem()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.number.addTextChangedListener(viewModel.phoneTextWatcher);
        binding.confirmNumber.addTextChangedListener(viewModel.confirmTextWatcher);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
