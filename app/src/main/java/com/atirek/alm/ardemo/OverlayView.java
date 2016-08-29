package com.atirek.alm.ardemo;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import tyrantgit.explosionfield.ExplosionField;

/**
 * Created by Alm on 7/21/2016.
 */
public class OverlayView extends View implements SensorEventListener {


    String accelData = "Accelerometer Data";
    String compassData = "Compass Data";

    public static Canvas canvas;

    boolean isAccelAvailable = false;
    boolean isCompassAvailable = false;

    float[] aData = new float[3];
    float[] cData = new float[3];

    Bitmap myBitmap = null;

    // compute rotation matrix
    float rotation[] = new float[9];
    float orientation[] = new float[3];
    float cameraRotation[] = new float[9];

    float dx;
    float dy;

    float xP = 0;
    float yP = 0;

    int x;
    int y;

    Context context;

    Dialog dialog;

    Camera.Parameters params;

    MediaPlayer bulletSound = new MediaPlayer();
    MediaPlayer boltPullSound;
    MediaPlayer explodeSound;

    public OverlayView(final Context context) {
        super(context);

        this.context = context;

        SensorManager sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        isAccelAvailable = sensors.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME);
        isCompassAvailable = sensors.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_GAME);

        myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.duck);

        boltPullSound = new MediaPlayer();
        boltPullSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
        boltPullSound = MediaPlayer.create(context, R.raw.ak47_boltpull);

        explodeSound = new MediaPlayer();
        explodeSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
        explodeSound = MediaPlayer.create(context, R.raw.duck_explode);

        boltPullSound.start();

        MainActivity.iv_fire.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {


                bulletSound.reset();
                bulletSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
                bulletSound = MediaPlayer.create(context, R.raw.ak47_gunshots);
                bulletSound.start();

                if (myBitmap != null) {

                    MainActivity.fireState = true;

                    x = canvas.getWidth() / 2;
                    Log.d("CanvasX", x + "");
                    y = canvas.getHeight() / 2;
                    Log.d("CanvasY", y + "");

                    if (x > xP && x < (xP + myBitmap.getScaledWidth(canvas)) && y > yP && y < (yP + myBitmap.getScaledHeight(canvas))) {

                        Toast.makeText(context, "Touch Down!!!!", Toast.LENGTH_SHORT).show();
                        MainActivity.iv_duck.setVisibility(VISIBLE);
                        MainActivity.iv_duck.setX(xP);
                        MainActivity.iv_duck.setY(yP);
                        explodeSound.start();
                        MainActivity.mExplosionField.expandExplosionBound(500, 500);
                        MainActivity.mExplosionField.explode(MainActivity.iv_duck);
                        myBitmap = null;

                        new Handler().postDelayed(startOverDelay, 1000);

                    } else {
                        Toast.makeText(context, "Oops! You missed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }


    Runnable startOverDelay = new Runnable() {
        @Override
        public void run() {

            startOver();

        }
    };


    Runnable dialogDelay = new Runnable() {
        @Override
        public void run() {
            MainActivity.mExplosionField.clear();
            dialog.dismiss();
            boltPullSound.start();
        }
    };

    public void startOver() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setCancelable(false);
        dialog.setTitle("Capture Page");
        Button button = (Button) dialog.findViewById(R.id.capturedButton);
        final ImageView image = (ImageView) dialog.findViewById(R.id.capturedImage);

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.duck);
                MainActivity.mExplosionField.explode(image);
                new Handler().postDelayed(dialogDelay, 500);
            }
        });

        image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.duck);
                MainActivity.mExplosionField.explode(image);
                new Handler().postDelayed(dialogDelay, 500);

            }
        });

        dialog.show();

    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setTextAlign(Paint.Align.LEFT);
        contentPaint.setTextSize(15);
        contentPaint.setStrokeWidth(5);
        contentPaint.setColor(Color.RED);

        if (MainActivity.fireState) {
            canvas.drawLine(x, y, canvas.getWidth(), canvas.getHeight(), contentPaint);
            MainActivity.fireState = false;
        }

        if (myBitmap != null && MainActivity.lastLocation != null && MainActivity.targetLocation != null && MainActivity.lastLocation.distanceTo(MainActivity.targetLocation) < 20) {

            float v = MainActivity.lastLocation.distanceTo(MainActivity.targetLocation);
            Log.d("Locate>>>", "Inside " + v);

            params = MainActivity.mCamera.getParameters();
            float verticalFOV = params.getVerticalViewAngle();
            float horizontalFOV = params.getHorizontalViewAngle();

            // use roll for screen rotation
            //canvas.rotate((float) (0.0f - Math.toDegrees(orientation[2])));

            // Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
            dx = (float) ((canvas.getWidth() / horizontalFOV) * (Math.toDegrees(orientation[0]) - MainActivity.curBearingToMW));
            dy = (float) ((canvas.getHeight() / verticalFOV) * Math.toDegrees(orientation[1]));

            // wait to translate the dx so the horizon doesn't get pushed off
            //canvas.translate(0.0f, 0.0f - dy);

            // make our line big enough to draw regardless of rotation and translation
            //canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight() / 2, canvas.getWidth() + canvas.getHeight(), canvas.getHeight() / 2, contentPaint);

            // now translate the dx

            //canvas.translate(0.0f, 0.0f);
            //canvas.translate(0.0f - dx, 0.0f);

            this.canvas = canvas;


            xP = canvas.getWidth() / 2 - dx;
            Log.d("CanvasXp", xP + "");
            yP = canvas.getHeight() / 2 - dy;
            Log.d("CanvasYp", yP + "");

            // draw our point -- we've rotated and translated this to the right spot already
            canvas.drawBitmap(myBitmap, xP, yP, null);

            MainActivity.iv_pointer.bringToFront();
            MainActivity.iv_gun.bringToFront();


        } else if (MainActivity.lastLocation != null && MainActivity.targetLocation != null) {

            float v = MainActivity.lastLocation.distanceTo(MainActivity.targetLocation);
            Log.d("Locate>>>", "Outside " + v);

        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

/*
        StringBuilder msg = new StringBuilder(sensorEvent.sensor.getName()).append(" ");
        for (float value : sensorEvent.values) {
            msg.append("[").append(value).append("]");
        }
*/

        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                //accelData = msg.toString();
                aData[0] = sensorEvent.values[0];
                aData[1] = sensorEvent.values[1];
                aData[2] = sensorEvent.values[2];
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                //compassData = msg.toString();
                cData[0] = sensorEvent.values[0];
                cData[1] = sensorEvent.values[1];
                cData[2] = sensorEvent.values[2];
                break;

        }


        boolean gotRotation = SensorManager.getRotationMatrix(rotation, null, aData, cData);

        if (gotRotation) {

            // remap such that the camera is pointing straight down the Y axis
            SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, cameraRotation);

            // orientation vector
            SensorManager.getOrientation(cameraRotation, orientation);

        }

        this.invalidate();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
