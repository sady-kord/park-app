package com.eos.parkban.viewmodels;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.eos.parkban.BaseActivity;
import com.eos.parkban.MainActivity;
import com.eos.parkban.PaymentActivity;
import com.eos.parkban.R;
import com.eos.parkban.core.anpr.helpers.RidingType;
import com.eos.parkban.dialogs.PaymentResultDialog;
import com.eos.parkban.helper.DateTimeHelper;
import com.eos.parkban.helper.FontHelper;
import com.eos.parkban.helper.ImageLoadHelper;
import com.eos.parkban.helper.ShowToast;
import com.eos.parkban.persistence.ParkbanDatabase;
import com.eos.parkban.persistence.models.ResponseResultType;
import com.eos.parkban.repositories.ParkbanRepository;
import com.eos.parkban.services.dto.CarRecordsDto;
import com.eos.parkban.services.dto.ParkAmountResultDto;
import com.eos.parkban.services.dto.SendRecordAndPayResultDto;

import java.util.Date;

public class PaymentViewModel extends ViewModel {

    public static String FirstTime = "FirstTime";
    public static String LastTime = "LastTime";
    public static String RecordDate = "Date";
    public static String ParkSpaceId = "ParkSpaceId";
    public static String Plate0 = "Plate0";
    public static String Plate1 = "Plate1";
    public static String Plate2 = "Plate2";
    public static String Plate3 = "Plate3";
    public static String Lat = "Lat";
    public static String Long = "Long";
    public static String PlateFileName = "PlateFileName";
    public static String ParkId = "Exit";
    public static String FirstDate = "FirstDate";
    public static String Plate = "Plate";
    public static String EditedPlate = "EditedPlate";
    public static String RidingType = "RidingType";

    private String firstTime, lastTime, plateFileName;
    private long parkSpaceId, parkId;
    private ParkbanRepository parkbanRepository;
    private Bundle extras;
    private MutableLiveData<String> plate0, plate1, plate2, plate3;
    private MutableLiveData<String> amount, duration, walletCashAmount;
    private MutableLiveData<Boolean> allowPay;
    private double latitude, longitude;
    private Date firstDate, date;
    private String phoneNumber, confirmPhoneNumber, plateNumber;
    private long parkAmount = 0;
    private boolean paySuccess = false;
    private int editedPlate;
    private MutableLiveData<Boolean> motorType, carType;


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

    public LiveData<String> getDuration() {
        if (duration == null)
            duration = new MutableLiveData<>();
        return duration;
    }

    public LiveData<String> getWalletCashAmount() {
        if (walletCashAmount == null)
            walletCashAmount = new MutableLiveData<>();
        return walletCashAmount;
    }

    public LiveData<Boolean> getAllowPay() {
        if (allowPay == null)
            allowPay = new MutableLiveData<>();
        return allowPay;
    }

    public LiveData<String> getPlate0() {
        if (plate0 == null)
            plate0 = new MutableLiveData<>();
        return plate0;
    }

    public LiveData<String> getPlate1() {
        if (plate1 == null)
            plate1 = new MutableLiveData<>();
        return plate1;
    }

    public LiveData<String> getPlate2() {
        if (plate2 == null)
            plate2 = new MutableLiveData<>();
        return plate2;
    }

    public LiveData<String> getPlate3() {
        if (plate3 == null)
            plate3 = new MutableLiveData<>();
        return plate3;
    }

    public LiveData<Boolean> getMotorType() {
        if (motorType == null)
            motorType = new MutableLiveData<>();
        return motorType;
    }

    public LiveData<Boolean> getCarType() {
        if (carType == null)
            carType = new MutableLiveData<>();
        return carType;
    }

    public void init(final Context context) {

        if (plate0 == null)
            plate0 = new MutableLiveData<>();
        if (plate1 == null)
            plate1 = new MutableLiveData<>();
        if (plate2 == null)
            plate2 = new MutableLiveData<>();
        if (plate3 == null)
            plate3 = new MutableLiveData<>();

        if (amount == null)
            amount = new MutableLiveData<>();

        if (duration == null)
            duration = new MutableLiveData<>();

        if (walletCashAmount == null)
            walletCashAmount = new MutableLiveData<>();

        if (allowPay == null)
            allowPay = new MutableLiveData<>();
        allowPay.setValue(true);

        if (carType == null)
            carType = new MutableLiveData<>();

        if (motorType == null)
            motorType = new MutableLiveData<>();

        getBundle(context);

        if (parkbanRepository == null) {
            ParkbanDatabase.getInstance(context.getApplicationContext(), new ParkbanDatabase.DatabaseReadyCallback() {
                @Override
                public void onReady(ParkbanDatabase instance) {
                    parkbanRepository = new ParkbanRepository(instance);
                    getParkAmount(context);
                }
            });
        }
    }

    private void getBundle(Context context) {
        extras = ((BaseActivity) context).getIntent().getExtras();
        if (extras != null) {
            if (extras.getString(FirstTime) != null) {
                firstTime = extras.getString(FirstTime);
                lastTime = extras.getString(LastTime);
                date = new Date(extras.getLong(RecordDate));
                parkSpaceId = extras.getLong(ParkSpaceId);
                plate0.setValue(extras.getString(Plate0));
                plate1.setValue(extras.getString(Plate1));
                plate2.setValue(extras.getString(Plate2));
                plate3.setValue(extras.getString(Plate3));
                latitude = extras.getDouble(Lat);
                longitude = extras.getDouble(Long);
                parkId = extras.getLong(ParkId);
                plateFileName = extras.getString(PlateFileName);
                firstDate = new Date(extras.getLong(FirstDate));
                plateNumber = extras.getString(Plate);
                editedPlate = extras.getInt(EditedPlate);
                int ridingTypeOrdinal = extras.getInt(RidingType);
                if (ridingTypeOrdinal == 0) {
                    carType.setValue(true);
                    motorType.setValue(false);
                }
                else {
                    carType.setValue(false);
                    motorType.setValue(true);
                }

            }
        }
    }

    private void getParkAmount(final Context context) {
        progress.setValue(10);
        parkbanRepository.getParkAmount(parkSpaceId, DateTimeHelper.DateToStr(date), firstTime, lastTime, new ParkbanRepository.ServiceResultCallBack<ParkAmountResultDto>() {
            @Override
            public void onSuccess(ParkAmountResultDto result) {
                progress.setValue(0);
                if (result.getValues() != null) {
                    parkAmount = result.getValues().getParkAmount();
                    amount.setValue(String.valueOf(result.getValues().getParkAmount()));
                    duration.setValue(String.valueOf(result.getValues().getDuration()));
                    walletCashAmount.setValue(String.valueOf(result.getValues().getWalletCashAmount()));
                    allowPay.setValue(result.getValues().isAllowPay());

                }else {
                    allowPay.setValue(false);
                }
            }

            @Override
            public void onFailed(ResponseResultType resultType, String message, int errorCode) {
                allowPay.setValue(false);
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

    public void payClick(final View view) {

        if (!allowPay.getValue()) {
            return;
        }

        if (parkAmount == 0) {
            ShowToast.getInstance().showWarning(view.getContext(), R.string.park_amount_zero);
            return;
        }

        if (phoneNumber != null)
            if (!phoneNumber.isEmpty()) {
                if (phoneNumber.length() < 11) {
                    ShowToast.getInstance().showWarning(view.getContext(), R.string.phone_number_length);
                    return;
                } else if (!phoneNumber.substring(0, 2).contains("09")) {
                    ShowToast.getInstance().showWarning(view.getContext(), R.string.phone_number_format);
                    return;
                }
                if (confirmPhoneNumber == null) {
                    ShowToast.getInstance().showWarning(view.getContext(), R.string.phone_number_confirm);
                    return;
                } else {
                    if (confirmPhoneNumber.length() < 11) {
                        ShowToast.getInstance().showWarning(view.getContext(), R.string.phone_number_confirm_length);
                        return;
                    }
                    if (!confirmPhoneNumber.substring(0, 2).contains("09")) {
                        ShowToast.getInstance().showWarning(view.getContext(), R.string.phone_number_confirm_format);
                        return;
                    }
                    if  (!confirmPhoneNumber.equals(phoneNumber)) {
                        ShowToast.getInstance().showWarning(view.getContext(), R.string.phone_number_not_same_confirm);
                        return;
                    }
                }
            }

        CarRecordsDto carRecordsDto = new CarRecordsDto();

        carRecordsDto.setLatitude(latitude);
        carRecordsDto.setLongitude(longitude);
        carRecordsDto.setPlateNo(plateNumber);
        carRecordsDto.setUserId(BaseActivity.CurrentUserId);
        carRecordsDto.setFirstParkDate(firstDate);
        carRecordsDto.setImageIntArray(ImageLoadHelper.getInstance().convertImageFileToIntArray(view.getContext(), plateFileName));

        carRecordsDto.setDateTime(date);
        carRecordsDto.setExit(true);
        carRecordsDto.setParkId(parkId);
        carRecordsDto.setParkingSpaceId(parkSpaceId);
        carRecordsDto.setDriverPhoneNumber(phoneNumber != null ? java.lang.Long.parseLong(phoneNumber) : 0);
        carRecordsDto.setVerificationStatus(editedPlate);

        progress.setValue(10);
        parkbanRepository.sendRecordAndPay(carRecordsDto, new ParkbanRepository.ServiceResultCallBack<SendRecordAndPayResultDto>() {
            @Override
            public void onSuccess(final SendRecordAndPayResultDto result) {
                parkId = result.getValues().getCarParkResult().getParkId();
                if (result.getValues().getCarParkResult().isStatus()) {
                    parkbanRepository.updateCarPlateRecordStatus(parkId,
                            result.getValues().getCarParkResult().isStatus(), new ParkbanRepository.DataBaseCarPlateUpdateCallBack() {
                                @Override
                                public void onSuccess() {
                                    //show Dialog
                                    progress.setValue(0);
                                    PaymentResultDialog dialog = new PaymentResultDialog();
                                    dialog.setItem(result.getValues().getReceiptCode(), 0,result.getValues().getQRCodeBase64());
                                    dialog.walletDisable(true);
                                    dialog.setCallBack(new PaymentResultDialog.DialogCallBack() {
                                        @Override
                                        public void onCallBack(String state) {
                                            ((BaseActivity) view.getContext()).finish();
                                        }
                                    });
                                    dialog.show(((Activity) view.getContext()).getFragmentManager(), "");
                                    paySuccess = true;
                                }

                                @Override
                                public void onFailed() {
                                    progress.setValue(0);
                                }
                            });
                } else {
                    progress.setValue(0);
                    ShowToast.getInstance().showWarning(view.getContext(), R.string.payment_failed);
                    return;
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

    public void cancelClick(View view) {
        paySuccess = false;
        parkbanRepository.updateCarPlateRecordStatus(parkId,
                false, new ParkbanRepository.DataBaseCarPlateUpdateCallBack() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailed() {

                    }
                });
        ((BaseActivity)view.getContext()).finish();
    }

    public void stopEvent(Context context) {

        if (!paySuccess)
            parkbanRepository.updateCarPlateRecordStatus(parkId,
                    false, new ParkbanRepository.DataBaseCarPlateUpdateCallBack() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailed() {

                        }
                    });
    }

}
