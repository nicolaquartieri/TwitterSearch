package com.bootcamp.globant.model;

public class WrapperItem {

	private TweetElement tweetElement;	
	
	public WrapperItem() {
		super();
	}

	public WrapperItem(TweetElement tweetElement) {
		super();
		this.tweetElement = tweetElement;
	}
	
	public WrapperItem(int barProgress) {
		super();
	}
	
	public WrapperItem(TweetElement tweetElement, int barProgress) {
		super();
		this.tweetElement = tweetElement;
	}

	public TweetElement getTweetElemento() {
		return tweetElement;
	}

	public void setTweetElemento(TweetElement tweetElement) {
		this.tweetElement = tweetElement;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((tweetElement == null) ? 0 : tweetElement.hashCode());
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
		WrapperItem other = (WrapperItem) obj;
		if (tweetElement == null) {
			if (other.tweetElement != null)
				return false;
		} else if (!tweetElement.equals(other.tweetElement))
			return false;
		return true;
	}
	
}
