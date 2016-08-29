package com.atirek.alm.ardemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {


    Handler handler = new Handler();
    ProgressBar progressBar;
    TextView tv_loader;
    public static int totalProgressTime = 100;
    public static int currentProgressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tv_loader = (TextView) findViewById(R.id.tv_loader);

        progressBar.setVisibility(View.GONE);
        tv_loader.setVisibility(View.GONE);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);

        checkPermission();

    }

    public void checkPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE}, 1);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE}, 1);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE}, 1);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tv_loader.setVisibility(View.VISIBLE);
        progressBar.post(splash);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(SplashActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();

            progressBar.setVisibility(View.VISIBLE);
            tv_loader.setVisibility(View.VISIBLE);
            progressBar.post(splash);

        } else {
            Toast.makeText(SplashActivity.this, "Permission Not Granted", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    public Runnable splash = new Runnable() {
        @Override
        public void run() {

            progressBar.setProgress(currentProgressTime);
            progressBar.setSecondaryProgress(currentProgressTime);
            tv_loader.setText("Loading " + currentProgressTime + "%");

            if (currentProgressTime == totalProgressTime) {
                handler.removeCallbacks(this);

                Intent startIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(startIntent);

                finish();
            } else {
                currentProgressTime = currentProgressTime + 1;
                handler.postDelayed(this, 20);
            }

        }
    };

}
