package com.ammar.myapplication;

import android.util.Log;

import java.util.logging.Logger;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;

public abstract class CallBackWithRetry<T> implements Callback<T> {



    private static final String TAG = "CallbackWithRetry";

    private int retryCount = 0;

    private final String requestName;
    private final int retryAttempts;

    protected CallBackWithRetry(@NonNull String requestName, int retryAttempts) {
        this.requestName = requestName;
        this.retryAttempts = retryAttempts;
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if (retryCount < retryAttempts) {
            Log.e(TAG, "Retrying ");
            retry(call);
        } else {
            Log.e(TAG, "Failed request ");
        }
    }

    private void retry(Call<T> call) {
        call.clone().enqueue(this);
    }
}