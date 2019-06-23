package com.eos.parkban.viewmodels;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.Toast;

import org.opencv.core.Mat;

import com.eos.parkban.BaseActivity;
import com.eos.parkban.ListPlatesActivity;
import com.eos.parkban.R;
import com.eos.parkban.anpr.farsi_ocr_anpr.FarsiOcrAnprProvider;
import com.eos.parkban.core.anpr.BaseAnprProvider;
import com.eos.parkban.core.anpr.helpers.PlateDetectionState;
import com.eos.parkban.core.anpr.onPlateDetectedCallback;
import com.eos.parkban.dialogs.ConfirmMessageDialog;
import com.eos.parkban.dialogs.CustomDialogFragment;
import com.eos.parkban.dialogs.ProgressDialog;
import com.eos.parkban.helper.DateTimeHelper;
import com.eos.parkban.helper.FontHelper;
import com.eos.parkban.helper.GPSTracker;
import com.eos.parkban.helper.ImageLoadHelper;
import com.eos.parkban.helper.Messenger;
import com.eos.parkban.helper.ShowToast;
import com.eos.parkban.persistence.ParkbanDatabase;
import com.eos.parkban.persistence.models.Car;
import com.eos.parkban.persistence.models.CarPlate;
import com.eos.parkban.persistence.models.ParkingSpace;
import com.eos.parkban.persistence.models.ParkingSpaceStatus;
import com.eos.parkban.repositories.ParkbanRepository;
import com.google.gson.annotations.Expose;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;

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
    private GPSTracker gpsTracker;
    private Location location;
    private Boolean isExit = false;
    private boolean mustNotSave = true;
    private MutableLiveData<String> parkingSpaceName;
    private ParkingSpace parkingSpaceSelected;
    private CheckBox checkBox;
    private MutableLiveData<Bitmap> carImageBitmap;
    private ParkbanRepository parkbanRepository;
    private boolean isNewCar = false;
    private MutableLiveData<Boolean> newCar;
    private Car car;
    private Dialog progressDialog;

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

        if (newCar == null)
            newCar = new MutableLiveData<>();
        newCar.setValue(true);

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

        if (parkbanRepository == null) {
            ParkbanDatabase.getInstance(context.getApplicationContext(), new ParkbanDatabase.DatabaseReadyCallback() {
                @Override
                public void onReady(ParkbanDatabase instance) {
                    parkbanRepository = new ParkbanRepository(instance);
                }
            });
        }

        parkingSpaceName.setValue(context.getResources().getString(R.string.unknown));
        setCarPlateLiveData(new CarPlate(ImageLoadHelper.getInstance().convertDrawableToBitmap(context, R.drawable.camera), "", "", "", ""));
    }

    public void checkGps(Activity context) {
        gpsTracker = new GPSTracker(context);
        location = gpsTracker.getLocation();
    }

    private void doDetectPlate(final Context context, Bitmap plateImage) {
        //((BaseActivity) context).showProgress(true);
        showProgress(context,true);
        anprProvider.getPlate(context, plateImage, new onPlateDetectedCallback() {
            @Override
            public void onPlateDetected(PlateDetectionState state, String plateNo, Bitmap plateImage, String part0, String part1, String part2, String part3) {
                if (state == PlateDetectionState.DETECTED) {
                    if (plateNo.contains("null")) {
                        Messenger.shortWarningMessage(context, context.getResources().getString(R.string.plate_not_detected));
                        showProgress(context,false);
                        mustNotSave = true;
                        return;
                    }
                    carPlate.setPart0(part0);
                    carPlate.setPart1(part1);
                    carPlate.setPart2(part2);
                    carPlate.setPart3(part3);
                    carPlate.setPlateNumber(FontHelper.toEnglishNumber(part0) + part1 + FontHelper.toEnglishNumber(part2) + FontHelper.toEnglishNumber(part3));
                    setCarPlateLiveData(carPlate);
                    mustNotSave = false;
                    carImageBitmap.setValue(carPlate.getPlateImage());

                    parkbanRepository.getCarByPlateNoAndDate(carPlate.getPlateNumber(), new ParkbanRepository.DataBaseCarResultCallBack() {
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
                                            isNewCar = false;
                                            newCar.setValue(false);
                                            for (ParkingSpace p : BaseActivity.parkingSpaceList) {
                                                if (p.getId() == car.getParkingSpaceId())
                                                    parkingSpaceName.setValue(p.getName());
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

                } else {
                    mustNotSave = true;
                    Messenger.shortWarningMessage(context, context.getResources().getString(R.string.plate_not_detected));
                }
                showProgress(context,false);
            }
        });
    }

    public void parkingSpaceClick(View view) {

         showParkingSpaceDialog(view.getContext());
    }

    private void showParkingSpaceDialog(Context context) {

        ArrayList<ParkingSpace> parkingSpaces = new ArrayList<>();
        parkingSpaces = (ArrayList<ParkingSpace>) BaseActivity.parkingSpaceList;
        final ArrayList<ParkingSpace> spaceList = new ArrayList<>();

        for (int i = 0; i < parkingSpaces.size(); i++) {
            Location locationB = new Location("point B");
            locationB.setLatitude(parkingSpaces.get(i).getLatitude());
            locationB.setLongitude(parkingSpaces.get(i).getLongitude());

            float distance = gpsTracker.getDistanceBetween(location, locationB);
            int round = Math.round(distance);
            parkingSpaces.get(i).setDistance(round);
            parkingSpaces.get(i).setSpaceStatus(ParkingSpaceStatus.EMPTY);
            spaceList.add(parkingSpaces.get(i));
        }

        Collections.sort(spaceList);

        final CustomDialogFragment dialogFragment = new CustomDialogFragment();
        dialogFragment.setItems(spaceList, R.string.parking_space, carPlate.getPart0(), carPlate.getPart1(), carPlate.getPart2(), carPlate.getPart3(), this,parkbanRepository, new CustomDialogFragment.DialogOnItemSelectedListener() {
            @Override
            public void OnItemSelected(Object selectedItem) {
                parkingSpaceSelected = (ParkingSpace) selectedItem;
                parkingSpaceName.setValue((parkingSpaceSelected).getName());
            }
        });

       // dialogFragment.setItems(spaceList, R.string.parking_space, carPlate.getPart0(), carPlate.getPart1(), carPlate.getPart2(), carPlate.getPart3(), this);

        dialogFragment.show(((Activity) context).getFragmentManager(), "");

//        parkbanRepository.getCarByPlaceId(parkingSpaceSelected.getId(), new ParkbanRepository.DataBaseBooleanCallBack() {
//            @Override
//            public void onSuccess(boolean exit) {
//                if (exit){
//                    ConfirmMessageDialog dialog = new ConfirmMessageDialog();
//                    dialog.setCallBack(new ConfirmMessageDialog.DialogCallBack() {
//                        @Override
//                        public void onCallBack(boolean confirm) {
//                            if (confirm){
//                                parkingSpaceName.setValue((parkingSpaceSelected).getName());
//                            }
//                        }
//                    });
//                }
//                else
//                    parkingSpaceName.setValue((parkingSpaceSelected).getName());
//            }
//
//            @Override
//            public void onFailed() {
//
//            }
//        });

    }

    public void getExitCheckStatus(View view) {
        checkBox = (CheckBox) view;
        isExit = ((CheckBox) view).isChecked();
    }

    public void showCameraClick(View view) {
        // init(view.getContext());
        takeNewPhoto(view.getContext());
    }

    public void saveRecordClick(final View view) {

        if (mustNotSave) {
            ShowToast.getInstance().showError(view.getContext(),R.string.not_detect);
           // Messenger.shortErrorMessage(view.getContext(), R.string.not_detect);
            return;
        }

        if (isNewCar)
            carPlate.setParkingSpaceId(parkingSpaceSelected.getId());
        carPlate.setExit(isExit);
        carPlate.setLatitude(location.getLatitude());
        carPlate.setLongitude(location.getLongitude());
        ((BaseActivity) view.getContext()).showProgress(true);

        ParkbanDatabase.getInstance(view.getContext().getApplicationContext(), new ParkbanDatabase.DatabaseReadyCallback() {
            @Override
            public void onReady(ParkbanDatabase instance) {
                int carId = 0;
                if (car == null)
                    carId = 0;
                else carId = car.getId();
                parkbanRepository.saveCarPlate(isNewCar, carId, carPlate, new ParkbanRepository.DataBaseResultCallBack() {
                    @Override
                    public void onSuccess(long id) {
                        ((BaseActivity) view.getContext()).showProgress(false);
                        //Messenger.showCompleteSave(view.getContext(), view.getContext().getResources().getString(R.string.save_success));
                        ShowToast.getInstance().showSuccess(view.getContext(),R.string.save_success);
                        resetActivity(view.getContext());
                        if (checkBox != null)
                            checkBox.setChecked(false);

                        mustNotSave = true;
                    }

                    @Override
                    public void onFailed() {
                        ((BaseActivity) view.getContext()).showProgress(false);
                        Messenger.showCompleteSave(view.getContext(), view.getContext().getResources().getString(R.string.save_failed));
                    }
                });
            }
        });


    }

    public void resetActivity(Context context) {
        isExit = false;
        Bitmap bitmap = ImageLoadHelper.getInstance().convertDrawableToBitmap(context, R.drawable.camera);
        setCarPlateLiveData(new CarPlate(bitmap, "", "", "", ""));
        parkingSpaceSelected = null;
       // parkingSpaceName.setValue(context.getResources().getString(R.string.unknown));
        carImageBitmap.setValue(null);
        newCar.setValue(true);

    }

    public void showCarsList(View view) {
        Intent i = new Intent(view.getContext(), ListPlatesActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        view.getContext().startActivity(i);
    }

    public void processActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            //  addNewImage(context, newPhotoFilePath);
            plateBitmap = ImageLoadHelper.getInstance().loadImage(context, newPhotoFilePath);
            carPlate.setPlateImage(plateBitmap);
            carPlateLiveData.setValue(carPlate);
            doDetectPlate(context, plateBitmap);
            carPlate.setRecordDate(gpsTracker.getDateAndTime(location));
        } else if (resultCode == Activity.RESULT_CANCELED) {
            //show toast
        }
    }

    public File createPrivateImageFile(Context context) throws IOException {
        String timeStamp = DateTimeHelper.DateToStr(new Date(), MEDIA_FILE_TIMESTAMP_FORMAT);
        String imageFileName = "PLATE_" + timeStamp;
        carPlate.setPlateFileName(imageFileName);
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
                    Uri photoURI = FileProvider.getUriForFile(context, "com.eos.parkban", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    ((Activity) context).startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    public void processOnSaveInstance(Bundle outState) {
        outState.putString(NEW_PHOTO_FILE_PATH_KEY, newPhotoFilePath);
    }

    public void processOnRestoreInstance(Bundle savedInstanceState) {
        newPhotoFilePath = savedInstanceState.getString(NEW_PHOTO_FILE_PATH_KEY);
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

    public void showProgress(Context context,final boolean show) {
        try {
            if (progressDialog == null) {
                progressDialog = new Dialog(context, android.R.style.Theme_Black);
                View view = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.control_progress_dialog, null);

                progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
                progressDialog.setContentView(view);
            }
            if (show)
                progressDialog.show();
            else
                progressDialog.dismiss();

        } catch (Exception e) {
            Log.i("progress ex", e.getMessage());
        }
    }
}
