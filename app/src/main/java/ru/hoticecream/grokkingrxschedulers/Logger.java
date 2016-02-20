package ru.hoticecream.grokkingrxschedulers;

import android.util.Log;

public class Logger {

    private static final String TAG = "GrokkingRxSchedulers";

    public static void d(String message) {
        Log.d(TAG, message);
    }

    public static void logThread(String message) {
        Logger.d(message + " : " + Thread.currentThread().getName());
    }
}
