package com.yeyaxi.android.ubercheckout;

import com.google.android.gms.maps.model.LatLng;

/**
 * A Simple Class to hold the Geo location
 * @author Yaxi Ye
 *
 */
public class Geo {
	private LatLng latLng;
	
	public Geo(double latitude, double longitude) {
		latLng = new LatLng(latitude, longitude);
	}
	
	public LatLng getLatLng() {
		return latLng;
	}
	
}
