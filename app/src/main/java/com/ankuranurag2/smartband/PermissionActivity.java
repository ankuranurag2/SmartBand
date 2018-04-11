package com.ankuranurag2.smartband;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.ankuranurag2.smartband.Utils.PermissionUtils;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class PermissionActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 2048;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        YoYo.with(Techniques.BounceIn)
                .duration(2500)
                .playOn(findViewById(R.id.imageView));

        PermissionUtils.requestPermission(this, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST_CODE:{

                if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    startActivity(new Intent(this,MainActivity.class));
                    this.finish();
                }else{
                    Toast.makeText(this, "Permission Denied.\n The app might not work properly!", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }
}
