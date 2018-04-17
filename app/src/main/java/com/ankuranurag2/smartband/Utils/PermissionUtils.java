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
import android.text.InputFilter;
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
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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


    public static void sendSMS(Context context) {
        SharedPreferences pref = context.getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
        String lat = pref.getString(MainActivity.PREF_LATEST_LAT, "0");
        String lon = pref.getString(MainActivity.PREF_LATEST_LONG, "0");
        String address = pref.getString(MainActivity.PREF_LATEST_ADDRESS, "");

        String homLat = pref.getString(MainActivity.PREF_HOME_LAT, "0");
        String homLong = pref.getString(MainActivity.PREF_HOME_LONG, "0");

        double distance = getDistanceBetweenTwoPoints(new PointF(Float.valueOf(homLat), Float.valueOf(homLong)), new PointF(Float.valueOf(lat), Float.valueOf(lon)));
        String d = new DecimalFormat("##.00").format(distance / 1000) + " KM";
        String message;
        if (address == null || address.isEmpty())
            message = "I NEED YOUR URGENT HELP!!!\nMy Location is\nLatitude: " + lat + "\nLongitude: " + lon + "\n" + d + " away from home.";
        else
            message = "I NEED YOUR URGENT HELP!!!\nMy Location is\nLatitude: " + lat + "\nLongitude: " + lon + "\n Address: " + address + "\n" + d + " away from home.";

        Gson gson=new Gson();
        LinkedHashMap<Integer,String> contactMap;
        Type type = new TypeToken<LinkedHashMap<Integer, String>>() {}.getType();
        if (pref.contains(MainActivity.PREF_CONTACT_LIST)) {
            String jsonString = pref.getString(MainActivity.PREF_CONTACT_LIST, "");
            contactMap = gson.fromJson(jsonString, type);
            for (Integer key:contactMap.keySet()){
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(contactMap.get(key), null, message, null, null);
                    Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else
            Toast.makeText(context, "Contact list empty!!!", Toast.LENGTH_SHORT).show();
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
                        editor.putString(MainActivity.PREF_HOME_LAT,"28.6692"); //pref.getString(MainActivity.PREF_LATEST_LAT, "0"));
                        editor.putString(MainActivity.PREF_HOME_LONG, "77.4538");//pref.getString(MainActivity.PREF_LATEST_LONG, "0"));
                        editor.commit();
                        Toast.makeText(context, "Home address updated.", Toast.LENGTH_SHORT).show();
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

        final SharedPreferences preferences = context.getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
        final Gson gson=new Gson();
        final LinkedHashMap<Integer,String> contactMap;
        final Type type = new TypeToken<LinkedHashMap<Integer, String>>() {}.getType();
        if (preferences.contains(MainActivity.PREF_CONTACT_LIST)) {
            String jsonString = preferences.getString(MainActivity.PREF_CONTACT_LIST, "");
            contactMap = gson.fromJson(jsonString, type);
        } else
            contactMap = new LinkedHashMap<>();

        if (contactMap.size() == 5)
            Toast.makeText(context, "Maximum five contacts can be added.", Toast.LENGTH_SHORT).show();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            final EditText input = new EditText(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(20, 20, 20, 20);
            input.setLayoutParams(lp);
            input.setPadding(20, 20, 20, 20);
            input.setHint("Mobile Number");
            input.setInputType(InputType.TYPE_CLASS_PHONE);
            input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
            builder.setTitle("Add Contact!!!")
                    .setMessage("Please enter mobile number.")
                    .setView(input)
                    .setCancelable(false)
                    .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String num = input.getText().toString();
                            if (num.length() < 10) {
                                Toast.makeText(context, "Invalid Phone number.", Toast.LENGTH_SHORT).show();
                            } else {
                                contactMap.put(contactMap.size(),num);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(MainActivity.PREF_CONTACT_LIST, gson.toJson(contactMap, type));
                                editor.commit();
                                Toast.makeText(context, "Phone number added.", Toast.LENGTH_SHORT).show();

                                dialog.dismiss();
                            }
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

    public static void showDeleteAlert(final Context context) {

        final SharedPreferences preferences = context.getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
        final LinkedHashMap<Integer, String> contactMap;
        final Gson gson=new Gson();
        final Type type = new TypeToken<LinkedHashMap<Integer, String>>() {}.getType();
        if (preferences.contains(MainActivity.PREF_CONTACT_LIST)) {
            String jsonString = preferences.getString(MainActivity.PREF_CONTACT_LIST, "");
            contactMap = gson.fromJson(jsonString, type);

            if (contactMap.size() > 0) {

                final boolean[] checkedContacts = new boolean[]{false, false, false, false, false};
                String[] contactArray=new String[contactMap.size()];
                int z=0;
                for (Map.Entry<Integer,String> entry:contactMap.entrySet()){
                    contactArray[z]=entry.getValue();
                    z++;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMultiChoiceItems(contactArray, checkedContacts, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedContacts[which]=isChecked;
                    }
                });
                builder.setTitle("Select contact to delete.")
                        .setCancelable(true)
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<Integer> keyList=new ArrayList<Integer>(contactMap.keySet());
                                for (int i = 0; i < checkedContacts.length; i++) {
                                    if (checkedContacts[i]){
                                        Integer key=keyList.get(i);
                                        contactMap.remove(key);
                                    }
                                }
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(MainActivity.PREF_CONTACT_LIST, gson.toJson(contactMap, type));
                                editor.commit();
                                dialog.dismiss();
                                Toast.makeText(context, "Deleted.", Toast.LENGTH_SHORT).show();
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
            } else
                Toast.makeText(context, "No number in contact list.", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(context, "No number in contact list.", Toast.LENGTH_SHORT).show();
    }
}
