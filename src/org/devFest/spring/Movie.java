package org.devFest.spring;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
	long id;
	String name;
	String imdbId;
	String url;
	String rating;
	String plotSummary;
	
	public long getId() { return id; }
	public String getPlotSummary() { return plotSummary; }
	public String getImdbId() { return imdbId; }
	public String getName() { return name; }
	public String getUrl() { return url; }
	public String getRating() { return rating; }
	
	public void setId(long id) { this.id = id; }
	public void setPlotSummary(String plotSummary) { this.plotSummary = plotSummary; }
	public void setImdbId(String imdbId) { this.imdbId = imdbId;}
	public void setName(String name) { this.name = name; }
	public void setUrl(String url) { this.url = url; }
	public void setRating(String rating) { this.rating = rating; }
	
	Movie(){}
	public Movie(long id, String imdbId, String name, String url) {
		this.id=id;
		this.imdbId=imdbId;
		this.name= name;
		this.url=url;
		this.rating="N/A";
		this.plotSummary="N/A";
	}
	
	public Movie(String imdbId, String name, String url) {
		this.imdbId=imdbId;
		this.name= name;
		this.url=url;
		this.rating="N/A";
		this.plotSummary="N/A";
	}
	
	public Movie(String imdbId, String name, String url, String rating) {
		this.imdbId=imdbId;
		this.name= name;
		this.url=url;
		this.rating=rating;
	}
	
	public Movie(String imdbId, String name, String url, String rating, String plot) {
		this.imdbId=imdbId;
		this.name= name;
		this.url=url;
		this.rating=rating;
		this.plotSummary=plot;
	}
	
	public String toString(){
		return "(" + name + "," + imdbId + "," + url + ")";
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(imdbId);
		dest.writeString(url);
		dest.writeString(rating);
		dest.writeString(plotSummary);
	}
	
	public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
	
	private Movie(Parcel movie) {
		this.name = movie.readString();
		this.imdbId = movie.readString();
		this.url = movie.readString();
		this.rating = movie.readString();
		this.plotSummary = movie.readString();
	}
}
