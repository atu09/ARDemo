package com.atirek.alm.ardemo;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import tyrantgit.explosionfield.ExplosionField;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public static Camera mCamera;
    public SurfaceHolder mHolder;
    public SurfaceView surfaceView;
    public OverlayView arContent;

    public static Location lastLocation = null;
    public static Location targetLocation = null;

    protected GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    public Handler handler;
    public LocationManager locationManager;

    public static float curBearingToMW = 0;
    FrameLayout arViewPane;

    public static Button iv_fire;
    public static ImageView iv_pointer, iv_gun, iv_duck;
    public static boolean fireState = false;
    public static ExplosionField mExplosionField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        arViewPane = (FrameLayout) findViewById(R.id.ar_view_pane);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        iv_fire = (Button) findViewById(R.id.iv_fire);
        iv_pointer = (ImageView) findViewById(R.id.iv_pointer);
        iv_gun = (ImageView) findViewById(R.id.iv_gun);
        iv_duck = (ImageView) findViewById(R.id.iv_duck);

        mExplosionField = ExplosionField.attach2Window(this);

        mHolder = surfaceView.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(this);

        arContent = new OverlayView(this);
        arViewPane.addView(arContent);

        //handler.post(UpdateLocation);



        buildGoogleApiClient();

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        try {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> prevSizes = params.getSupportedPreviewSizes();
        for (Camera.Size s : prevSizes) {
            if ((s.height <= height) && (s.width <= width)) {
                params.setPreviewSize(s.width, s.height);
                break;
            }
        }

        mCamera.setParameters(params);
        mCamera.startPreview();
        mCamera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        //handler.removeCallbacks(UpdateLocation);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }


    //***************************************************************************************************************************************************************************

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //createLocationRequest();
    }

    //***************************************************************************************************************************************************************************
/*

    public void getLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            Log.v("Locate>>>", "Best provider: " + LocationManager.GPS_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            onLocationChanged(lastLocation);
            return;
        }

        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            lastLocation = location;
            Log.v("Locate>>>", "Best provider: " + LocationManager.NETWORK_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
            onLocationChanged(lastLocation);
        }


    }

    public Runnable UpdateLocation = new Runnable() {
        @Override
        public void run() {
            getLocation();
            handler.postDelayed(this, 1000);
        }
    };

*/
    //***************************************************************************************************************************************************************************

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            //Bitmap bitmap;
            // create bitmap screen capture

            View view = MainActivity.this.getWindow().getDecorView().getRootView();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.destroyDrawingCache();
            view.setDrawingCacheEnabled(false);


/*
            arViewPane.setDrawingCacheEnabled(true);
            arViewPane.buildDrawingCache();
            Bitmap bitmap1 = Bitmap.createBitmap(arViewPane.getDrawingCache());
            arViewPane.destroyDrawingCache();
            arViewPane.setDrawingCacheEnabled(false);
*/


/*
            surfaceView.setDrawingCacheEnabled(true);
            surfaceView.buildDrawingCache();
            Bitmap bitmap2 = Bitmap.createBitmap(surfaceView.getDrawingCache());
            surfaceView.destroyDrawingCache();
            surfaceView.setDrawingCacheEnabled(false);
*/

/*
            Bitmap bmOverlay = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), bitmap1.getConfig());
            Canvas canvas = new Canvas(bmOverlay); //Overlaying the 2 bitmaps
            canvas.drawBitmap(bitmap1, 0, 0, null);
            canvas.drawBitmap(bitmap2, 0, 0, null);
            bitmap = bmOverlay;
*/

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setCancelable(false);
        dialog.setTitle("Capture Page");
        Button button = (Button) dialog.findViewById(R.id.capturedButton);
        ImageView image = (ImageView) dialog.findViewById(R.id.capturedImage);
        image.setImageURI(Uri.fromFile(imageFile));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Captured Successfully", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        dialog.show();

    }

    //***************************************************************************************************************************************************************************

/*
    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;
        targetLocation = new Location("manual");
        targetLocation.setLatitude(22.296848);
        targetLocation.setLongitude(73.171780);
        curBearingToMW = lastLocation.bearingTo(targetLocation);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
        Log.d("Locate>>>", "Changed");

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
    }
*/

    //***************************************************************************************************************************************************************************

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) != null) {

            lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            targetLocation = lastLocation;
            curBearingToMW = lastLocation.bearingTo(targetLocation);

        } else {
            //startLocationUpdates();
        }

    }


    @Override
    public void onConnectionSuspended(int i) {

        Log.i("Locate>>>", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("Locate>>>", "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }


    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        //handler.removeCallbacks(UpdateLocation);

    }


    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;
        curBearingToMW = lastLocation.bearingTo(targetLocation);

        Log.d("Locate>>>", "Changed");

    }
}
