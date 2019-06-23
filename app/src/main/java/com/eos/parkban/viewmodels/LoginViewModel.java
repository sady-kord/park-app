package com.eos.parkban.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.eos.parkban.BaseActivity;
import com.eos.parkban.R;
import com.eos.parkban.RecordPlateActivity;
import com.eos.parkban.helper.Messenger;
import com.eos.parkban.helper.Preferences;
import com.eos.parkban.helper.ShowToast;
import com.eos.parkban.persistence.ParkbanDatabase;
import com.eos.parkban.persistence.models.ResponseResultType;
import com.eos.parkban.services.dto.LoginResultDto;
import com.eos.parkban.repositories.ParkbanRepository;
import com.eos.parkban.services.ParkbanServiceProvider;

public class LoginViewModel extends ViewModel {

    private ParkbanRepository parkbanRepository;
    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void init(final Context context) {
        if (parkbanRepository == null) {
            ParkbanDatabase.getInstance(context.getApplicationContext(), new ParkbanDatabase.DatabaseReadyCallback() {
                @Override
                public void onReady(ParkbanDatabase instance) {
                    parkbanRepository = new ParkbanRepository(instance);
                }
            });
        }
    }

    public void loginClick(final View view) {
        ((BaseActivity) view.getContext()).showProgress(true);

        if (userName != null)
            Preferences.setUserName(userName, view.getContext());
        if (password != null)
            Preferences.setPassword(password, view.getContext());

        parkbanRepository.login(userName, password,
                new ParkbanRepository.ServiceResultCallBack<LoginResultDto.Parkban>() {
                    @Override
                    public void onSuccess(LoginResultDto.Parkban result) {
                        ((BaseActivity) view.getContext()).showProgress(false);
                        ParkbanServiceProvider.setUserToken(result.getToken());

                        BaseActivity.CurrentUserId = result.getParkbanId();
                        BaseActivity.parkingSpaceList = result.getParkingSpaces();

                        Intent i = new Intent(view.getContext(), RecordPlateActivity.class);

                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        view.getContext().startActivity(i);
                    }

                    @Override
                    public void onFailed(ResponseResultType resultType, String message, int errorCode) {
                        ((BaseActivity) view.getContext()).showProgress(false);
                        switch (resultType) {
                            case RetrofitError:
                                ShowToast.getInstance().showError(view.getContext(), R.string.exception_msg);
                               // Messenger.showErrorMessage(view.getContext(), R.string.exception_msg);
                                break;
                            case ServerError:
                                if (errorCode != 0)
                                    Messenger.showErrorMessageFail(view.getContext(), message, errorCode);
                                else
                                    Messenger.showErrorMessage(view.getContext(), R.string.connection_failed);
                                break;
                            default:
                                Messenger.showErrorMessageWithResponse(view.getContext(), resultType.ordinal());
                        }
                    }
                });
    }

    public void fillDefaultUserAndPass(Context context) {
        String userName = Preferences.getUserName(context);
        String password = Preferences.getPassword(context);

        if (userName != null) {
            setUserName(userName);
        }
        if (password != null)
            setPassword(password);
    }
}
