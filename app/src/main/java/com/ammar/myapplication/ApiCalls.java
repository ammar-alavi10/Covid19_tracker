package com.ammar.myapplication;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiCalls {

    String BASE_URL = "http://tuhina840.pythonanywhere.com";

    @POST("tracker/register/")
    Call<Object> getRegistered(@Body User user);

    @FormUrlEncoded
    @POST("api-token-auth/")
    Call<Object> getToken(@Field("username") String username, @Field("password") String password);

    @POST("tracker/updateUserDetail/")
    Call<Object> updateUserDetail(@Header("Authorization") String token, @Body int userStatusChoice);

    /*
            OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiCalls.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiCalls statusApi = retrofit.create(ApiCalls.class);
        Call<Object> call = statusApi.getToken(token, statusCode);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                if(!response.isSuccessful()) {

                }

            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {

            }
        });
     */

    @FormUrlEncoded
    @POST("tracker/inputLocation")
    Call<Object> locationUpdater(@Header("Authorization") String token, @Field("latitude") float latitude, @Field("longitude") float longitude);
    /*
    OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiCalls.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiCalls locationApi = retrofit.create(ApiCalls.class);
        Call<Object> call = locationApi.getToken(token, latitude, longitude);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                if(!response.isSuccessful()) {

                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {

            }
        });

     */

    @POST("/tracker/table/")
    Call<Object> getLocationNearby(@Header("Authorization") String token);


}