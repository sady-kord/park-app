package com.eos.parkban;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.eos.parkban.databinding.ActivityPaymentBinding;
import com.eos.parkban.viewmodels.PaymentViewModel;

public class PaymentActivity extends BaseActivity {

    private PaymentViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivityPaymentBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_payment);

        viewModel = ViewModelProviders.of(this).get(PaymentViewModel.class);

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        viewModel.init(this);

        viewModel.getProgress().observe(this, new Observer<Integer>() {
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
    protected void onStop() {
        super.onStop();

        Log.i("--------------->>>>","onStop ");
        viewModel.stopEvent(PaymentActivity.this);
    }
}
