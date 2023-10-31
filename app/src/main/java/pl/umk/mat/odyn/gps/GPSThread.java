/*
    BSD 3-Clause License
    Copyright (c) Viacheslav Kushinir <kushnir@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.gps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import pl.umk.mat.odyn.main_service.ServiceConnector;
import pl.umk.mat.odyn.main_service.types.IconType;
import pl.umk.mat.odyn.settings.SettingNames;
import pl.umk.mat.odyn.settings.SettingOptions;
import pl.umk.mat.odyn.settings.SettingsProvider;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;

/**
 * Wątek GPS, zbiera informacje o lokalizacji, prędkości, przyspieszeniu. Dodatkowo implementuje uproszczoną metodę wykrywania kolizji.
 * GPS thread, gathers information about location, speed, acceleration. Additionally implements simplified method to detect collisions.
 */
public class GPSThread extends Thread implements SensorEventListener {
	private LocationManager locationManager;
	private LocationListener locationListener;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Context context;
	private Handler handler;
	private SettingsProvider settingsProvider;

	private float acceleration;
	private float currentAcceleration;
	private float lastAcceleration;
	private double speed;
	private double accelerometerSen;
	private int minimumSpeed;
	private long lastSettingsUpdateTime = 0;

	public GPSThread(Context context) {
		this.context = context;
		this.handler = new Handler(Looper.getMainLooper());
	}

	/**
	 * Jest to metoda uruchamiająca wątek do obsługi GPS.
	 * Method starting the thread
	 */
	@Override
	public void run() {
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {
			/**
			 * Metoda, która aktualizuje wartości w DataHolder po zmianie lokalizacji.
			 * Method that updates values in DataHolder when locations is changed.
			 */
			@Override
			public void onLocationChanged(Location location) {
				DataHolder dataHolder = DataHolder.getInstance();
				dataHolder.setLatitude("Lat: " + location.getLatitude());
				dataHolder.setLongitude("Long: " + location.getLongitude());
				speed = location.getSpeed() * 3.6;
				dataHolder.setSpeed("Speed: " + String.format("%.1f", speed));
				//Log.d("GPSThread", dataHolder.getLatitude() + ", " + dataHolder.getLongitude() + ", " + dataHolder.getSpeed());
			}

			/**
			 * Jest to metoda odpowiadająca za zmianę statusu wątku nawigacji.
			 * Method responsible for handling status change
			 * @param provider
			 * @param status
			 * @param extras
			 */
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			/**
			 * Jest to metoda odpowiadająca za działania po uruchomieniu pracy dostawcy.
			 * Method responsible for handling event of provider being enabled
			 * @param provider
			 */
			@Override
			public void onProviderEnabled(String provider) {
			}

			/**
			 * Jest to metoda odpowiadająca za działania po zakończeniu pracy dostawcy.
			 * Method responsible for handling event of provider being disabled
			 * @param provider
			 */
			@Override
			public void onProviderDisabled(String provider) {
			}
		};

		Log.d("GPSThread", "Latitude");

		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		acceleration = 0.0f;
		currentAcceleration = SensorManager.GRAVITY_EARTH;
		lastAcceleration = SensorManager.GRAVITY_EARTH;

		handler.post(new Runnable() {
			@SuppressLint("MissingPermission") // already requested on app startup
			@Override
			public void run() {
				int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
				if (resultCode == ConnectionResult.SUCCESS) {
					//Log.d("GPSThread", "handler runnable");
					locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 1000, 0, locationListener);
					sensorManager.registerListener(GPSThread.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
				} else {
					Log.e("GPSThread", "Google Play services are not available or need an update");
				}
			}
		});
		locationManager.removeUpdates(locationListener);
		sensorManager.unregisterListener(this);
	}

	/**
	 * Jest to metoda odpowiadająca za zakończenie pracy wątku GPS.
	 * Method responsible for stopping the thread
	 */
	public void stopGPS() {
		locationManager.removeUpdates(locationListener);
		sensorManager.unregisterListener(this);
		interrupt();
	}

	/**
	 * Jest to metoda odpowiadająca za zmianę sensora.
	 * Method to detect phone shake using acceleration sensor
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];

		lastAcceleration = currentAcceleration;
		currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);

		// make sure settings are not needlessly fetched 50 times/sec
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastSettingsUpdateTime >= 2000) { // updates values every 2s
			getSettingsValues();
			lastSettingsUpdateTime = currentTime;
		}

		float delta = currentAcceleration - lastAcceleration;
		acceleration = acceleration * 0.9f + delta;
		if (acceleration > accelerometerSen && speed >= minimumSpeed) {
			startEmergency();
		}
	}

	/**
	 * Jest to metoda służąca do otrzymywania ustawień
	 * Method returning settings value
	 */
	private void getSettingsValues() {
		try {
			settingsProvider = new SettingsProvider();
			int selectedAccelerometerPosition = settingsProvider.getSettingInt(SettingNames.spinners[6]);
			accelerometerSen = SettingOptions.accelerometerSens[selectedAccelerometerPosition];
			int selectedMinSpeedPosition = settingsProvider.getSettingInt(SettingNames.spinners[7]);
			minimumSpeed = SettingOptions.minimumSpeed[selectedMinSpeedPosition];
			//Log.d("GPSThread", ">>> Current acceleration sens: " + accelerometerSen);
			//Log.d("GPSThread", ">>> Current minimumSpeed: " + minimumSpeed);
		} catch (JSONException e) {
			Log.e("GPSThread", ">>> ERROR, "+ e);
			accelerometerSen = 1;
			minimumSpeed = 20;
		}
	}

	/**
	 * Jest to metoda odpowiadająca za zmianę dokładności.
	 * Method responsible for handling accuracy change
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	/**
	 * Jest to metoda odpowiadająca za rozpoczęcie nagrywania awaryjnego.
	 * Method to start emergency recording
	 */
	private void startEmergency() {
		Log.d("GPSThread", ">>> nagrywanie awaryjne");
		ServiceConnector.onClickIcon(IconType.emergency);
	}
}
