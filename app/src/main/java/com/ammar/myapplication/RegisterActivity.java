package com.ammar.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    EditText userid;
    EditText password;
    EditText registerName;
    EditText email;
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

        ConstraintLayout constraintLayout = findViewById(R.id.constraint_register);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);

        animationDrawable.start();

    }


    public void registerUser(View view) {
        userid = findViewById(R.id.mobile_no_register);
        password = findViewById(R.id.password_register);
        registerName = findViewById(R.id.name_register);
        email = findViewById(R.id.email_id);
        spinner = findViewById(R.id.spin);
        final String id = userid.getText().toString();
        final String pwd = password.getText().toString();
        final int category = spinner.getSelectedItemPosition() + 1;
        final String mail = email.getText().toString();
        final String name = registerName.getText().toString();
        //User object
        final User user = new User(id, pwd, mail,category,name);

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
                    if(response.body() == null)
                    {
                        Toast.makeText(RegisterActivity.this,"Please Retry",Toast.LENGTH_LONG).show();
                    }
                    else {
                        showAlertDialogue();
                        Toast.makeText(RegisterActivity.this, response.body().toString(), Toast.LENGTH_LONG).show();
                        Log.d("APICALL", "FAILED");
                    }
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
                loginUser(id,pwd);
//                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
//                startActivity(intent);
//                finish();

            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.d("APICALL","FAILED"+t.toString());
                Toast.makeText(RegisterActivity.this,"Please Retry",Toast.LENGTH_LONG).show();
                showAlertDialogue();
            }
        });
    }

    public void loginUser(String id, String pwd){
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
        Call<Object> call = loginApi.getToken(id, pwd);

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                if (!response.isSuccessful()) {
                    showAlertDialogue();
                    Log.d("APICALL", "FAILED");
                    Toast.makeText(RegisterActivity.this, "Please Retry", Toast.LENGTH_LONG).show();
                    return;
                }

                String token = new Gson().toJson(response.body()).split(":\"")[1].split("\"")[0];
                SharedPreferences.Editor editor = myPrefs.edit();
                editor.putString("TOKEN", token);
                editor.apply();

                Intent intent = new Intent(RegisterActivity.this,MapsActivity.class);
                startActivity(intent);
                finish();

            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                showAlertDialogue();
                Log.d("APICALL","FAILED"+t.toString());
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
