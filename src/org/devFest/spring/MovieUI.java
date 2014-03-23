package org.devFest.spring;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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

import android.content.ContentResolver;
import android.database.Cursor;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RemoteControlClient.OnGetPlaybackPositionListener;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.app.Activity;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

public class MovieUI extends Activity {

	GestureDetector gDetector;
	ImageView gImage;
	private List<Movie> movies;
	private int currentMovie=-1;
	private static final String TAG="MovieUI";
	private static final int MOVIES_BUFFER_LENGTH=10;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie_ui);
		
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
		movies = new ArrayList<Movie>();
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
		showData();
		showMovie();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.movie_ui, menu);
		return true;
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
		if (currentMovie == -1) return;
		if (currentMovie == movies.size())
			currentMovie=0;
		
		String imagePath = movies.get(currentMovie).url;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
		Bitmap bImage = BitmapFactory.decodeFile(imagePath, options);
		gImage.setImageBitmap(bImage);
		
		// NEXT MOVIE TO SHOW
		// currentMovie++;
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
			movie.setImdbId(cursor.getInt(indexID));
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
		DatabaseHelper dbHelper = new DatabaseHelper(this);
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
				} else {
					Log.v("down swipe", "dX: " + dX + ", dY: " + dY);
					Toast.makeText(getApplicationContext(), "Down Swipe", Toast.LENGTH_SHORT).show();
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
            return true;
        }
        
	}
	
}
