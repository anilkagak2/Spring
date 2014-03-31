package org.devFest.spring;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.webkit.WebView;
import android.widget.TextView;

public class ShowWatched extends Activity {
	private AdView adView;
	private static final String AD_UNIT_ID = "a153397577f3c4a";
	private static final String DEVICE_HASH_ID="INSERT_YOUR_HASHED_DEVICE_ID_HERE";
	
	private static final String keyToLook="Movies";
	private static final String showNow="showNow";
	private ArrayList<Movie> movies;
	private String nameXML;

	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_show_watched);

	    if (savedInstanceState == null) {
	        Bundle extras = getIntent().getExtras();
	        if(extras == null) {
	            movies = new ArrayList<Movie>();
	            nameXML="";
	        } else {
	            movies = extras.getParcelableArrayList(keyToLook);
	            nameXML = extras.getString(showNow);
	        }
	    } else {
	    	movies =  savedInstanceState.getParcelableArrayList(keyToLook);
	    	nameXML = savedInstanceState.getString(showNow);
	    }
	    
	    TextView aboutMovie = (TextView) findViewById(R.id.watchedMovies);
	    WebView webView = (WebView) findViewById(R.id.watchedContent);
	    
	    String moviesContent="<ul> ";
	    for (Movie movie : movies) {
	    	moviesContent += " <li> " + movie.name + " </li> ";
	    }
	    moviesContent += "</ul> ";

	    Spanned aboutText = Html.fromHtml(
	    		"<h2> " + nameXML + " </h2>" + moviesContent);
	    aboutMovie.setText(aboutText);
	    webView.loadDataWithBaseURL(null, moviesContent, "text/html", "utf-8", null);
	    
	    // Look up the AdView as a resource and load a request.
	    AdView adView = (AdView)this.findViewById(R.id.adViewShowWatched);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    adView.loadAd(adRequest);
	}


	@Override
	public void onResume() {
		super.onResume();
		if (adView != null) {
			adView.resume();
		}
	}

	@Override
	public void onPause() {
		if (adView != null) {
			adView.pause();
	    }
	    super.onPause();
	}

	/** Called before the activity is destroyed. */
	@Override
	public void onDestroy() {
		// Destroy the AdView.
	    if (adView != null) {
	    	adView.destroy();
	    }
	    super.onDestroy();
	}
}
