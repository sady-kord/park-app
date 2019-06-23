package com.eos.parkban.services;

import com.eos.parkban.services.dto.BooleanResultDto;
import com.eos.parkban.services.dto.CashDetailsResultDto;
import com.eos.parkban.services.dto.CurrentShiftDto;
import com.eos.parkban.services.dto.FunctionalityResultDto;
import com.eos.parkban.services.dto.IncreaseDriverWalletResultDto;
import com.eos.parkban.services.dto.IntResultDto;
import com.eos.parkban.services.dto.LongResultDto;
import com.eos.parkban.services.dto.ParkAmountResultDto;
import com.eos.parkban.services.dto.ParkbanShiftResultDto;
import com.eos.parkban.services.dto.SendRecordAndPayResultDto;
import com.eos.parkban.services.dto.SendRecordResultDto;
import com.eos.parkban.services.dto.CarRecordsDto;
import com.eos.parkban.services.dto.LoginResultDto;
import com.eos.parkban.services.dto.StringResultDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ParkbanService {

    @POST("Users/LoginUser")
    @FormUrlEncoded
    Call<LoginResultDto> login(@Field("UserName") String UserName
            , @Field("UserPass") String Password , @Field("UserType") int userType );

    @POST("CarPark/SaveCarPark")
    Call<SendRecordResultDto> sendRecord(@Body CarRecordsDto records);

    @GET("Parkban/GetParkMarginLimit")
    Call<IntResultDto> getParkMarginLimit();

    @GET("Parkban/GetAppLatestVersion")
    Call<StringResultDto> getLastVersion();

    @GET("Parkban/GetCurrentParkbanShift")
    Call<CurrentShiftDto> getCurrentShift(@Query("ParkbanUserId") long parkbanUserId);

    @GET("Parkban/GetParkbanShift")
    Call<ParkbanShiftResultDto> getParkbanShift(@Query("ParkbanUserId") long parkbanUserId ,@Query("startDate") String startDate
    ,@Query("endDate") String endDate);

    @GET("Parkban/ParkbanFunctionalityOnShift")
    Call<FunctionalityResultDto> getParkbanFunctionality(@Query("ParkbanUserId") long parkbanUserId , @Query("beginDateTime") String startDate
            , @Query("endDateTime") String endDate,@Query("workShiftType") int workShiftType);

    @GET("CarPark/GetParkAmount")
    Call<ParkAmountResultDto> getParkAmount(@Query("parkSpaceId") long parkSpaceId , @Query("parkDateTime") String parkDateTime
            , @Query("startTime") String startTime, @Query("endTime") String endTime);

    @POST("CarPark/SaveCarParkAndPayByParkban")
    Call<SendRecordAndPayResultDto> sendRecordAndPay( @Body CarRecordsDto records);

    @POST("Parkban/IncreaseDriverWallet")
    Call<IncreaseDriverWalletResultDto> increaseDriverWallet(@Body IncreaseDriverWalletResultDto.IncreaseDriverWalletDto wallet);

    @GET("parkban/HasParkbanCredit")
    Call<BooleanResultDto> hasParkbanCredit(@Query("amount") long amount);

    @GET("Parkban/GetParkbanCashDetailsOnDays")
    Call<CashDetailsResultDto> getChargeReport(@Query("parkbanId") long parkbanUserId ,@Query("startDate") String startDate
            ,@Query("endDate") String endDate);

    @GET("carpark/GetUnregisteredCarDebt")
    Call<LongResultDto> getUnregisteredCarDebt(@Query("plate") String plate);

    @GET("carpark/GetDriverFullDebt")
    Call<LongResultDto> getDriverFullDebt(@Query("driverPhon") long phoneNumber);

}
