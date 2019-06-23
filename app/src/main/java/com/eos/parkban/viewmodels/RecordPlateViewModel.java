package com.eos.parkban.viewmodels;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;

import com.eos.parkban.BaseActivity;
import com.eos.parkban.ListPlatesActivity;
import com.eos.parkban.LoginActivity;
import com.eos.parkban.MainActivity;
import com.eos.parkban.PaymentActivity;
import com.eos.parkban.R;
import com.eos.parkban.anpr.farsi_ocr_anpr.FarsiOcrAnprProvider;
import com.eos.parkban.core.anpr.BaseAnprProvider;
import com.eos.parkban.core.anpr.helpers.PlateDetectionState;
import com.eos.parkban.core.anpr.helpers.RidingType;
import com.eos.parkban.core.anpr.onPlateDetectedCallback;
import com.eos.parkban.dialogs.ConfirmMessageDialog;
import com.eos.parkban.dialogs.EditParkSpaceDialog;
import com.eos.parkban.dialogs.CustomDialogFragment;
import com.eos.parkban.dialogs.EditPlateDialog;
import com.eos.parkban.dialogs.GetLocationDialog;
import com.eos.parkban.dialogs.ToastDialog;
import com.eos.parkban.helper.DateTimeHelper;
import com.eos.parkban.helper.FontHelper;
import com.eos.parkban.helper.GPSTracker;
import com.eos.parkban.helper.ImageLoadHelper;
import com.eos.parkban.helper.LocationTracker;
import com.eos.parkban.helper.Messenger;
import com.eos.parkban.helper.Preferences;
import com.eos.parkban.helper.ShowToast;
import com.eos.parkban.persistence.ParkbanDatabase;
import com.eos.parkban.persistence.models.Car;
import com.eos.parkban.persistence.models.CarPlate;
import com.eos.parkban.persistence.models.VerificationStatus;
import com.eos.parkban.persistence.models.model;
import com.eos.parkban.services.dto.LongResultDto;
import com.eos.parkban.services.dto.ParkingSpaceDto;
import com.eos.parkban.persistence.models.ParkingSpaceStatus;
import com.eos.parkban.persistence.models.ResponseResultType;
import com.eos.parkban.repositories.ParkbanRepository;
import com.eos.parkban.services.dto.IntResultDto;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class RecordPlateViewModel extends ViewModel {

    private static final String MEDIA_FILE_TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";
    private static final int REQUEST_IMAGE_CAPTURE = 10;
    private static final String TAG = "ImagePlate";
    private static final String NEW_PHOTO_FILE_PATH_KEY = "newPhotoFilePathKey";

    private BaseAnprProvider anprProvider;
    private MutableLiveData<CarPlate> carPlateLiveData;
    private String newPhotoFilePath;
    private Bitmap plateBitmap;
    private CarPlate carPlate;
    private Location location;
    private MutableLiveData<Boolean> exit;
    private boolean mustNotSave = true;
    private MutableLiveData<String> parkingSpaceName;
    private ParkingSpaceDto parkingSpaceSelected;
    private CheckBox checkBox;
    private MutableLiveData<Bitmap> carImageBitmap;
    private ParkbanRepository parkbanRepository;
    private boolean isNewCar = false;
    private MutableLiveData<Boolean> newCar;
    private Car car;
    private Dialog progressDialog;
    private ArrayList<ParkingSpaceDto> parkingSpaces;
    private List<Long> parkSpaceFull;
    private ArrayList<ParkingSpaceDto> spaceList;
    private boolean updateCar, updateCarPlate;
    private boolean exitBySystem = false;
    private Context context;
    private String statusOfPark = "None";
    private MutableLiveData<Boolean> saveEnable;
    private MutableLiveData<Boolean> motorType, carType, carPhoto;
    private RidingType ridingTypeDetected;
    private boolean detectedPlate = false;
    private MutableLiveData<Boolean> showParkingSpace;
    private TextView debtValue;

    public MutableLiveData<Integer> progress = new MutableLiveData<>();
    private Crouton crouton;
    private Configuration croutonConfig;
    private View crotounView;
    private Uri photoURI;

    public LiveData<Integer> getProgress() {
        return progress;
    }

    public LiveData<Boolean> getCarPhoto() {
        if (carPhoto == null)
            carPhoto = new MutableLiveData<>();
        return carPhoto;
    }

    public MutableLiveData<Boolean> getMotorType() {
        return motorType;
    }

    public MutableLiveData<Boolean> getCarType() {
        return carType;
    }

    public LiveData<Boolean> getSaveEnable() {
        if (saveEnable == null)
            saveEnable = new MutableLiveData<>();
        return saveEnable;
    }

    public LiveData<Boolean> getShowParkingSpace() {
        if (showParkingSpace == null)
            showParkingSpace = new MutableLiveData<>();
        return showParkingSpace;
    }

    public LiveData<Boolean> getExit() {
        if (exit == null)
            exit = new MutableLiveData<>();
        return exit;
    }

    public LiveData<String> getParkingSpaceName() {
        if (parkingSpaceName == null) {
            parkingSpaceName = new MutableLiveData<>();
        }
        return parkingSpaceName;
    }

    public LiveData<CarPlate> getCarPlate() {
        if (carPlateLiveData == null) {
            carPlateLiveData = new MutableLiveData<>();
        }
        return carPlateLiveData;
    }

    public LiveData<Bitmap> getCarImageBitmap() {
        if (carImageBitmap == null)
            carImageBitmap = new MutableLiveData<>();
        return carImageBitmap;
    }

    public LiveData<Boolean> getNewCar() {
        if (newCar == null)
            newCar = new MutableLiveData<>();
        return newCar;
    }

    public void setCarPlateLiveData(CarPlate value) {
        carPlateLiveData.setValue(value);
    }

    public void init(final Context context) {

        this.context = context;

        // gpsTracker = new GPSTracker(context);
        // location = gpsTracker.getLocation(BaseActivity.RecordActivity);

        // new LocationTracker(context);
        BaseActivity.locationTracker.checkGpsAvailability(context);
        location = BaseActivity.locationTracker.getLocation();
        if (location == null)
            ShowToast.getInstance().showWarning(context, R.string.pls_wait_for_location);

        if (newCar == null)
            newCar = new MutableLiveData<>();
        newCar.setValue(true);

        if (exit == null)
            exit = new MutableLiveData<>();
        exit.setValue(false);

        carPlate = new CarPlate();
        if (anprProvider == null) {
            anprProvider = new FarsiOcrAnprProvider(context);
        }
        if (carPlateLiveData == null) {
            carPlateLiveData = new MutableLiveData<>();
        }

        if (parkingSpaceName == null) {
            parkingSpaceName = new MutableLiveData<>();
        }

        if (carImageBitmap == null)
            carImageBitmap = new MutableLiveData<>();

        if (saveEnable == null)
            saveEnable = new MutableLiveData<>();
        saveEnable.setValue(false);

        if (carType == null)
            carType = new MutableLiveData<>();

        if (motorType == null)
            motorType = new MutableLiveData<>();

        if (carPhoto == null)
            carPhoto = new MutableLiveData<>();
        carPhoto.setValue(false);

        if (showParkingSpace == null)
            showParkingSpace = new MutableLiveData<>();
        showParkingSpace.setValue(false);

        //define crouton
        crotounView = ((Activity) context).getLayoutInflater().inflate(R.layout.crouton_debt_view, null);
        debtValue = crotounView.findViewById(R.id.value);
        croutonConfig = new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build();
        crouton = Crouton.make(((Activity) context), crotounView, R.id.main_layout, croutonConfig);

        if (parkbanRepository == null) {
            ParkbanDatabase.getInstance(context.getApplicationContext(), new ParkbanDatabase.DatabaseReadyCallback() {
                @Override
                public void onReady(ParkbanDatabase instance) {
                    parkbanRepository = new ParkbanRepository(instance);
                }
            });
        }

        parkSpaceFull = new ArrayList<>();

        parkingSpaceName.setValue(context.getResources().getString(R.string.unknown));
        setCarPlateLiveData(new CarPlate(ImageLoadHelper.getInstance().convertDrawableToBitmap(context, R.drawable.camera), "", "", "", ""));

        parkingSpaces = (ArrayList<ParkingSpaceDto>) BaseActivity.parkingSpaceList;

        if (parkingSpaces == null) {
            ShowToast.getInstance().showWarning(context, R.string.login_again);
            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        } else if (parkingSpaces.size() > 0)
            getParkSpaceFull();

        if (BaseActivity.locationTracker.isGPSEnabled)
            openCamera();

    }

    private void getParkSpaceFull() {

        parkingSpaces = (ArrayList<ParkingSpaceDto>) BaseActivity.parkingSpaceList;

        parkbanRepository.getParkSpaceFull(Calendar.getInstance().getTime().getTime() - (BaseActivity.marginLimit * 60000), new ParkbanRepository.DataBaseParkSpaceCallBack() {
            @Override
            public void onSuccess(List<Long> result) {
                parkSpaceFull = result;

//                HashSet<Long> hashSet = new HashSet<Long>();
//                hashSet.addAll(parkSpaceFull);
//                parkSpaceFull.clear();
//                parkSpaceFull.addAll(hashSet);
                try {
                    if (parkSpaceFull == null || parkSpaceFull.size() == 0) {
                        for (int i = 0; i < parkingSpaces.size(); i++) {
                            parkingSpaces.get(i).setSpaceStatus(ParkingSpaceStatus.EMPTY);
                        }
                    } else {
                        for (int i = 0; i < parkingSpaces.size(); i++) {
                            parkingSpaces.get(i).setSpaceStatus(ParkingSpaceStatus.EMPTY);
                            for (int j = 0; j < parkSpaceFull.size(); j++) {
                                if (parkingSpaces.get(i).getId() == parkSpaceFull.get(j)) {
                                    parkingSpaces.get(i).setSpaceStatus(ParkingSpaceStatus.FULL);
                                    break;
                                }
                            }
                        }
                    }
                    String exception = "";
                    spaceList = new ArrayList<>();

                    for (int i = 0; i < parkingSpaces.size(); i++) {
                        try {
                            Location locationB = new Location("point B");
                            locationB.setLatitude(parkingSpaces.get(i).getLatitude());
                            locationB.setLongitude(parkingSpaces.get(i).getLongitude());

                            if (BaseActivity.locationTracker.getLocation() != null) {
                                float distance = BaseActivity.locationTracker.getDistanceBetween(BaseActivity.locationTracker.getLocation(), locationB);
                                int round = Math.round(distance);
                                parkingSpaces.get(i).setDistance(round);
                            }

                            spaceList.add(parkingSpaces.get(i));
                        } catch (Exception e) {
                            exception = "Distance : " + e.getMessage();
                        }
                    }
                    if (exception != "")
                        ShowToast.getInstance().showErrorStringMsg(context, exception);
                    Collections.sort(spaceList);
                } catch (Exception e) {
                    ShowToast.getInstance().showErrorStringMsg(context, "General : " + e.getMessage());
                }
            }

            @Override
            public void onFailed() {
                spaceList = new ArrayList<>();
                String exception = "";
                for (int i = 0; i < parkingSpaces.size(); i++) {
                    try {
                        Location locationB = new Location("point B");
                        locationB.setLatitude(parkingSpaces.get(i).getLatitude());
                        locationB.setLongitude(parkingSpaces.get(i).getLongitude());

                        if (BaseActivity.locationTracker.getLocation() != null) {
                            float distance = BaseActivity.locationTracker.getDistanceBetween(BaseActivity.locationTracker.getLocation(), locationB);
                            int round = Math.round(distance);
                            parkingSpaces.get(i).setDistance(round);
                        }

                        spaceList.add(parkingSpaces.get(i));
                    } catch (Exception e) {
                        exception = "Distance onFailed : " + e.getMessage();
                    }
                }
                if (exception != "")
                    ShowToast.getInstance().showErrorStringMsg(context, exception);
            }
        });
    }

    private void doDetectPlate(final Context context, Bitmap plateImage) {
        updateCar = false;
        updateCarPlate = false;
        statusOfPark = "None";
        anprProvider.getPlate(context, plateImage, new onPlateDetectedCallback() {
            @Override
            public void onPlateDetected(PlateDetectionState state, RidingType ridingType, String plateNo, Bitmap plateImage, String part0, String part1, String part2, String part3) {
                ridingTypeDetected = ridingType;

                if (ridingType == RidingType.CAR) {
                    carType.setValue(true);
                    motorType.setValue(false);
                } else {
                    carType.setValue(false);
                    motorType.setValue(true);
                }

                if (plateNo.contains("null")) {
                    carType.setValue(true);
                    motorType.setValue(false);
                    saveEnable.setValue(false);
                    ShowToast.getInstance().showWarning(context, R.string.plate_not_detected);
                    carPlate.setPart0("");
                    carPlate.setPart1("");
                    carPlate.setPart2("");
                    carPlate.setPart3("");
                    setCarPlateLiveData(carPlate);

                    carImageBitmap.setValue(carPlate.getPlateImage());

                } else if (state == PlateDetectionState.DETECTED) {
                    showParkingSpace.setValue(true);
                    detectedPlate = true;
                    saveEnable.setValue(true);
                    if (ridingType == RidingType.CAR) {
                        carPlate.setPart0(part0);
                        carPlate.setPart1(part1);
                        carPlate.setPart2(part2);
                        carPlate.setPart3(part3);
                        carPlate.setPlateNumber(FontHelper.toEnglishNumber(part0) + part1 + FontHelper.toEnglishNumber(part2) + FontHelper.toEnglishNumber(part3));
                    } else {
                        if (plateNo != null) {
                            carPlate.setPart0(plateNo.substring(0, 3));
                            carPlate.setPart1(plateNo.substring(3, plateNo.length()));
                        }
                        carPlate.setPlateNumber(FontHelper.toEnglishNumber(plateNo));
                    }
                    carPlate.setEditedPlate(VerificationStatus.None.ordinal());
                    setCarPlateLiveData(carPlate);
                    mustNotSave = false;
                    carImageBitmap.setValue(carPlate.getPlateImage());

                    //show debt car by crouton
                    //crouton.show();
                    getUnregisteredCarDebt(carPlate.getPlateNumber());

                    //...............
                    checkCar(carPlate.getPlateNumber());

                } else if (state == PlateDetectionState.NOT_FOUND) {
                    showParkingSpace.setValue(false);
                    detectedPlate = false;
                    carType.setValue(true);
                    motorType.setValue(false);
                    //mustNotSave = true;
                    saveEnable.setValue(false);
                    ShowToast.getInstance().showWarning(context, R.string.plate_not_detected);
                    carPlate.setPart0("");
                    carPlate.setPart1("");
                    carPlate.setPart2("");
                    carPlate.setPart3("");
                    setCarPlateLiveData(carPlate);

                    carImageBitmap.setValue(carPlate.getPlateImage());
                }
            }
        });
    }

    private void checkCar(String plate) {
        parkbanRepository.getCarByPlateNoAndDate(plate, new ParkbanRepository.DataBaseCarResultCallBack() {
            @Override
            public void onSuccess(Car item) {
                car = item;
                if (car == null) {
                    isNewCar = true;
                    newCar.setValue(true);
                    showParkingSpaceDialog(context);
                } else {
                    parkbanRepository.checkIsExitOfCar(car.getId(), new ParkbanRepository.DataBaseBooleanCallBack() {
                        @Override
                        public void onSuccess(boolean exit) {
                            if (exit) {
                                isNewCar = true;
                                newCar.setValue(true);
                                showParkingSpaceDialog(context);
                            } else {

                                /**check distance between 2 park (Margin Limit)**/

                                if (BaseActivity.marginLimit != 0) {
                                    parkbanRepository.getAllCarPlates(car.getId(), new ParkbanRepository.DataBaseCarPlatesResultCallBack() {
                                        @Override
                                        public void onSuccess(List<CarPlate> carPlateList) {
                                            List<CarPlate> carPlates = new ArrayList<>();
                                            carPlates = carPlateList;

                                            if (carPlates.size() > 0) {
                                                java.sql.Date recordDate = carPlates.get(carPlates.size() - 1).getRecordDate();
                                                long dateDiff = DateTimeHelper.getDateDifferent(recordDate, new Date(), TimeUnit.MINUTES);
                                                if (dateDiff > BaseActivity.marginLimit) {
                                                    isNewCar = true;
                                                    newCar.setValue(true);
                                                    showParkingSpaceDialog(context);

//                                                                for (int i = 0; i < spaceList.size(); i++) {
//                                                                    if (spaceList.get(i).getId() == car.getParkingSpaceId())
//                                                                        spaceList.get(i).setSpaceStatus(ParkingSpaceStatus.EMPTY);
//                                                                }

                                                } else {
                                                    isNewCar = false;
                                                    newCar.setValue(false);
                                                    if (car != null)
                                                        for (ParkingSpaceDto p : BaseActivity.parkingSpaceList) {
                                                            if (p.getId() == car.getParkingSpaceId()) {
                                                                parkingSpaceName.setValue(p.getName());
                                                                parkingSpaceSelected = p;
                                                            }
                                                        }
                                                }

                                            }
                                        }

                                        @Override
                                        public void onFailed() {

                                        }
                                    });
                                } else {

                                    isNewCar = false;
                                    newCar.setValue(false);
                                    for (ParkingSpaceDto p : BaseActivity.parkingSpaceList) {
                                        if (p.getId() == car.getParkingSpaceId()) {
                                            parkingSpaceName.setValue(p.getName());
                                            parkingSpaceSelected = p;
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailed() {

                        }
                    });
                }
            }

            @Override
            public void onFailed() {

            }
        });
    }

    public void editPlateClick(View view) {
        EditPlateDialog dialog = new EditPlateDialog();
        dialog.setCallBack(new EditPlateDialog.DialogCallBack() {
            @Override
            public void onCallBack(String plate, String part0, String part1, String part2, String part3, RidingType ridingType, String state) {
                ridingTypeDetected = ridingType;
                showParkingSpace.setValue(true);
                if (ridingType == RidingType.CAR) {
                    carType.setValue(true);
                    motorType.setValue(false);

                    carPlate.setPart0(part0);
                    carPlate.setPart1(part1);
                    carPlate.setPart2(part2);
                    carPlate.setPart3(part3);
                    carPlate.setPlateNumber(FontHelper.toEnglishNumber(part0) + part1 + FontHelper.toEnglishNumber(part2) + FontHelper.toEnglishNumber(part3));

                } else {
                    carType.setValue(false);
                    motorType.setValue(true);

                    carPlate.setPart0(part0);
                    carPlate.setPart1(part1);
                    carPlate.setPlateNumber(FontHelper.toEnglishNumber(part0) + FontHelper.toEnglishNumber(part1));
                }

                //show debt on crouton
                //crouton.show();
                getUnregisteredCarDebt(carPlate.getPlateNumber());

                mustNotSave = false;
                parkingSpaceSelected = null;
                parkingSpaceName.setValue(context.getResources().getString(R.string.unknown));
                checkCar(plate);
                setCarPlateLiveData(carPlate);
                saveEnable.setValue(true);
                carPlate.setEditedPlate(VerificationStatus.NeedVerify.ordinal());
            }
        });
        if (detectedPlate)
            dialog.setPlate(carPlate.getPart0(), carPlate.getPart1(), carPlate.getPart2(), carPlate.getPart3(), detectedPlate,
                    ridingTypeDetected);
        dialog.show(((Activity) context).getFragmentManager(), "");

    }

    public void parkingSpaceClick(final View view) {

        if (car == null)
            showParkingSpaceDialog(view.getContext());

        if (car != null && isNewCar == true)
            showParkingSpaceDialog(view.getContext());

        if (car != null && isNewCar == false) {
            EditParkSpaceDialog dialog = new EditParkSpaceDialog();
            String carParkName = null;
            for (int i = 0; i < parkingSpaces.size(); i++) {
                if (parkingSpaces.get(i).getId() == car.getParkingSpaceId())
                    carParkName = parkingSpaces.get(i).getName();
            }
            dialog.setItem(carParkName);
            dialog.setCallBack(new EditParkSpaceDialog.DialogCallBack() {
                @Override
                public void onCallBack(String state) {
                    if (state == "newPark") {
                        statusOfPark = "NewCar";
//                        isNewCar = true;
//                        newCar.setValue(true);
//                        updateCarPlate = true;
                        showParkingSpaceDialog(view.getContext());
                    } else if (state == "confirm") {
                        statusOfPark = "UpdateCar";
//                        isNewCar = false;
//                        newCar.setValue(false);
//                        updateCar = true;
                        showParkingSpaceDialog(view.getContext());
                    }
                }
            });
            dialog.show(((Activity) view.getContext()).getFragmentManager(), "");
        }
    }

    private void showParkingSpaceDialog(final Context context) {

        final CustomDialogFragment dialogFragment = new CustomDialogFragment();
        dialogFragment.setItems(spaceList, R.string.parking_space, carPlate.getPart0(), carPlate.getPart1(), carPlate.getPart2(), carPlate.getPart3(), this, parkbanRepository, ridingTypeDetected);
        dialogFragment.setCallBack(new CustomDialogFragment.DialogCallBack() {
            @Override
            public void onCallBack(Object selectedItem) {
                parkingSpaceSelected = (ParkingSpaceDto) selectedItem;
                parkingSpaceName.setValue((parkingSpaceSelected).getName());

                if (statusOfPark == "NewCar") {
                    isNewCar = true;
                    newCar.setValue(true);
                    updateCarPlate = true;
                } else if (statusOfPark == "UpdateCar") {
                    isNewCar = false;
                    newCar.setValue(false);
                    updateCar = true;
                }

                if (parkingSpaceSelected.getSpaceStatus() == ParkingSpaceStatus.FULL) {

                    //**  check for last pic has this space more than T time  **//
                    parkbanRepository.getParkSpaceFull(DateTimeHelper.getCurrentTimeForDB() - BaseActivity.marginLimit, new ParkbanRepository.DataBaseParkSpaceCallBack() {
                        @Override
                        public void onSuccess(List<Long> parkFull) {
                            for (int i = 0; i < parkFull.size(); i++) {
                                if (parkingSpaceSelected.getId() == parkFull.get(i)) {

                                    final ConfirmMessageDialog confirmDialog = new ConfirmMessageDialog();
                                    confirmDialog.setMessage(context.getString(R.string.park_space_busy));

                                    confirmDialog.setCallBack(new ConfirmMessageDialog.DialogCallBack() {
                                        @Override
                                        public void onCallBack(String state) {
                                            if (state == ConfirmMessageDialog.CANCEL) {
                                                showParkingSpaceDialog(context);
                                                // confirmDialog.dismiss();
                                            } else if (state == ConfirmMessageDialog.CONFIRM) {
                                                // confirmDialog.dismiss();
                                                dialogFragment.dismiss();
                                            }
                                        }
                                    });

                                    confirmDialog.show(((Activity) context).getFragmentManager(), "");
                                }
                            }

                        }

                        @Override
                        public void onFailed() {

                        }
                    });
                }
            }
        });

        dialogFragment.show(((Activity) context).getFragmentManager(), "");

    }

    private void updateCarPlate(int carId, final Context context) {

        parkbanRepository.getLastAndUpdateCarPlate(carId, new ParkbanRepository.DataBaseCarPlateUpdateCallBack() {
            @Override
            public void onSuccess() {
                Log.i("+++++++++++++++++++", "carplate update");
            }

            @Override
            public void onFailed() {
                Log.i("++++++++++==", "failed and show dialog");
                ShowToast.getInstance().showError(context, R.string.update_failed);
                showParkingSpaceDialog(context);
            }
        });
    }

    private void updateCarParkSpace(Car car) {

        parkbanRepository.updateCar(car, new ParkbanRepository.DataBaseCarPlateUpdateCallBack() {
            @Override
            public void onSuccess() {
                Log.i("+++++++++++++++++++", "car update");
            }

            @Override
            public void onFailed() {

            }
        });
    }

    public void getExitCheckStatus(View view) {
//        checkBox = (CheckBox) view;
//        exit = ((CheckBox) view).isChecked();
        if (exit.getValue())
            exit.setValue(false);
        else
            exit.setValue(true);
    }

    public void showCameraClick(View view) {
        openCamera();
    }

    private void openCamera() {

        if (crouton != null)
            crouton.hide();

        BaseActivity.locationTracker.checkGpsAvailability(context);

        if (!BaseActivity.locationTracker.isGPSEnabled)
            return;

        carPlate = new CarPlate();

        saveEnable.setValue(false);
        showParkingSpace.setValue(false);

        if (BaseActivity.locationTracker.checkMockLocation()) {

            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);

            ShowToast.getInstance().showError(context, R.string.fake_location);
            return;
        }

        location = BaseActivity.locationTracker.getLocation();
        if (location == null) {
            Log.i("============>", "loc null in show camera");
            GetLocationDialog dialog = new GetLocationDialog();
            dialog.show(((Activity) context).getFragmentManager(), "");
            return;
        }

        //******************************* comment just for QC

        long lastRecordTime = Preferences.getLastTime(context);
        if (lastRecordTime != 0)
            if (location.getTime() <= lastRecordTime) {

                final ConfirmMessageDialog confirmDialog = new ConfirmMessageDialog();
                confirmDialog.setMessage(context.getString(R.string.time_not_refresh));

                confirmDialog.setCallBack(new ConfirmMessageDialog.DialogCallBack() {
                    @Override
                    public void onCallBack(String state) {
                        if (state == ConfirmMessageDialog.CANCEL) {
                            Intent i = new Intent(context, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(i);
                        } else if (state == ConfirmMessageDialog.CONFIRM) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(intent);
                        }
                    }
                });

                confirmDialog.show(((Activity) context).getFragmentManager(), "");

                return;
            }

        //***********************************************

        Log.i("========------==>", "BeginTimeShift " + BaseActivity.BeginTimeShift);
        boolean isShift = BaseActivity.BeginTimeShift == -1 ? false :
                DateTimeHelper.checkShiftTime(BaseActivity.BeginTimeShift, BaseActivity.EndTimeShift);

        Log.i("========------==>", "isShift " + isShift);
        if (!isShift) {
            Intent i = new Intent(context, LoginActivity.class);
            context.startActivity(i);
            ShowToast.getInstance().showWarning(context, R.string.end_shift);
            return;
//            ((BaseActivity) context).getPrakbanCurrentShift(BaseActivity.CurrentUserId);
//
//            boolean checkShiftAgain = DateTimeHelper.checkShiftTime(BaseActivity.BeginTimeShift, BaseActivity.EndTimeShift);
//
//            if (checkShiftAgain) {
//                BaseActivity.parkingSpaceList.clear();
//                BaseActivity.parkingSpaceList = BaseActivity.parkingSpaceListShift;
//            } else {
//                ShowToast.getInstance().showWarning(context, R.string.no_shift);
//                return;
//            }
        }

        Log.i("========------==>", "parkingSpaces.size() " + parkingSpaces.size());
        if (parkingSpaces.size() == 0) {
            ShowToast.getInstance().showWarning(context, R.string.no_shift);
            return;
        }

        getParkSpaceFull();

        resetActivity(context);
        carPhoto.setValue(false);
        takeNewPhoto(context);
    }

    public void saveRecordClick(final View view) {

        if (!saveEnable.getValue()) {
            return;
        }

        Log.i("++++++++++==", "save");
        save(view.getContext(), false);
    }

    public void saveAndPayRecordClick(final View view) {

        if (!exit.getValue())
            return;

        save(view.getContext(), true);

        parkbanRepository.getFirstRecordDate(car.getId(), new ParkbanRepository.DataBaseRecordDateCallBack() {
            @Override
            public void onSuccess(Date recordDate) {
                Date firstDate = new Date(recordDate.getTime());
                int hour1 = firstDate.getHours();
                int minutes1 = firstDate.getMinutes();
                int second1 = firstDate.getSeconds();

                Date lastDate = new Date(carPlate.getRecordDate().getTime());
                int hour2 = lastDate.getHours();
                int minutes2 = lastDate.getMinutes();
                int second2 = lastDate.getSeconds();

                String firstTime = hour1 + ":" + minutes1 + ":" + second1;
                String lastTime = hour2 + ":" + minutes2 + ":" + second2;

                Intent paymentIntent = new Intent(context, PaymentActivity.class);
                paymentIntent.putExtra(PaymentViewModel.FirstTime, firstTime);

                paymentIntent.putExtra(PaymentViewModel.LastTime, lastTime);
                paymentIntent.putExtra(PaymentViewModel.RecordDate, carPlate.getRecordDate().getTime());
                paymentIntent.putExtra(PaymentViewModel.ParkSpaceId, car.getParkingSpaceId());
                paymentIntent.putExtra(PaymentViewModel.Plate0, carPlate.getPart0());
                paymentIntent.putExtra(PaymentViewModel.Plate1, carPlate.getPart1());
                paymentIntent.putExtra(PaymentViewModel.Plate2, carPlate.getPart2());
                paymentIntent.putExtra(PaymentViewModel.Plate3, carPlate.getPart3());

                paymentIntent.putExtra(PaymentViewModel.Lat, car.getLatitude());
                paymentIntent.putExtra(PaymentViewModel.Long, car.getLongitude());
                paymentIntent.putExtra(PaymentViewModel.PlateFileName, carPlate.getPlateFileName());
                paymentIntent.putExtra(PaymentViewModel.ParkId, carPlate.getId());
                paymentIntent.putExtra(PaymentViewModel.FirstDate, recordDate.getTime());
                paymentIntent.putExtra(PaymentViewModel.Plate, carPlate.getPlateNumber());
                paymentIntent.putExtra(PaymentViewModel.EditedPlate, carPlate.getEditedPlate());
                paymentIntent.putExtra(PaymentViewModel.RidingType, ridingTypeDetected.ordinal());

                context.startActivity(paymentIntent);
            }

            @Override
            public void onFailed() {

            }
        });

    }

    private void save(final Context context, final boolean pay) {

        if (parkbanRepository == null) {
            ParkbanDatabase.getInstance(context.getApplicationContext(), new ParkbanDatabase.DatabaseReadyCallback() {
                @Override
                public void onReady(ParkbanDatabase instance) {
                    parkbanRepository = new ParkbanRepository(instance);
                }
            });
        }

        if (carPlate.getPlateNumber() == null) {
            saveEnable.setValue(false);
            return;
        }

        if (!saveEnable.getValue()) {
            return;
        }

        if (parkingSpaceSelected == null) {
            ShowToast.getInstance().showWarning(context, R.string.select_park_space);
            return;
        }

        if (updateCar) {
            car.setParkingSpaceId(parkingSpaceSelected.getId());
            for (ParkingSpaceDto p : BaseActivity.parkingSpaceList) {
                if (p.getId() == car.getParkingSpaceId())
                    parkingSpaceName.setValue(p.getName());
            }

            updateCarParkSpace(car);
        }

        if (updateCarPlate) {
            updateCarPlate(car.getId(), context);
        }

        if (mustNotSave) {
            ShowToast.getInstance().showError(context, R.string.not_detect);
            return;
        }

        if (isNewCar)
            carPlate.setParkingSpaceId(parkingSpaceSelected.getId());

        carPlate.setExitBySystem(exitBySystem);
        carPlate.setExit(exit.getValue());
        carPlate.setLongitude(BaseActivity.locationTracker.getLocation().getLongitude());
        carPlate.setLatitude(BaseActivity.locationTracker.getLocation().getLatitude());

        crouton.hide();

        progress.setValue(10);
        int carId = 0;
        if (car == null)
            carId = 0;
        else carId = car.getId();
        parkbanRepository.saveCarPlate(isNewCar, carId, carPlate, pay, new ParkbanRepository.DataBaseResultCallBack() {
            @Override
            public void onSuccess(long id) {

                if (carPlate.getRecordDate() != null)
                    Preferences.setLastTime(carPlate.getRecordDate().getTime(), context);

                progress.setValue(0);
                carPlate.setId(id);
                ShowToast.getInstance().showSuccess(context, R.string.save_success);
                resetActivity(context);
                mustNotSave = true;
                carPhoto.setValue(false);
                showParkingSpace.setValue(false);

            }

            @Override
            public void onFailed() {
                progress.setValue(0);
                ShowToast.getInstance().showError(context, R.string.save_failed);
                return;
            }
        });
    }

    public void resetActivity(Context context) {
        newCar.setValue(true);
        exit.setValue(false);
        Bitmap bitmap = ImageLoadHelper.getInstance().convertDrawableToBitmap(context, R.drawable.camera);
        setCarPlateLiveData(new CarPlate(bitmap, "", "", "", ""));
        parkingSpaceSelected = null;
        // parkingSpaceName.setValue(context.getResources().getString(R.string.unknown));
        carImageBitmap.setValue(null);
        parkingSpaceName.setValue(context.getResources().getString(R.string.unknown));
        saveEnable.setValue(false);

    }

    public void showCarsList(View view) {
        Intent i = new Intent(view.getContext(), ListPlatesActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        view.getContext().startActivity(i);
    }

    public void processActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            try {
                plateBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photoURI);

            } catch (IOException e) {
                e.printStackTrace();
            }
             //plateBitmap = ImageLoadHelper.getInstance().loadImage(context, newPhotoFilePath);
            carPlate.setPlateImage(plateBitmap);
            carPlateLiveData.setValue(carPlate);
            doDetectPlate(context, plateBitmap);
            carPlate.setRecordDate(BaseActivity.locationTracker.getDateAndTime());
            carPhoto.setValue(true);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            //show toast
        }
    }

    public File createPrivateImageFile(Context context) throws IOException {
        String timeStamp = DateTimeHelper.DateToStr(new Date(), MEDIA_FILE_TIMESTAMP_FORMAT);
        String imageFileName = "PLATE_" + timeStamp;
        carPlate.setPlateFileName(imageFileName);
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File storageDir = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES);

        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public void takeNewPhoto(Context context) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createPrivateImageFile(context);
                newPhotoFilePath = photoFile.getAbsolutePath();

                //set file name in model for save to database
                carPlate.setPlateFileName(photoFile.getName());

            } catch (Exception e) {
                Log.e(TAG, "error in createPrivateImageFile", e);
            }

            if (photoFile != null) {
                try {


                    //for Android 4.4-
//                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
//                        photoURI = Uri.fromFile(photoFile);
//                    } else
                    photoURI = FileProvider.getUriForFile(context, "com.eos.parkban", photoFile);

                    List<ResolveInfo> resolvedIntentActivities =context.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                        String packageName = resolvedIntentInfo.activityInfo.packageName;
                        context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    //takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    ((Activity) context).startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    public static void revokeFileReadPermission(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            String dirpath = context.getFilesDir() + File.separator + "directory";
            File file = new File(dirpath + File.separator + "file.txt");
            Uri uri = FileProvider.getUriForFile(context, "com.eos.parkban", file);
            context.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    public void processOnSaveInstance(Bundle outState) {
        outState.putString(NEW_PHOTO_FILE_PATH_KEY, newPhotoFilePath);
    }

    public void processOnRestoreInstance(Bundle savedInstanceState) {
        newPhotoFilePath = savedInstanceState.getString(NEW_PHOTO_FILE_PATH_KEY);
    }

    private void getUnregisteredCarDebt(String plate) {
        parkbanRepository.getUnregisteredCarDebt(plate, new ParkbanRepository.ServiceResultCallBack<LongResultDto>() {
            @Override
            public void onSuccess(LongResultDto result) {
                if (result.getValues() > 0) {
                    debtValue.setText(FontHelper.RialFormatter(result.getValues()));
                    crouton = Crouton.make(((Activity) context), crotounView, R.id.main_layout, croutonConfig);
                    crouton.show();
                }
            }

            @Override
            public void onFailed(ResponseResultType resultType, String message, int errorCode) {

            }
        });
    }

    public void cancelCrouton() {
        crouton.cancel();
    }

    private void addNewImage(Context context, String imageFilePath) {
        plateBitmap = ImageLoadHelper.getInstance().loadImage(context, imageFilePath);
        // carPlateLiveData.setValue(new CarPlate(ImageLoadHelper.getInstance().loadImage(context, imageFilePath), new Date()));
        if (plateBitmap != null)
            doDetectPlate(context, plateBitmap);
    }

    public void onPostResumeActivity(Context context) {
        if (newPhotoFilePath != null) {
            addNewImage(context, newPhotoFilePath);
        }
    }


}
