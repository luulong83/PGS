package com.example.gps.gps;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class DLsTracking extends Service   {
	private String mImei = "";
	//private final int mPort = 8001;
	private final int mPort = 2000;
	private String mData = "";
	private String lat = "0", lng = "0";
	float speed = 0, head = 0;
	long time = 0;
	int _interval = 60;
	long strDate;
	double altitude;
	int _mcc, _mnc, celid, lac;
	String _deviceID, status, accuracy, heading, mileage;
	boolean _isSend = false, _activated = false, _isView = false,
			_isFirst = true, _isStart = false, _isValid = false,
			mIsChanged = false;
	private LocationManager mlocManager;
	//private final String mIPServer = "192.168.234.11";
	private final String mIPServer = "103.45.233.101";

	private NotificationManager mNM;
	private int NOTIFICATION = 2000;
	private Handler handler;
	private long mDelayTime = 15000;
	private Thread threadLoadSendServer = null;
	private DecimalFormat df;
	private SharedPreferences mSettings = null;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		if (null != mNM) {
			mNM.cancel(NOTIFICATION);
		}
		if (null != threadLoadSendServer) {

			String strReturn = "";
			/*SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
			String dateString = formatter.format(new Date());*/

//			strReturn = "[adr:" + mImei + "," + dateString + "," + 0 + ","
//					+ 50.0 + "," + lat + "," + lng + "," + speed * 3.6 + "," + 0.0 + ","
//					+ 0.0 + "," + _mcc + ":" + _mnc + ":" + lac + ":" + celid
//					+ "," + -1 + ",stop]";

//			strReturn = "adr:" + mImei + ",,,," + lat + "," + lng + "," + speed * 3.6 + "," + 0.0
//					+ "," + 0.0 + "," + _mcc + ":" + _mnc + ":" + lac + ":" + celid
//					+ "," + -1 + ",stop";

			strReturn = "adr:" + mImei + "," + lat + "," + lng + ",stop";

			mData = strReturn;


			mData = strReturn;
			try {
				SendToServer();
			} catch (UnknownHostException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}

			//threadLoadSendServer.stop();
			threadLoadSendServer.interrupt();
			//threadLoadSendServer.stop();
			handler.removeCallbacks(runnable);
		}
		super.onDestroy();
	}

	public void getTelManager() {

		if (null == this.mSettings) {
			this.mSettings = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
		}

		mImei = this.mSettings.getString("imei", "");
//		celid = this.mSettings.getInt("celid", 0);
//		lac = this.mSettings.getInt("lac", 0);
//		_mcc = this.mSettings.getInt("mcc", 0);
//		_mnc = this.mSettings.getInt("mnc", 0);

//		Toast.makeText(getApplicationContext(),
//				mImei + ":" + celid + lac + _mcc + _mnc, Toast.LENGTH_LONG)
//				.show();

		Toast.makeText(getApplicationContext(),
				mImei, Toast.LENGTH_LONG)
				.show();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		getTelManager();

		/* Handler Thread */
		handler = new Handler();
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy);
		/* Format Lng , Lat */
		df = new DecimalFormat();
		df.setMinimumFractionDigits(6);
		df.setMaximumFractionDigits(6);
		/* Get Notification */
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		/* Set Notification */
		showNotification("");

		/* Check GPS Enable */
		//CheckEnableGPS();
		List<String> permissionsNeeded = new ArrayList<>();
		if(Build.VERSION.SDK_INT >= 23){
			if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
				permissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
			}
			if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
				permissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
			}
			if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
				permissionsNeeded.add(android.Manifest.permission.READ_PHONE_STATE);
			}
		}

		if (permissionsNeeded.size() > 0) {
			//Toast.makeText(DLsTracking.this, "First enable LOCATION ACCESS Or GPS in settings.", Toast.LENGTH_LONG).show();

		}else{
			try{
				mlocManager = (LocationManager)
						getSystemService(Context.LOCATION_SERVICE);
				Criteria criteria = new Criteria();
				String provider = mlocManager.getBestProvider(criteria, false);
				Location loc = mlocManager.getLastKnownLocation(provider);

				if (loc != null) {
					CreateSendData(loc);
			 		/* Send to server */
					SendFirst();
				}
				mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,new MyLocationListener());

				loc = mlocManager.getLastKnownLocation(provider);
		 		/* Set Thread */
				threadLoadSendServer = new Thread(new Runnable() {
					public void run() {
						handler.post(runnable);
					}
				});
				threadLoadSendServer.start();
			}catch (Exception e){
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
				String dateString = formatter.format(new Date());
				String strReturn = "adr:" + mImei + "," + dateString + ",error:" + e.getMessage() + "";
				mData = strReturn;
				try {
					SendToServer();
				} catch (UnknownHostException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		}
	}

	private void CreateSendData(Location loc) {

		if (loc.getAccuracy() != 0) {
			//strDate = loc.getTime();
			altitude = loc.getAltitude();
			//double nlat = Double.parseDouble(df.format(loc.getLatitude()));
			String nlat = String.valueOf(loc.getLatitude()).replace(",",".");
			//double nlng = Double.parseDouble(df.format(loc.getLongitude()));
			String nlng = String.valueOf(loc.getLongitude()).replace(",",".");
			//long ntime = loc.getTime();

			if (!nlat.equals(lat) || !nlng.equals(lng) ) {
				//lat = Double.parseDouble(df.format(nlat));
				lat = nlat;
				//lng = Double.parseDouble(df.format(nlng));
				lng = nlng;
				/*time = ntime;
				if (loc.hasSpeed())
					speed = loc.getSpeed();
				if (loc.hasBearing())
					head = loc.getBearing();
				_isValid = true;
				if (loc.getLatitude() > 0) {
					accuracy = "N";
				} else {
					accuracy = "S";
				}
				if (loc.getLongitude() > 0) {
					accuracy = "E";
				} else {
					accuracy = "W";
				}*/
				if (!_isSend) {
					mData = CreateStringNMEA();
				} else {
					//if (speed > 1) {
						mData = CreateStringNMEA();
					//}
				}
			}

		}
	}

	private void SendFirst() {
		if (!_isSend) {
			try {
				SendProcess();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void SendProcess() throws UnknownHostException, IOException {

		if (0 == mData.length()) {

			String strReturn = "";
//			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
//			String dateString = formatter.format(new Date());

//			strReturn = "[adr:" + mImei + "," + dateString + "," + 0 + ","
//					+ 50.0 + "," + 0 + "," + 0 + "," + 0 + "," + 0.0 + ","
//					+ 0.0 + "," + _mcc + ":" + _mnc + ":" + lac + ":" + celid
//					+ "," + -1 + ",running]";


//			strReturn = "adr:" + mImei + ",,,," + 0 + "," + 0 + "," + 0 + "," + 0.0 + ","
//					+ 0.0 + "," + _mcc + ":" + _mnc + ":" + lac + ":" + celid
//					+ "," + -1 + ",running";

			strReturn = "adr:" + mImei + ",0.0,0.0" + ",running";

			mData = strReturn;

		}
		SendToServer();
	}

	private void SendToServer() throws UnknownHostException, IOException {
		Socket skt = new Socket(mIPServer, mPort);
		BufferedWriter os = null;
		try{

			//Socket skt = new Socket();
			//skt.connect(new InetSocketAddress(InetAddress.getByName(mIPServer),mPort),3000);

			int count = 0;
			while (skt.isConnected() == false){
				if(count == 5) break;
				skt.connect(new InetSocketAddress(InetAddress.getByName(mIPServer),mPort),3000);
				count++;
				Thread.sleep(3000);
			}

			//DataOutputStream outToServer = new DataOutputStream(skt.getOutputStream());

			if (mData.length() > 0 && skt.isConnected()) {
				os = new BufferedWriter(new OutputStreamWriter(skt.getOutputStream()));
				os.write(mData);
				os.newLine();
				os.flush();
				//outToServer.writeBytes(mData);
				if (null != mNM) {
					mNM.cancel(NOTIFICATION);
					showNotification("");
				}
			/* The first */
				if (!_isSend) {
					_isSend = true;
				}
			}

			//skt.close();
			//speed = 0;
		}catch (java.net.SocketException e){
			e.printStackTrace();
			System.out.print( "Loi: SocketException" +e.toString()+"\n");
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.print( "Loi: InterruptedException" +e.toString()+"\n");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.print( "Loi: UnknownHostException" +e.toString()+"\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.print( "Loi: IOException" +e.toString()+"\n");
		}
		finally {
			skt.close();
			speed = 0;
		}

	}

	private Runnable runnable = new Runnable() {

		public void run() {
			if (_isSend) {
				try {
					SendProcess();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				SendFirst();
			}
			handler.postDelayed(this, mDelayTime);
		}
	};

	private void CheckEnableGPS() {
		String provider = Settings.Secure.getString(getContentResolver(),Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (!provider.equals("")) {
			// GPS Enabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			getApplicationContext().sendBroadcast(poke);
		} else {
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			getApplicationContext().sendBroadcast(poke);

		}
		Toast.makeText(this, "Please Enabled GPS and Network!",
				Toast.LENGTH_LONG).show();
		// GpsStatus gpsstat = mlocManager.getGpsStatus(null);

		// Iterable sats = gpsstat.getSatellites();
		// Iterator satI = sats.iterator();
		// int count = 0;
		// while (satI.hasNext()) {
		// GpsSatellite gpssatellite = (GpsSatellite) satI.next();
		// if (gpssatellite.usedInFix()) {
		// count++;
		// }
		// }
		// GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
		// public void onGpsStatusChanged(int event) {
		// if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
		//
		// }
		// }
		// };
	}

	private void showNotification(String strStatus) {

		// Set the icon, scrolling text and timestamp
		/*Notification notification = new Notification(R.drawable.ic_launcher,
				strStatus, System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		// The PendingIntent to launch our activity if the user selects this
		// notification
			Intent toLaunch = new Intent(this, ViewMap.class);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				toLaunch, 0);

		try {
			// Set the info for the views that show in the notification panel.
			notification.setLatestEventInfo(this, "DragonLinks - Tracking",
					"View Map", contentIntent);
		} catch (Exception ex) {
		}
		// Send the notification.
		mNM.notify(NOTIFICATION, notification);
		*/
	}

	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {
			if (null != loc) {
				CreateSendData(loc);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
	}

	public Boolean CheckAscSpeed(double speed, double nearestSpeed,
			double minute) {
		Boolean result = false;
		if (minute < 5) {
			double asc = Math.abs(speed - nearestSpeed);
			if (minute > 1)
				asc = asc / minute;
			if (asc > 70)
				result = true;
		}
		return result;
	}

	public String CreateStringNMEA() {

		String strReturn = "";
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
//		String dateString = "";
		/*int statusGPS = 2;
		if ("0" == lat && "0" == lng) {
			dateString = formatter.format(new Date());
			statusGPS = 0;
		} else {
			dateString = formatter.format(new Date(strDate));
		}*/
//		strReturn = "[adr:" + mImei + "," + dateString + "," + statusGPS + ","
//				+ 50.0 + "," + lat + "," + lng + "," + speed * 3.6 + "," + 0.0
//				+ "," + 0.0 + "," + _mcc + ":" + _mnc + ":" + lac + ":" + celid
//				+ "," + -1 + ",running]";
//		strReturn = "adr:" + mImei + ",,,," + lat + "," + lng + "," + speed * 3.6 + "," + 0.0
//				+ "," + 0.0 + "," + _mcc + ":" + _mnc + ":" + lac + ":" + celid
//				+ "," + -1 + ",running";

		strReturn = "adr:" + mImei + "," + lat + "," + lng + ",running";
		return strReturn;
	}
}
