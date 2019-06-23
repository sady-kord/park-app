package com.eos.parkban;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.eos.parkban.databinding.ActivityLoginBinding;
import com.eos.parkban.viewmodels.LoginViewModel;

public class LoginActivity extends BaseActivity {

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        binding.setViewModel(loginViewModel);

        loginViewModel.init(this);
        loginViewModel.fillDefaultUserAndPass(this);

    }


}
