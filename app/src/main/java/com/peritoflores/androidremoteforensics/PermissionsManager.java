package com.peritoflores.androidremoteforensics;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class PermissionsManager {

    private static final String TAG = "PermissionsManager";

    /**
     * <uses-permission android:name="android.permission.INTERNET"/>
     * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
     * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
     * <uses-permission android:name="android.permission.RECORD_AUDIO"/>
     * <uses-permission android:name="android.permission.RECEIVE_SMS"/>
     * <uses-permission android:name="android.permission.SEND_SMS"/>
     * <uses-permission android:name="android.permission.READ_SMS"/>
     * <uses-permission android:name="android.permission.READ_CONTACTS"/>
     * <uses-permission android:name="android.permission.READ_CALL_LOG"/>
     * <uses-feature android:name="android.hardware.camera" android:required="true"/>
     * <uses-permission android:name="android.permission.CAMERA"/>
     * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
     * <uses-permission android:name="android.permission.GET_ACCOUNTS"/>
     * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
     * <p>
     * -->
     **/


    public static void askPermissions(Activity activity) {


        askSinglePermission(activity);


    }


    public static void askSinglePermission(Activity activity) {

        int requestCode = 3;
        String singlePermission = Manifest.permission.READ_SMS;
        // Here, thisActivity is the current activity
        String[] permissionsNeeded = {
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
        };


        boolean askPermissions = false;
        for (int i = 0; i < permissionsNeeded.length; i++) {

            if (ContextCompat.checkSelfPermission(activity, permissionsNeeded[i])
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, permissionsNeeded[i] + " not granted.  Need to ask");
                askPermissions = true;
            }

        }

        if (askPermissions) {

            // Permission is not granted
            // Should we show an explanation?

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    singlePermission)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(activity,
                        permissionsNeeded,
                        requestCode);


                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Log.d(TAG, singlePermission + " permission has been granted");            // Permission has already been granted
        }


    }


}
