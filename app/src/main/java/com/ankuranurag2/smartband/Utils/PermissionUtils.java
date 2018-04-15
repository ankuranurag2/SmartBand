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
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ankuranurag2.smartband.MainActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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


    public static void sendSMS(Context context, String phoneNo) {
        SharedPreferences pref = context.getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
        String lat = pref.getString(MainActivity.PREF_LATEST_LAT, "0");
        String lon = pref.getString(MainActivity.PREF_LATEST_LONG, "0");
        String address = pref.getString(MainActivity.PREF_LATEST_ADDRESS, "");

        String homLat = pref.getString(MainActivity.PREF_HOME_LAT, "0");
        String homLong = pref.getString(MainActivity.PREF_HOME_LONG, "0");

        double distance = getDistanceBetweenTwoPoints(new PointF(Float.valueOf(homLat), Float.valueOf(homLong)), new PointF(Float.valueOf(lat), Float.valueOf(lon)));
        String d = new DecimalFormat("##.00").format(distance / 1000) + " KM";
        String message;
        if (address.isEmpty())
            message = "I NEED YOUR URGENT HELP!!!\nMy Location is\nLatitude: " + lat + "\nLongitude: " + lon + "\n" + d + " away from home.";
        else
            message = "I NEED YOUR URGENT HELP!!!\nMy Location is\nLatitude: " + lat + "\nLongitude: " + lon + "\n Address: " + address + "\n" + d + " away from home.";


//        try {
//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
//            Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
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

    public static void showHomeAlert(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set Home Location!!!")
                .setMessage("Set current location as your Home Location?")
                .setCancelable(true)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences pref = context.getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString(MainActivity.PREF_HOME_LAT, pref.getString(MainActivity.PREF_LATEST_LAT, "0"));
                        editor.putString(MainActivity.PREF_HOME_LONG, pref.getString(MainActivity.PREF_LATEST_LONG, "0"));
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

    public static void showContactAlert(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final EditText input = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(16,16,16,16);
        input.setLayoutParams(lp);
        input.setPadding(16, 16, 16, 16);
        input.setHint("Mobile Number");
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setTitle("Add Contact!!!")
                .setMessage("Please enter mobile number.")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences preferences = context.getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
                        ArrayList<String> list;
                        String jsonString;
                        Type type = new TypeToken<List<String>>() {}.getType();
                        if (preferences.contains(MainActivity.PREF_CONTACT_LIST)) {
                            jsonString = preferences.getString(MainActivity.PREF_CONTACT_LIST, "");
                            list = new Gson().fromJson(jsonString, type);
                        } else
                            list = new ArrayList<String>();

                        if (list.size() >= 5) {
                            Toast.makeText(context, "Only five contacts can be added.", Toast.LENGTH_SHORT).show();
                        } else {
                            list.add(list.size(), input.getText().toString());
                            SharedPreferences.Editor editor=preferences.edit();
                            editor.putString(MainActivity.PREF_CONTACT_LIST,new Gson().toJson(list,type));
                            editor.apply();
                        }

                        dialog.dismiss();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
}
