package com.example.gps.gps;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class DragonLinksTrackingActivity extends Activity {
    private Button btnEx;
    private Button btnCamera;

    private boolean isRunning;
    public static TelephonyManager tel;
    private SharedPreferences mSettings = null;
    private GsmCellLocation mGsmCellLocation;
    private int _mcc = 0, _mnc = 0, celid = 0, lac = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dragon_links_tracking);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        /*boolean chk = saveTelManager();
        int count = 1;
        while (chk == false && count < 5){
            chk = saveTelManager();
            count++;
        }*/
        if(Build.VERSION.SDK_INT >= 23){
            List<String> permissionsNeeded = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                permissionsNeeded.add(android.Manifest.permission.READ_PHONE_STATE);
            }

            if(permissionsNeeded.size() > 0){
                ActivityCompat.requestPermissions(this,
                        permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                        111);
            }
        }

        btnEx = (Button) findViewById(R.id.btnEx);
        btnCamera = (Button) findViewById(R.id.camera);

        isRunning = isServiceRunning();
        if (isRunning) {
            btnEx.setText("STOP");
        } else {
            btnEx.setText("START");
        }

        /*threadLoadSendServer = new Thread(new Runnable() {
            public void run() {
                handler.post(startService);
            }
        });
        threadLoadSendServer.start();*/

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startService();
            }
        }, 3000);

        btnEx.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                isRunning = isServiceRunning();
                Intent service = new Intent(getApplicationContext(),
                        DLsTracking.class);
                if (!isRunning) {
                    boolean chk = saveTelManager();
                    if(chk == true){
                        getApplicationContext().startService(service);
                        btnEx.setText("STOP");
                    }
                } else {
                    getApplicationContext().stopService(service);
                    btnEx.setText("START");
                }
            }
        });

        btnCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent frmcamera = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(frmcamera);

            }
        });
    }

    private  void startService(){
        boolean chk = saveTelManager();

        while(chk == false){
            try {
                Thread.sleep(3000);
                chk = saveTelManager();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(chk == true){
            isRunning = isServiceRunning();
            if(!isRunning){
                Intent service = new Intent(getApplicationContext(),
                        DLsTracking.class);
                getApplicationContext().startService(service);
            }
        }
    }

//    private Runnable startService = new Runnable() {
//
//        public void run() {
//
//            boolean chk = saveTelManager();
//
//            while(chk == false){
//                try {
//                    Thread.sleep(3000);
//                    chk = saveTelManager();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if(chk == true){
//                isRunning = isServiceRunning();
//                if(!isRunning){
//                    Intent service = new Intent(getApplicationContext(),
//                            DLsTracking.class);
//                    getApplicationContext().startService(service);
//                    threadLoadSendServer.interrupt();
//                    handler.removeCallbacks(startService);
//                }
//            }
//        }
//    };

    public boolean saveTelManager() {

        if(Build.VERSION.SDK_INT >= 23){
            List<String> permissionsNeeded = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                permissionsNeeded.add(android.Manifest.permission.READ_PHONE_STATE);
            }

            if(permissionsNeeded.size() > 0){
                ActivityCompat.requestPermissions(this,
                        permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                        111);
                return false;
            }
        }

        if (null == this.mSettings) {
            this.mSettings = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
        }

        /*if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.READ_PHONE_STATE},
                    111);
            return  false;
        }else if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    111);
            return  false;
        }else if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_PHONE_STATE},
                    111);
            return  false;
        }*/

        tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        /*mGsmCellLocation = (GsmCellLocation) tel.getCellLocation();
        String networkOperator = tel.getNetworkOperator();
        if (null != mGsmCellLocation) {
            celid = mGsmCellLocation.getCid();
            lac = mGsmCellLocation.getLac();
        }
        if (networkOperator != null && !networkOperator.equals("")) {
            _mcc = Integer.parseInt(networkOperator.substring(0, 3));
            _mnc = Integer.parseInt(networkOperator.substring(3));
        }
        */
        final SharedPreferences.Editor editor = this.mSettings.edit();

        editor.putString("imei", tel.getDeviceId());
//        editor.putInt("celid", celid);
//        editor.putInt("lac", lac);
//        editor.putInt("mcc", _mcc);
//        editor.putInt("mnc", _mnc);

        editor.commit();

        return true;
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.gps.gps.DLsTracking".equals(service.service
                    .getClassName())) {
                return true;
            }
        }
        return false;
    }
}
