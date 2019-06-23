package com.eos.parkban.services;

import com.eos.parkban.services.dto.IntResultDto;
import com.eos.parkban.services.dto.SendRecordResultDto;
import com.eos.parkban.services.dto.CarRecordsDto;
import com.eos.parkban.services.dto.LoginResultDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ParkbanService {

    @POST("Users/LoginUser")
    @FormUrlEncoded
    Call<LoginResultDto> login(@Field("UserName") String UserName
            , @Field("UserPass") String Password , @Field("UserType") int userType );

    @POST("CarPark/SaveCarPark")
    Call<SendRecordResultDto> sendRecord(@Body CarRecordsDto records);

    @GET("Parkban/GetParkMarginLimit")
    Call<IntResultDto> getParkMarginLimit();

}
