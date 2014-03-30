package org.devFest.spring;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;
 
    // Database Name
    private static final String DATABASE_NAME = "moviesManager";
 
    // Movies table name
    private static final String TABLE_MOVIES = "movies";
    private static final String TABLE_WATCHED_MOVIES = "watched_movies";
 
    // Movies Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_IMDB = "imdbId";
    private static final String KEY_NAME = "name";
    private static final String KEY_URL = "url";
    private static final String KEY_PLOT = "plot";
    private static final String KEY_RATING = "rating";
    private static final String KEY_LIKED = "liked";
    
    private static final String LIKED_STR="Y";
    private static final String DISLIKED_STR="N";

    // Create Table queries
    private static final String CREATE_TABLE_MOVIES =
    		"CREATE TABLE " + TABLE_MOVIES + "("
            + KEY_ID 		+ " INTEGER PRIMARY KEY,"
            + KEY_IMDB 		+ " TEXT UNIQUE ON CONFLICT REPLACE,"
    		+ KEY_NAME 		+ " TEXT,"
            + KEY_URL 		+ " TEXT,"
            + KEY_PLOT 		+ " TEXT,"
            + KEY_RATING 	+ " TEXT" + ")";
    
    private static final String CREATE_TABLE_WATCHED_MOVIES =
    		"CREATE TABLE " + TABLE_WATCHED_MOVIES + "("
            + KEY_ID 		+ " INTEGER PRIMARY KEY,"
            + KEY_IMDB 		+ " TEXT UNIQUE ON CONFLICT REPLACE,"
    		+ KEY_NAME 		+ " TEXT,"
            + KEY_URL 		+ " TEXT,"
            + KEY_PLOT 		+ " TEXT,"
            + KEY_RATING 	+ " TEXT,"
    		+ KEY_LIKED 	+ " INTEGER" + ")";
    
	Context myContext;
	
	public DatabaseHelper(Context context) {
		super( context, DATABASE_NAME, null, DATABASE_VERSION );
	}
	
    public synchronized void close() {
        super.close();
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
	    db.execSQL(CREATE_TABLE_MOVIES);
	    db.execSQL(CREATE_TABLE_WATCHED_MOVIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATCHED_MOVIES);
 
        // Create tables again
        onCreate(db);
	}

	public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATCHED_MOVIES);
 
        // Create tables again
        onCreate(db);
	}
	
	private void addMovieAddOn (String tableName, ContentValues values) {
		SQLiteDatabase db = this.getWritableDatabase();
		
	    // Inserting Row
	    db.insert(tableName, null, values);
	    db.close(); // Closing database connection
	}
	
	public void addMovie (Movie movie) {
	    ContentValues values = new ContentValues();
	    values.put(KEY_IMDB, movie.getImdbId()); 
	    values.put(KEY_NAME, movie.getName()); 
	    values.put(KEY_URL, movie.getUrl());
	    values.put(KEY_PLOT, movie.getPlotSummary());
	    values.put(KEY_RATING, movie.getRating());
	 
	    // Inserting Row
	    addMovieAddOn(TABLE_MOVIES, values);
	}
	
	public void addWatchedMovie (Movie movie, boolean liked) {	 
	    ContentValues values = new ContentValues();
	    values.put(KEY_IMDB, movie.getImdbId()); 
	    values.put(KEY_NAME, movie.getName()); 
	    values.put(KEY_URL, movie.getUrl());
	    values.put(KEY_PLOT, movie.getPlotSummary());
	    values.put(KEY_RATING, movie.getRating());
	    
	    if (liked)  values.put(KEY_LIKED, LIKED_STR);
	    else 		values.put(KEY_LIKED, DISLIKED_STR);
	 
	    // Inserting Row
	    addMovieAddOn(TABLE_WATCHED_MOVIES, values);
	}
	
	public Movie getMovie (int id) {
	    SQLiteDatabase db = this.getReadableDatabase();
	 
	    Cursor cursor = db.query(TABLE_MOVIES, new String[] { KEY_ID, KEY_IMDB,
	            KEY_NAME, KEY_URL}, KEY_ID + "=?",
	            new String[] { String.valueOf(id) }, null, null, null, null);
	    if (cursor != null)
	        cursor.moveToFirst();
	 
	    Movie movie = new Movie(
	    		Integer.parseInt(cursor.getString(0)), 
	    		cursor.getString(1),
	            cursor.getString(2),
	            cursor.getString(3));
	    
	    return movie;
	}

	private List<Movie> getAllMovies(String selectQuery) {
	    List<Movie> movieList = new ArrayList<Movie>();
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            Movie movie = new Movie();
	            movie.setId(Integer.parseInt(cursor.getString(0)));
	            movie.setImdbId(cursor.getString(1));
	            movie.setName(cursor.getString(2));
	            movie.setUrl(cursor.getString(3));
	            
	            // Adding movie to list
	            movieList.add(movie);
	        } while (cursor.moveToNext());
	    }
	 
	    return movieList;
	}
	
	public List<Movie> getAllMovies() {
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_MOVIES;
	    return getAllMovies(selectQuery);
	}
	
	public List<Movie> getAllWatchedMovies(boolean liked) {
		String likedStr = "";
		if (liked) 	likedStr = LIKED_STR ;
		else 		likedStr = DISLIKED_STR;
		
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_WATCHED_MOVIES
	    		+ " WHERE " + KEY_LIKED + " = ? ";
	    
	    List<Movie> movieList = new ArrayList<Movie>();
		 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, new String[] { likedStr });
	 
	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            Movie movie = new Movie();
	            movie.setId(Integer.parseInt(cursor.getString(0)));
	            movie.setImdbId(cursor.getString(1));
	            movie.setName(cursor.getString(2));
	            movie.setUrl(cursor.getString(3));
	            
	            // Adding movie to list
	            movieList.add(movie);
	        } while (cursor.moveToNext());
	    }
	    
	    return movieList;
	}
	
	public int getMoviesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_MOVIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
 
        // return count
        return cursor.getCount();
    }

	public int updateMovie(Movie movie) {
	    SQLiteDatabase db = this.getWritableDatabase();
	 
	    ContentValues values = new ContentValues();
	    values.put(KEY_NAME, movie.getName());
	    values.put(KEY_IMDB, movie.getImdbId());
	    values.put(KEY_URL, movie.getUrl());
	    values.put(KEY_PLOT, movie.getPlotSummary());
	    values.put(KEY_RATING, movie.getRating());
	 
	    // updating row
	    return db.update(TABLE_MOVIES, values, KEY_ID + " = ?",
	            new String[] { String.valueOf(movie.getId()) });
	}

	public void deleteMovie(Movie movie) {
	    SQLiteDatabase db = this.getWritableDatabase();
	    db.delete(TABLE_MOVIES, KEY_ID + " = ?",
	            new String[] { String.valueOf(movie.getId()) });
	    db.close();
	}
}
