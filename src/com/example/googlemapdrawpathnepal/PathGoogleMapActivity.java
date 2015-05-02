package com.example.googlemapdrawpathnepal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class PathGoogleMapActivity extends FragmentActivity {

	LinearLayout llMap;
	ProgressBar pbWait;

	private static final LatLng LOWER_MANHATTAN = new LatLng(40.722543,
			-73.998585);
	private static final LatLng BROOKLYN_BRIDGE = new LatLng(40.7057, -73.9964);
	private static final LatLng WALL_STREET = new LatLng(40.7064, -74.0094);

	// here we come to nepal
	private static final LatLng KUPONDOLE = new LatLng(27.687597, 85.316775);
	private static final LatLng RATNAPARK = new LatLng(27.706774, 85.314743);
	private static final LatLng MAITIGHAR = new LatLng(27.6924726, 85.3225654);
	GoogleMap googleMap;
	final String TAG = "PathGoogleMapActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_path_google_map);
		llMap = (LinearLayout) findViewById(R.id.llMap);
		pbWait = (ProgressBar) findViewById(R.id.pbWait);
		Thread timer = new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {

					PathGoogleMapActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							pbWait.setVisibility(View.GONE);
							llMap.setVisibility(View.VISIBLE);
						}
					});
				}
				super.run();
			}

		};
		timer.start();
		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		googleMap = fm.getMap();

		MarkerOptions options = new MarkerOptions();
		options.position(KUPONDOLE);
		options.position(MAITIGHAR);
		options.position(RATNAPARK);

		googleMap.addMarker(options);
		String url = getMapsApiDirectionsUrl();
		ReadTask downloadTask = new ReadTask();
		downloadTask.execute(url);

		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MAITIGHAR, 13));
		addMarkers();

	}

	// corrected one
	/**
	 * private String getMapsApiDirectionsUrl() { String waypoints =
	 * "&waypoints=optimize:true|" + BROOKLYN_BRIDGE.latitude + "," +
	 * BROOKLYN_BRIDGE.longitude;
	 * 
	 * String sensor = "sensor=false&mode=driving&alternatives=true"; String
	 * params = waypoints + "&" + sensor; String sourcelat =
	 * Double.toString(LOWER_MANHATTAN.latitude); String sourcelng =
	 * Double.toString(LOWER_MANHATTAN.longitude); String destlat =
	 * Double.toString(WALL_STREET.latitude); String destlng =
	 * Double.toString(WALL_STREET.longitude);
	 * 
	 * String url =
	 * "https://maps.googleapis.com/maps/api/directions/json?origin=" +
	 * sourcelat + "," + sourcelng + "&destination=" + destlat + "," + destlng +
	 * params; return url; }
	 */

	/**
	 * public String getMapsApiDirectionsUrl() { StringBuilder urlString = new
	 * StringBuilder();
	 * urlString.append("http://maps.googleapis.com/maps/api/directions/json");
	 * urlString.append("?origin=");// from
	 * urlString.append(Double.toString(LOWER_MANHATTAN.latitude));
	 * urlString.append(",");
	 * urlString.append(Double.toString(LOWER_MANHATTAN.longitude));
	 * urlString.append("&destination=");// to
	 * urlString.append(Double.toString(BROOKLYN_BRIDGE.latitude));
	 * urlString.append(",");
	 * urlString.append(Double.toString(BROOKLYN_BRIDGE.longitude));
	 * urlString.append("&sensor=false&mode=driving&alternatives=true"); return
	 * urlString.toString(); }
	 */
	private String getMapsApiDirectionsUrl() {
		// TODO Auto-generated method stub
		StringBuilder urlString = new StringBuilder();
		urlString.append("http://maps.googleapis.com/maps/api/directions/json");
		urlString.append("?origin=");
		urlString.append(Double.toString(KUPONDOLE.latitude));

		urlString.append(",");
		urlString.append(Double.toString(KUPONDOLE.longitude));

		urlString.append("&destination=");
		urlString.append(Double.toString(RATNAPARK.latitude));

		urlString.append(",");
		urlString.append(Double.toString(RATNAPARK.longitude));

		urlString.append("&waypoints=optimize:true|");
		urlString.append(Double.toString(MAITIGHAR.latitude));

		urlString.append(",");
		urlString.append(Double.toString(MAITIGHAR.longitude));

		urlString.append("&sensor=false&mode=driving&alternatives=true");
		return urlString.toString();
	}

	private void addMarkers() {
		if (googleMap != null) {
			googleMap.addMarker(new MarkerOptions().position(MAITIGHAR).title(
					"Through Point"));
			googleMap.addMarker(new MarkerOptions().position(KUPONDOLE).title(
					"Start"));
			googleMap.addMarker(new MarkerOptions().position(RATNAPARK).title(
					"End"));
		}
	}

	private class ReadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			String data = "";
			try {
				HttpConnection http = new HttpConnection();
				data = http.readUrl(url[0]);
			} catch (Exception e) {
				// this exception is caught
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			new ParserTask().execute(result);
		}
	}

	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				PathJSONParser parser = new PathJSONParser();
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
			ArrayList<LatLng> points = null;
			PolylineOptions polyLineOptions = null;
			// traversing through routes
			for (int i = 0; i < routes.size(); i++) {
				points = new ArrayList<LatLng>();
				polyLineOptions = new PolylineOptions();
				List<HashMap<String, String>> path = routes.get(i);
				/** get() method returns the elements at i location in the list */
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);
					points.add(position);
				}
				polyLineOptions.addAll(points);
				polyLineOptions.width(4);
				polyLineOptions.color(Color.BLUE);
			}
			googleMap.addPolyline(polyLineOptions);
		}
	}
}
