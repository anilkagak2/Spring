package org.devFest.spring;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

public class AboutMovie extends Activity {
	private AdView adView;
	private static final String AD_UNIT_ID = "a153397577f3c4a";
	private static final String DEVICE_HASH_ID="INSERT_YOUR_HASHED_DEVICE_ID_HERE";
	
	private static final String keyToLook="AboutMovie";
	private Movie movie;// = new Movie("Her", "9", "http://nourl.com");

	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_about_movie);

	    if (savedInstanceState == null) {
	        Bundle extras = getIntent().getExtras();
	        if(extras == null) {
	            movie = null;
	        } else {
	            movie = (Movie) extras.getParcelable(keyToLook);
	        }
	    } else {
	    	movie = (Movie) savedInstanceState.getParcelable(keyToLook);
	    }
	    
	    TextView aboutMovie = (TextView) findViewById(R.id.aboutMovie);

	    Spanned aboutText = Html.fromHtml(
	    		"<h2> Plot Summary </h2>" + movie.getPlotSummary());
	    aboutMovie.setText(aboutText);
	    
	    // Look up the AdView as a resource and load a request.
	    AdView adView = (AdView)this.findViewById(R.id.adViewAboutMovie);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    adView.loadAd(adRequest);
	}
}
