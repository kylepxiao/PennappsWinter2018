/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.face.facetracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.graphics.Canvas;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.CameraSource.PictureCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Policy;

//import static android.hardware.Camera.*;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends AppCompatActivity {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private static boolean clicked = false;

    MediaPlayer mp;

    private Camera camera;
    Camera.Parameters params;

    // getting camera parameters
    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e("asdf", e.getMessage());
            }
        }
    }

    /*
    * Turning On flash
    */
    private void turnOnFlash() {
        if (camera == null || params == null) {
            return;
        }
        try {
            Log.i("asdf", "This line");
            //params = camera.getParameters();
            params = Camera.open().getParameters();
            Log.i("asdf", "That line");
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
        }catch (Exception e) {
            Log.i("asdf", Log.getStackTraceString(e));
        }
    }
    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        //getCamera();
         mp = MediaPlayer.create(this, R.raw.lazer);
        Button fire = (Button) findViewById(R.id.fire);

        final ImageView demoImage = (ImageView)findViewById(R.id.asdf);

        final int imagesToShow[] = { R.drawable.laser_01, R.drawable.laser_02, R.drawable.laser_03, R.drawable.laser_04, R.drawable.laser_05, R.drawable.laser_06 };

        fire.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.i("asdf", "FIRIN MAH LAZAR");
                clicked = true;

                animate(demoImage, imagesToShow, 0, false);

                new CountDownTimer(1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        clicked = false;
                    }
                }.start();
            }

            private void animate(final ImageView imageView, final int images[], final int imageIndex, final boolean forever) {

                //imageView <-- The View which displays the images
                //images[] <-- Holds R references to the images to display
                //imageIndex <-- index of the first image to show in images[]
                //forever <-- If equals true then after the last image it starts all over again with the first image resulting in an infinite loop. You have been warned.

                int fadeInDuration = 500; // Configure time values here
                int timeBetween = 3000;
                int fadeOutDuration = 1000;

                imageView.setVisibility(View.INVISIBLE);    //Visible or invisible by default - this will apply when the animation ends
                imageView.setImageResource(images[imageIndex]);

                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
                fadeIn.setDuration(fadeInDuration);

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
                fadeOut.setStartOffset(fadeInDuration + timeBetween);
                fadeOut.setDuration(fadeOutDuration);

                AnimationSet animation = new AnimationSet(false); // change to false
                animation.addAnimation(fadeIn);
                animation.addAnimation(fadeOut);
                animation.setRepeatCount(1);
                imageView.setAnimation(animation);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationEnd(Animation animation) {
                        if (images.length - 1 > imageIndex) {
                            animate(imageView, images, imageIndex + 1, forever); //Calls itself until it gets to the end of the array
                        } else {
                            if (forever) {
                                animate(imageView, images, 0, forever);  //Calls itself to start the animation all over again in a loop if forever = true
                            }
                        }
                    }

                    public void onAnimationRepeat(Animation animation) {
                        // TODO Auto-generated method stub
                    }

                    public void onAnimationStart(Animation animation) {
                        // TODO Auto-generated method stub
                    }
                });
            }
            //mp.start();
            //turnOnFlash();
                /*mCameraSource.takePicture(null, new PictureCallback(){
                    @Override
                    public void onPictureTaken(byte[] data) {
                        if (data != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data .length);
                            Log.i("asdf", "TOOK IN DATA");
                            if(bitmap!=null){
                                Log.i("asdf", "BITMAP NOT NULL");
                                File file=new File(Environment.getExternalStorageDirectory()+"/dirr");
                                if(!file.isDirectory()){
                                    file.mkdir();
                                }

                                file=new File(Environment.getExternalStorageDirectory()+"/dirr",System.currentTimeMillis()+".jpg");
                                try
                                {
                                    file.mkdirs();
                                    Log.i("asdf", "TRIED SAVING");
                                    FileOutputStream fileOutputStream=new FileOutputStream(file);
                                    Log.i("asdf", "TRIED SAVING1");
                                    bitmap.compress(Bitmap.CompressFormat.JPEG,100, fileOutputStream);
                                    Log.i("asdf", "TRIED SAVING2");
                                    fileOutputStream.flush();
                                    fileOutputStream.close();

                                    Log.i("asdf", "SAVE SUCCESS");
                                }
                                catch(IOException e){
                                    e.printStackTrace();
                                    Log.i("asdf", Log.getStackTraceString(e));
                                }
                                catch(Exception exception)
                                {
                                    exception.printStackTrace();
                                    Log.i("asdf", Log.getStackTraceString(exception));
                                }

                            }
                        }
                    }
                });*/
        });

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //getCamera();
    }


    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, BitmapFactory.decodeResource(getResources(), R.drawable.afro), BitmapFactory.decodeResource(getResources(), R.drawable.crosshair));
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
            if (clicked) {
                Log.i("asdf", Float.toString(face.getPosition().y));
                Rect hitbox = new Rect(Math.round(face.getPosition().x), Math.round(face.getPosition().y), Math.round(face.getPosition().x + face.getWidth()), Math.round(face.getPosition().y + face.getHeight()));
                if (hitbox.contains(150, 200)) {
                    mp.start();
                }
            }
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }
}