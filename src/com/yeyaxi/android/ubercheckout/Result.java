package com.yeyaxi.android.ubercheckout;

import java.net.URL;

import com.google.android.gms.maps.model.LatLng;

/**
 * A Class that holds the result of tweets search
 * @author Yaxi Ye
 *
 */
public class Result {

	private String from_user, 
					from_user_id_str, 
					from_user_name,
					text;
	private Geo geo;
	private URL profile_image_url;

	public Result(String from_user, String from_user_id_str, String from_user_name, 
					String text, Geo geo, URL profile_image_url) {
		this.from_user = from_user;
		this.from_user_id_str = from_user_id_str;
		this.from_user_name = from_user_name;
		this.text = text;
		this.geo = geo;
		this.profile_image_url = profile_image_url;
	}

	public String getFrom_user() {
		return from_user;
	}

	public String getFrom_user_id_str() {
		return from_user_id_str;
	}

	public String getFrom_user_name() {
		return from_user_name;
	}

	public String getText() {
		return text;
	}

	public LatLng getGeo() {
		return geo.getGeo();
	}

	public URL getProfile_image_url() {
		return profile_image_url;
	}

//	public void setFrom_user(String from_user) {
//		this.from_user = from_user;
//	}
//
//	public void setFrom_user_id_str(String from_user_id_str) {
//		this.from_user_id_str = from_user_id_str;
//	}
//
//	public void setFrom_user_name(String from_user_name) {
//		this.from_user_name = from_user_name;
//	}
//
//	public void setText(String text) {
//		this.text = text;
//	}
//
//	public void setGeo(Geo geo) {
//		this.geo = geo;
//	}
//
//	public void setProfile_image_url(URL profile_image_url) {
//		this.profile_image_url = profile_image_url;
//	}
}
