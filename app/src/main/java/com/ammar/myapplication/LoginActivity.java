package com.ammar.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences mypref;
    String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mypref = getSharedPreferences(",myPrefs", MODE_PRIVATE);
        username = mypref.getString("USERNAME", "username");
        password = mypref.getString("PASSWORD", "password");
    }

    public void toRegister(View view) {
    }

    public void loginUser(View view) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiCalls.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiCalls loginApi = retrofit.create(ApiCalls.class);
        Call<Object> call = loginApi.getToken(username, password);

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                if(!response.isSuccessful()) {
                    showAlertDialogue();
                    return;
                }

                String token = new Gson().toJson(response.body()).split(":\"")[1].split("\"")[0];
                SharedPreferences.Editor editor = mypref.edit();
                editor.putString("TOKEN", token);
                editor.commit();

                int statusCode = mypref.getInt("CATEGORY",0);


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
                Call<Object> call2 = statusApi.updateUserDetail(token, statusCode);
                call2.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call2, Response<Object> response) {

                    if(!response.isSuccessful()) {
                        showAlertDialogue();

                    }

                    Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                    startActivity(intent);
                    finish();

                }

                @Override
                public void onFailure(Call<Object> call2, Throwable t) {

                    showAlertDialogue();
                }
            });

            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                showAlertDialogue();
            }
        });

    }

    private void showAlertDialogue(){
        new AlertDialog.Builder(LoginActivity.this)
                .setTitle("Error")
                .setMessage("Oops! Encountered some error! Try again!")
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                    }
                })
                .setCancelable(false)
                .create();
    }
}
