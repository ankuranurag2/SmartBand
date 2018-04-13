//package com.ankuranurag2.smartband;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.provider.Settings;
//import android.util.Log;
//import android.widget.Toast;
//
//import static android.content.Context.LOCATION_SERVICE;
//
//public class GPSTracker implements LocationListener {
//
//    private final Context mContext;
//
//    boolean isGPSEnabled = false;
//
//    boolean isNetworkEnabled = false;
//
//    boolean canGetLocation = false;
//
//    Location location;
//    double latitude;
//    double longitude;
//
//    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
//
//    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
//
//    protected LocationManager locationManager;
//
//    public GPSTracker(Context context) {
//        this.mContext = context;
//        getLocation();
//    }
//
//    public Location getLocation() {
//        try {
//            locationManager = (LocationManager) mContext
//                    .getSystemService(LOCATION_SERVICE);
//
//            isGPSEnabled = locationManager
//                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
//
//            isNetworkEnabled = locationManager
//                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//            if (!isGPSEnabled && !isNetworkEnabled) {
//            } else {
//                this.canGetLocation = true;
//                if (isNetworkEnabled) {
//                    try {
//                        locationManager.requestLocationUpdates(
//                                LocationManager.NETWORK_PROVIDER,
//                                MIN_TIME_BW_UPDATES,
//                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//                        Log.d("ankur", "Network");
//                        if (locationManager != null) {
//                            location = locationManager
//                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                            if (location != null) {
//                                latitude = location.getLatitude();
//                                longitude = location.getLongitude();
//                            }
//                        }
//                    } catch (SecurityException e) {
//                        Toast.makeText(mContext, "Net error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        e.printStackTrace();
//                    }
//                }
//                if (isGPSEnabled) {
//                    if (location == null) {
//                        try {
//                            locationManager.requestLocationUpdates(
//                                    LocationManager.GPS_PROVIDER,
//                                    MIN_TIME_BW_UPDATES,
//                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//
//                            if (locationManager != null) {
//                                location = locationManager
//                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                                if (location != null) {
//                                    latitude = location.getLatitude();
//                                    longitude = location.getLongitude();
//                                }
//                            }
//                        } catch (SecurityException e) {
//                            Toast.makeText(mContext, "GPS error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return location;
//    }
//
//    public void stopUsingGPS() {
//        if (locationManager != null) {
//            locationManager.removeUpdates(GPSTracker.this);
//        }
//    }
//
//    public double getLatitude() {
//        if (location != null) {
//            latitude = location.getLatitude();
//        }
//        return latitude;
//    }
//
//    public double getLongitude() {
//        if (location != null) {
//            longitude = location.getLongitude();
//        }
//        return longitude;
//    }
//
//    public boolean canGetLocation() {
//        return this.canGetLocation;
//    }
//
//    public void showSettingsAlert() {
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
//
//        alertDialog.setTitle("GPS is settings");
//
//        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
//
//        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                mContext.startActivity(intent);
//            }
//        });
//
//        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//
//        alertDialog.show();
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//    }
//
//    @Override
//    public void onStatusChanged(String s, int i, Bundle bundle) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//    }
//}
