package com.ammar.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

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
        String username = myPrefs.getString("USERNAME",null);

        if(username != null)
        {
            Intent intent = new Intent(RegisterActivity.this, MapsActivity.class);
            startActivity(intent);
        }

    }


    public void registerUser(View view) {
        userid = findViewById(R.id.mobile_no_register);
        password = findViewById(R.id.password_register);
        spinner = findViewById(R.id.spin);
        String id = userid.getText().toString();
        String pwd = password.getText().toString();
        int category = spinner.getSelectedItemPosition();
        //Code to send details to server
        // and below is code if registration successful
//        myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = myPrefs.edit();
//        editor.putString("USERNAME", id);
//        editor.commit();
//        editor.putString("PASSWORD",pwd);
//        editor.commit();
//        Intent intent = new Intent(RegisterActivity.this, MapsActivity.class);
//        startActivity(intent);
    }


    public void toLogin(View view) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
