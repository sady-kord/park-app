package com.eos.parkban.viewmodels;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.view.View;

import com.eos.parkban.BaseActivity;
import com.eos.parkban.R;
import com.eos.parkban.adapters.PlateListAdapter;
import com.eos.parkban.dialogs.CarTimesDialog;
import com.eos.parkban.dialogs.RecordsStatusDialog;
import com.eos.parkban.helper.ImageLoadHelper;
import com.eos.parkban.helper.Messenger;
import com.eos.parkban.persistence.ParkbanDatabase;
import com.eos.parkban.persistence.models.Car;
import com.eos.parkban.persistence.models.CarItems;
import com.eos.parkban.persistence.models.CarPlate;
import com.eos.parkban.persistence.models.CarPlateHistory;
import com.eos.parkban.persistence.models.ResponseResultType;
import com.eos.parkban.persistence.models.SendStatus;
import com.eos.parkban.services.dto.CarRecordsDto;
import com.eos.parkban.services.dto.SendRecordResultDto;
import com.eos.parkban.repositories.ParkbanRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ProgressDialog;

public class PlateListViewModel extends ViewModel {

    private MutableLiveData<Car> carLiveData;
    private LiveData<List<Car>> cars;
    private ParkbanRepository parkbanRepository;
    private int successCount, failedCount = 0;
    private RecordsStatusDialog recordDialog;
    private Context context;
    private ProgressDialog progressDialog;
    private ParkbanRepository carRepository;
    private int sendingRecordCount = 0;

    public LiveData<Car> getCarLiveDate() {
        if (carLiveData == null) {
            carLiveData = new MutableLiveData<>();
        }
        return carLiveData;
    }

    public void init(final Context context) {

        if (parkbanRepository == null) {
            ParkbanDatabase.getInstance(context.getApplicationContext(), new ParkbanDatabase.DatabaseReadyCallback() {
                @Override
                public void onReady(ParkbanDatabase instance) {
                    parkbanRepository = new ParkbanRepository(instance);
                    getCarFromDatabase(context);
                }
            });
        }
    }


    private PlateListAdapter plateListAdapter = new PlateListAdapter();

    public PlateListAdapter getPlateListAdapter() {
        return plateListAdapter;
    }

    private void getCarFromDatabase(final Context context) {
        this.context = context;

        ((BaseActivity) context).showProgress(true);

        cars = parkbanRepository.getCars();

        cars.observe((BaseActivity) context, new Observer<List<Car>>() {
            @Override
            public void onChanged(@Nullable final List<Car> cars) {

                for (final Car car : cars) {
                    LiveData<List<CarPlate>> carPlates = parkbanRepository.getCarPlates(car.getId());
                    carPlates.observe((BaseActivity) context, new Observer<List<CarPlate>>() {
                        @Override
                        public void onChanged(@Nullable List<CarPlate> carPlates) {
                            car.getCarPlates().addAll(carPlates);

                            plateListAdapter.setCarList(cars);

                        }
                    });
                }

                plateListAdapter.setCarList(cars);
                ((BaseActivity) context).showProgress(false);
            }
        });

    }

    public void onCarItemSelected(View view, CarItems item) {
        Car car = findCarById(item.getCarId());
        if (car == null)
            return;

        CarTimesDialog dialog = new CarTimesDialog();
        ArrayList<CarPlateHistory> carPlateHistories = new ArrayList<>();

        for (int i = 0; i < car.getCarPlates().size(); i++) {
            CarPlateHistory carPlateHistory = new CarPlateHistory();
            Date date = new Date(car.getCarPlates().get(i).getRecordDate().getTime());
            String time = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
            carPlateHistory.setTime(time);
            carPlateHistory.setStatus(car.getCarPlates().get(i).getStatus());

            carPlateHistories.add(carPlateHistory);
        }
        dialog.setItem(carPlateHistories);
        dialog.show(((Activity) context).getFragmentManager(), "");
    }

    private Car findCarById(long carId) {
        for (int i = 0; i < cars.getValue().size(); i++) {
            if (cars.getValue().get(i).getId() == carId)
                return cars.getValue().get(i);
        }
        return null;
    }

    public void sendRecords(View view) {

        sendingRecordCount = 0;

        for (int i = 0; i < cars.getValue().size(); i++) {
            for (int j = 0; j < cars.getValue().get(i).getCarPlates().size(); j++) {
                if (cars.getValue().get(i).getCarPlates().get(j).getStatus() == SendStatus.FAILED.ordinal() ||
                        cars.getValue().get(i).getCarPlates().get(j).getStatus() == SendStatus.PENDING.ordinal()) {
                    sendingRecordCount += 1;
                }
            }
        }

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(view.getContext());
            progressDialog.setMessage("sending records");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMax(sendingRecordCount);
            progressDialog.show();
        }

        new sendRecordsToServerAsyncTask().execute(view.getContext());
    }

    private class sendRecordsToServerAsyncTask extends AsyncTask<Context, Integer, Void> {

        @Override
        protected Void doInBackground(Context... params) {
            successCount = 0;
            failedCount = 0;

            for (int i = 0; i < cars.getValue().size(); i++) {
                Car car = cars.getValue().get(i);

                final CarRecordsDto carRecords = new CarRecordsDto();

                for (int j = 0; j < car.getCarPlates().size(); j++) {

                    if (car.getCarPlates().get(j).getStatus() == SendStatus.FAILED.ordinal() ||
                            car.getCarPlates().get(j).getStatus() == SendStatus.PENDING.ordinal()) {

                        carRecords.setLatitude(car.getLatitude());
                        carRecords.setLongitude(car.getLongitude());
                        carRecords.setPlateNo(car.getPlateNo());
                        carRecords.setUserId(BaseActivity.CurrentUserId);
                        carRecords.setFirstParkDate(new Date(car.getCarPlates().get(0).getRecordDate().getTime()));
                        carRecords.setImageByteArray(ImageLoadHelper.getInstance().convertImageFileToIntArray(params[0], car.getCarPlates().get(j).getPlateFileName()));

                        carRecords.setDateTime(new Date(car.getCarPlates().get(j).getRecordDate().getTime()));
                        carRecords.setExit(car.getCarPlates().get(j).isExit());
                        carRecords.setParkId(car.getCarPlates().get(j).getId());
                        carRecords.setParkingSpaceId(car.getParkingSpaceId());


                        sendRecordToServer(params[0], carRecords);
                        publishProgress(sendingRecordCount);

                    }
                }
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... updates) {
            super.onProgressUpdate(updates);
            progressDialog.setProgress(updates[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.hide();
            if (sendingRecordCount == 0) {
                Messenger.shortWarningMessage(context, "اطلاعات جدیدی جهت ارسال یافت نشد");
            }
        }
    }

    private void sendRecordToServer(final Context context, CarRecordsDto carRecords) {
        parkbanRepository.sendRecord(carRecords,
                new ParkbanRepository.ServiceResultCallBack<SendRecordResultDto.SendRecordStatus>() {
                    @Override
                    public void onSuccess(final SendRecordResultDto.SendRecordStatus result) {

                        // if (result.isStatus()) {
                        parkbanRepository.getAndUpdateCarPlate(result.getParkId(), result.isStatus(), new ParkbanRepository.DataBaseCarPlateUpdateCallBack() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailed() {

                            }
                        });

                        if (result.isStatus())
                            successCount += 1;
                        else
                            failedCount += 1;

                        if (successCount + failedCount == sendingRecordCount) {
                            recordDialog = new RecordsStatusDialog();
                            recordDialog.setItem(String.valueOf(sendingRecordCount), String.valueOf(successCount), String.valueOf(failedCount));
                            recordDialog.show(((Activity) context).getFragmentManager(), "");

                            getCarFromDatabase(context);
                        }


                    }

                    @Override
                    public void onFailed(ResponseResultType resultType, String message, int errorCode) {

                        switch (resultType) {
                            case RetrofitError:
                                Messenger.showErrorMessage(context, R.string.exception_msg);
                                break;
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

}
