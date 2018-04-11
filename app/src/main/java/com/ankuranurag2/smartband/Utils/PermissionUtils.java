package com.ankuranurag2.smartband.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;

import com.ankuranurag2.smartband.MainActivity;

public class PermissionUtils {

    public static void requestPermission(final Activity activity, int REQUEST_CODE) {
        String[] perms = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (!hasPermissions(activity,perms)){
            ActivityCompat.requestPermissions(activity, perms, REQUEST_CODE);
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.startActivity(new Intent(activity, MainActivity.class));
                    activity.finish();
                }
            },2000);
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
}
