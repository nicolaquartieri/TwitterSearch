package com.bootcamp.globant.model;

public class TweetElement {
	private String textoFrom;
	private String textoTweet;
	private String imagen;
	
	public TweetElement() {
		super();
	}

	public TweetElement(String textoFrom, String textoTweet, String imagen) {
		super();
		this.textoFrom = textoFrom;
		this.textoTweet = textoTweet;
		this.imagen = imagen;
	}



	public String getTextoFrom() {
		return textoFrom;
	}

	public void setTextoFrom(String textoFrom) {
		this.textoFrom = textoFrom;
	}

	public String getTextoTweet() {
		return textoTweet;
	}

	public void setTextoTweet(String textoTweet) {
		this.textoTweet = textoTweet;
	}

	public String getImagen() {
		return imagen;
	}

	public void setImagen(String imagen) {
		this.imagen = imagen;
	}

	
	
}
