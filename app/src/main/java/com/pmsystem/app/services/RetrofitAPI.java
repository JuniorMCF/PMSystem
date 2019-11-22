package com.pmsystem.app.services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitAPI {

    private static RetrofitAPI instance = null;
    private static final String BASE_URL = "https://thingspeak.com/";

    // Keep your services here, build them in buildRetrofit method later
    private ThingspeakService thingspeakService;

    public static RetrofitAPI getInstance() {
        if (instance == null) {
            instance = new RetrofitAPI();
        }

        return instance;
    }

    // Build retrofit once when creating a single instance
    private RetrofitAPI() {
        // Implement a method to build your retrofit
        buildRetrofit();
    }

    private void buildRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Build your services once
        this.thingspeakService = retrofit.create(ThingspeakService.class);

    }

    public ThingspeakService getThingspeakService() {
        return this.thingspeakService;
    }

}
