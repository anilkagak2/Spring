package org.devFest.spring;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.webkit.WebView;
import android.widget.TextView;

public class ShowWatched extends Activity {
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
	}
}
