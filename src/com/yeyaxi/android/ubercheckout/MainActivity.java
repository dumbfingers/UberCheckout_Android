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
import java.util.Locale;

import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.yeyaxi.android.ubercheckout.utilities.Constant;

/**
 * 
 * @author Yaxi Ye
 *
 */
public class MainActivity extends SherlockFragmentActivity {

	private SharedPreferences pref;
	private static final String TAG = "MainActivity";
	private SeekBar seekBar_radius;
	private TextView textView_radius;
	private RadioButton radio_km;
	private RadioButton radio_mi;
	private Spinner spinner_language;
	private Button button_auth;

	//	private static Handler mHandler;
	private LocationManager mLocationManager;
	//	private static final int SEARCH_TWEETS = 2;
	//	private static final int DRAW_MARKER = 1;
	private static final int TEN_MINUTES = 1000 * 60 * 10;
	private static final int TEN_METERS = 10;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static GoogleMap map;
	private float[] coords = new float[2];// coords[0] is latitude, coords[1] is longitude

	private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	private static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

	private CommonsHttpOAuthConsumer httpOauthConsumer = new CommonsHttpOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET); 
	private OAuthProvider httpOauthprovider = new CommonsHttpOAuthProvider("https://api.twitter.com/oauth/request_token", 
			"https://api.twitter.com/oauth/access_token", 
			"https://api.twitter.com/oauth/authorize");
	static final String TWITTER_CALLBACK_URL = "oauth://ubercheckout";
	static String TWITTER_CONSUMER_KEY = "mcz7i4BDru2TE1LoTWbQ";
	static String TWITTER_CONSUMER_SECRET = "xl0HHTUToe5860H4SikYcmi2bL38gqdUmUAAlJ7SeU8";
	private Twitter twitter;

	private String query;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		pref = getSharedPreferences("pref", MODE_PRIVATE);

		//Set up map view
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		//		mHandler = new TaskHandler(this); 

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Sliding Menu settings
		SlidingMenu menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.LEFT);
		// Sliding from margin will open the menu
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(R.layout.slide_menu);

		// Set ActionBarSherlock
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Set up View
		seekBar_radius = (SeekBar)menu.findViewById(R.id.seekBar_radius);
		textView_radius = (TextView)menu.findViewById(R.id.textView_radius);
		radio_km = (RadioButton)menu.findViewById(R.id.radio_km);
		radio_mi = (RadioButton)menu.findViewById(R.id.radio_mi);
		button_auth = (Button)menu.findViewById(R.id.button_auth);
		
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
		seekBar_radius.setProgress(200);
		updateSlider(seekBar_radius.getProgress());
		seekBar_radius.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				seekBar.getProgress();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser == true) {
					updateSlider(progress);
				}
			}
		});
		
		// Set up the OAuth button
		button_auth.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				oAuth.execute();
			}
		});
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

		// Locate the user automatically as the app starts
		getCurrentLocation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Uri uri = getIntent().getData();
		if (isTwitterLoggedInAlready() == false) {
			//Check if you got NewIntent event due to Twitter Call back only 
			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) { 
				String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER); 
				Log.d(TAG, "Verifier: " + verifier);
				RetrieveAccessTokenTask.execute(verifier);
//				try {
//					String token = pref.getString("request_token", "");
//					String tokenSecret = pref.getString("request_token_secret", "");
//					// Set request token and request secret
//					httpOauthConsumer.setTokenWithSecret(token, tokenSecret);
//					// this will populate token and token_secret in consumer 
//					httpOauthprovider.retrieveAccessToken(httpOauthConsumer, verifier); 
//
//					String userKey = httpOauthConsumer.getToken(); 
//					String userSecret = httpOauthConsumer.getConsumerSecret(); 
//					Log.d(TAG, "Got it: " + userKey + userSecret);
//
//					AccessToken accessToken = new AccessToken(httpOauthConsumer.getToken(), httpOauthConsumer.getConsumerSecret()); 
//
//					twitter = new TwitterFactory().getInstance();
//					twitter.setOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET); 
//
//					twitter.setOAuthAccessToken(accessToken);
//
//					SharedPreferences.Editor e = pref.edit();
//
//					// After getting access token, access token secret
//					// store them in application preferences
//					e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
//					e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
//
//					// Store login status - true
//					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
//					e.commit(); // save changes
//
//					Log.d(TAG, "Twitter OAuth Token > " + accessToken.getToken());
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
		}
	}
	
	AsyncTask<String, Void, Void> RetrieveAccessTokenTask = new AsyncTask<String, Void, Void>(){

		@Override
		protected Void doInBackground(String... verifiers) {
			String token = pref.getString("request_token", "");
			String tokenSecret = pref.getString("request_token_secret", "");
			Log.d(TAG, "Request Token & Secret" + token + tokenSecret);
			// Set request token and request secret
			httpOauthConsumer.setTokenWithSecret(token, tokenSecret);
			// this will populate token and token_secret in consumer 
			try {
				httpOauthprovider.retrieveAccessToken(httpOauthConsumer, verifiers[0]);
			} catch (OAuthMessageSignerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (OAuthNotAuthorizedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (OAuthExpectationFailedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (OAuthCommunicationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 

			String userKey = httpOauthConsumer.getToken(); 
			String userSecret = httpOauthConsumer.getConsumerSecret(); 
			Log.d(TAG, "Got it: " + userKey + userSecret);

			AccessToken accessToken = new AccessToken(httpOauthConsumer.getToken(), httpOauthConsumer.getConsumerSecret()); 

			twitter = new TwitterFactory().getInstance();
			twitter.setOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET); 

			twitter.setOAuthAccessToken(accessToken);

			SharedPreferences.Editor e = pref.edit();

			// After getting access token, access token secret
			// store them in application preferences
			e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
			e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());

			// Store login status - true
			e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
			e.commit(); // save changes

			Log.d(TAG, "Twitter OAuth Token > " + accessToken.getToken());
			return null;
		}
		
	};

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
		//		if (Intent.ACTION_VIEW.equals("x-ubercheckout-oauth-twitter")) {
		//			Log.d(TAG, "Twitter Callback");
		//		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		// Get the SearchView and set the searchable configuration
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

		return true;
	}


	/**
	 * Set the radius while sliding the progress slider
	 * @param sliderProgress the current value of the slider
	 */
	private void updateSlider(int sliderProgress) {
		textView_radius.setText(String.valueOf(sliderProgress));
	}

	/**
	 * Perform search using the given keywords
	 * @param keywords to be searched
	 */
	public void performSearch(String keywords) {
		// Check if we need to append some search parameters
		Log.i(TAG, Integer.toString(seekBar_radius.getProgress()));
		if (spinner_language.getSelectedItem().toString().equals(" ") == true &&
				seekBar_radius.getProgress() == 0) {
			// finish the search term
			Log.i(TAG, "in this clause");
		} else if (seekBar_radius.getProgress() > 0) {
			// Search with radius
			String unit = "km";
			if (radio_km.isChecked())
				unit = "km";
			else if (radio_mi.isChecked())
				unit = "mi";

			query = Constant.BASE_SEARCH_URL + keywords + 
					"&geocode=" + coords[0] + "," + coords[1] + "," + seekBar_radius.getProgress() + unit;
			Log.i(TAG, query);
			if (isTwitterLoggedInAlready() == false) {
				// Do OAuth
//				oAuth.execute();
			} else {
				PerformSearch.execute(query);
			}
		}

	}

	// We need to use separate thread to handle the network operation 
	AsyncTask<Void, Void, Void> oAuth = new AsyncTask<Void, Void, Void>() {

		@Override
		protected Void doInBackground(Void... params) {
			doOauth();
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
//			PerformSearch.execute(query);
		}

	};


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
			mLocationManager.requestLocationUpdates(provider, TEN_MINUTES, TEN_METERS, geoListener);
			location = mLocationManager.getLastKnownLocation(provider);
		}
		return location;
	}

	private void updateUIwithLocation(Location location) {

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
			m.setDraggable(true);
		}
	}


	/**
	 * AsyncTask to perform the burden of Network stuff
	 * @author Yaxi Ye
	 *
	 */
	AsyncTask<String, Void, ArrayList<Result>> PerformSearch = new AsyncTask<String, Void, ArrayList<Result>>() {

		private static final String TAG = "PerformSearch";

		@Override
		protected ArrayList<Result> doInBackground(String... params) {
			ArrayList<Result> resultList = new ArrayList<Result>();
			resultList = readTwitterFeed(params[0]);
			return resultList;
		}

		@Override
		protected void onPostExecute(ArrayList<Result> list) {
			super.onPostExecute(list);
			// Put markers on the map
			DrawTweetMarkers.execute(list);
		}
	};

	private ArrayList<Result> readTwitterFeed(String searchString) {

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


	private ArrayList<Result> readResultsArray(JsonReader reader) throws IOException {
		ArrayList<Result> results = new ArrayList<Result>();

		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("results")){
				reader.beginArray();
				while (reader.hasNext()) {
					results.add(readResult(reader));
				}
				reader.endArray();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();

		return results;
	}


	private Result readResult(JsonReader reader) throws IOException {

		String from_user = null, 
				from_user_id_str = null, 
				from_user_name = null,
				text = null,
				location = null;
		Geo geo = null;
		URL profile_image_url = null;

		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("from_user")){

				from_user = reader.nextString();
				Log.i(TAG, from_user);
			} else if (name.equals("from_user_id_str")) {

				from_user_id_str = reader.nextString();
				Log.i(TAG, from_user_id_str);
			} else if (name.equals("from_user_name")) {

				from_user_name = reader.nextString();
				Log.i(TAG, from_user_name);
			} else if (name.equals("geo")) {

				if (reader.hasNext()) {
					JsonToken peek = reader.peek();
					if (peek == JsonToken.NULL)
					{
						reader.skipValue();
					}
					else
					{
						reader.beginObject();
						while(reader.hasNext()) {
							if (name.equals("coordinates")) {
								reader.beginArray();
								double latitude = 0;
								double longitude = 0;
								while (reader.hasNext()) {
									latitude = reader.nextDouble();
									longitude = reader.nextDouble();
									Log.i(TAG, latitude + ", " + longitude);
									geo = new Geo(latitude, longitude);
								}
								reader.endArray();
							} else {
								reader.skipValue();
							}
						}
						reader.endObject();
					}
				}
			} else if (name.equals("location")) {

				location = reader.nextString();
				Log.i(TAG, location);
			} else if (name.equals("profile_image_url")) {

				profile_image_url = new URL(reader.nextString());

			} else if (name.equals("text")) {

				text = reader.nextString();
				Log.i(TAG, text);
			} else {

				reader.skipValue();

			}

		}
		reader.endObject();

		return new Result(from_user, from_user_id_str, from_user_name, text, geo, location, profile_image_url);			
	}

	AsyncTask<ArrayList<Result>, Void, Void> DrawTweetMarkers = new AsyncTask<ArrayList<Result>, Void, Void>(){
		private ArrayList<Result> tweetResult;

		@Override
		protected Void doInBackground(ArrayList<Result>... params) {
			tweetResult = new ArrayList<Result>();
			tweetResult = params[0];

			for (int i = 0; i < tweetResult.size(); i++) {
				Result result = (Result) tweetResult.get(i);
				URL image_url = result.getProfile_image_url();
				try {
					downloadUrl(image_url, result.getFrom_user());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			// Clear Map first
			map.clear();
			map.animateCamera(CameraUpdateFactory.zoomTo(5), 2000, null);
			// put markers
			if (tweetResult != null && tweetResult.isEmpty() == false) {
				for (int i = 0; i < tweetResult.size(); i++) {
					Result r = (Result) tweetResult.get(i);

					final Bitmap bm = decodeSampledBitmapFromFile(r.getFrom_user(), 40, 40);

					if (r.getGeo() != null) {
						// Set up markers
						Marker marker = map.addMarker(new MarkerOptions()
						.position(r.getGeo().getLatLng())
						.title(r.getFrom_user())
						.snippet(r.getText()));
						map.setOnInfoWindowClickListener(infoClickListener);
						map.setInfoWindowAdapter(new InfoWindowAdapter() {

							@Override
							public View getInfoWindow(Marker marker) {

								return null;
							}
							// Set up Infor windows
							@Override
							public View getInfoContents(Marker marker) {
								// Inflate info windows
								View infoView = getLayoutInflater().inflate(R.layout.marker_info_window, null);
								TextView username = (TextView)infoView.findViewById(R.id.textView_username);
								//									TextView twitterId = (TextView)infoView.findViewById(R.id.textView_user);
								TextView tweets = (TextView)infoView.findViewById(R.id.textView_tweets);
								ImageView thumbnail = (ImageView)infoView.findViewById(R.id.imageView_profile_thumb);
								thumbnail.setImageBitmap(decodeSampledBitmapFromFile(marker.getTitle(), 40, 40));
								username.setText(marker.getTitle());
								tweets.setText(marker.getSnippet());
								return infoView;
							}
						});					
						marker.showInfoWindow();

					} else if (r.getLocation() != null){
						// If no geo=null, we use the location to put markers
						Geocoder geoCoder = new Geocoder(MainActivity.this, Locale.UK);
						List<Address> addressList;
						try {
							addressList = geoCoder.getFromLocationName(r.getLocation(), 1);
							if (addressList.size() != 0) {
								Address address = addressList.get(0);
								double longitude = address.getLongitude();
								double latitude = address.getLatitude();
								Log.i(TAG, "Get Location: " + longitude + ", " + latitude);

								// Set up markers
								Marker marker = map.addMarker(new MarkerOptions()
								.position(new LatLng(latitude, longitude))
								.title(r.getFrom_user())
								.snippet(r.getText()));
								map.setOnInfoWindowClickListener(infoClickListener);
								map.setInfoWindowAdapter(new InfoWindowAdapter() {
									@Override
									public View getInfoWindow(Marker marker) {
										return null;
									}
									@Override
									public View getInfoContents(Marker marker) {
										// Inflate info windows
										View infoView = getLayoutInflater().inflate(R.layout.marker_info_window, null);
										TextView username = (TextView)infoView.findViewById(R.id.textView_username);
										TextView tweets = (TextView)infoView.findViewById(R.id.textView_tweets);
										ImageView thumbnail = (ImageView)infoView.findViewById(R.id.imageView_profile_thumb);
										thumbnail.setImageBitmap(decodeSampledBitmapFromFile(marker.getTitle(), 40, 40));
										username.setText(marker.getTitle());
										tweets.setText(marker.getSnippet());
										return infoView;
									}
								});
								marker.showInfoWindow();
							}

						} catch (IOException e) {
							// 
							e.printStackTrace();
						}
					}
				}
			}
		}
	};

	// Handle the clicks on info window
	OnInfoWindowClickListener infoClickListener = new OnInfoWindowClickListener() {

		@Override
		public void onInfoWindowClick(Marker marker) {
			TweetDialog dialog = new TweetDialog();
			// Create a bundel to pass the info to tweets dialog
			// We'll decode the user's profile image later in tweets dialog
			Bundle args = new Bundle();
			args.putString("username", marker.getTitle());
			args.putString("tweets", marker.getSnippet());
			dialog.setArguments(args);
			dialog.show(getSupportFragmentManager(), "");
		}
	};

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
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(15000 /* milliseconds */);
		conn.setConnectTimeout(20000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		// Starts the query
		conn.connect();
		InputStream stream = conn.getInputStream();
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




	private void doOauth() {
		if (isTwitterLoggedInAlready() == false) {
			try { 
//				httpOauthConsumer = new CommonsHttpOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET); 
				// Avoid the DefaultOAuthProvider which works not well with the commons library
//				httpOauthprovider = new CommonsHttpOAuthProvider("https://api.twitter.com/oauth/request_token", 
//						"https://api.twitter.com/oauth/access_token", 
//						"https://api.twitter.com/oauth/authorize"); 
				String authUrl = httpOauthprovider.retrieveRequestToken(httpOauthConsumer, TWITTER_CALLBACK_URL); 
				
				// Persist the request token and request token secret
				SharedPreferences.Editor editor = pref.edit();
				editor.putString("request_token", httpOauthConsumer.getToken());
				editor.putString("request_token_secret", httpOauthConsumer.getTokenSecret());
				editor.commit();
				
				this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))); 
			} catch (Exception e) {

				e.printStackTrace();
			}
		} else {
			Log.d(TAG, "Twitter already authorised.");
			return;
		}
	}

	/**
	 * Check user already logged in your application using twitter Login flag is
	 * fetched from Shared Preferences
	 * */
	private boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return pref.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}

}
