package com.yeyaxi.android.ubercheckout;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;



public class MapViewFragment extends SherlockFragment implements 
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
LocationListener {

	private GoogleMap map;
	// Global constants
	/*
	 * Define a request code to send to Google Play services
	 * This code is returned in Activity.onActivityResult
	 */
	private final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	private LocationClient mLocationClient;

	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL =
			MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL =
			MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
	
	private LocationRequest mLocationRequest;
	
	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mEditor;

	private final String TAG = MapViewFragment.class.getSimpleName();
	
	private Context mContext;

	private boolean mUpdatesRequested;
	
	private Location mCurrentLocation;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_map, container, false);

		mContext = getSherlockActivity();
		
		//Set up map view
		map = ((SupportMapFragment) getSherlockActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true);
		mLocationClient = new LocationClient(getSherlockActivity(), this, this);

		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create();
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

		// Open the shared preferences
		mPrefs = mContext.getSharedPreferences("SharedPreferences", mContext.MODE_PRIVATE);
		// Get a SharedPreferences editor
		mEditor = mPrefs.edit();

		// Start with updates turned off
		mUpdatesRequested = false;
        
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();

		// Connect the client
		mLocationClient.connect();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		/*
		 * Get any previous setting for location updates
		 * Gets "false" if an error occurs
		 */
		if (mPrefs.contains("KEY_UPDATES_ON")) {
			mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);

			// Otherwise, turn off location updates
		} else {
			mEditor.putBoolean("KEY_UPDATES_ON", false);
			mEditor.commit();
		}

	}

	@Override
	public void onPause() {
		// Save the current setting for updates
		mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
		mEditor.commit();
		super.onPause();
	}
	
	@Override
	public void onStop() {
		
		mLocationClient.disconnect();
		super.onStop();
	}
	

	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getSherlockActivity());
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d(TAG, "Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			Log.e(TAG, "Google Play services is not available.");

			return false;
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// Display the connection status
		Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT).show();
		// If already requested, start periodic updates
		if (mUpdatesRequested) {
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
		}
		mCurrentLocation = mLocationClient.getLastLocation();
		
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 18));
		map.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(Location location) {
		if (mCurrentLocation == null)
			mCurrentLocation = location;
		if (location.distanceTo(mCurrentLocation) > 100)
			mCurrentLocation = location;
		
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18));
		map.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
	}
	
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
			dialog.show(getSherlockActivity().getSupportFragmentManager(), "");
		}
	};
}
