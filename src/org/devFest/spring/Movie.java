package org.devFest.spring;

public class Movie {
	long id;
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	long imdbId;
	String name;
	String url;
	
	public long getImdbId() {return imdbId; }
	
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
	public void setImdbId(long imdbId) {
		this.imdbId = imdbId;
	}
	
	Movie(){}
	public Movie(long id, long imdbId, String name, String url) {
		this.id=id;
		this.imdbId=imdbId;
		this.name= name;
		this.url=url;
	}
	
	public Movie(long imdbId, String name, String url) {
		this.imdbId=imdbId;
		this.name= name;
		this.url=url;
	}
	
}
