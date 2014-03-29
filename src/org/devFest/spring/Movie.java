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
	}
	
	public Movie(String imdbId, String name, String url) {
		this.imdbId=imdbId;
		this.name= name;
		this.url=url;
	}
	
	public String toString(){
		return "(" + name + "," + imdbId + "," + url + ")";
	}
}
