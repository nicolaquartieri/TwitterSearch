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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((imagen == null) ? 0 : imagen.hashCode());
		result = prime * result
				+ ((textoFrom == null) ? 0 : textoFrom.hashCode());
		result = prime * result
				+ ((textoTweet == null) ? 0 : textoTweet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TweetElement other = (TweetElement) obj;
		if (imagen == null) {
			if (other.imagen != null)
				return false;
		} else if (!imagen.equals(other.imagen))
			return false;
		if (textoFrom == null) {
			if (other.textoFrom != null)
				return false;
		} else if (!textoFrom.equals(other.textoFrom))
			return false;
		if (textoTweet == null) {
			if (other.textoTweet != null)
				return false;
		} else if (!textoTweet.equals(other.textoTweet))
			return false;
		return true;
	}	
}
