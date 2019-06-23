package com.eos.parkban;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.eos.parkban.databinding.ActivitySettingBinding;
import com.eos.parkban.viewmodels.SettingViewModel;

public class SettingActivity extends BaseActivity {

    private ActivitySettingBinding binding;
    private SettingViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);

        viewModel = ViewModelProviders.of(this).get(SettingViewModel.class);
        binding.setViewModel(viewModel);

        binding.setLifecycleOwner(this);

        viewModel.init(this);

    }

    @Override
    public void onBackPressed() {
        viewModel.backSelect(this);

    }
}
