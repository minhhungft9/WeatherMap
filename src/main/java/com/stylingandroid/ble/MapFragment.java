package com.stylingandroid.ble;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {
	public static MapFragment newInstance() {
		return new MapFragment();
	}

	private MapView map;
	private IMapController mapController;
	private TextView tv;
	private TextView lightSensor = null;
	private TextView tempSensor = null;
	private TextView humSensor = null;
	//private TextView sensorLabel = null;

	private JsonData data;
	private double traveledDistance;

	public double temp;
	public double humi;
	public double lightLevel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_map, container, false);
		if (v != null) {
			tv = (TextView) v.findViewById(R.id.textView);
			tv.setText("Not connected");
			//sensorLabel = v.findViewById(R.id.sensorLabel);

			lightSensor = v.findViewById(R.id.lightLevel);
			tempSensor = v.findViewById(R.id.temperature);
			humSensor = v.findViewById(R.id.humidity);

			/* Use the LocationManager class to obtain GPS locations */
			LocationManager locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
			LocationListener locListener = new MapFragment.MyLocationListener();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
						PackageManager.PERMISSION_GRANTED && getActivity().checkSelfPermission(
						Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					return null;
				}
			}
			if (locManager != null) {
				locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, locListener);
				locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1, locListener);
			}

			map = (MapView) v.findViewById(R.id.googleMap);
			map.setTileSource(TileSourceFactory.MAPNIK);
			mapController = map.getController();
			mapController.setZoom(31);
			map.setMultiTouchControls(true);
			map.setBuiltInZoomControls(true);
		}
		return v;
	}

	public void setData(float temperature, float humidity) {
		if(tempSensor != null){
			tempSensor.setText(getString(R.string.temp_label) + getString(R.string.temp_format, temperature));
		}
		if(humSensor != null){
			humSensor.setText(getString(R.string.humidity_label) + getString(R.string.humidity_format, humidity));
		}
		temp = Math.round(temperature*10) / 10.0;
		humi = Math.round(humidity*10) / 10.0;
	}

	public void setLuxometerData(float luxometer){
		if (lightSensor != null){
		    lightSensor.setText(getString(R.string.light_level) + getString(R.string.light_level_format, luxometer));
        }
	    lightLevel = Math.round(luxometer*10) / 10.0;

	}

	/* Class My Location Listener */

	public class MyLocationListener implements LocationListener
	{
		@Override
		public void onLocationChanged(Location loc){
			Log.d("tag", "Finding Latitude");
			double lat = loc.getLatitude();
			Log.d("tag", "Lat: "+String.valueOf(lat));
			Log.d("tag", "Finding Longitude");
			double lon = loc.getLongitude();
			Log.d("tag", "Lon: "+String.valueOf(lon));

			data = new JsonData(temp, humi, lightLevel, loc);
			traveledDistance++;

			String connect = "";
			if(isConnected()){
				connect = "You are connected";
				if(traveledDistance == 1000){
					data.pushToSever();
					traveledDistance = 0;
				}
			}
			else{
				connect = "You are NOT conncted";
			}
			// Display location
			String Text = "Your current location is: " + "\nLatitude: " + lat + "\nLongitude: " + lon + "\n" + connect;
			tv.setText(Text);

			GeoPoint curLoc = new GeoPoint(loc.getLatitude(), loc.getLongitude());
			List<Overlay> mapOverlays = new ArrayList<>();
			Drawable icon = null;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
				icon = getResources().getDrawable(R.drawable.ic_location_on_black_24dp, null);
			} else {
				icon = getResources().getDrawable(R.drawable.ic_location_on_black_24dp);
			}

			Marker marker = new Marker(map);
			marker.setPosition(curLoc);
			marker.setTitle("Temperature: "+ temp + "\u2103" + "\nHumidity: " + humi + "%" + "\nLight level: " + lightLevel + "lux");
			marker.setIcon(icon);
			mapOverlays.add(marker);

			map.getOverlays().clear();
			map.getOverlays().addAll(mapOverlays);
			map.invalidate();
			mapController.setCenter(curLoc);
		}

		@Override
		public void onProviderDisabled(String provider){
			Toast.makeText(getActivity().getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
		}

		@Override
		public void onProviderEnabled(String provider){
			Toast.makeText(getActivity().getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras){

		}
	}

	public boolean isConnected(){
		ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
			return true;
		else
			return false;
	}
}
