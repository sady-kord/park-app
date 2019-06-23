package com.eos.parkban.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.eos.parkban.persistence.models.ParkingSpace;

import java.util.List;

@Dao
public interface ParkingSpaceDao {

    @Insert
    long saveParkingSpace(ParkingSpace parkingSpaceList);

    @Update
    void updateParkingSpace(ParkingSpace parkingSpace);

    @Query("SELECT * FROM parking_space WHERE id=:id")
    ParkingSpace getParkingSpace(long id);

}
