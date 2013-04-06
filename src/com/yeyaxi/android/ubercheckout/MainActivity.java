package com.yeyaxi.android.ubercheckout;




import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
	private RibbonMenuView rbmView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		/** Menu **/
		rbmView = (RibbonMenuView) findViewById(R.id.ribbonMenuView);
		// Hide the slide menu
//		rbmView.setVisibility(View.GONE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
}
