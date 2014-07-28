package com.bootcamp.globant;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.CheckBox;

import com.bootcamp.globant.adapter.ListCustomAdapter;
import com.bootcamp.globant.contentprovider.MiTwitterContentProvider;
import com.bootcamp.globant.dialog.DialogSearch;
import com.bootcamp.globant.dialog.DialogSearch.OnMesajeSend;
import com.bootcamp.globant.model.Result;
import com.bootcamp.globant.model.SearchRespuesta;
import com.bootcamp.globant.model.TweetElement;
import com.bootcamp.globant.model.WrapperItem;
import com.bootcamp.globant.sql.MiSQLiteHelper;
import com.google.gson.Gson;

public class SearchActivity extends FragmentActivity implements OnMesajeSend, OnScrollListener {

	private Button mbuttonSearch = null;
	private ListCustomAdapter adapter = null;
	private List<WrapperItem> lista = new ArrayList<WrapperItem>();
	private TweetSearchTask tweetSearchTask = null;
	private DialogSearch ds = null;
	private CheckBox mcheckParallel;
	private boolean checkParallel = false;
	private Button mbuttonLogin;
	
	static String TWITTER_CONSUMER_KEY = "0wE7SWv4GxQIbGEeE9n7H0fNG";
    static String TWITTER_CONSUMER_SECRET = "NvhHjqKg3DLtsCfei3ZaxJDtX1YuCv4GOttD8wv3K6nqTFQY05";
    
    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    
    static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";
    
    // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
    
    private static Twitter twitter;
    
    private LoginAsyncTask loginAsyncTask = null;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
			
		// Evitar esto a toda costa.
//		ThreadPolicy tp = ThreadPolicy.LAX;
//		StrictMode.setThreadPolicy(tp);			
		
		mbuttonLogin = (Button) findViewById(R.id.loginButton);
		mbuttonLogin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loginToTwitter();
			}
		});
		
		// TODO Perteneciente a activity_search.xml
//		ListView listaCustom = (ListView)findViewById(R.id.listViewResult);
//		listaCustom.setOnScrollListener(this);
//		adapter = new ListCustomAdapter(this, R.layout.listview_textimage, lista);		
//		listaCustom.setAdapter(adapter);		
//		
//		mbuttonSearch = (Button) findViewById(R.id.buttonSearch);
//		mbuttonSearch.setOnClickListener(new OnClickListener() {			
//			@Override
//			public void onClick(View v) {
//				// TODO Validations
//				lista.clear();
//				adapter.notifyDataSetChanged();
//								
//				mcheckParallel = (CheckBox) findViewById(R.id.checkParallel);
//				setCheckParallel(mcheckParallel.isChecked());
//				
//				tweetSearchTask = new TweetSearchTask(SearchActivity.this);
//				EditText tv = (EditText) findViewById(R.id.searchText);
//				tweetSearchTask.execute(tv.getText().toString());				
//				
//				showDialogSearch();												
//			}		
//		});
	}
	
	protected void loginToTwitter() {
		loginAsyncTask = new LoginAsyncTask(SearchActivity.this);
		loginAsyncTask.execute();
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}
	
	public void setListResults(List<Result> result) {
		ds.dismissAllowingStateLoss();
						
		getContentResolver().delete(MiTwitterContentProvider.CONTENT_URI.buildUpon().build(), null , null);
		toTweetList(result, true);
	}
	
    private void toTweetList(List<Result> resultados, boolean doSave) {
    	for (Result result : resultados) {
    		lista.add(new WrapperItem(new TweetElement(result.fromUser, result.text, result.profileImageUrl)));
    		
    		// TODO Refactoring
    		if (doSave) {    			
	    		ContentValues cv = new ContentValues();
	    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_FROM, result.from_user_id );
	    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_TWEET, result.text);
	    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_IMAGEN, result.profileImageUrl);
	    		getContentResolver().insert(MiTwitterContentProvider.CONTENT_URI, cv);
    		}
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
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		
		getContentResolver().delete(MiTwitterContentProvider.CONTENT_URI.buildUpon().build(), null , null);
		for (WrapperItem item : lista) {   		    		    		    		
    		ContentValues cv = new ContentValues();
    		
    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_FROM,  item.getTweetElemento().getTextoFrom());
    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_TWEET, item.getTweetElemento().getTextoTweet());
    		cv.put(MiSQLiteHelper.TWEET_COLUMNA_IMAGEN, item.getTweetElemento().getImagen());
    		getContentResolver().insert(MiTwitterContentProvider.CONTENT_URI, cv);    		
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {	
		super.onRestoreInstanceState(savedInstanceState);
		
		Cursor cursor = managedQuery(MiTwitterContentProvider.CONTENT_URI , null, null, null, null);
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
	protected void onResume() {	
		super.onResume();
		
		// TODO Perteneciente a activity_search.xml 
//		adapter.notifyDataSetChanged();
//    	findViewById(R.id.listViewResult).setVisibility(View.VISIBLE);
	}
	
	
	// AsyncTask
    private static class TweetSearchTask extends AsyncTask<String, Void, List<Result>> {
        private SearchActivity mActivity;
        private List<Result> resultados = null;
    	
        
    	public TweetSearchTask(SearchActivity activity) {
    		attach(activity);
		}
    	
		public void detach() {
    		mActivity = null;
    	}
    	
    	public void attach(SearchActivity activity) {
    		mActivity = activity;
    	}    	    
    	
		protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
			InputStream in = entity.getContent();
			StringBuffer out = new StringBuffer();
			int n = 1;
			while (n > 0) {
				byte[] b = new byte[4096];
				n = in.read(b);
				if (n > 0)
					out.append(new String(b, 0, n));
			}
			return out.toString();
		}

    	protected List<Result> doInBackground(String... param) {    		
    		String text = null;
            
        	HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			
			String url = new String("http://search.twitter.com/search.json?q=" + URLEncoder.encode(param[0]) +
																		  "&rpp=" + URLEncoder.encode("30") +
																		  "&include_entities=" + URLEncoder.encode("true") +
																		  "&result_type=" + URLEncoder.encode("mixed"));
			
			Log.e("INFO", url);
			
			HttpGet httpGet = new HttpGet(url);			
						
			HttpResponse response = null;		    
			try {
				response = httpClient.execute(httpGet, localContext);
				HttpEntity entity = response.getEntity();
				text = getASCIIContentFromEntity(entity);
					
				Gson gson = new Gson();
				
				SearchRespuesta sr = gson.fromJson(text, SearchRespuesta.class);
				
				resultados = sr.results;											
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            	
            return resultados;
        }
    	
        @Override
        protected void onProgressUpdate(Void... values) {      
        	
        }
        
        protected void onPostExecute(List<Result> result) {
        	if (result != null)
        		mActivity.setListResults(result);
        }
		
    }

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		Log.e("INFO", "firstVisibleItem: "+firstVisibleItem+" ;visibleItemCount: "+visibleItemCount+" ;"+totalItemCount);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
	
	
	// LoginAsyncTask
    private static class LoginAsyncTask extends AsyncTask<Void, Void, RequestToken> {
        private SearchActivity mActivity;
        private List<Result> resultados = null;
        private static RequestToken requestToken;
        
    	public LoginAsyncTask(SearchActivity activity) {
    		attach(activity);
		}
    	
		public void detach() {
    		mActivity = null;
    	}
    	
    	public void attach(SearchActivity activity) {
    		mActivity = activity;
    	}    	    
    	
		protected RequestToken doInBackground(Void... param) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
	        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
	        builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
	        Configuration configuration = builder.build();
	        
	        TwitterFactory factory = new TwitterFactory(configuration);
	        twitter = factory.getInstance();

	        try {
	            requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
	        } catch (TwitterException e) {
	            e.printStackTrace();
	        }	
	        
            return requestToken;
        }
    	
        @Override
        protected void onProgressUpdate(Void... values) {      
        	
        }
        
        protected void onPostExecute(List<Result> result) {
        	if (result != null)
        		mActivity.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( requestToken.getAuthenticationURL() ) ) );
        }
		
    }
}
