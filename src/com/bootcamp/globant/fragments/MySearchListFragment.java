package com.bootcamp.globant.fragments;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.bootcamp.globant.LoginActivity;
import com.bootcamp.globant.R;
import com.bootcamp.globant.SearchActivity;
import com.bootcamp.globant.adapter.ListCustomAdapter;
import com.bootcamp.globant.model.TweetElement;
import com.bootcamp.globant.model.WrapperItem;

public class MySearchListFragment extends ListFragment implements OnScrollListener {
	
	private String myQuery = null;
	
	private ListCustomAdapter mAdapter = null;
	
	private List<WrapperItem> lista = new ArrayList<WrapperItem>();
	
	private MenuItem refreshMenuItem = null;
		
	private SearchActivity mSearchActivity = null;

	private boolean checkParallel = false;
	
	private Switch mSwitch = null;

	private TweetSearchTask tweetSearchTask = null;
	
	
	@Override
	public void onAttach(Activity activity) {
		mSearchActivity = (SearchActivity) activity;
		
		super.onAttach(activity);
	}
	
	@Override
	public void onDetach() {
		mSearchActivity = null;
		
		super.onDetach();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		((SearchActivity)getActivity()).setMySearchListFragment(getId());
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		LayoutInflater li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View header = li.inflate(R.layout.fragments_header, null, false);
		getListView().addHeaderView( header );
		
		mSwitch = (Switch) header.findViewById(R.id.background_switch);
		if ( savedInstanceState != null )
			checkParallel = savedInstanceState.getBoolean( "checkParallel" );
		mSwitch.setChecked( checkParallel );
		mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checkParallel = isChecked;
			}
		});
		
		getListView().setOnScrollListener(this);
		
		mAdapter = new ListCustomAdapter( this, R.layout.listview_textimage, lista );
		setListAdapter(mAdapter);
		
		setEmptyText(getResources().getText(R.string.tweet_error));
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onPause() {
		Bundle outState = new Bundle();
		outState.putBoolean("checkParallel", getCheckParallel() );
		
		onSaveInstanceState(outState);
		
		super.onPause();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("checkParallel", getCheckParallel() );
		
		super.onSaveInstanceState(outState);
	}
	
	public void doFilter(String newFilter) {
		mAdapter.getFilter().filter(newFilter);
	}
	
	public void setCheckParallel(boolean checked) {
		checkParallel = checked;
	}
	
	public boolean getCheckParallel() {
		return checkParallel;
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		Log.e("INFO", "firstVisibleItem: "+firstVisibleItem+" ;visibleItemCount: " + visibleItemCount+" ; totalItemCount" + totalItemCount);
		
		int totalElementsSaw = firstVisibleItem + visibleItemCount;
		
		if ( totalElementsSaw == totalItemCount && totalElementsSaw != 0 ) { 
			setListShown(false);
			
			tweetSearchTask = new TweetSearchTask( (SearchActivity) this.getActivity() );
			tweetSearchTask.execute( myQuery );
		}
	}
	
	// AsyncTask
    private class TweetSearchTask extends AsyncTask<String, Void, List<twitter4j.Status>> {
        private SearchActivity mActivity;
        private List<twitter4j.Status> resultados = null;
		private SharedPreferences mSP;
    	
        
    	public TweetSearchTask(SearchActivity activity) {
    		attach(activity);
		}
    	
		public void detach() {
    		mActivity = null;
    	}
    	
    	public void attach(SearchActivity activity) {
    		mActivity = activity;
    		
    		mSP = mActivity.getApplicationContext().getSharedPreferences("TwitterSearchPref", 0);
    	}
    	
    	protected List<twitter4j.Status> doInBackground(String... param) {
			
			try {
				String token = mSP.getString(LoginActivity.PREF_KEY_OAUTH_TOKEN, null);
				String tokenAuth = mSP.getString(LoginActivity.PREF_KEY_OAUTH_SECRET, null);
				
				Twitter twitter = LoginActivity.getTwitterInstance( new AccessToken( token, tokenAuth) );
			    Query query = new Query( param[0] );
			    QueryResult result = twitter.search( query );
			    
				resultados = result.getTweets();
			} catch (TwitterException e) {
				e.printStackTrace();
			}
            
            return resultados;
        }
    	
        @Override
        protected void onProgressUpdate(Void... values) {
        	
        }
        
        protected void onPostExecute(List<twitter4j.Status> results) {
        	mAdapter.setNotifyOnChange(true);
        	
        	if (results != null)
        		for (twitter4j.Status element : results) {
        			WrapperItem wi = new WrapperItem(new TweetElement(element.getUser().getName(), 
											  						  element.getText(), 
											  						  element.getUser().getProfileImageURL()));
        			synchronized ( mAdapter ) {
        				if ( !mAdapter.contains( wi ) )
            				mAdapter.add( wi );
					}
        		}
        	setListShown(true);
        }
    }
    
    
	public void doSearch(String queryString) {
		myQuery = queryString;
		
		setListShown(false);
		
		tweetSearchTask = new TweetSearchTask( (SearchActivity) this.getActivity() );
		tweetSearchTask.execute( queryString );
	}
}
