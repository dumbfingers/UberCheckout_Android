//package com.yeyaxi.android.ubercheckout.utilities;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.util.Log;
//import android.widget.Toast;
//import twitter4j.Twitter;
//import twitter4j.TwitterException;
//import twitter4j.TwitterFactory;
//import twitter4j.auth.AccessToken;
//import twitter4j.auth.RequestToken;
//import twitter4j.conf.Configuration;
//import twitter4j.conf.ConfigurationBuilder;
//
//public class TwitterOAuth {
//
//	static String TWITTER_CONSUMER_KEY = "mcz7i4BDru2TE1LoTWbQ";
//	static String TWITTER_CONSUMER_SECRET = "xl0HHTUToe5860H4SikYcmi2bL38gqdUmUAAlJ7SeU8";
//
//	// Preference Constants
//	static String PREFERENCE_NAME = "twitter_oauth";
//	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
//	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
//	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
//
//	static final String TWITTER_CALLBACK_URL = "oauth://ubercheckout";
//
//	// Twitter oauth urls
//	static final String URL_TWITTER_AUTH = "auth_url";
//	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
//	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
//
//	// Twitter
//	public static Twitter twitter;
//	private static RequestToken requestToken;
////	private static AccessToken accessToken;
//	
//	private Context context;
//	private SharedPreferences pref;
//
//	
//	public TwitterOAuth (Context c, Uri uri) {
//		this.context = c;
//		pref = context.getSharedPreferences("pref", context.MODE_PRIVATE);
//		
//		if (!isTwitterLoggedInAlready()) {
////			Uri uri = getIntent().getData();
//			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
//				// oAuth verifier
//				String verifier = uri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
//
//				try {
//					// Get the access token
//					AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
//
//					SharedPreferences.Editor e = pref.edit();
//
//					// After getting access token, access token secret
//					// store them in application preferences
//					e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
//					e.putString(PREF_KEY_OAUTH_SECRET,
//							accessToken.getTokenSecret());
//
//					// Store login status - true
//					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
//					e.commit(); // save changes
//			
//					Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
//				} catch (Exception exception) {
//				
//				}
//			}
//		}
//		
//		if (isNetworkConnected() == true) {
//			// Execute the twitter OAuth
//			oAuthTask.execute();
//		} else {
//			// Network not available, check connectivity
//			Toast.makeText(context, 
//					"Network unavailable, please check your Internet connection.", Toast.LENGTH_LONG).show();
//			
//		}
//
//	}
//	
//	/**
//	 * Function to login twitter
//	 * */
//	private void loginToTwitter() {
//		// Check if already logged in
//		if (!isTwitterLoggedInAlready()) {
//			ConfigurationBuilder builder = new ConfigurationBuilder();
//			builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
//			builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
//			Configuration configuration = builder.build();
//
//			TwitterFactory factory = new TwitterFactory(configuration);
//			twitter = factory.getInstance();
//
//			try {
//				requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
//				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
////				accessToken = twitter.getOAuthAccessToken();
//				Log.d("TAG", requestToken.getToken());
//			} catch (TwitterException e) {
//				e.printStackTrace();
//			}
//		} else {
//			// user already logged into twitter
//			Toast.makeText(context, "Already Logged into twitter", Toast.LENGTH_LONG).show();
//			return;
//		}
//	}
//	
//	/**
//	 * Check user already logged in your application using twitter Login flag is
//	 * fetched from Shared Preferences
//	 * */
//	private boolean isTwitterLoggedInAlready() {
//		// return twitter login status from Shared Preferences
//		return pref.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
//	}
//
//	/**
//	 * Check network connectivity.
//	 * @return true if the current network is connected.
//	 */
//	private boolean isNetworkConnected() {
//		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
//		NetworkInfo ni = cm.getActiveNetworkInfo();
//		if (ni != null && ni.isConnected()) {
//			return true;
//		} else {
//			return false;
//		}
//		
//	}
//	
//	AsyncTask<Void, Void, Void> oAuthTask = new AsyncTask<Void, Void, Void>() {
//
//		@Override
//		protected Void doInBackground(Void... params) {
//			loginToTwitter();
//			return null;
//		}
//	};
//}
