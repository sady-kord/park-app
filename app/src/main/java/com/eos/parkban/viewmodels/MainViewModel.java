package com.eos.parkban.viewmodels;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.eos.parkban.BaseActivity;
import com.eos.parkban.ChargeReportActivity;
import com.eos.parkban.DriverChargeActivity;
import com.eos.parkban.FunctionalityReportActivity;
import com.eos.parkban.ListPlatesActivity;
import com.eos.parkban.MainActivity;
import com.eos.parkban.R;
import com.eos.parkban.RecordPlateActivity;
import com.eos.parkban.SettingActivity;
import com.eos.parkban.ShiftActivity;
import com.eos.parkban.dialogs.ReportDialog;
import com.eos.parkban.helper.DateTimeHelper;
import com.eos.parkban.helper.ExportDataBase;
import com.eos.parkban.helper.GPSTracker;
import com.eos.parkban.helper.Messenger;
import com.eos.parkban.helper.Preferences;
import com.eos.parkban.helper.ShowToast;
import com.eos.parkban.persistence.ParkbanDatabase;
import com.eos.parkban.persistence.models.Car;
import com.eos.parkban.persistence.models.CarPlate;
import com.eos.parkban.persistence.models.ResponseResultType;
import com.eos.parkban.repositories.ParkbanRepository;
import com.eos.parkban.services.dto.BooleanResultDto;
import com.eos.parkban.services.dto.IntResultDto;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {

    private final int interval = 10000; // 1 Second
    private Handler handler = new Handler();
    private ParkbanRepository parkbanRepository;
    private List<CarPlate> plateList;
    private File storageDir;
    private boolean enableCharge;
    private boolean doubleBackToExitPressedOnce = false;
    private static final long EXIT_TIMEOUT = 3000;
    private ParkbanDatabase database;

    public void init(final Context context) {

        ((BaseActivity) context).setHandler();

        if (parkbanRepository == null) {
            ParkbanDatabase.getInstance(context.getApplicationContext(), new ParkbanDatabase.DatabaseReadyCallback() {
                @Override
                public void onReady(ParkbanDatabase instance) {
                    database = instance;
                    parkbanRepository = new ParkbanRepository(instance);
                    //deleteLastCarPlateSent();
                    getLastCarPlatesSent();
                    updateAllCarPlateStatus();
                    getParkMarginLimit(context);
                }
            });
        }

        storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    private void getParkMarginLimit(final Context context) {
        parkbanRepository.getParkMarginLimit(new ParkbanRepository.ServiceResultCallBack<IntResultDto>() {
            @Override
            public void onSuccess(IntResultDto result) {
                BaseActivity.marginLimit = result.getValue();
            }

            @Override
            public void onFailed(ResponseResultType resultType, String message, int errorCode) {
                switch (resultType) {
                    case RetrofitError: {
                        ShowToast.getInstance().showError(context, R.string.exception_msg);
                        ShowToast.getInstance().showErrorStringMsg(context, message);
                        break;
                    }
                    case ServerError:
                        if (errorCode != 0)
                            Messenger.showErrorMessageFail(context, message, errorCode);
                        else
                            Messenger.showErrorMessage(context, R.string.connection_failed);
                        break;
                    default:
                        Messenger.showErrorMessageWithResponse(context, resultType.ordinal());
                }
            }
        });
    }

    private void hasParkbanCredit(long amount, final Context context) {
        parkbanRepository.hasParkbanCredit(amount, new ParkbanRepository.ServiceResultCallBack<BooleanResultDto>() {
            @Override
            public void onSuccess(BooleanResultDto result) {
                if (result.getValue()) {
                    enableCharge = true;
                    Intent i = new Intent(context, DriverChargeActivity.class);
                    context.startActivity(i);
                } else {
                    enableCharge = false;
                    ShowToast.getInstance().showError(context, R.string.credit_not_enough);
                    return;
                }
            }

            @Override
            public void onFailed(ResponseResultType resultType, String message, int errorCode) {
                switch (resultType) {
                    case RetrofitError: {
                        ShowToast.getInstance().showErrorStringMsg(context, message);
                        ShowToast.getInstance().showError(context, R.string.exception_msg);
                        break;
                    }
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

    public void CameraLayoutClick(View view) {
        Intent i = new Intent(view.getContext(), RecordPlateActivity.class);
        view.getContext().startActivity(i);
    }

    public void SendRecordsClick(View view) {
        Intent i = new Intent(view.getContext(), ListPlatesActivity.class);
        view.getContext().startActivity(i);
    }

    public void ChargeClick(View view) {

        hasParkbanCredit(20000, view.getContext());

    }

    public void ShiftClick(View view) {
        Intent i = new Intent(view.getContext(), ShiftActivity.class);
        view.getContext().startActivity(i);

        //File appDBFile = view.getContext().getDatabasePath("parkban_db");

        //database.close();

        //new ExportDataBase(appDBFile,"Db1").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // new ExportAppDataAsyncTask(appDBFile, "Db1")
        //       .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private static class ExportAppDataAsyncTask extends AsyncTask<Object, Integer, Integer> {

        private final File appDbFile;
        private final String exportFileName;

        public ExportAppDataAsyncTask(File appDbFile, String exportFileName) {
            this.appDbFile = appDbFile;
            this.exportFileName = exportFileName;
        }

        @Override
        protected Integer doInBackground(Object... params) {
            try {

                String path = Environment.getExternalStorageDirectory().getPath() + "/TestDb/";
                File dbFile = new File(path, "parkban_db");

                checkAndCreateDirectory(path);

                copyFile(appDbFile, dbFile);


            } catch (Exception e) {
                return 0;
            }
            return 1;
        }

    }

    public static void checkAndCreateDirectory(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdir())
                throw new IOException("mkdir Error:" + path);
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new IOException("copyFile Error", e);
            }
        } else {
            try {
                doCopyFile(sourceFile, destFile);
            } catch (Exception e) {
                throw new IOException("copyFile Error", e);
            }
        }
    }

    private static void doCopyFile(File sourceFile, File destFile) throws IOException {
        try (InputStream in = new FileInputStream(sourceFile)) {
            try (OutputStream out = new FileOutputStream(destFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    public void SettingClick(View view) {
        Intent i = new Intent(view.getContext(), SettingActivity.class);
        view.getContext().startActivity(i);

    }

    public void ReportClick(View view) {
        final Context context = view.getContext();
        ReportDialog dialog = new ReportDialog();
        dialog.setCallBack(new ReportDialog.DialogCallBack() {
            @Override
            public void onCallBack(String state) {
                if (state == ReportDialog.Func) {
                    Intent i = new Intent(context, FunctionalityReportActivity.class);
                    context.startActivity(i);
                } else if (state == ReportDialog.Charge) {
                    Intent i = new Intent(context, ChargeReportActivity.class);
                    context.startActivity(i);
                }
            }
        });
        dialog.show(((Activity) view.getContext()).getFragmentManager(), "");
    }

    private void getLastCarPlatesSent() {
        parkbanRepository.getCarplateForDel(DateTimeHelper.getCurrentTimeForDB(), new ParkbanRepository.DataBaseCarPlatesResultCallBack() {
            @Override
            public void onSuccess(List<CarPlate> carPlates) {
                plateList = new ArrayList<>();
                plateList = carPlates;

                for (int i = 0; i < plateList.size(); i++) {
                    CarPlate plate = new CarPlate();
                    plate = plateList.get(i);

                    File file = new File(storageDir, plate.getPlateFileName());
                    boolean deleted = file.delete();

                    if (deleted)
                        deleteLastCarPlateSent(plate, i == plateList.size() - 1);
                }
            }

            @Override
            public void onFailed() {

            }
        });
    }

    private void deleteLastCarPlateSent(CarPlate carPlate, final boolean delete) {
        parkbanRepository.deleteCarPlatesSent(carPlate, new ParkbanRepository.DataBaseCarPlateUpdateCallBack() {
            @Override
            public void onSuccess() {
                if (delete)
                    deleteCars();
                Log.i("------------->", "onSuccess");
            }

            @Override
            public void onFailed() {

            }
        });
    }

    private void deleteCars() {
        parkbanRepository.deleteCars(new ParkbanRepository.DataBaseCarPlateUpdateCallBack() {
            @Override
            public void onSuccess() {
                Log.i("------------->", "onSuccess");
            }

            @Override
            public void onFailed() {

            }
        });
    }

    private void getCars() {
        parkbanRepository.getCarForDel(new ParkbanRepository.DataBaseCarsResultCallBack() {
            @Override
            public void onSuccess(List<Car> cars) {
                List<Car> plates = new ArrayList<>();
                plates = cars;
            }

            @Override
            public void onFailed() {

            }
        });
    }

    private void updateAllCarPlateStatus() {
        parkbanRepository.updateAllCarPlateStatus(new ParkbanRepository.DataBaseCarPlateUpdateCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed() {

            }
        });
    }

    public void backPress(Context context) {
        if (doubleBackToExitPressedOnce) {
            ((BaseActivity) (context)).finish();
        }

        this.doubleBackToExitPressedOnce = true;

        ShowToast.getInstance().showExit(context, R.string.click_back_again);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, EXIT_TIMEOUT);
    }


}
