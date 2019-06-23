package com.eos.parkban;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.eos.parkban.databinding.ActivityLoginBinding;
import com.eos.parkban.viewmodels.LoginViewModel;

public class LoginActivity extends BaseActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        binding.setViewModel(loginViewModel);

        binding.setLifecycleOwner(this);

        loginViewModel.init(this);
        loginViewModel.fillDefaultUserAndPass(this);

        loginViewModel.getProgress().observe(this, new Observer<Integer>() {
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

    @Override
    protected void onResume() {
        super.onResume();
        loginViewModel.fillDefaultUserAndPass(this);
    }
}
