package com.eos.parkban.services;

import android.text.TextUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ParkbanServiceProvider {

    private static final String HEADER_TOKEN = "UserToken";
    private static final String BASE_URL = "http://tehranmobsrv.parkometer.ir/api/";

    private static ParkbanService instance;
    private static String userToken;

    private ParkbanServiceProvider() {
    }

    public static synchronized ParkbanService getInstance() {
        if (instance == null) {
            instance = createParkbanService(BASE_URL);
        }
        return instance;
    }

    private static ParkbanService createParkbanService(String baseUrl) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .readTimeout(80, TimeUnit.SECONDS)
                .connectTimeout(80, TimeUnit.SECONDS);

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-type", "application/json");

                if (!TextUtils.isEmpty(userToken)) {
                    requestBuilder.addHeader(HEADER_TOKEN, userToken);
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        OkHttpClient client = httpClient.build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ParkbanService.class);

    }

    public static void setUserToken(String userToken) {
        ParkbanServiceProvider.userToken = userToken;
    }
}
