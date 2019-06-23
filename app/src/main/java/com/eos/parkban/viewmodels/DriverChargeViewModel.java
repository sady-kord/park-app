package com.eos.parkban.viewmodels;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.eos.parkban.BaseActivity;
import com.eos.parkban.R;
import com.eos.parkban.dialogs.ChargeValueDialog;
import com.eos.parkban.dialogs.ConfirmMessageDialog;
import com.eos.parkban.dialogs.ItemListDialog;
import com.eos.parkban.dialogs.PaymentResultDialog;
import com.eos.parkban.helper.FontHelper;
import com.eos.parkban.helper.ShowToast;
import com.eos.parkban.persistence.ParkbanDatabase;
import com.eos.parkban.persistence.models.ChargeAmount;
import com.eos.parkban.persistence.models.ResponseResultType;
import com.eos.parkban.repositories.ParkbanRepository;
import com.eos.parkban.services.dto.BooleanResultDto;
import com.eos.parkban.services.dto.IncreaseDriverWalletResultDto;
import com.eos.parkban.services.dto.LongResultDto;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Configuration;

public class DriverChargeViewModel extends ViewModel {

    private MutableLiveData<Boolean> car;
    private MutableLiveData<Boolean> motor;
    private MutableLiveData<String> amount, debtValue;
    private String phoneNumber, confirmPhoneNumber;
    private String plate0, plate1, plate2, plate3;
    private String mPlate0, mPlate1;
    private String carPlate, motorPlate;
    private List<ChargeAmount> chargeAmountList;
    private ArrayList<ChargeAmount> chargeAmountArrayList;
    private long chargeItemSelected;
    private NumberFormat formatter = new DecimalFormat("#,###");
    private ParkbanRepository parkbanRepository;
    private MutableLiveData<Boolean> enableCharge, hastDebt, noDebt;

    public TextWatcher phoneTextWatcher, confirmTextWatcher;

    public MutableLiveData<Integer> progress = new MutableLiveData<>();

    public LiveData<Integer> getProgress() {
        return progress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getConfirmPhoneNumber() {
        return confirmPhoneNumber;
    }

    public void setConfirmPhoneNumber(String confirmPhoneNumber) {
        this.confirmPhoneNumber = confirmPhoneNumber;
    }

    public LiveData<String> getAmount() {
        if (amount == null)
            amount = new MutableLiveData<>();
        return amount;
    }

    public LiveData<String> getDebtValue() {
        if (debtValue == null)
            debtValue = new MutableLiveData<>();
        return debtValue;
    }

    public MutableLiveData<Boolean> getCar() {
        if (car == null)
            car = new MutableLiveData<>();
        return car;
    }

    public MutableLiveData<Boolean> getMotor() {
        if (motor == null)
            motor = new MutableLiveData<>();
        return motor;
    }

    public LiveData<Boolean> getEnableCharge() {
        if (enableCharge == null)
            enableCharge = new MutableLiveData<>();
        return enableCharge;
    }

    public LiveData<Boolean> getHasDebt() {
        if (hastDebt == null)
            hastDebt = new MutableLiveData<>();
        return hastDebt;
    }

    public LiveData<Boolean> getNoDebt() {
        if (noDebt == null)
            noDebt = new MutableLiveData<>();
        return noDebt;
    }

    public String getPlate0() {
        return plate0;
    }

    public void setPlate0(String plate0) {
        this.plate0 = plate0;
    }

    public String getPlate1() {
        return plate1;
    }

    public void setPlate1(String plate1) {
        this.plate1 = plate1;
    }

    public String getPlate2() {
        return plate2;
    }

    public void setPlate2(String plate2) {
        this.plate2 = plate2;
    }

    public String getPlate3() {
        return plate3;
    }

    public void setPlate3(String plate3) {
        this.plate3 = plate3;
    }

    public String getMPlate0() {
        return mPlate0;
    }

    public void setMPlate0(String mPlate0) {
        this.mPlate0 = mPlate0;
    }

    public String getMPlate1() {
        return mPlate1;

    }

    public void setMPlate1(String mPlate1) {
        this.mPlate1 = mPlate1;
    }

    public void init(Context context) {

        if (car == null)
            car = new MutableLiveData<>();
        car.setValue(true);

        if (enableCharge == null)
            enableCharge = new MutableLiveData<>();
        enableCharge.setValue(true);

        if (hastDebt == null)
            hastDebt = new MutableLiveData<>();
        hastDebt.setValue(false);

        if (noDebt == null)
            noDebt = new MutableLiveData<>();
        noDebt.setValue(false);

        if (debtValue == null)
            debtValue = new MutableLiveData<>();

        //put value of VacationRequest Enum in a List
        chargeAmountList = new ArrayList<ChargeAmount>(EnumSet.allOf(ChargeAmount.class));
        //convert List to ArrayList
        chargeAmountArrayList = new ArrayList<>(chargeAmountList.size());
        chargeAmountArrayList.addAll(chargeAmountList);

        if (amount == null)
            amount = new MutableLiveData<>();
        chargeItemSelected = ChargeAmount.two.getValue();
        amount.setValue(formatter.format(chargeItemSelected) + " ریال");

        if (parkbanRepository == null) {
            ParkbanDatabase.getInstance(context.getApplicationContext(), new ParkbanDatabase.DatabaseReadyCallback() {
                @Override
                public void onReady(ParkbanDatabase instance) {
                    parkbanRepository = new ParkbanRepository(instance);
                }
            });
        }

        phoneTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                hastDebt.setValue(false);
                noDebt.setValue(false);
            }
        };

        confirmTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                hastDebt.setValue(false);
                noDebt.setValue(false);
            }
        };
    }

    public void showDebtClick(final View view) {

        hastDebt.setValue(false);
        noDebt.setValue(false);

        if (checkPhoneValidation(view.getContext()))
            if (!phoneNumber.equals("")) {
                progress.setValue(10);
                parkbanRepository.getDriverFullDebt(Long.parseLong(phoneNumber), new ParkbanRepository.ServiceResultCallBack<LongResultDto>() {
                    @Override
                    public void onSuccess(LongResultDto result) {
                        progress.setValue(0);
                        if (result.getValues() > 0) {
                            hastDebt.setValue(true);
                            debtValue.setValue(FontHelper.RialFormatter(result.getValues()));
                        } else {
                            noDebt.setValue(true);
                        }
                    }

                    @Override
                    public void onFailed(ResponseResultType resultType, String message, int errorCode) {
                        progress.setValue(0);
                        switch (resultType) {
                            case RetrofitError:
                                ShowToast.getInstance().showError(view.getContext(), R.string.exception_msg);
                                break;
                            case ServerError:
                                if (errorCode != 0)
                                    ShowToast.getInstance().showError(view.getContext(), errorCode);
                                else
                                    ShowToast.getInstance().showError(view.getContext(), R.string.connection_failed);
                                break;
                            default:
                                ShowToast.getInstance().showError(view.getContext(), resultType.ordinal());
                        }
                    }
                });
            }

    }

    public boolean checkPhoneValidation(Context context) {
        boolean status = true;
        if (phoneNumber == null || phoneNumber.equals("")) {
            ShowToast.getInstance().showWarning(context, R.string.phone_number_need);
            status = false;
        } else if (phoneNumber != null)
            if (!phoneNumber.isEmpty()) {
                if (phoneNumber.length() < 11) {
                    ShowToast.getInstance().showWarning(context, R.string.phone_number_length);
                    status = false;
                } else if (!phoneNumber.substring(0, 2).contains("09")) {
                    ShowToast.getInstance().showWarning(context, R.string.phone_number_format);
                    status = false;
                }
                if (confirmPhoneNumber == null) {
                    ShowToast.getInstance().showWarning(context, R.string.phone_number_confirm);
                    status = false;
                } else {
                    if (confirmPhoneNumber.length() < 11) {
                        ShowToast.getInstance().showWarning(context, R.string.phone_number_confirm_length);
                        status = false;
                    }
                    if (!confirmPhoneNumber.substring(0, 2).contains("09")) {
                        ShowToast.getInstance().showWarning(context, R.string.phone_number_confirm_format);
                        status = false;
                    }
                    if (!confirmPhoneNumber.equals(phoneNumber)) {
                        ShowToast.getInstance().showWarning(context, R.string.phone_number_not_same_confirm);
                        status = false;
                    }
                }
            }

        return status;
    }

    public void carCheckClick(View view) {

        car.setValue(true);
        motor.setValue(false);
        carPlate = plate0 + plate1 + plate2 + plate3;
    }

    public void motorCheckClick(View view) {
        car.setValue(false);
        motor.setValue(true);
        motorPlate = mPlate0 + mPlate1;
    }

    public void chargeAmountClick(final View view) {

        ChargeValueDialog dialog = new ChargeValueDialog();
        dialog.setItems(chargeAmountArrayList, R.string.charge_amounts);
        dialog.setCallBack(new ChargeValueDialog.DialogCallBack() {
            @Override
            public void onCallBack(String state, long chargeValue) {
                if (state == ChargeValueDialog.CONFIRM) {
                    setAmount(chargeValue, view.getContext());
                } else {

                }
            }
        });
        dialog.show(((Activity) view.getContext()).getFragmentManager(), "");

//        ItemListDialog dialog = new ItemListDialog();
//        dialog.setItems(chargeAmountArrayList, R.string.charge_amounts, new ItemListDialog.DialogOnItemSelectedListener() {
//            @Override
//            public void OnItemSelected(Object selectedItem) {
//                setAmount((ChargeAmount) selectedItem, view.getContext());
//            }
//        });
//        dialog.show(((Activity) view.getContext()).getFragmentManager(), "");
    }

    private void setAmount(long itemSelected, Context context) {
        chargeItemSelected = itemSelected;
        amount.setValue(formatter.format(chargeItemSelected) + " ریال");

        hasParkbanCredit(chargeItemSelected, context);

    }

    private void hasParkbanCredit(long amount, final Context context) {
        progress.setValue(10);
        parkbanRepository.hasParkbanCredit(amount, new ParkbanRepository.ServiceResultCallBack<BooleanResultDto>() {
            @Override
            public void onSuccess(BooleanResultDto result) {
                progress.setValue(0);
                if (!result.getValue()) {
                    ShowToast.getInstance().showError(context, R.string.credit_not_enough);
                    enableCharge.setValue(false);
                    return;
                } else
                    enableCharge.setValue(true);
            }

            @Override
            public void onFailed(ResponseResultType resultType, String message, int errorCode) {
                progress.setValue(0);
                switch (resultType) {
                    case RetrofitError:
                        ShowToast.getInstance().showError(context, R.string.exception_msg);
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

    public void chargeClick(final View view) {

        if (!enableCharge.getValue()) {
            return;
        }


        chargeDriverWallet(view.getContext(), 0);
    }

    private void chargeDriverWallet(final Context context, long realPhoneNumber) {
        if (car.getValue()) {
            if (plate0 == null || plate1 == null || plate2 == null || plate3 == null) {
                ShowToast.getInstance().showWarning(context, R.string.plate_not_complete);
                return;
            } else if (plate0.length() < 2 || plate2.length() < 3 || plate3.length() < 2) {
                ShowToast.getInstance().showWarning(context, R.string.plate_not_complete);
                return;
            } else
                carPlate = plate0 + plate1 + plate2 + plate3;
        } else if (motor.getValue()) {
            if (mPlate1 == null || mPlate0 == null) {
                ShowToast.getInstance().showWarning(context, R.string.plate_not_complete);
                return;
            } else if (mPlate0.length() < 3 || mPlate1.length() < 5) {
                ShowToast.getInstance().showWarning(context, R.string.plate_not_complete);
                return;
            } else
                motorPlate = mPlate0 + mPlate1;
        }

        if (phoneNumber == null) {
            ShowToast.getInstance().showWarning(context, R.string.phone_number_need);
            return;
        } else if (phoneNumber != null || phoneNumber.equals(""))
            if (!phoneNumber.isEmpty()) {
                if (phoneNumber.length() < 11) {
                    ShowToast.getInstance().showWarning(context, R.string.phone_number_length);
                    return;
                } else if (!phoneNumber.substring(0, 2).contains("09")) {
                    ShowToast.getInstance().showWarning(context, R.string.phone_number_format);
                    return;
                }
                if (confirmPhoneNumber == null) {
                    ShowToast.getInstance().showWarning(context, R.string.phone_number_confirm);
                    return;
                } else {
                    if (confirmPhoneNumber.length() < 11) {
                        ShowToast.getInstance().showWarning(context, R.string.phone_number_confirm_length);
                        return;
                    }
                    if (!confirmPhoneNumber.substring(0, 2).contains("09")) {
                        ShowToast.getInstance().showWarning(context, R.string.phone_number_confirm_format);
                        return;
                    }
                    if (!confirmPhoneNumber.equals(phoneNumber)) {
                        ShowToast.getInstance().showWarning(context, R.string.phone_number_not_same_confirm);
                        return;
                    }
                }
            }

        enableCharge.setValue(false);
        IncreaseDriverWalletResultDto.IncreaseDriverWalletDto wallet = new IncreaseDriverWalletResultDto.IncreaseDriverWalletDto();

        if (realPhoneNumber != 0)
            wallet.setPhoneNumber(realPhoneNumber);
        else
            wallet.setPhoneNumber(java.lang.Long.parseLong(phoneNumber));

        wallet.setAmount(chargeItemSelected);

        if (car.getValue())
            wallet.setPlate(carPlate);
        else
            wallet.setPlate(motorPlate);


        progress.setValue(10);
        parkbanRepository.increaseDiverWallet(wallet, new ParkbanRepository.ServiceResultCallBack<IncreaseDriverWalletResultDto>() {
            @Override
            public void onSuccess(final IncreaseDriverWalletResultDto result) {
                if (result.getValue().getErrorMessage() == null || result.getValue().getErrorMessage() == "") {
                    if (result.getValue().getReceiptCode() != null) {
                        PaymentResultDialog dialog = new PaymentResultDialog();
                        dialog.setItem(result.getValue().getReceiptCode(), result.getValue().getDriverWalletCashAmount(), result.getValue().getQRCodeBase64());
                        dialog.setCallBack(new PaymentResultDialog.DialogCallBack() {
                            @Override
                            public void onCallBack(String state) {
                                ((BaseActivity) context).finish();
                            }
                        });
                        dialog.show(((Activity) context).getFragmentManager(), "");

                    }

                    enableCharge.setValue(true);

                } else if (result.getValue().getRealPhoneNumber() != 0) {
                    final long realNumber = result.getValue().getRealPhoneNumber();
                    ConfirmMessageDialog dialog = new ConfirmMessageDialog();

                    //set * for some character
                    String realPhone = String.valueOf(result.getValue().getRealPhoneNumber());
                    StringBuilder builder = new StringBuilder(realPhone);

                    builder.setCharAt(4, '*');
                    builder.setCharAt(5, '*');
                    builder.setCharAt(6, '*');

                    dialog.setMessage("این پلاک به این شماره تعلق دارد \n " + builder + "\n افزایش اعتبار برای این شماره انجام خواهد شد \n آیا از ادامه شارژ اطمینان دارید ؟ ");
                    dialog.setCallBack(new ConfirmMessageDialog.DialogCallBack() {
                        @Override
                        public void onCallBack(String state) {
                            if (state == ConfirmMessageDialog.CONFIRM) {
                                chargeDriverWallet(context, realNumber);
                            }
                        }
                    });
                    dialog.show(((Activity) context).getFragmentManager(), "");

                    enableCharge.setValue(true);
                }

                progress.setValue(0);
            }

            @Override
            public void onFailed(ResponseResultType resultType, String message, int errorCode) {
                progress.setValue(0);
                switch (resultType) {
                    case RetrofitError:
                        ShowToast.getInstance().showError(context, R.string.exception_msg);
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
}
