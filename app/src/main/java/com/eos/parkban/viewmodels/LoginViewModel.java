package com.eos.parkban.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;

import com.eos.parkban.BaseActivity;
import com.eos.parkban.LoginActivity;
import com.eos.parkban.MainActivity;
import com.eos.parkban.R;
import com.eos.parkban.RecordPlateActivity;
import com.eos.parkban.helper.DateTimeHelper;
import com.eos.parkban.helper.EncryptionPassword;
import com.eos.parkban.helper.FontHelper;
import com.eos.parkban.helper.Preferences;
import com.eos.parkban.helper.ShowToast;
import com.eos.parkban.persistence.ParkbanDatabase;
import com.eos.parkban.persistence.models.CarPlate;
import com.eos.parkban.persistence.models.ResponseResultType;
import com.eos.parkban.persistence.models.User;
import com.eos.parkban.services.dto.CurrentShiftDto;
import com.eos.parkban.services.dto.LoginResultDto;
import com.eos.parkban.repositories.ParkbanRepository;
import com.eos.parkban.services.ParkbanServiceProvider;

import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class LoginViewModel extends ViewModel  {

    private ParkbanRepository parkbanRepository;
    private String userName;
    private String password, finalPassword;
    private Context context;
    private String version;
    private boolean loc = false;
    private MutableLiveData<Boolean> rememberPassword ;
    private User user;

    public User getUser() {
        return user;
    }

    public LiveData<Boolean> getRememberPassword() {
        if (rememberPassword == null)
            rememberPassword = new MutableLiveData<>();
        return rememberPassword;
    }

    public String getVersion() {
        if (version == null)
           // version = "test3";
            version = BaseActivity.Version;
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public MutableLiveData<Integer> progress = new MutableLiveData<>();

    public LiveData<Integer> getProgress() {
        return progress;
    }

    public void init(Context context) {
        this.context = context;

        user  = new User();

        if (rememberPassword == null)
            rememberPassword = new MutableLiveData<>();

        if (!((LoginActivity) context).checkPlayServices()) {

            ShowToast.getInstance().showError(context,R.string.Play_services);
            final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
            return;
        }

        if (parkbanRepository == null) {
            ParkbanDatabase.getInstance(context.getApplicationContext(), new ParkbanDatabase.DatabaseReadyCallback() {
                @Override
                public void onReady(ParkbanDatabase instance) {
                    parkbanRepository = new ParkbanRepository(instance);
                }
            });
        }

        loc = ((LoginActivity) context).checkLocationPermission1(BaseActivity.LoginActivity);
    }

    public void loginClick(View view) {

        EncryptionPassword pass = new EncryptionPassword();

        if (!loc) {
            loc = ((LoginActivity) context).checkLocationPermission1(BaseActivity.LoginActivity);
            return;
        }

        if (user.getUserName().isEmpty()) {
            ShowToast.getInstance().showError(view.getContext(), R.string.username_required);
            return;
        }
        if (user.getPassword().isEmpty() ) {
            ShowToast.getInstance().showError(view.getContext(), R.string.password_required);
            return;
        }

        userName = FontHelper.removeEnter(FontHelper.convertArabicToPersian(user.getUserName()));
        password = FontHelper.toEnglishNumber(FontHelper.removeEnter(user.getPassword()));

        try {
            byte[] bytes = password.getBytes("ASCII");
            finalPassword = FontHelper.removeEnter(pass.encryptAsBase64(bytes));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        progress.setValue(10);
        parkbanRepository.login(userName, finalPassword,
                new ParkbanRepository.ServiceResultCallBack<LoginResultDto.Parkban>() {
                    @Override
                    public void onSuccess(LoginResultDto.Parkban result) {
                        progress.setValue(0);

                        if (userName != null)
                            Preferences.setUserName(userName, context);
//                        if (password != null)
//                            Preferences.setPassword(password, context);
                        if(rememberPassword.getValue()) {
                            Preferences.setPassword(password, context);
                            Preferences.setRememberCheck(true,context);
                        }else {
                            Preferences.setPassword("", context);
                            Preferences.setRememberCheck(false, context);
                        }

                        ((BaseActivity) context).getPrakbanCurrentShift(result.getParkbanId());

                        ParkbanServiceProvider.setUserToken(result.getToken());

                        BaseActivity.CurrentUserId = result.getParkbanId();
                        BaseActivity.parkingSpaceList = result.getParkingSpaces();
                        Log.i("========------==>", "result.getParkingSpaces() " + result.getParkingSpaces().size());

                        ((BaseActivity)context).finish();
                        Intent i = new Intent(context, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(i);
                    }

                    @Override
                    public void onFailed(ResponseResultType resultType, String message, int errorCode) {
                        progress.setValue(0);
                        switch (resultType) {
                            case RetrofitError:
                                ShowToast.getInstance().showError(context, R.string.exception_msg);
                                // Messenger.showErrorMessage(view.getContext(), R.string.exception_msg);
                                break;
                            case ServerError:
                                if (errorCode != 0)
                                    ShowToast.getInstance().showError(context, errorCode);
                                else
                                    ShowToast.getInstance().showError(context, R.string.connection_failed);
                                break;
                            default:
                                ShowToast.getInstance().showError(context, resultType.ordinal());
                        }
                    }
                });
    }

    public void fillDefaultUserAndPass(Context context) {
        String userName = Preferences.getUserName(context);
        String password = Preferences.getPassword(context);
        boolean rememberPass = Preferences.getRememberCheck(context);

        if (userName != null) {
            user.setUserName(userName);
        }
//        if (password != null)
//            setPassword(password);
        if (rememberPass) {
            user.setPassword(password);
            rememberPassword.setValue(true);
        }else {
            user.setPassword("");
            rememberPassword.setValue(false);
        }
    }

    public void getRememberPassStatus(View view){
        if (rememberPassword.getValue())
            rememberPassword.setValue(false);
        else
            rememberPassword.setValue(true);
    }

}
