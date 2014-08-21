package com.bootcamp.globant;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bootcamp.globant.contentprovider.MiTwitterContentProvider;
import com.bootcamp.globant.dialog.DialogSearch;
import com.bootcamp.globant.fragments.MySearchListFragment;
import com.bootcamp.globant.model.TweetElement;
import com.bootcamp.globant.model.WrapperItem;
import com.bootcamp.globant.sql.MiSQLiteHelper;

public class SearchActivity extends ActionBarActivity implements DialogSearch.OnMesajeSend, OnQueryTextListener {
	
	private List<WrapperItem> lista = new ArrayList<WrapperItem>();
	
	private String queryText = null;
	private int mySearchViewListFragment;
	private String searchVoiceText;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		setTitle(null);
		
		// Evitar esto a toda costa.
//		ThreadPolicy tp = ThreadPolicy.LAX;
		// StrictMode.setThreadPolicy(tp);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new MySearchListFragment()).commit();
		}
	}
	
	private void handleIntent(Intent intent) {
		// Search Voice 
        final Intent queryIntent = getIntent();
        final String queryAction = getIntent().getAction();
        if ( Intent.ACTION_SEARCH.equals( queryAction ) ) {
        	queryText = queryIntent.getStringExtra( SearchManager.QUERY );
        }
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
		
		MySearchListFragment mSF = (MySearchListFragment)getSupportFragmentManager().findFragmentById(mySearchViewListFragment);
		mSF.doSearch();
		
		super.onNewIntent(intent);
	}
	
	public void setListResults(List<twitter4j.Status> result) {
		getContentResolver().delete(MiTwitterContentProvider.CONTENT_URI.buildUpon().build(), null , null);
		toTweetList(result, true);
	}
	
    private void toTweetList(List<twitter4j.Status> resultados, boolean doSave) {
    	for (twitter4j.Status result : resultados) {
    		lista.add(new WrapperItem(new TweetElement(result.getUser().getName(), 
    												   result.getText(), 
    												   result.getUser().getProfileImageURL())));
    		
    		if (doSave) {
	    		ContentValues cv = new ContentValues();
	    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_FROM, result.getUser().getId() );
	    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_TWEET, result.getText());
	    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_IMAGEN, result.getUser().getProfileImageURL() );
	    		getContentResolver().insert(MiTwitterContentProvider.CONTENT_URI, cv);
    		}
		}
	}
    
	@Override
	public void sendMsj(String msj) {
		if (msj.equalsIgnoreCase("stop")) {
			lista.clear();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		getContentResolver().delete(MiTwitterContentProvider.CONTENT_URI.buildUpon().build(), null , null);
		for (WrapperItem item : lista) {
    		ContentValues cv = new ContentValues();
    		
    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_FROM,  item.getTweetElemento().getTextoFrom());
    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_TWEET, item.getTweetElemento().getTextoTweet());
    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_IMAGEN, item.getTweetElemento().getImagen());
    		getContentResolver().insert(MiTwitterContentProvider.CONTENT_URI, cv);
		}
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		
		super.onRestoreInstanceState(savedInstanceState);
		
		String[] columns = new String[] { MiSQLiteHelper.TWEET_ID,
										  MiSQLiteHelper.TWEET_COLUMNA_TWEET,
										  MiSQLiteHelper.TWEET_COLUMNA_IMAGEN };
		
		Cursor cursor = getContentResolver().query(MiTwitterContentProvider.CONTENT_URI, columns, null, null, null);
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {			
			lista.add(new WrapperItem(new TweetElement(cursor.getString(1), 
													   cursor.getString(2), 
													   cursor.getString(3))));
			
			cursor.moveToNext();
		}
		
		cursor.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
    	
    	SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
    	SearchableInfo si = searchManager.getSearchableInfo( new ComponentName(getApplicationContext(), SearchActivity.class) );
    	
    	MenuItem item = menu.findItem(R.id.action_search);
    	SearchView searchView = (SearchView) item.getActionView();
    	searchView.setSearchableInfo(si);
    	searchView.setOnQueryTextListener(this);
    	searchView.setQueryHint(getResources().getText(R.string.search));
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onQueryTextChange(String newText) {
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
        
		return true;
	}
	
	@Override
	public boolean onQueryTextSubmit(String arg0) {
		return false;
	}
	
	public void setMySearchListFragment(int mySearchViewListFragment) {
		this.mySearchViewListFragment = mySearchViewListFragment;
	}
	
	public String getSearchVoiceText() {
		return searchVoiceText;
	}
	
	public String getQueryString() {
		return queryText;
	}
}
