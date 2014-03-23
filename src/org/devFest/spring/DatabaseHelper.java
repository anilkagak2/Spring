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
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "moviesManager";
 
    // Contacts table name
    private static final String TABLE_MOVIES = "movies";
 
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_IMDB = "imdbId";
    private static final String KEY_NAME = "name";
    private static final String KEY_URL = "url";

	 Context myContext;
	
	public DatabaseHelper(Context context) {
		super( context, DATABASE_NAME, null, DATABASE_VERSION );
	}
	
    public synchronized void close() {
        super.close();
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Creating Tables
	    String CREATE_MOVIES_TABLE = "CREATE TABLE " + TABLE_MOVIES + "("
	            + KEY_ID + " INTEGER PRIMARY KEY,"
	            + KEY_IMDB + " INTEGER,"
	    		+ KEY_NAME + " TEXT,"
	            + KEY_URL + " TEXT" + ")";
	    db.execSQL(CREATE_MOVIES_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
 
        // Create tables again
        onCreate(db);
	}

	public void addMovie (Movie movie) {
		SQLiteDatabase db = this.getWritableDatabase();
		 
	    ContentValues values = new ContentValues();
	    values.put(KEY_IMDB, movie.getImdbId()); 
	    values.put(KEY_NAME, movie.getName()); 
	    values.put(KEY_URL, movie.getUrl()); 	
	 
	    // Inserting Row
	    db.insert(TABLE_MOVIES, null, values);
	    db.close(); // Closing database connection
	}
	
	public Movie getMovie (int id) {
	    SQLiteDatabase db = this.getReadableDatabase();
	 
	    Cursor cursor = db.query(TABLE_MOVIES, new String[] { KEY_ID, KEY_IMDB,
	            KEY_NAME, KEY_URL}, KEY_ID + "=?",
	            new String[] { String.valueOf(id) }, null, null, null, null);
	    if (cursor != null)
	        cursor.moveToFirst();
	 
	    Movie movie = new Movie(Integer.parseInt(cursor.getString(0)), 
	    		Integer.parseInt(cursor.getString(1)),
	            cursor.getString(2), cursor.getString(3));
	    
	    return movie;
	}

	public List<Movie> getAllMovies() {
	    List<Movie> movieList = new ArrayList<Movie>();
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_MOVIES;
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            Movie movie = new Movie();
	            movie.setId(Integer.parseInt(cursor.getString(0)));
	            movie.setImdbId(Integer.parseInt(cursor.getString(1)));
	            movie.setName(cursor.getString(2));
	            movie.setUrl(cursor.getString(3));
	            
	            // Adding movie to list
	            movieList.add(movie);
	        } while (cursor.moveToNext());
	    }
	 
	    // return contact list
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
