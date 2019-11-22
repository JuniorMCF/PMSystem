package com.pmsystem.app.services;

import com.pmsystem.app.clases.DataThingSpeak;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ThingspeakService {
    @GET("update")
    Call<List<DataThingSpeak>> getData(@Query("api_key") String apiKey, @QueryMap Map<String, String> options);

}
