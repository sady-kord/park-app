package com.eos.parkban.persistence;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.eos.parkban.persistence.models.CarPlate;

import java.util.List;

@Dao
public interface CarPlateDao {

    @Insert
    long saveCarPlate(CarPlate carPlate);

    @Query("SELECT * FROM car_plates WHERE car_id=:carId")
    LiveData<List<CarPlate>> getCarPlates(int carId);

    @Query("SELECT * FROM car_plates WHERE id=:id")
    CarPlate getCarPlate(long id);

    @Query("UPDATE car_plates SET status=:status WHERE id = :car_id")
    void update(int status,int car_id);

    @Query("SELECT is_exit FROM car_plates WHERE car_id=:carId AND is_exit=1")
    boolean checkIsExitOfCar(long carId);

    @Update
    void updateCarPlate(CarPlate carPlate);

}
