package com.eos.parkban.persistence;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.eos.parkban.persistence.models.Car;

import java.util.List;

@Dao
public interface CarDao {

    @Insert
    long saveCar(Car car);

    @Query("SELECT * FROM CARS WHERE id in (SELECT car_id FROM Car_plates WHERE record_date>=:date)")
    LiveData<List<Car>> getCars(long date);

    @Query("SELECT A.* FROM CARS A INNER JOIN car_plates B ON B.car_id=A.id WHERE A.id in (SELECT B.car_id FROM Car_plates WHERE B.record_date>=:date)")
    LiveData<List<Car>> getCars1(long date);

    @Query("SELECT A.* FROM CARS A INNER JOIN car_plates B ON B.car_id=A.id WHERE A.palet_no=:plateNo AND B.record_date>=:date ORDER BY B.record_date DESC LIMIT 1")
    Car getCarByPlateNoAndDate(String plateNo, long date);

    @Query("SELECT * FROM CARS WHERE palet_no=:plateNo")
    LiveData<Car> getCarByPlateNo(String plateNo);

    @Query("SELECT * FROM CARS WHERE parking_space_id=:id")
    boolean getCarByPlaceId(long id);

    @Query("SELECT * FROM CARS WHERE id=:id")
    Car getCarById(int id);

    @Update
    void updateCarPlateState(Car car);

    @Delete
    void deleteCar(Car car);


}
