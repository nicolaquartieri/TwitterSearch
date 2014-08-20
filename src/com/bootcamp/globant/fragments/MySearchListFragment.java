package com.bootcamp.globant.fragments;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.bootcamp.globant.R;
import com.bootcamp.globant.SearchActivity;
import com.bootcamp.globant.adapter.ListCustomAdapter;
import com.bootcamp.globant.loader.MySearchLoader;
import com.bootcamp.globant.model.WrapperItem;
import com.bootcamp.globant.view.ScrollableImageView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MySearchListFragment extends ListFragment implements OnQueryTextListener, LoaderManager.LoaderCallbacks<List<Status>> {
	
	private static String myQuery;
	
	private ListCustomAdapter mAdapter = null;
	
	private List<WrapperItem> lista = new ArrayList<WrapperItem>();
	
	private MenuItem refreshMenuItem;
	
	private static final int TOP_HEIGHT = 200;
	
	private ScrollableImageView mBlurredImageHeader;
	
	private SearchActivity mSearchActivity;

	private boolean checkParallel = false;
	
	
	public static MySearchListFragment newInstance( String query ) {
		
		MySearchListFragment sf = new MySearchListFragment();
		
		myQuery = query;
		
		return sf;
	}
	
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
		
		Switch mSwitch = (Switch) header.findViewById(R.id.background_switch);
		if ( savedInstanceState != null )
			checkParallel = savedInstanceState.getBoolean( "checkParallel" );
		mSwitch.setChecked( checkParallel );
		mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checkParallel = isChecked;
			}
		});
		
		mAdapter = new ListCustomAdapter( this, R.layout.listview_textimage, lista );
		
		setListAdapter(mAdapter);
		
		setListShown(false);
		
		setRetainInstance(true);
		
		setEmptyText(getResources().getText(R.string.tweet_error));
		
		setHasOptionsMenu(true);
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onPause() {
		Bundle outState = new Bundle();
		outState.putBoolean("checkParallel", getCheckParallel() );
		
		onSaveInstanceState(outState);
		
		super.onPause();
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("checkParallel", getCheckParallel() );
		
		super.onSaveInstanceState(outState);
	}
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//    	inflater.inflate(R.menu.menu, menu);
//
//    	SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
//        SearchableInfo si = searchManager.getSearchableInfo( new ComponentName(getActivity().getApplicationContext(), SearchActivity.class) );
//        
//    	MenuItem item = menu.findItem(R.id.action_search);
//    	SearchView searchView = (SearchView) item.getActionView();
//    	searchView.setSearchableInfo(si);
//    	searchView.setOnQueryTextListener(this);
//    	searchView.setQueryHint(getResources().getText(R.string.search));
//    	
//		super.onCreateOptionsMenu(menu, inflater);
//	}
	
	@Override
	public Loader<List<Status>> onCreateLoader(int arg0, Bundle arg1) {
		myQuery = ((SearchActivity)getActivity()).getQueryString();
		
		return new MySearchLoader( getActivity(), myQuery );
	}
	
	@Override
	public void onLoadFinished(Loader<List<Status>> arg0, List<Status> data) {
		
		if (refreshMenuItem != null) {
			refreshMenuItem.collapseActionView();
			refreshMenuItem.setActionView(null);
		}
		
		if (data == null) {
			Crouton.makeText(getActivity(), R.string.tweet_error, Style.ALERT).show();
			
			data = new ArrayList<Status>();
		}
		
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
		
		mAdapter.clear();
		mAdapter.setData(data);
		
		// Voice Speak Filter
		String searchVoiceText = ((SearchActivity) getActivity()).getSearchVoiceText();
		if (searchVoiceText != null && !searchVoiceText.isEmpty())
			doFilter(searchVoiceText);
		
		mAdapter.notifyDataSetChanged();
	}
	
	public void doFilter(String newFilter) {
		mAdapter.getFilter().filter(newFilter);
	}
	
	@Override
	public void onLoaderReset(Loader<List<Status>> arg0) {
		mAdapter.clear();
	}
	
	
	@Override
	public boolean onQueryTextChange(String newText) {
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
        
        doFilter(newFilter);
        
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		return true;
	}

	public void setCheckParallel(boolean checked) {
		checkParallel = checked;
	}
	
	public boolean getCheckParallel() {
		return checkParallel;
	}

	public void doSearch() {
		getLoaderManager().restartLoader(0, null, this);
		
		setListShown(false);
	}
}
