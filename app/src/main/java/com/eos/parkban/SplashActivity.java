package com.eos.parkban;

import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.eos.parkban.databinding.ActivitySplashBinding;
import com.eos.parkban.viewmodels.SplashViewModel;

import static com.eos.parkban.viewmodels.SplashViewModel.STORAGE_PERMISSION;

public class SplashActivity extends BaseActivity {

    private SplashViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySplashBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        viewModel = ViewModelProviders.of(this).get(SplashViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        viewModel.init(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.getLastVersion();
                } else {
                    System.exit(0);
                }
                return;
            }
        }
    }
}
