package org.devFest.spring;

public class Movie {
	long id;
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	String imdbId;
	String name;
	String url;
	String rating;
	String plotSummary;
	
	public String getPlotSummary() {
		return plotSummary;
	}

	public void setPlotSummary(String plotSummary) {
		this.plotSummary = plotSummary;
	}

	public String getImdbId() {return imdbId; }
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void setImdbId(String imdbId) {
		this.imdbId = imdbId;
	}
	
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
	
	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String toString(){
		return "(" + name + "," + imdbId + "," + url + ")";
	}
}
