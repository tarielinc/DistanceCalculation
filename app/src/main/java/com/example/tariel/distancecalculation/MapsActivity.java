package com.example.tariel.distancecalculation;

/**
 * Created by Tariel on 2/7/2018.
 */

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;


public class MapsActivity extends FragmentActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private Marker marker1, marker2;
    private LatLng posMarker1, posMarker2;
    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Button button = findViewById(R.id.button);
        Button route = findViewById(R.id.route);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap.clear();
            }
        });
        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (polyline!=null){
                    polyline.remove();
                }
                if (marker1==null && marker2==null){

                }else {

                    polyline = mMap.addPolyline(new PolylineOptions()
                            .add(posMarker1,posMarker2)
                            .color(Color.BLUE)
                            .geodesic(true)
                            .clickable(true)
                            .width(18f));
                }

            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                double meter = round(SphericalUtil.computeDistanceBetween
                        (posMarker1,posMarker2),1);
                double km = round(SphericalUtil.computeDistanceBetween
                        (posMarker1,posMarker2)/1000,2);

                Toast.makeText(MapsActivity.this,
                        String.valueOf(meter) + " meters" +
                                '\n' + String.valueOf(km) + " kilometers",
                        Toast.LENGTH_LONG).show();

            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (marker2 != null) {
                    marker2.remove();
                }
                marker2 = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .draggable(true)
                        .visible(true)
                        .title("Point B"));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                posMarker2=marker2.getPosition();

            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (marker1 != null) {
                    marker1.remove();
                }
                marker1 = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .draggable(true)
                        .visible(true)
                        .title("Point A"));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                posMarker1=marker1.getPosition();

            }
        });
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(MapsActivity.this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
        } else {
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}