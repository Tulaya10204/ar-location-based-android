package ng.dat.ar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.opengl.Matrix;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;

import ng.dat.ar.dao.PlaceCollectionDao;
import ng.dat.ar.model.http.HttpManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ARActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, SensorEventListener {

    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private AROverlayView arOverlayView;
    private Camera camera;
    private ARCamera arCamera;
    private TextView tvCurrentLocation;

    private SensorManager sensorManager;
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_container_layout);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        tvCurrentLocation = (TextView) findViewById(R.id.tv_current_location);
        arOverlayView = new AROverlayView(this);

        //connect google api
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //init();
    }

    private void init() {
        Call<PlaceCollectionDao> call = HttpManager.getInstance().getService().loadData();
        call.enqueue(new Callback<PlaceCollectionDao>() {
            @Override
            public void onResponse(Call<PlaceCollectionDao> call, Response<PlaceCollectionDao> response) {
                if (response.isSuccessful()) {
                    PlaceCollectionDao dao = response.body();
                    Toast.makeText(ARActivity.this, dao.getItemDaos().get(0).getPlaceName(), Toast.LENGTH_LONG).show();
                } else {
                    try {
                        Toast.makeText(ARActivity.this, response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<PlaceCollectionDao> call, Throwable t) {
                Toast.makeText(ARActivity.this, t.toString(), Toast.LENGTH_LONG).show();
            }

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
            googleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requestCameraPermission();
        registerSensors();
        initAROverlayView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        releaseCamera();
        unregisterSensors();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
    }

    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }

    public void initAROverlayView() {
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        cameraContainerLayout.addView(arOverlayView);
    }

    public void initARCameraView() {
        reloadSurfaceView();

        if (arCamera == null) {
            arCamera = new ARCamera(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainerLayout.addView(arCamera);
        arCamera.setKeepScreenOn(true);
        initCamera();
    }

    private void initCamera() {
        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);
            } catch (RuntimeException ex) {
                Toast.makeText(this, "Camera not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void reloadSurfaceView() {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }

        cameraContainerLayout.addView(surfaceView);
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    private void registerSensors() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrixFromVector = new float[16];
            float[] projectionMatrix = new float[16];
            float[] rotatedProjectionMatrix = new float[16];

            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sensorEvent.values);

            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }

            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //do nothing
    }


    private void updateLatestLocation(Location location) {
        if (arOverlayView != null && location != null) {
            arOverlayView.updateCurrentLocation(location);
            tvCurrentLocation.setText(String.format("lat: %s \nlon: %s \naltitude: %s \n",
                    location.getLatitude(), location.getLongitude(), location.getAltitude()));
        }
    }

    //Google Api Location Services

    @Override
    public void onConnected(Bundle bundle) {
        //connect success

        LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);
            if (locationAvailability.isLocationAvailable()) {
                Toast.makeText(ARActivity.this, "GPS responce", Toast.LENGTH_LONG).show();
                LocationRequest locationRequest = new LocationRequest()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(5000)
                        .setFastestInterval(1000);
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            } else {
                //Do something when location not available
                Toast.makeText(ARActivity.this, "GPS not responce", Toast.LENGTH_LONG).show();
            }
    }

    @Override
    public void onLocationChanged(Location location){
        updateLatestLocation(location);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //un-enable connect

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //connect fail

    }
}
