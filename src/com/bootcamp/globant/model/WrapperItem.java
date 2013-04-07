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
	
}

