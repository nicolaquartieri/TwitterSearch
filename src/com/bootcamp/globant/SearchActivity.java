package com.bootcamp.globant;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.bootcamp.globant.adapter.ListCustomAdapter;
import com.bootcamp.globant.contentprovider.MiTwitterContentProvider;
import com.bootcamp.globant.dialog.DialogSearch;
import com.bootcamp.globant.dialog.DialogSearch.OnMesajeSend;
import com.bootcamp.globant.model.TweetElement;
import com.bootcamp.globant.model.WrapperItem;

public class SearchActivity extends FragmentActivity implements OnMesajeSend, OnScrollListener {

	private Button mbuttonSearch = null;
	private ListCustomAdapter adapter = null;
	private List<WrapperItem> lista = new ArrayList<WrapperItem>();
	private TweetSearchTask tweetSearchTask = null;
	private DialogSearch ds = null;
	private CheckBox mcheckParallel;
	private boolean checkParallel = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
			
		// Evitar esto a toda costa.
//		ThreadPolicy tp = ThreadPolicy.LAX;
		// StrictMode.setThreadPolicy(tp);

		ListView listaCustom = (ListView) findViewById(R.id.listViewResult);
		listaCustom.setOnScrollListener(this);
		adapter = new ListCustomAdapter(this, R.layout.listview_textimage,lista);
		listaCustom.setAdapter(adapter);

		mbuttonSearch = (Button) findViewById(R.id.buttonSearch);
		mbuttonSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				lista.clear();
				adapter.notifyDataSetChanged();

				mcheckParallel = (CheckBox) findViewById(R.id.checkParallel);
				setCheckParallel(mcheckParallel.isChecked());

				tweetSearchTask = new TweetSearchTask(SearchActivity.this);
				EditText tv = (EditText) findViewById(R.id.searchText);
				tweetSearchTask.execute(tv.getText().toString());

				showDialogSearch();
			}
		});
	}

	protected void setCheckParallel(boolean checked) {
		checkParallel = checked;
	}

	public boolean getCheckParallel() {
		return checkParallel;
	}
	
	private void showDialogSearch() {
		ds = new DialogSearch();		
		FragmentManager fm = getSupportFragmentManager();	
		ds.show(fm, "fragment_dialog");
	}

	public void setListResults(List<twitter4j.Status> result) {
		ds.dismissAllowingStateLoss();
						
		getContentResolver().delete(MiTwitterContentProvider.CONTENT_URI.buildUpon().build(), null , null);
		toTweetList(result, true);
	}
	
    private void toTweetList(List<twitter4j.Status> resultados, boolean doSave) {
    	for (twitter4j.Status result : resultados) {
    		lista.add(new WrapperItem(new TweetElement(result.getUser().getName(), 
    												   result.getText(), 
    												   result.getUser().getProfileImageURL())));
    		
    		// TODO Update deprecated methods
//    		if (doSave) {
//	    		ContentValues cv = new ContentValues();
//	    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_FROM, result.from_user_id );
//	    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_TWEET, result.text);
//	    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_IMAGEN, result.profileImageUrl);
//	    		getContentResolver().insert(MiTwitterContentProvider.CONTENT_URI, cv);
//    		}
		}
    	
    	adapter.notifyDataSetChanged();
    	findViewById(R.id.listViewResult).setVisibility(View.VISIBLE);		
	}

	@Override
	public void sendMsj(String msj) {
		if (msj.equalsIgnoreCase("stop")) {
			lista.clear();
			tweetSearchTask.cancel(true);
		}
	}

	// TODO Update deprecated methods
//	@Override
//	protected void onSaveInstanceState(Bundle outState) {	
//		super.onSaveInstanceState(outState);
//		
//		getContentResolver().delete(MiTwitterContentProvider.CONTENT_URI.buildUpon().build(), null , null);
//		for (WrapperItem item : lista) {   		    		    		    		
//    		ContentValues cv = new ContentValues();
//    		
//    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_FROM,  item.getTweetElemento().getTextoFrom());
//    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_TWEET, item.getTweetElemento().getTextoTweet());
//    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_IMAGEN, item.getTweetElemento().getImagen());
//    		getContentResolver().insert(MiTwitterContentProvider.CONTENT_URI, cv);    		
//		}
//	}

//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {	
//		super.onRestoreInstanceState(savedInstanceState);
//		
//		Cursor cursor = managedQuery(MiTwitterContentProvider.CONTENT_URI , null, null, null, null);
//		cursor.moveToFirst();
//		while (!cursor.isAfterLast()) {			
//			lista.add(new WrapperItem(new TweetElement(cursor.getString(1), 
//													   cursor.getString(2), 
//													   cursor.getString(3))));
//			
//			cursor.moveToNext();
//		}		
//				
//		cursor.close();
//	}
	
	@Override
	protected void onResume() {	
		super.onResume();
		
		adapter.notifyDataSetChanged();
    	findViewById(R.id.listViewResult).setVisibility(View.VISIBLE);
	}
	
	
	// AsyncTask
    private static class TweetSearchTask extends AsyncTask<String, Void, List<twitter4j.Status>> {
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
        
        protected void onPostExecute(List<twitter4j.Status> result) {
        	if (result != null)
        		mActivity.setListResults( result );
        }
		
    }

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		Log.e("INFO", "firstVisibleItem: "+firstVisibleItem+" ;visibleItemCount: "+visibleItemCount+" ;"+totalItemCount);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
}
