package com.yeyaxi.android.ubercheckout;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class TweetDialog extends SherlockDialogFragment{
	private String username = "";
	private String tweets = "";
	private TextView usernameTextView;
	private TextView tweetsTextView;
	private ImageView profileImageView;
	
	private static final int PROFILE_IMAGE_SIZE = 100;
	public TweetDialog() {
		Bundle bundle = getArguments();
		if (bundle.isEmpty() == false) {
			username = bundle.getString("username");
			tweets = bundle.getString("tweets");
		}
//		setArguments(getArguments());
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_tweet, null);
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		usernameTextView = (TextView) view.findViewById(R.id.textView_username);
		tweetsTextView = (TextView) view.findViewById(R.id.textView_tweets);
		profileImageView = (ImageView) view.findViewById(R.id.imageView_profile_thumb);
		
		usernameTextView.setText(username);
		tweetsTextView.setText(tweets);
		
		builder.setView(view);
		
		return builder.create();
	}
	
	/*
	 *try {
		Intent intent = new Intent(Intent.ACTION_VIEW,
    	Uri.parse("twitter://user?screen_name=[user_name]"));
		startActivity(intent);

		}catch (Exception e) {
    		startActivity(new Intent(Intent.ACTION_VIEW,
         	Uri.parse("https://twitter.com/#!/[user_name]"))); 
}  
	 */
	
	@Override
	public void onStart() {
		super.onStart();
		Bitmap bm = decodeSampledBitmapFromFile(username, PROFILE_IMAGE_SIZE, PROFILE_IMAGE_SIZE);
		profileImageView.setImageBitmap(bm);
	}
	
	private Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {
		Bitmap bm = null;
		File cachePath;
		if (getSherlockActivity().getExternalFilesDir(null) != null) {
			cachePath = getSherlockActivity().getExternalFilesDir(null); // Priorly use External Cache
		} else {
			cachePath = getSherlockActivity().getFilesDir();// Use Internal cache instead if external one is not available
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

	private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

}