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
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.StringRequest;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.FileEntity;

//import static android.hardware.Camera.*;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends AppCompatActivity {
    private static final String TAG1 = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private String name = "Alice";

    private String enemy = "Bob";

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private static boolean clicked = false;

    private int Points = 0;

    private int Health = 5;

    private int EnemyHealth = 5;

    MediaPlayer mp;

    MediaPlayer zoop;

    MediaPlayer yeah;

    MediaPlayer alive;

    MediaPlayer dust;

    private Camera camera;
    Camera.Parameters params;

    RequestQueue mRequestQueue;


    // Flash variables
    private static final String TAG = "Flashlight";
    private Camera mCamera;
    private Camera.Parameters parameters;
    private CameraManager camManager;
    private Context context;

    private String getUserId (Bitmap bmp) {
        return "";
    }

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
    * Turning On Flash
    */
    private void turnFlashlightOn() {
        // if (camera == null || params == null) {
        //     return;
        // }
        // try {
        //     Log.i("asdf", "This line");
        //     //params = camera.getParameters();
        //     params = Camera.open().getParameters();
        //     Log.i("asdf", "That line");
        //     params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        //     camera.setParameters(params);
        //     camera.startPreview();
        // }catch (Exception e) {
        //     Log.i("asdf", Log.getStackTraceString(e));
        // }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    camManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                    String cameraId = null; // Usually front camera is at 0 position.
                    if (camManager != null) {
                        cameraId = camManager.getCameraIdList()[0];
                        camManager.setTorchMode(cameraId, true);
                    }
                } catch (CameraAccessException e) {
                    Log.e(TAG, e.toString());
                }
            } else {
                mCamera = Camera.open();
                parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            }
    }
    /*
    * Turning Off Flash
    */
    private void turnFlashlightOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                String cameraId;
                camManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                if (camManager != null) {
                    cameraId = camManager.getCameraIdList()[0]; // Usually front camera is at 0 position.
                    camManager.setTorchMode(cameraId, false);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            mCamera = Camera.open();
            parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
            mCamera.stopPreview();
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    public static void saveFile(Context context, Bitmap b, String picName) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(picName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }
        catch (FileNotFoundException e) {
            Log.d(TAG, "file not found");
            e.printStackTrace();
        }
        catch (IOException e) {
            Log.d(TAG, "io exception");
            e.printStackTrace();
        } finally {
            fos.close();
        }
    }

    public static Bitmap loadBitmap(Context context, String picName) throws IOException {
        Bitmap b = null;
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(picName);
            b = BitmapFactory.decodeStream(fis);
        }
        catch (FileNotFoundException e) {
            Log.d(TAG, "file not found");
            e.printStackTrace();
        }
        catch (IOException e) {
            Log.d(TAG, "io exception");
            e.printStackTrace();
        } finally {
            fis.close();
        }
        return b;
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("asdf", "Directory not created");
        }
        return file;
    }

    private void takePicture(){
        mCameraSource.takePicture(null, new PictureCallback(){
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPictureTaken(byte[] data) {
                if (data != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length);

                    if(bitmap!=null){
                        //((ImageView) findViewById(R.id.crosshair)).setImageBitmap(bitmap);

                        /*AsyncHttpClient client = new AsyncHttpClient();
                        client.addHeader("Content-Type", "application/octet-stream");
                        client.addHeader("Ocp-Apim-Subscription-Key", "ddc8bd86b06b4a9ea2f12037e6d8a903");
                        RequestParams params = new RequestParams();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        try {
                            stream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/

                        //String save = "http://ec2-54-84-172-101.compute-1.amazonaws.com/instance/" + System.currentTimeMillis() + ".bmp";
                        //Log.i("asdf", save);
                        /*try {
                            params.put("data", f);
                        } catch (FileNotFoundException e) {
                            Log.i("asdf", Log.getStackTraceString(e));
                        }*/
                        AsyncHttpClient client = new AsyncHttpClient();
                        //client.addHeader("Content-Type", "application/octet-stream");
                        client.addHeader("Ocp-Apim-Subscription-Key", "ddc8bd86b06b4a9ea2f12037e6d8a903");
                        RequestParams params = new RequestParams();
                        params.put("picture", bitmap);
                        String name = System.currentTimeMillis() + ".bmp";
                        params.put("name", name);
                        params.put("filename", name);
                        params.put("id", name);
                        Log.i("asdf", name);
                        client.post("http://ec2-54-84-172-101.compute-1.amazonaws.com/upload_file", params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            }

                            /*@Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                // error handling
                                Log.i("asdf", responseString);
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                // success
                                Log.i("asdf", responseString);
                            }*/
                        });


                        /*MultipartRequest multipartRequest =
                                new MultipartRequest(url, params, image_path, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.e(TAG, "Success Response: " + response.toString());

                                    }
                                }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                        if (error.networkResponse != null) {
                                            Log.e(TAG, "Error Response code: " +
                                                    error.networkResponse.statusCode);
                                        }
                                    });
                        // Add the request to the RequestQueue.
                        MySingleton.getInstance(context).addToRequestQueue(multipartrequest);*/



                        /*Log.i("asdf", "BITMAP NOT NULL");
                        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/EmailClient/");
                        folder.mkdirs();
                        File file = new File(folder,System.currentTimeMillis() + ".JPEG");
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
                        }*/

                    }
                }
            }
        });
    }
    //==============================================================================================
    // Activity Methods
    //==============================================================================================
    final Handler handler = new Handler();
    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        //getCamera();
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        mp = MediaPlayer.create(this, R.raw.lazer);

        zoop = MediaPlayer.create(this, R.raw.laser);

        yeah = MediaPlayer.create(this, R.raw.yeah);

        alive = MediaPlayer.create(this, R.raw.alive);

        dust = MediaPlayer.create(this, R.raw.dust);

        final Runnable r = new Runnable() {
            public void run() {
                handler.postDelayed(this, 3000);
                String url ="https://blooming-hollows-76968.herokuapp.com/get_table";
                JsonArrayRequest jsObjRequest = new JsonArrayRequest
                        (url, new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                JSONObject person = findByName(response, name);
                                JSONObject badperson = findByName(response, enemy);
                                try {
                                    int beforeHealth = Health;
                                    Points = (Integer) person.get("points");
                                    Health = (Integer) person.get("health");
                                    if (Health < beforeHealth) {
                                        dust.start();
                                    }
                                    EnemyHealth = (Integer) badperson.get("health");
                                    ((Button)findViewById(R.id.fire)).setText("Health: " + Integer.toString(Health) + "/5     " + "Score: " + Integer.toString(Points));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO Auto-generated method stub

                            }
                        });
                //MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
                mRequestQueue.add(jsObjRequest);
            }
        };
        handler.postDelayed(r, 0000);

        Button fire = (Button) findViewById(R.id.fire);

        final ImageView demoImage = (ImageView)findViewById(R.id.asdf);

        final int imagesToShow[] = { R.drawable.laser_01, R.drawable.laser_02, R.drawable.laser_03, R.drawable.laser_04, R.drawable.laser_05, R.drawable.laser_06 };

        fire.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                zoop.start();
                //clicked = true;
                //turnFlashlightOn();
                animate(demoImage);
            }

            private void animate(final ImageView imageView) {
                imageView.setImageResource(R.drawable.laser_v2_00000);
                imageView.setVisibility(View.VISIBLE);
                final int duration = 1;
                new CountDownTimer(duration, duration) {
                    public void onTick(long millisUntilFinished) {}
                    public void onFinish() {
                        imageView.setImageResource(R.drawable.laser_v2_00001);
                        new CountDownTimer(duration, duration) {
                            public void onTick(long millisUntilFinished) {}
                            public void onFinish() {
                                imageView.setImageResource(R.drawable.laser_v2_00002);
                                new CountDownTimer(duration, duration) {
                                    public void onTick(long millisUntilFinished) {}
                                    public void onFinish() {
                                        imageView.setImageResource(R.drawable.laser_v2_00003);
                                        clicked = true;
                                        new CountDownTimer(200, 200) {
                                            public void onTick(long millisUntilFinished) {
                                            }
                                            public void onFinish() {
                                                clicked = false;
                                            }
                                        }.start();
                                        new CountDownTimer(duration, duration) {
                                            public void onTick(long millisUntilFinished) {}
                                            public void onFinish() {
                                                imageView.setImageResource(R.drawable.laser_v2_00004);
                                                new CountDownTimer(duration, duration) {
                                                    public void onTick(long millisUntilFinished) {}
                                                    public void onFinish() {
                                                        imageView.setVisibility(View.INVISIBLE);
                                                    }
                                                }.start();
                                            }
                                        }.start();
                                    }
                                }.start();
                            }
                        }.start();
                    }
                }.start();
            }
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

    private JSONObject findByName(JSONArray arr, String name) {
        for (int i = 0; i < arr.length(); i++) {
            try {
                if (arr.getJSONObject(i).get("name").equals(name)) {
                    return (JSONObject) arr.get(i);
                }
            }catch(Exception e) {}
        }
        return null;
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
            int fro = R.drawable.afro_5;
            if (EnemyHealth == 4) {
                fro = R.drawable.afro_4;
            } else if (EnemyHealth == 3) {
                fro = R.drawable.afro_3;
            } else if (EnemyHealth == 2) {
                fro = R.drawable.afro_2;
            } else if (EnemyHealth == 1) {
                fro = R.drawable.afro_1;
            } else if (EnemyHealth == 0) {
                fro = R.drawable.bald;
            }
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, BitmapFactory.decodeResource(getResources(), fro), BitmapFactory.decodeResource(getResources(), R.drawable.crosshair));
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
            int fro = R.drawable.afro_5;
            if (EnemyHealth == 4) {
                fro = R.drawable.afro_4;
            } else if (EnemyHealth == 3) {
                fro = R.drawable.afro_3;
            } else if (EnemyHealth == 2) {
                fro = R.drawable.afro_2;
            } else if (EnemyHealth == 1) {
                fro = R.drawable.afro_1;
            } else if (EnemyHealth == 0){
                fro = R.drawable.bald;
            }
            mFaceGraphic.afro = BitmapFactory.decodeResource(getResources(), fro);
            if (clicked) {
                Rect hitbox = new Rect(Math.round(face.getPosition().x), Math.round(face.getPosition().y), Math.round(face.getPosition().x + face.getWidth()), Math.round(face.getPosition().y + face.getHeight()));
                if (hitbox.contains(240, 320)) {
                    yeah.start();

                    takePicture();

                    String url = "https://blooming-hollows-76968.herokuapp.com/register_hit";

                    // Formulate the request and handle the response.
                    StringRequest jsObjRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject formattedResponse = new JSONObject(response);
                                        int before = Points;
                                        Log.i("asdf", response.toString());
                                        Points = (Integer) formattedResponse.get("score");
                                        if (Points % 500 < before % 500) {
                                            alive.start();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.i("asdf", Log.getStackTraceString(error));
                                    error.printStackTrace();
                                }
                            }) {
                        @Override
                        public Map<String, String> getParams() {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("data", name + "-hit-" + enemy);
                            return params;
                        }
                    };
                    /*JsonObjectRequest jsObjRequest = new JsonObjectRequest
                            (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        int before = Points;
                                        Log.i("asdf", response.toString());
                                        Points = (Integer) response.get("score");
                                        if (Points % 500 < before % 500) {
                                            alive.start();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // TODO Auto-generated method stub
                                    Log.i("asdf", Log.getStackTraceString(error));
                                }
                            }) {
                        @Override
                        public Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("data", name + "-hit-" + enemy);
                            return params;
                        }
                    };*/
                    //MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
                    mRequestQueue.add(jsObjRequest);
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
