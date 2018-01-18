package com.p532.arbusschedules;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.danlew.android.joda.JodaTimeAndroid;

import eu.kudan.kudan.ARAPIKey;

public class SplashActivity extends AppCompatActivity {

    public static String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        JodaTimeAndroid.init(this);
        ARAPIKey arapiKey = ARAPIKey.getInstance();
        arapiKey.setAPIKey(getString(R.string.api_key));

        packageName = getApplicationContext().getPackageName();

        permissionsRequest();
    }

    //Request App Permissions
    private void permissionsRequest(){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, 111);
        }else{
            startARActivity();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 111:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startARActivity();
                }else{
                    permissionsNotGranted();
                }
        }
    }

    //Function to start ARMainActivity
    private void startARActivity(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, ARMainActivity.class);
                startActivity(intent);
            }
        }, 9000);
    }

    //Function to handle when Permissions are not granted
    private void permissionsNotGranted(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(getString(R.string.no_permission_title));
        alertBuilder.setMessage(getString(R.string.no_permission_body));
        alertBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                System.exit(1);
            }
        });
        AlertDialog noPermission = alertBuilder.create();
        noPermission.show();
    }
}
