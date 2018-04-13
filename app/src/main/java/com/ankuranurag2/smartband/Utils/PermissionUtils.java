package com.ankuranurag2.smartband.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.ankuranurag2.smartband.MainActivity;

import java.text.DecimalFormat;

import static android.content.Context.MODE_PRIVATE;

public class PermissionUtils {

    public static void requestPermission(final Activity activity, int REQUEST_CODE) {
        String[] perms = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (!hasPermissions(activity, perms)) {
            ActivityCompat.requestPermissions(activity, perms, REQUEST_CODE);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.startActivity(new Intent(activity, MainActivity.class));
                    activity.finish();
                }
            }, 2000);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null &&
                permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    public static void sendSMS(Context context, String phoneNo, String msg) {
        SharedPreferences pref = context.getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
        String lat = pref.getString("lat", "0");
        String lon = pref.getString("long", "0");
        String address = pref.getString("add", "");

        String homLat= pref.getString("homeLat","0");
        String homLong= pref.getString("homeLong","0");

        double distance=getDistanceBetweenTwoPoints(new PointF(Float.valueOf(homLat),Float.valueOf(homLong)),new PointF(Float.valueOf(lat),Float.valueOf(lon)));
        String d=new DecimalFormat("##.00").format(distance/1000)+" KM";
        String message="I NEED YOUR URGENT HELP!!!\nMy Location is\nLatitude: "+lat+"\nLongitude: "+lon+"\n"+d+" away from home.";
        Log.d("TAG",message);
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static double getDistanceBetweenTwoPoints(PointF p1, PointF p2) {
        double R = 6371000; // m
        double dLat = Math.toRadians(p2.x - p1.x);
        double dLon = Math.toRadians(p2.y - p1.y);
        double lat1 = Math.toRadians(p1.x);
        double lat2 = Math.toRadians(p2.x);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;

        return d;
    }

    public static void showAlert(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set Home Location!!!")
                .setMessage("Set current location as your Home Location?")
                .setCancelable(true)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences pref = context.getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("homeLat", pref.getString("lat", "0"));
                        editor.putString("homeLong", pref.getString("long", "0"));
                        editor.putString("homeAdd", pref.getString("add", ""));
                        editor.apply();

                        dialog.dismiss();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
}
