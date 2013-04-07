package com.yeyaxi.android.ubercheckout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yeyaxi.android.ubercheckout.utilities.Constant;

/**
 * 
 * @author Yaxi Ye
 *
 */
public class MainActivity extends Activity {
	
	private RibbonMenuView rbmView;
	private SharedPreferences pref;
	private static final String TAG = "MainActivity";
	private SeekBar seekBar_radius;
	private TextView textView_radius;
	private RadioButton radio_km;
	private RadioButton radio_mi;
	private Spinner spinner_language;
	
//	private static Handler mHandler;
	private LocationManager mLocationManager;
//	private static final int SEARCH_TWEETS = 2;
//	private static final int DRAW_MARKER = 1;
    private static final int TEN_SECONDS = 10000;
    private static final int TEN_METERS = 10;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static GoogleMap map;
	private float[] coords = new float[2];// coords[0] is latitude, coords[1] is longitude
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		pref = getSharedPreferences("pref", MODE_PRIVATE);
		
		// Set up View
		seekBar_radius = (SeekBar)findViewById(R.id.seekBar_radius);
		textView_radius = (TextView) findViewById(R.id.textView_radius);
		radio_km = (RadioButton) findViewById(R.id.radio_km);
		radio_mi = (RadioButton) findViewById(R.id.radio_mi);

		//Set up map view
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		
//		mHandler = new TaskHandler(this); 

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		// Sliding Menu
		rbmView = (RibbonMenuView) findViewById(R.id.ribbonMenuView);

		// Set up the Language Spinner
		spinner_language = (Spinner) findViewById(R.id.spinner_language);
		
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(this,
		        R.array.languages_array, android.R.layout.simple_spinner_item);
		
		// Specify the layout to use when the list of choices appears
		languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		// Apply the adapter to the spinner
		spinner_language.setAdapter(languageAdapter);
		spinner_language.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, 
		            int pos, long id) {
				// Use space as the default choice
				if (parent.getItemAtPosition(pos).equals(" ") == false) {
					SharedPreferences.Editor editor = pref.edit();
					editor.putString(Constant.KEY_LANGUAGE, parent.getItemAtPosition(pos).toString());
					editor.commit();
				}
				Log.d(TAG, "Selected: " + parent.getItemAtPosition(pos).toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// If nothing is selected, just load the first value.
//				parent.getItemAtPosition(0).toString();				
			}
		});

	    // Init the value of seek bar as 20
	    seekBar_radius.setIndeterminate(false);
	    seekBar_radius.setProgress(20);
	    seekBar_radius.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    
	    OnMarkerDragListener markerDragListener = new OnMarkerDragListener() {



			@Override
			public void onMarkerDragStart(Marker marker) {
				// Called when the marker drag is started

			}

			@Override
			public void onMarkerDragEnd(Marker marker) {
				// Called when the marker is dropped down.
				coords[0] = (float) marker.getPosition().latitude;
				coords[1] = (float) marker.getPosition().longitude;
//				RestoreUIwithSavedLocation(coords);
				Log.d(TAG, "Pin Dropped at: " + coords[0] + ", " + coords[1]);
			}

			@Override
			public void onMarkerDrag(Marker marker) {

			}
		};

		map.setOnMarkerDragListener(markerDragListener);
			   
	}
	

	@Override
	protected void onStart() {
		super.onStart();

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		final boolean gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (!gpsEnabled) {
			Dialog dialog = enableGpsDialog(this);
			dialog.show();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Locate the user automatically as the app starts
		getCurrentLocation();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Stop listening to the Loation Providers
		mLocationManager.removeUpdates(geoListener);
		Log.i(TAG, "Location updates stopped.");
	}
	
	 @Override
	 protected void onNewIntent(Intent intent) {
		 // Get the intent, verify the action and get the query
		 if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
		      String keywords = intent.getStringExtra(SearchManager.QUERY);
		      // Do Search in a separate thread
		      
		      performSearch(keywords);
		    }
	 }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		// Get the SearchView and set the searchable configuration
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_options:
	            rbmView.toggleMenu();
	            return true;
	        
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void performSearch(String keywords) {
		// Check if we need to append some search parameters
		Log.i(TAG, Integer.toString(seekBar_radius.getProgress()));
		if (spinner_language.getSelectedItem().toString().equals(" ") == true &&
				seekBar_radius.getProgress() == 0) {
			//TODO finish the search term
			Log.i(TAG, "in this clause");
		} else if (seekBar_radius.getProgress() > 0) {
			//TODO Search with radius
			String unit = "km";
			if (radio_km.isChecked())
				unit = "km";
			else if (radio_mi.isChecked())
				unit = "mi";
			
			String query = Constant.BASE_SEARCH_URL + keywords + 
					"&geocode=" + coords[0] + "," + coords[1] + "," + seekBar_radius.getProgress() + unit;
			Log.i(TAG, query);
			PerformSearch task = new PerformSearch();
			task.execute(query);
//			new PerformSearch().execute(query);
		}
			
	}

	private void getCurrentLocation() {
		// Show mapview, drop position pin
		Location gps = requestLocationFromProvider(LocationManager.GPS_PROVIDER, 0);
		Location aGps = requestLocationFromProvider(LocationManager.NETWORK_PROVIDER, 0);
		// If got two location data
		if (gps != null && aGps != null) {
			updateUIwithLocation(getBetterLocation(gps, aGps));
		} else if (gps != null) {
			updateUIwithLocation(gps);
		} else if (aGps != null) {
			updateUIwithLocation(aGps);
		}
	}
	
	private Location getBetterLocation(Location newLocation, Location currentLocation) {
		if (currentLocation == null)
			// Just use a location rather than a null location
			return newLocation;

		// Check whether the new location fix is newer or older
		long timeDelta = newLocation.getTime() - currentLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// Use the new location if the time span is more than 2 mins
		if (isSignificantlyNewer)
			return newLocation;
		else if (isSignificantlyOlder)
			return currentLocation;

		// Check the accuracy
		int accuracyDelta = (int) (newLocation.getAccuracy() - currentLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the providers are the same
		boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), currentLocation.getProvider());
		if (isMoreAccurate)
			return newLocation;
		else if (isNewer && !isLessAccurate)
			return newLocation;
		else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
			return newLocation;
		mLocationManager.removeUpdates(geoListener);
		Log.i(TAG, "Location updates stopped.");
		return currentLocation;
	}

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }

	private Location requestLocationFromProvider(final String provider, final int errResId) {
		Location location = null;
		if (mLocationManager.isProviderEnabled(provider)) {
			mLocationManager.requestLocationUpdates(provider, TEN_SECONDS, TEN_METERS, geoListener);
			location = mLocationManager.getLastKnownLocation(provider);
		}
		return location;
	}

	private void updateUIwithLocation(Location location) {

//		Message.obtain(mHandler, UPDATE_LATLNG, location.getLatitude() + ", " + location.getLongitude()).sendToTarget();
//		Message.obtain(mHandler, DRAW_MARKER, location).sendToTarget();
		new LocationUpdate().execute(location);
	}
	
	private final LocationListener geoListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onLocationChanged(Location location) {
			updateUIwithLocation(location);			
		}
	};

    // Method to launch Settings
    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

	 /**
     * Dialog to prompt users to enable GPS on the device.
     */
    public Dialog enableGpsDialog(Context context) {
    	return new AlertDialog.Builder(context)
    	.setTitle("Enable GPS Location")
    	.setMessage("GPS not enabled, would you like to enable it?")
    	.setPositiveButton("Go Settings", new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			enableLocationSettings();
    		}
    	})
    	.create();
    }

    private class LocationUpdate extends AsyncTask<Location, Void, LatLng> {

    	double coordLat, coordLng;
		@Override
		protected LatLng doInBackground(Location... params) {
			coordLat = (params[0]).getLatitude();
			coordLng = (params[0]).getLongitude();
			LatLng location = new LatLng(coordLat, coordLng);
			return location;
		}
    	
		@Override
		protected void onPostExecute(LatLng location) {
			coords[0] = (float)coordLat;
			coords[1] = (float)coordLng;
			map.clear();
			Marker m = map.addMarker(new MarkerOptions().position(location));				
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
			map.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
			// Instantiates a new CircleOptions object and defines the center and radius
			CircleOptions circleOptions = new CircleOptions()
			    .center(location)
			    .radius(1000); // In meters
			
			// Get back the mutable Circle
			Circle circle = map.addCircle(circleOptions);
			circle.setFillColor(Color.argb(100, 81, 207, 245));
			circle.setStrokeColor(Color.argb(255, 81, 207, 245));
			m.setDraggable(true);			
		}
    }

    
    /**
     * AsyncTask to perform the burden of Network stuff
     * @author Yaxi Ye
     *
     */
    public class PerformSearch extends AsyncTask<String, Void, List> {

    	public List resultList;
    	
    	private static final String TAG = "PerformSearch";

    	@Override
    	protected List doInBackground(String... params) {
    		resultList = new ArrayList<Result>();
    		resultList = readTwitterFeed(params[0]);
    		return resultList;
    	}


    	@Override
    	protected void onPostExecute(List list) {
    		//TODO Put markers on the map
    		new DrawTweetMarkers().execute(list);
    	}

    	private List readTwitterFeed(String searchString) {
    		//		StringBuilder builder = new StringBuilder();
    		HttpClient client = new DefaultHttpClient();
    		HttpGet httpGet = new HttpGet(searchString);
    		try {
    			HttpResponse response = client.execute(httpGet);
    			StatusLine statusLine = response.getStatusLine();
    			int statusCode = statusLine.getStatusCode();
    			if (statusCode == 200) {
    				HttpEntity entity = response.getEntity();
    				InputStream content = entity.getContent();
    				JsonReader reader = new JsonReader(new InputStreamReader(content, "UTF-8"));
    				try {
    					return readResultsArray(reader);
    				}     finally {
    					reader.close();
    				}
    			} else {
    				Log.e(TAG, "Failed to download file");
    			}
    		} catch (ClientProtocolException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		return null;
    	}


    	private List readResultsArray(JsonReader reader) throws IOException {
    		List results = new ArrayList();
    		
    		reader.beginObject();
    		while (reader.hasNext()) {
    			String name = reader.nextName();
    			if (name.equals("results")) 
    				results.add(readResult(reader));
    			else
    				reader.skipValue();
    		}
    		reader.endObject();

    		return results;
    	}


    	private Result readResult(JsonReader reader) throws IOException {

    		String from_user = null, 
    				from_user_id_str = null, 
    				from_user_name = null,
    				text = null;
    		Geo geo = null;
    		URL profile_image_url = null;

    		reader.beginArray();
			reader.beginObject();
//			reader.beginArray();
    		while (reader.hasNext()) {
    			String name = reader.nextName();
    			if (name.equals("from_user"))
    				from_user = reader.nextString();
    			else if (name.equals("from_user_id_str"))
    				from_user_id_str = reader.nextString();
    			else if (name.equals("from_user_name"))
    				from_user_name = reader.nextString();
    			else if (name.equals("geo")) {
    				reader.beginObject();
    				while(reader.hasNext()) {
//    					String s = reader.nextName();
    					if (name.equals("coordinates")) {
        					reader.beginArray();
    						double latitude = 0;
    						double longitude = 0;
    						while (reader.hasNext()) {
    							latitude = reader.nextDouble();
    							longitude = reader.nextDouble();
    							geo = new Geo(latitude, longitude);
    						}
						}
    				}
    				reader.endArray();
    				reader.endObject();
    			}
    			else if (name.equals("profile_image_url"))
    				profile_image_url = new URL(reader.nextString());
    			else if (name.equals("text"))
    				text = reader.nextString();
    			else
    				reader.skipValue();

    		}
//    		reader.endArray();
    		reader.endObject();
    		reader.endArray();

    		return new Result(from_user, from_user_id_str, from_user_name, text, geo, profile_image_url);
    	}

    }
    
    private class DrawTweetMarkers extends AsyncTask<List, Void, LatLng> {
    	private List tweetResult;
		@Override
		protected LatLng doInBackground(List... params) {
			tweetResult = new ArrayList<Result>();
			tweetResult = params[0];

			for (int i = 0; i < tweetResult.size(); i++) {
				Result result = (Result) tweetResult.get(i);
				URL image_url = result.getProfile_image_url();
				try {
					downloadUrl(image_url, result.getFrom_user_id_str());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}


			return null;
		}
		
		@Override
		protected void onPostExecute(LatLng latLng) {
			//TODO put markers
			if (tweetResult != null && tweetResult.isEmpty() == false) {
				for (int i = 0; i < tweetResult.size(); i++) {
					Result r = (Result) tweetResult.get(i);
					
					final Bitmap bm = decodeSampledBitmapFromFile(r.getFrom_user_id_str(), 40, 40);
					
					// Inflate info windows
					final View infoView = getLayoutInflater().inflate(R.layout.marker_info_window, null);
					
					// Set up markers
					map.addMarker(new MarkerOptions()
					.position(r.getGeo())
					.title(r.getFrom_user_name())
					.snippet(r.getText()));
					map.setInfoWindowAdapter(new InfoWindowAdapter() {

						@Override
						public View getInfoWindow(Marker marker) {
							
							return null;
						}

						@Override
						public View getInfoContents(Marker marker) {
							TextView username = (TextView)infoView.findViewById(R.id.textView_username);
							TextView tweets = (TextView)infoView.findViewById(R.id.textView_tweets);
							ImageView thumbnail = (ImageView)infoView.findViewById(R.id.imageView_profile_thumb);
							thumbnail.setImageBitmap(bm);
							username.setText(marker.getTitle());
							tweets.setText(marker.getSnippet());
							return infoView;
						}
					});

				}
			}
		}
		
		
		private Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {
			Bitmap bm = null;
			File cachePath;
	        if (getExternalFilesDir(null) != null) {
	        	cachePath = getExternalFilesDir(null); // Priorly use External Cache
	        } else {
	        	cachePath = getFilesDir();// Use Internal cache instead if external one is not available
	        }
			File file = new File(cachePath, filename);
			if (file != null) {
				// First decode with inJustDecodeBounds=true to check dimensions
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(file.getAbsolutePath(), options);

				// Calculate inSampleSize
				options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

				// Decode bitmap with inSampleSize set
				options.inJustDecodeBounds = false;
				bm = BitmapFactory.decodeFile(file.getAbsolutePath(), options); 
			}
			return bm;
		}
		
		public Bitmap decodeSampledBitmapFromUrl(URL url, String filename, int reqWidth, int reqHeight) {
			Bitmap bm = null;

			File file;
			try {
				file = downloadUrl(url, filename);


				// First decode with inJustDecodeBounds=true to check dimensions
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(file.getAbsolutePath(), options);

				// Calculate inSampleSize
				options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

				// Decode bitmap with inSampleSize set
				options.inJustDecodeBounds = false;
				bm = BitmapFactory.decodeFile(file.getAbsolutePath(), options); 
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return bm;  
		}
		
		public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
			// Raw height and width of image
			final int height = options.outHeight;
			final int width = options.outWidth;
			int inSampleSize = 1;

			if (height > reqHeight || width > reqWidth) {
				if (width > height) {
					inSampleSize = Math.round((float)height / (float)reqHeight);   
				} else {
					inSampleSize = Math.round((float)width / (float)reqWidth);   
				}   
			}

			return inSampleSize;   
		}
		
		/**
		 * Download file from given URL
		 * @param urlString the URL of file to be downloaded
		 * @return downloaded file
		 * @throws IOException
		 */
		private File downloadUrl(URL url, String filename) throws IOException {
//	        URL url = new URL(urlString);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(15000 /* milliseconds */);
	        conn.setConnectTimeout(20000 /* milliseconds */);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        // Starts the query
	        conn.connect();
	        InputStream stream = conn.getInputStream();     
//	        File cacheDir = getStorageDir(getApplicationContext());
	        File cacheDir;
	        if (getExternalFilesDir(null) != null) {
	        	cacheDir = getExternalFilesDir(null); // Priorly use External Cache
	        } else {
	        	cacheDir = getFilesDir();// Use Internal cache instead if external one is not available
	        }
	        File cache = new File(cacheDir, filename);
	        FileOutputStream fos = new FileOutputStream(cache);
	        byte[] buffer = new byte[1024];
	        int bufferLength = 0;
	        while ((bufferLength = stream.read(buffer)) > 0) {
	        	fos.write(buffer, 0, bufferLength);
	        }	            
	        fos.flush();
	        fos.close();
	        return cache;
	    }
		
		/**
		 * Check network connectivity.
		 * @return true if the current network is connected.
		 */
		private boolean isNetworkConnected() {
			ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if (ni != null && ni.isConnected()) {
				return true;
			} else {
				return false;
			}
			
		}
		
		/** 
		 * Checks if external storage is available for read and write
		 * @return true if external storage is available for read & write
		 */
		public boolean isExternalStorageWritable() {
		    String state = Environment.getExternalStorageState();
		    if (Environment.MEDIA_MOUNTED.equals(state)) {
		        return true;
		    }
		    return false;
		}
		
		/** 
		 * Checks if external storage is available to at least read
		 * @return true if external storage is readable
		 */
		public boolean isExternalStorageReadable() {
		    String state = Environment.getExternalStorageState();
		    if (Environment.MEDIA_MOUNTED.equals(state) ||
		        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		        return true;
		    }
		    return false;
		}
    	
    }
    
}
