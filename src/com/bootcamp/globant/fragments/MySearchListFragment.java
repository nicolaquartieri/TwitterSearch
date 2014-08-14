package com.bootcamp.globant.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bootcamp.globant.R;
import com.bootcamp.globant.SearchActivity;

public class MySearchListFragment extends ListFragment {

	
	public static MySearchListFragment newInstance() {
		
		MySearchListFragment sf = new MySearchListFragment();

		return sf;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		((SearchActivity)getActivity()).setMySearchListFragment(getId());
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new ListRepoCustomAdapter(getActivity(), R.layout.repo_request_row, apps);
		setListAdapter(mAdapter);

		setListShown(false);

		setEmptyText(getResources().getText(R.string.repo_and_user_error));

		setHasOptionsMenu(true);

		getLoaderManager().initLoader(0, null, this);
	}
}
