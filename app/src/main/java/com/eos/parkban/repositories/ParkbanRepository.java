package com.eos.parkban.repositories;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.eos.parkban.persistence.ParkbanDatabase;
import com.eos.parkban.persistence.models.Car;
import com.eos.parkban.persistence.models.CarPlate;
import com.eos.parkban.persistence.models.ResponseResultType;
import com.eos.parkban.persistence.models.SendStatus;
import com.eos.parkban.services.dto.CarRecordsDto;
import com.eos.parkban.services.dto.IntResultDto;
import com.eos.parkban.services.dto.LoginResultDto;
import com.eos.parkban.services.dto.SendRecordResultDto;
import com.eos.parkban.services.ParkbanServiceProvider;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParkbanRepository {

    private static final String TAG = "ParkbanRepository";
    private final ParkbanDatabase database;

    public interface DataBaseResultCallBack {
        void onSuccess(long id);

        void onFailed();
    }

    public interface DataBaseBooleanCallBack {
        void onSuccess(boolean exit);
        void onFailed();
    }

    public interface DataBaseCarPlateResultCallBack {
        void onSuccess(CarPlate carPlate);

        void onFailed();
    }

    public interface DataBaseCarPlateUpdateCallBack {
        void onSuccess();

        void onFailed();
    }

    public interface DataBaseCarResultCallBack {
        void onSuccess(Car cars);

        void onFailed();
    }

    public interface ServiceResultCallBack<T> {
        void onSuccess(T result);

        void onFailed(ResponseResultType resultType, String message, int errorCode);
    }

    public ParkbanRepository(ParkbanDatabase database) {
        this.database = database;
    }

    public void login(String userName, String password, final ServiceResultCallBack<LoginResultDto.Parkban> callBack) {

        ParkbanServiceProvider.getInstance().login(userName, password,1).enqueue(new Callback<LoginResultDto>() {
            @Override
            public void onResponse(Call<LoginResultDto> call, Response<LoginResultDto> response) {
                try {
                    if (response.isSuccessful()) {
                        if (response.body().getResponseResultType() != ResponseResultType.Ok.getValue())
                            callBack.onFailed(ResponseResultType.values()[response.body().getResponseResultType()], "", 0);
                        else {
                            callBack.onSuccess(response.body().getValue());
                        }
                    } else {
                        callBack.onFailed(ResponseResultType.ServerError, response.message(), response.code());
                    }
                } catch (Exception e) {
                    callBack.onFailed(ResponseResultType.RetrofitError, "", 0);
                }
            }

            @Override
            public void onFailure(Call<LoginResultDto> call, Throwable t) {
                callBack.onFailed(ResponseResultType.ServerError, t.getMessage(), 0);
            }
        });
    }

    public void sendRecord(CarRecordsDto carRecords, final ServiceResultCallBack<SendRecordResultDto.SendRecordStatus> callBack) {
        ParkbanServiceProvider.getInstance().sendRecord(carRecords)
                .enqueue(new Callback<SendRecordResultDto>() {
                    @Override
                    public void onResponse(Call<SendRecordResultDto> call, Response<SendRecordResultDto> response) {
                        try {
                            if (response.isSuccessful()) {
                                if (response.body().getResponseResultType() != ResponseResultType.Ok.getValue())
                                    callBack.onFailed(ResponseResultType.values()[response.body().getResponseResultType()], "", 0);
                                else {
                                    callBack.onSuccess(response.body().getValue());
                                }
                            } else {
                                callBack.onFailed(ResponseResultType.ServerError, response.message(), response.code());
                            }
                        } catch (Exception e) {
                            callBack.onFailed(ResponseResultType.RetrofitError, "", 0);
                        }

                    }

                    @Override
                    public void onFailure(Call<SendRecordResultDto> call, Throwable t) {
                        callBack.onFailed(ResponseResultType.ServerError, t.getMessage(), 0);
                    }
                });
    }

    public void getParkMarginLimit(final ServiceResultCallBack<IntResultDto> callBack){
        ParkbanServiceProvider.getInstance().getParkMarginLimit().enqueue(new Callback<IntResultDto>() {
            @Override
            public void onResponse(Call<IntResultDto> call, Response<IntResultDto> response) {
                try {
                    if (response.isSuccessful()) {
                        if (response.body().getResponseResultType() != ResponseResultType.Ok.getValue())
                            callBack.onFailed(ResponseResultType.values()[response.body().getResponseResultType()], "", 0);
                        else {
                            callBack.onSuccess(response.body());
                        }
                    } else {
                        callBack.onFailed(ResponseResultType.ServerError, response.message(), response.code());
                    }
                } catch (Exception e) {
                    callBack.onFailed(ResponseResultType.RetrofitError, "", 0);
                }
            }

            @Override
            public void onFailure(Call<IntResultDto> call, Throwable t) {
                callBack.onFailed(ResponseResultType.ServerError, t.getMessage(), 0);
            }
        });
    }

    public void saveCarPlate(boolean isNewCar,int carId,CarPlate carPlate, final DataBaseResultCallBack callBack) {
        new SaveCarPlateAsyncTask(callBack)
                .execute(database, carPlate , isNewCar , carId);
    }

    private static class SaveCarPlateAsyncTask extends AsyncTask<Object, Void, Long> {

        private final DataBaseResultCallBack callBack;

        public SaveCarPlateAsyncTask(DataBaseResultCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected Long doInBackground(Object... params) {
            ParkbanDatabase database = (ParkbanDatabase) params[0];
            CarPlate carPlate = (CarPlate) params[1];
            boolean isNewCar = (boolean) params[2];
            int carId = (int) params[3];

            try {

                if (isNewCar) {
                    Car car = new Car(carPlate.getPlateNumber(), carPlate.getLatitude(), carPlate.getLongitude(),carPlate.getParkingSpaceId());
                    car.setStatus(SendStatus.PENDING.ordinal());
                    car.setId((int) database.getCarDao().saveCar(car));
                    carId = car.getId();
                }

                if (carId <= 0) {
                    return 0L;
                }

                carPlate.setCarId(carId);
                carPlate.setStatus(SendStatus.PENDING.ordinal());
                return database.getCarPlateDao().saveCarPlate(carPlate);

            } catch (Exception e) {
                Log.e(TAG, "Error in saveCarPlate", e);
                return 0L;
            }
        }

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            if (result > 0) {
                callBack.onSuccess(result);
            } else {
                callBack.onFailed();
            }
        }
    }

    public LiveData<Car> getCarByPlateNo(String plateNo){
        return database.getCarDao().getCarByPlateNo(plateNo);
    }

    public LiveData<List<Car>> getCars() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return database.getCarDao().getCars(calendar.getTime().getTime());
    }

    public LiveData<List<CarPlate>> getCarPlates(int carId) {
        return database.getCarPlateDao().getCarPlates(carId);
    }

    public void getCarByPlateNoAndDate(String plateNo , final DataBaseCarResultCallBack callBack){
        new GetCarByPlateNoAndDate(callBack).execute(database,plateNo);
    }

    private static class GetCarByPlateNoAndDate extends AsyncTask<Object , Void , Car>{
        private final DataBaseCarResultCallBack callBack;

        public GetCarByPlateNoAndDate(DataBaseCarResultCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected Car doInBackground(Object... params) {
            ParkbanDatabase database = (ParkbanDatabase) params[0];
            String plateNo =(String) params[1];
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            Car car = null;

            try{

                car = database.getCarDao().getCarByPlateNoAndDate(plateNo, calendar.getTime().getTime());

            }catch (Exception e){
                Log.e(TAG, "Error in getAndUpdateCarPlate", e);
            }
            return car;
        }

        @Override
        protected void onPostExecute(Car car) {
            super.onPostExecute(car);
            callBack.onSuccess(car);
            callBack.onFailed();
        }
    }

    public void checkIsExitOfCar(long carId , final DataBaseBooleanCallBack callBack){
        new CheckIsExitOfCar(callBack).execute(database,carId);
    }

    private static class CheckIsExitOfCar extends AsyncTask<Object, Void, Boolean>{
        private final DataBaseBooleanCallBack callBack;

        public CheckIsExitOfCar(DataBaseBooleanCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            ParkbanDatabase database = (ParkbanDatabase) params[0];
            long carId = (long) params[1];

            boolean result;
            try{
                result = database.getCarPlateDao().checkIsExitOfCar(carId);
            }catch (Exception e) {
                Log.e(TAG, "Error in checkIsExitOfCar", e);
                return false;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            callBack.onSuccess(aBoolean);
            callBack.onFailed();

        }
    }

    public void getCarByPlaceId(long parkingSpaceId , final DataBaseBooleanCallBack callBack){
        new GetCarByPlaceId(callBack).execute(database,parkingSpaceId);
    }

    private static class GetCarByPlaceId extends AsyncTask<Object, Void, Boolean>{
        private final DataBaseBooleanCallBack callBack;

        public GetCarByPlaceId(DataBaseBooleanCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            ParkbanDatabase database = (ParkbanDatabase) params[0];
            long parkingSpaceId = (long) params[1];

            boolean result;
            try{
                result = database.getCarDao().getCarByPlaceId(parkingSpaceId);
            }catch (Exception e) {
                Log.e(TAG, "Error in getCarByPlaceId", e);
                return false;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            callBack.onSuccess(aBoolean);
            callBack.onFailed();

        }
    }

    public void getAndUpdateCarPlate(long carPlateId, boolean status, final DataBaseCarPlateUpdateCallBack callBack) {
        new GetAndUpdateCarPlateAsyncTask(callBack)
                .execute(database, carPlateId, status);
    }

    private static class GetAndUpdateCarPlateAsyncTask extends AsyncTask<Object, Void, Boolean> {
        private final DataBaseCarPlateUpdateCallBack callBack;

        public GetAndUpdateCarPlateAsyncTask(DataBaseCarPlateUpdateCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            ParkbanDatabase database = (ParkbanDatabase) params[0];
            long id = (long) params[1];
            boolean status = (boolean) params[2];

            try {
                CarPlate carPlate = database.getCarPlateDao().getCarPlate(id);

                if (status)
                    carPlate.setStatus(SendStatus.SENT.ordinal());
                else
                    carPlate.setStatus(SendStatus.FAILED.ordinal());

                database.getCarPlateDao().updateCarPlate(carPlate);

                return true;

            } catch (Exception e) {
                Log.e(TAG, "Error in getAndUpdateCarPlate", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result)
                callBack.onSuccess();
            else
                callBack.onFailed();
        }
    }

}
