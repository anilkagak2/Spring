package org.devFest.spring;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.internal.ht;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RemoteControlClient.OnGetPlaybackPositionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract.Directory;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.R.string;
import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MovieUI extends Activity {

	// DONE remove some movies of the deck after some point
	private AdView adView;
	private static final String AD_UNIT_ID = "a153397577f3c4a";
	private static final String DEVICE_HASH_ID="INSERT_YOUR_HASHED_DEVICE_ID_HERE";

	GestureDetector gDetector;
	ImageView gImage;
	TextView ratingView,titleView;
	private Context context;
	private DatabaseHelper dbHelper;
	private List<Movie> movies;
	private List<MovieIdSeen> movieIds;
	private int movieIdRange;
	private int currentMovie=-1;
	private long moviesShown=0;
	private boolean downloadGoing=false;		// TODO Take care of the race condition
	private File picturesDir;
	private static final String TAG="MovieUI";
	private static final int MOVIES_BUFFER_LENGTH=5;
	private static final int MOVIES_DECK_SIZE=15;
	private static final String POSTER_NA = "N/A";
	private static final String MOVIE_BASE_URL = "http://www.omdbapi.com/?i=";
	private static final String MOVIE_IDS_ARRAY_FILE = "movieIdsArray.txt";
	
	private static final String JSON_POSTER 	="Poster";
	private static final String JSON_TITLE		="Title";
	private static final String JSON_ID			="imdbID";
	private static final String JSON_RATING		="imdbRating";
	private static final String JSON_PLOT		="Plot";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_movie_ui);
		setContentView(R.layout.activity_movie_ui_new);
		
		context = this;
		dbHelper = new DatabaseHelper(this);
		
		gDetector = new GestureDetector(this, new MyGestureDetector());
		View mainView = (View) findViewById(R.id.mainView);
		mainView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (gDetector.onTouchEvent(event))
					return true;
				return false;
			}
		});
		
		gImage = (ImageView) findViewById(R.id.imgDisplay);
		/*gImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MovieUI.this, AboutMovie.class);
				intent.putExtra("AboutMovie", movies.get(currentMovie));
				startActivity(intent);
			}
		});*/
		
		movies = new ArrayList<Movie>();
		
		ratingView = (TextView) findViewById(R.id.ImdbRatingValue);
		titleView = (TextView) findViewById(R.id.NameValue);
				
		picturesDir = getAlbumStorageDir(this, "SpringMovies");
		//Toast.makeText(this, "Dir : SpringMovies created", Toast.LENGTH_SHORT).show();
		
		// Read Movie Ids & we'll start showing movies from the list one-by-one
		// TODO remove movies [pictures might take a lot of space]
		// TODO save this variable on disk & read from there after first invocation
		movieIds = new ArrayList<MovieIdSeen>();
		readMovieIds();
		
		// TODO DEBUG
		// Toast.makeText(this, "Movie Ids: " + movieIds.size(), Toast.LENGTH_SHORT).show();
		
		// Look up the AdView as a resource and load a request.
	    AdView adView = (AdView)this.findViewById(R.id.adViewMainUI);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    adView.loadAd(adRequest);
		
		/*// Create an ad.
	    adView = new AdView(this);
	    adView.setAdSize(AdSize.BANNER);
	    adView.setAdUnitId(AD_UNIT_ID);
		
	    // LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);
	    RelativeLayout layout = (RelativeLayout) mainView;
	    layout.addView(adView);
	    
	    // Create an ad request. Check logcat output for the hashed device ID to
	    // get test ads on a physical device.
	    AdRequest adRequest = new AdRequest.Builder()
	        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
	        .addTestDevice(DEVICE_HASH_ID)
	        .build();
	    
	    // Start loading the ad in the background.
	    adView.loadAd(adRequest);*/
	    
		/*List<String> moviesToFetch = getNewMovieIdsToLoad();
		for (String movie : moviesToFetch) {
			Log.v("toFetch", movie);
		}*/
		
/*		int loader = R.drawable.loader;
		
		String image_url = "http://api.androidhive.info/images/sample.jpg";
		
		// ImageLoader class instance
        ImageLoader imgLoader = new ImageLoader(getApplicationContext());
         
        // whenever you want to load an image from url
        // call DisplayImage function
        // url - image url to load
        // loader - loader image, will be displayed before getting image
        // image - ImageView 
        imgLoader.DisplayImage(image_url, loader, gImage);*/
		
		// getRecommendedMovies();
		// createDummyDatabase();
		/*new DownloadMoviesTask()
			.execute(moviesToFetch.toArray(new String[moviesToFetch.size()]));*/
			//.execute(new String[] {"http://harshversion1.appspot.com/get_movie/tt2308606"});
		downloadNewMovies();
		
		// showData();
		showMovie();
	}	
	
	/*@Override
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

	@Override
	public void onDestroy() {
		// Destroy the AdView.
	    if (adView != null) {
	    	adView.destroy();
	    }
	    super.onDestroy();
	}*/
	
	@Override
	protected void onStop() {
	    super.onStop();  // Always call the superclass method first

	    // Save the movie Ids array to stable store
	    // TODO resolve this bug
	    // saveMovieIdsArray();
	}
	
	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	private void downloadNewMovies () {
		if (downloadGoing) return;
		
		downloadGoing=true;
		List<String> moviesToFetch = getNewMovieIdsToLoad();
		for (String movie : moviesToFetch) {
			Log.v("toFetch", movie);
		}
		
		new DownloadMoviesTask()
		.execute(moviesToFetch.toArray(new String[moviesToFetch.size()]));
		
	}
	
	// Removes movies from the deck[ min (count, size/2) ] from the beginning
	private void removeMoviesOfDeck (int count) {
		int toDelete = Math.min(count, movies.size()/2);
		for (int i=0; i<toDelete; ++i) {
			// TODO should remove only the shown movies
			if (movies.get(i).shown) moviesShown--;
		}
		
		movies.subList(0, toDelete).clear();
		currentMovie=0;
	}
	
	public File getAlbumStorageDir(Context context, String albumName) {
	    // Get the directory for the app's private pictures directory. 
	    File file = new File(context.getExternalFilesDir(
	            Environment.DIRECTORY_PICTURES), albumName);
	    if (!file.mkdirs()) {
	        Log.e(TAG, "Directory not created");
	    }
	    return file;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.movie_ui, menu);
		return true;
	}
	
	private void showMovies (boolean liked) {
		Log.v("Show Movies", "--");
    	
    	ArrayList<Movie> watchedMovies = (ArrayList<Movie>) dbHelper.getAllWatchedMovies(liked);
    	if (watchedMovies == null) {
    		Log.v("watched movies", "is null");
    		return;
    	}
    	
    	String showNow="";
    	if (liked) {
    		showNow="Movies you liked";
    	} else {
    		showNow = "Movies you disliked";
    	}
    	
    	Intent intent = new Intent(MovieUI.this, ShowWatched.class);
    	intent.putParcelableArrayListExtra("Movies", watchedMovies);
    	intent.putExtra("showNow", showNow);
    	Log.v("showMovies", "starting the activity");
		startActivity(intent);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_showliked:
	            showMovies(true);
	            return true;
	        case R.id.action_showdisliked:
	        	showMovies(false); 
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void showNext() {
		if (currentMovie == -1) return;
		currentMovie++;
		
		if (currentMovie == movies.size())
			currentMovie=0;
		showMovie();
	}
	
	private void showPrev() {
		if (currentMovie == -1) return;
		currentMovie--;
		
		if (currentMovie == -1)
			currentMovie=movies.size()-1;
		showMovie();
	}
	
	private void showMovie() {
		// TODO Show some error or warning stuff
		if (movies.size() == 0) return;
		
		if (currentMovie == -1) return;
		if (currentMovie == movies.size())
			currentMovie=0;
		
		// Increment the number of movies user has seen
		if (movies.get(currentMovie).shown == false) {
			movies.get(currentMovie).shown = true;
			moviesShown++;
		}
		
		// Remove some movies from the deck
		if (movies.size() >= MOVIES_DECK_SIZE) {
			
			removeMoviesOfDeck(MOVIES_DECK_SIZE/2);
		}
		
		// If remaining movies are less than half of movies buffer => get new movies
		if (movies.size() - moviesShown <= MOVIES_BUFFER_LENGTH/2) {
			downloadNewMovies();
		}
		
		//String imagePath = movies.get(currentMovie).url;
		// TODO check for the existence
		// Ideally we should not load from the imagePath instead we can 
		// maintain the bitmaps we've downloaded
		titleView.setText(movies.get(currentMovie).name);
		ratingView.setText(movies.get(currentMovie).rating);
		
		String imagePath = picturesDir.getAbsolutePath() + File.separator
				+ movies.get(currentMovie).imdbId + ".jpg";
		Log.v("SHowing movie", imagePath);
		
		//File imageFile = getBaseContext().getFileStreamPath(imagePath);
		File imageFile = new File(imagePath);
		if (!imageFile.exists()) {
			gImage.setImageResource(R.drawable.not_available);
			return;	// No image to display
		} else {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;
			Bitmap bImage = BitmapFactory.decodeFile(imagePath, options);
			gImage.setImageBitmap(bImage);
		}
		
		// NEXT MOVIE TO SHOW
		// currentMovie++;
	}
	
	private void saveWatchedMovie(boolean liked) {
		if (currentMovie == -1) {
			Log.v("save watchedMovie", "No movie on the deck");
			return;
		}
		
		// Add the movie to watched movies & remove it from this list
		dbHelper.addWatchedMovie(movies.get(currentMovie), liked);
		movies.remove(currentMovie);
		showMovie();
	}
	

	// Saves the movie ids array mapping to the sd card
	private void saveMovieIdsArray() {
		/*if (isExternalStorageWritable()) {
			Log.v("StorageNotWriteable", "saveMovieIdsArray storage not readable");
			Toast.makeText(this, "SDCard not available for writing", Toast.LENGTH_LONG).show();
			
			// TODO check if this is what we wanted
			Log.v("CloseApp", "finish called at saveMovieIdsArray");
			finish();
		}
		*/
		Log.v("SavingMovieIdsArray", "--");
		try {
			File file = new File(picturesDir + File.separator + MOVIE_IDS_ARRAY_FILE);
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file));
			for (MovieIdSeen mix : movieIds) {
				out.write(mix.movieId);
				out.write('-');
				
				if (mix.seen) out.write('Y');
				else out.write('N');
				out.write('\n');
			}
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Reads the movie ids array mapping from the sd card
	private void readMovieIdsArray() {
		if(movieIds.size() > 0) {
			Log.v("movieIds", "size not 0");
			movieIds.clear();
		}
		
		try {
			File file = new File(picturesDir + File.separator + MOVIE_IDS_ARRAY_FILE);
			InputStreamReader instream = new InputStreamReader(new FileInputStream(file));
			BufferedReader reader = new BufferedReader(instream);
			String mixStr = reader.readLine();
			
			while (mixStr != null) {
				MovieIdSeen mix = new MovieIdSeen("", false);
				mix.movieId =  mixStr.substring(0, mixStr.indexOf('-'));
				String seen = mixStr.substring(mixStr.indexOf('-')+1, mixStr.length());
				
				if (seen == "N") mix.seen=false;
				else mix.seen=true;
				movieIds.add(mix);
			}
			
			reader.close();
			movieIdRange = movieIds.size();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readMovieIds () {
		/*if (isExternalStorageReadable()) {
			Log.v("StorageNotReadable", "readMovieIds storage not readable");
			Toast.makeText(this, "SDCard not available for reading", Toast.LENGTH_LONG).show();
			
			// TODO check if this is what we wanted
			Log.v("CloseApp", "finish called at readMovieIds");
			finish();
		}*/
		
		/*File file = new File(picturesDir + File.separator + MOVIE_IDS_ARRAY_FILE);
		if (file.exists()) {
			Log.v("movieIds", "reading the movie ids from sdcard");
			readMovieIdsArray();
		}
		else {*/
			Log.v("movieIds", "reading the movie ids from raw resource");
			InputStream input = getResources().openRawResource(R.raw.movieids);
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String id=null;
			try {
				id = reader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			while (id != null) {
				movieIds.add(new MovieIdSeen(id, false));
				try {
					id = reader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			movieIdRange = movieIds.size();
		//}
	}
	
	private List<String> getNewMovieIdsToLoad () {
		List<String> moviesToFetch = new ArrayList<String>();
		
		// TODO test this function's logic [Shouldn't loop forever]
		if (movieIdRange > MOVIES_BUFFER_LENGTH) {
			Random random = new Random();
			int idsGenerated=0;
			while (idsGenerated != MOVIES_BUFFER_LENGTH) {
				int randomNum = random.nextInt(movieIdRange);
				if (movieIds.get(randomNum).seen == false) {
					movieIds.get(randomNum).seen = true;
					
					// add to the movieList to download from the web site
					String movieUrl = MOVIE_BASE_URL + movieIds.get(randomNum).movieId;
					Log.v("MovieUrl -> ", movieUrl);
					moviesToFetch.add(movieUrl);
					
					// Movie this unseen movie to first seen movie 
					MovieIdSeen tmp = movieIds.get(movieIdRange-1);
					movieIds.set(movieIdRange-1, movieIds.get(randomNum));
					movieIds.set(randomNum, tmp);
					
					movieIdRange--;
					idsGenerated++;
				}
			}
		}
		
		return moviesToFetch;
	}
	
	private void showData() {
		String[] columns = new String[] {
		                ImageColumns._ID,
		                ImageColumns.TITLE,
		                ImageColumns.DATA };
		
		Cursor cursor = this.getContentResolver()
				.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		                columns,
		                null,
		                null,
		                null);
		
		if (cursor == null) {
			Log.v("Cursor: ","NULL");
			return;
		}
		 
		if (cursor.getCount() < 0) {
			Log.v("Cursor: ", "NO match");
			return;
		}
		
		Log.v("Count : ", ""+cursor.getCount());
		Log.v("Movie name:", ""+cursor.getColumnIndex(ImageColumns._ID));
		Log.v("Movie imdbid:", "" + cursor.getColumnIndex(ImageColumns.TITLE));
		
		int indexID = cursor.getColumnIndex(ImageColumns._ID);
		int indexTitle = cursor.getColumnIndex(ImageColumns.TITLE);
		int indexData = cursor.getColumnIndex(ImageColumns.DATA);

		for (int i=0; i<Math.min(cursor.getCount(), MOVIES_BUFFER_LENGTH); ++i) {
			cursor.moveToPosition(i);
			Movie movie = new Movie();
			movie.setImdbId(cursor.getString(indexID));
			movie.setName(cursor.getString(indexTitle));
			movie.setUrl(cursor.getString(indexData));
			Log.v("Movie name:", movie.name);
			Log.v("Movie imdbid:", "" + movie.imdbId);
			Log.v("Movie data: ", movie.getUrl());
			movies.add(movie);
		}
		
		if(movies.size()>0) currentMovie=0;
	}
	
	private void createDummyDatabase () {
		//DatabaseHelper dbHelper = new DatabaseHelper(this);
		Log.v(TAG, ": Inserting into the DB");
//		dbHelper.addMovie(new Movie(10,"Her", "http://www.imdb.com/her.jpg"));
//		dbHelper.addMovie(new Movie(11,"Iron Man", "http://www.imdb.com/ironMan.jpg"));
//		dbHelper.addMovie(new Movie(12,"Hercules", "http://www.imdb.com/hercules.jpg"));
		
		Toast.makeText(this, "inserted into the DB", Toast.LENGTH_SHORT).show();
		
		Log.v(TAG, ": Reading all the movies");
		List<Movie> movies = dbHelper.getAllMovies();
		
		Toast.makeText(this, "count = " + movies.size(), Toast.LENGTH_SHORT).show();
		for(Movie movie: movies) {
			String log = "Id: " + movie.getId() + ", IMDBId: " + movie.getImdbId()
					+ ", Name: " + movie.getName() + ", URL: " + movie.getUrl();
			Log.d(TAG+": movieDesc=> ", log);
		}
	}
	
	private void getRecommendedMovies () {
		// Creating HTTP client
		HttpClient httpClient = new DefaultHttpClient();
		
		// Creating HTTP Post
		// HttpPost httpPost = new HttpPost("http://192.168.0.109:8014/uuid=12");
		HttpGet httpGet = new HttpGet("http://192.168.0.109:8014/uuid=12");
		
		// Building post parameters, key and value pair
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
		nameValuePair.add(new BasicNameValuePair("email", "user@gmail.com"));
		nameValuePair.add(new BasicNameValuePair("message", "Stuck at Login Screen"));
		
		// Url Encoding the POST parameters
/*		try {
			Toast.makeText(this, "Encoding the post request.", Toast.LENGTH_LONG).show();
		    //httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
		}
		catch (UnsupportedEncodingException e) {
		    // writing error to Log
		    e.printStackTrace();
		}*/
		
		// Making HTTP Request
		try {
			Toast.makeText(this, "Executing the post request.", Toast.LENGTH_LONG).show();
		    //HttpResponse response = httpClient.execute(httpPost);
		    HttpResponse response = httpClient.execute(httpGet);
		    HttpEntity httpEntity = response.getEntity();
		 
		    Toast.makeText(this, "Got Response from the server" + response.toString(), Toast.LENGTH_LONG).show();
		    
		    // writing response to log
		    Log.d("Http Response:", response.toString());
		    Log.d("Http Response:", EntityUtils.toString(httpEntity));
		 
		} catch (ClientProtocolException e) {
		    // writing exception to log
		    e.printStackTrace();
		         
		} catch (IOException e) {
		    // writing exception to log
		    e.printStackTrace();
		}
		
	}
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;
	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

			float dX = e2.getX()-e1.getX();
			float dY = e1.getY()-e2.getY();

			Log.v("distances::", "dX: " + dX + ", dY: " + dY);
			if (Math.abs(dY)<SWIPE_MAX_OFF_PATH &&
					Math.abs(velocityX)>=SWIPE_THRESHOLD_VELOCITY &&
					Math.abs(dX)>=SWIPE_MIN_DISTANCE ) {
				if (dX>0) {
					Log.v("right swipe", "dX: " + dX + ", dY: " + dY);
					Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();
					
					showPrev();
				} else {
					Log.v("left swipe", "dX: " + dX + ", dY: " + dY);
					Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
					
					showNext();
				}

				return true;
			} else if (Math.abs(dX)<SWIPE_MAX_OFF_PATH &&
						Math.abs(velocityY)>=SWIPE_THRESHOLD_VELOCITY &&
						Math.abs(dY)>=SWIPE_MIN_DISTANCE ) {
				if (dY>0) {
					Log.v("up swipe", "dX: " + dX + ", dY: " + dY);
					Toast.makeText(getApplicationContext(), "Up Swipe", Toast.LENGTH_SHORT).show();
					
					saveWatchedMovie(true);
					// Remove the currentMovie from movies & put in watchedMovies with liked=true
					// Show other movie in all of these cases
				} else {
					Log.v("down swipe", "dX: " + dX + ", dY: " + dY);
					Toast.makeText(getApplicationContext(), "Down Swipe", Toast.LENGTH_SHORT).show();
					
					saveWatchedMovie(false);
					// Remove the currentMovie from movies & put in watchedMovies with liked=false
				}

				return true;
			}
			return false;
        }

        // It is necessary to return true from onDown for the onFling event to register
        @Override
        public boolean onDown(MotionEvent e) {
                return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        	Log.v("Long Press", "--");
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                float distanceY) {
        	Log.v("Scroll Button", "--");
            return false;
        }
        @Override
        public void onShowPress(MotionEvent e) {
        	Log.v("Show Press", "--");
        }
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
        	Log.v("Single Tap up", "--");
        	Intent intent = new Intent(MovieUI.this, AboutMovie.class);
			intent.putExtra("AboutMovie", movies.get(currentMovie));
			startActivity(intent);
        	
            return true;
        }
        
	}
	
	private class DownloadMoviesTask extends AsyncTask<String, Void, List<Movie>> {
		private ProgressDialog progressDialog = null; 
		private boolean showOnPostExecution=false;
		
		@Override
		protected void onPreExecute(){ 
			   super.onPreExecute();
			   if (movies.size() == 0) { 
				   showOnPostExecution=true;
			       progressDialog = new ProgressDialog(context);
			       progressDialog.setMessage("Downloading the movie recommendations for you..");
			       progressDialog.show();   
			   }
		}
		
		@Override
		protected List<Movie> doInBackground(String... urls) {
			List<Movie> downloadedPictures = new ArrayList<Movie>();
			
			// http://harshversion1.appspot.com/get_movie/tt2308606
			/*int count=0;*/
			for (String url : urls) {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				
				// Making HTTP Request
				try {
				    HttpResponse response = httpClient.execute(httpGet);
				    HttpEntity httpEntity = response.getEntity();
				    try {
				    	BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent(), "UTF-8"));
				    	String json = reader.readLine();
				    	Log.v("json string ", json);
						JSONObject moviesArray = new JSONObject(json);
						
						// TODO change it to ENUM or something similar
						String moviePicUrl = moviesArray.getString(JSON_POSTER);
						String movieId = moviesArray.getString(JSON_ID);
						String movieName = moviesArray.getString(JSON_TITLE);
						String movieRating = moviesArray.getString(JSON_RATING);
						String moviePlot = moviesArray.getString(JSON_PLOT);
						
						/*if (showOnPostExecution) {
							count++;
							progressDialog.setMessage("Movie downloaded: " + movieName
									+ "\n Movies left to download: " + (urls.length - count));
						}*/
						
						Log.v("Characters poster", moviePicUrl.length() +"");
						Log.v("Characters N/A", POSTER_NA.length() + "");
						
						if (moviePicUrl.equals(POSTER_NA)) {
							Log.v("No Poster for movie", movieId);
						} else {
							Log.v("Poster for movie ", movieId + " -> " + moviePicUrl);
							downloadAndSaveImage (moviePicUrl, movieId);
						}
						
						downloadedPictures.add(new Movie(movieId, movieName, moviePicUrl, movieRating, moviePlot));
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				} catch (ClientProtocolException e) {
				    // writing exception to log
				    e.printStackTrace();
				         
				} catch (IOException e) {
				    // writing exception to log
				    e.printStackTrace();
				}
				
				if (isCancelled()) {
					// TODO do book keeping here [Early exit]
					break;
				}
			}
			
			return downloadedPictures;
		}

		@Override
		protected void onPostExecute(List<Movie> moviesDownloaded) {
			super.onPostExecute(moviesDownloaded);
			if (progressDialog != null)
				progressDialog.dismiss();
			
			Toast.makeText(context, "Movies downloaded: " + moviesDownloaded.size(), Toast.LENGTH_LONG).show();
			// TODO managing the list properly so that memory consuption is low
			// clear the current movies list
			// movies.clear();
			
			for (Movie movie : moviesDownloaded) {
				// TODO modify this to check if the movie is already present
				Log.v("onPostExecute", movie.toString());
				dbHelper.addMovie(movie);
				movies.add(movie);
			}
			
			if(movies.size()>0 && showOnPostExecution) {
				currentMovie=0;
				showMovie();
			}
			
			downloadGoing=false;
			// You won't be in need of this
			// showOnPostExecution = false;
		}
		
		private String downloadAndSaveImage (String imageUrl, String ID) {
			String savedFileName="";
			
			// TODO some movies don't have the poster available
			
			try {
				InputStream is = (InputStream) new URL(imageUrl).getContent();
		        byte[] buffer = new byte[8192];
		        int bytesRead;
		        ByteArrayOutputStream output = new ByteArrayOutputStream();
		        while ((bytesRead = is.read(buffer)) != -1) {
		            output.write(buffer, 0, bytesRead);
		        }
		        
		        savedFileName = picturesDir.getAbsolutePath() + File.separator + ID + ".jpg";
		        Log.v("File saving name:", savedFileName);
		        File outputFile = new File(savedFileName);
		        FileOutputStream fos = new FileOutputStream(outputFile);
		        fos.write(output.toByteArray());
		        fos.close();
		    } catch (MalformedURLException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
			
			return savedFileName;
		}
		
	}

	private class MovieIdSeen {
		private String movieId;
		private boolean seen;
		
		public MovieIdSeen(String movieId, boolean seen) {
			this.movieId = movieId;
			this.seen = seen;
		}
		
		public String getMovieId() {
			return movieId;
		}
		public void setMovieId(String movieId) {
			this.movieId = movieId;
		}
		public boolean isSeen() {
			return seen;
		}
		public void setSeen(boolean seen) {
			this.seen = seen;
		}
	}
}
