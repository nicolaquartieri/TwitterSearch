package com.bootcamp.globant.loader;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.AsyncTaskLoader;

import com.bootcamp.globant.LoginActivity;

public class MySearchLoader extends AsyncTaskLoader<List<Status>> {
	
    private SharedPreferences mSP;
    
	private List<Status> resultados = new ArrayList<Status>();
	private List<Status> myList;
	
	private String myQuery;
	
	
	public MySearchLoader(Context context, String query) {
		super(context);
		
		myQuery = query;
		
		mSP = getContext().getSharedPreferences("TwitterSearchPref", 0);
	}
	
	@Override
	public List<Status> loadInBackground() {
		try {
			String token = mSP.getString(LoginActivity.PREF_KEY_OAUTH_TOKEN, null);
			String tokenAuth = mSP.getString(LoginActivity.PREF_KEY_OAUTH_SECRET, null);
			
			Twitter twitter = LoginActivity.getTwitterInstance( new AccessToken( token, tokenAuth) );
		    Query query = new Query( myQuery );
		    if ( myQuery != null && !myQuery.isEmpty() ) {
		    	QueryResult rslt = twitter.search( query );
		    	
		    	resultados = rslt.getTweets();
		    }
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		return resultados;
	}
	
	@Override
	public void deliverResult(List<Status> list) {
		if (isReset()) {
			if (list != null) {
				onReleaseResources(list);
			}
		}
		List<Status> oldApps = list;
		myList = list;

		if (isStarted()) {
			super.deliverResult(list);
		}

		if (oldApps != null) {
			onReleaseResources(oldApps);
		}
	}
	
	@Override
	protected void onStartLoading() {
		if (myList != null) {
			deliverResult(myList);
		}

		if (takeContentChanged() || myList == null) {
			forceLoad();
		}
	}
	
	@Override
	protected void onStopLoading() {
		cancelLoad();
	}
	
	@Override
	public void onCanceled(List<Status> data) {
		super.onCanceled(data);

		onReleaseResources(data);
	}
	
	@Override
	protected void onReset() {
		super.onReset();
	}
	
	protected void onReleaseResources(List<Status> apps) {
		
	}
}
