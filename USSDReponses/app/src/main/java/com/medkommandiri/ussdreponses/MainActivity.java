package com.medkommandiri.ussdreponses;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.karan.churi.PermissionManager.PermissionManager;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    PermissionManager permissionManager;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(MainActivity.this,USSDService.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionManager = new PermissionManager() {};
            permissionManager.checkAndRequestPermissions(this);
        }

        if (!isAccessibilitySettingsOn(getApplicationContext())){
            Toast.makeText(MainActivity.this, "Please Enable Service for APP USSD Responses", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.checkResult(requestCode,permissions,grantResults);

    }

    private void dialNumber(String code){
        String ussdcode = "*"+code+ Uri.encode("#");
        startActivity(new Intent("android.intent.action.CALL",Uri.parse("tel:"+ussdcode)));
    }

    private boolean isAccessibilitySettingsOn(Context mContext){
        int accesibilityEnabled = 0;
        final String service = getPackageName()+"/"+USSDService.class.getCanonicalName();
        try {
            accesibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(USSDService.class.getSimpleName(),"Accessibility Enabled");
        } catch (Settings.SettingNotFoundException e) {
           Log.e(USSDService.class.getSimpleName(),"Error Finding Setting :"+e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accesibilityEnabled == 1) {
            Log.v(USSDService.class.getSimpleName(), "Accesibility is Enable");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) ;
                {
                    String accessibilityService = mStringColonSplitter.next();
                    Log.v(USSDService.class.getSimpleName(), "Accessibility Service : " + accessibilityService + "" + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(USSDService.class.getSimpleName(), "We've found corretct setting");
                        return true;
                    }
                }
            }
        } else{
            Log.v(USSDService.class.getSimpleName(),"ACCESSIBILITY IS DISABLE");
        }
        return false;
    }

    public void CALL(View view) {
        dialNumber("806*77");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView = findViewById(R.id.respon);
                textView.setText(String.valueOf(USSDService.responUSSD));

            }
        },2000);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (! hasFocus ) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

}
