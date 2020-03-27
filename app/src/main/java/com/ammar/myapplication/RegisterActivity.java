package com.ammar.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    EditText userid;
    EditText password;
    Spinner spinner;
    SharedPreferences myPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        String username = myPrefs.getString("USERNAME","null");

        if(!username.equals("null"))
        {
            Intent intent = new Intent(RegisterActivity.this, MapsActivity.class);
            startActivity(intent);
        }

    }


    public void registerUser(View view) {
        userid = findViewById(R.id.mobile_no_register);
        password = findViewById(R.id.password_register);
        final EditText email = findViewById(R.id.email_id);
        spinner = findViewById(R.id.spin);
        final String id = userid.getText().toString();
        final String pwd = password.getText().toString();
        final int category = spinner.getSelectedItemPosition() + 1;
        final String mail = email.getText().toString();
        //User object
        final User user = new User(id, pwd, mail);

        //Code to send details to server
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiCalls.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiCalls registrationApi = retrofit.create(ApiCalls.class);
        Call<Object> call = registrationApi.getRegistered(user);

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                if(!response.isSuccessful()) {
                    showAlertDialogue();
                    Log.d("APICALL","FAILED");
                    return;
                }

                Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();
                myPrefs = RegisterActivity.this.getSharedPreferences("myPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = myPrefs.edit();
                editor.putString("USERNAME", id);
                editor.putString("PASSWORD",pwd);
                editor.putString("EMAIL", mail);
                editor.putInt("CATEGORY", category);
                editor.apply();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.d("APICALL","FAILED"+t.toString());
                showAlertDialogue();
            }
        });
    }

    public void toLogin(View view) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }


    private void showAlertDialogue(){
        new AlertDialog.Builder(RegisterActivity.this)
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
                        startActivity(new Intent(RegisterActivity.this, RegisterActivity.class));
                    }
                })
                .setCancelable(false)
                .create();
    }

}
