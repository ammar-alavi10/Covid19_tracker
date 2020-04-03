package com.ammar.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class ChangeStatus extends AppCompatActivity {

    Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_status);
    }

    public void Changestatus(View view) {
        spinner = findViewById(R.id.spinchange);
        final int category = spinner.getSelectedItemPosition() + 1;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiCalls.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SharedPreferences myPrefs = ChangeStatus.this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        String token = myPrefs.getString("TOKEN","") ;

        ApiCalls statusApi = retrofit.create(ApiCalls.class);
        Call<Object> call2 = statusApi.updateUserDetail("Token "+token, category);
        call2.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call2, Response<Object> response) {

                if(!response.isSuccessful()) {
                    Log.d("APICALL","FAILED");

                    Toast.makeText(ChangeStatus.this,"Please Retry",Toast.LENGTH_LONG).show();
                }
                SharedPreferences.Editor editor = myPrefs.edit();
                editor.putInt("CATEGORY",category);
                editor.apply();

                Toast.makeText(ChangeStatus.this,"Change Successful",Toast.LENGTH_LONG).show();

                Intent intent = new Intent(ChangeStatus.this, MapsActivity.class);
                startActivity(intent);
                finish();

            }

            @Override
            public void onFailure(Call<Object> call2, Throwable t) {

                Log.d("APICALL","FAILED"+t.toString());
                Toast.makeText(ChangeStatus.this,"Please Retry",Toast.LENGTH_LONG).show();
            }
        });
    }
}
