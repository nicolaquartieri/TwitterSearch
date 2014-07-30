package com.bootcamp.globant;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoginActivity extends FragmentActivity {

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
    
    private static SharedPreferences mSharedPreferences;
    
    private static RequestToken requestToken;
    
    private LoginAsyncTask loginAsyncTask = null;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
		
		// Evitar esto a toda costa.
//		ThreadPolicy tp = ThreadPolicy.LAX;
//		StrictMode.setThreadPolicy(tp);			
		
		mSharedPreferences = getApplicationContext().getSharedPreferences("TwitterSearchPref", 0);
		
		mbuttonLogin = (Button) findViewById(R.id.loginButton);
		mbuttonLogin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loginToTwitter();
			}
		});
		
		// When we turn back from web twitter authentication...
		if ( !isTwitterLogin() ) {
			Uri uri = getIntent().getData();
			
			if ( uri != null && uri.toString().startsWith( TWITTER_CALLBACK_URL ) ) {
				String verifier = uri.getQueryParameter( URL_TWITTER_OAUTH_VERIFIER );
				
				AccessTokenAsyncTask accessTokenAsyncTask = new AccessTokenAsyncTask( LoginActivity.this );
				accessTokenAsyncTask.execute( verifier );
			}
		} else {
			// TODO ir directamente a la vista de Search.
		}
	}
	
	private boolean isTwitterLogin() {
		return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}

	protected void loginToTwitter() {
		if ( !isTwitterLogin() ) {
			loginAsyncTask = new LoginAsyncTask(LoginActivity.this);
			loginAsyncTask.execute();
		}
	}
	
	// TODO create Button for this call.
	protected void logoutToTwitter() {
		Editor e = mSharedPreferences.edit();
		
		e.remove(PREF_KEY_TWITTER_LOGIN);
		e.remove(PREF_KEY_OAUTH_TOKEN);
		e.remove(PREF_KEY_OAUTH_SECRET);
		
		e.commit();
	}
	
	@Override
	protected void onResume() {	
		super.onResume();
	}
	
	
	// LoginAsyncTask
    private static class LoginAsyncTask extends AsyncTask<Void, Void, RequestToken> {
        private LoginActivity mActivity;
        
    	public LoginAsyncTask(LoginActivity activity) {
    		attach(activity);
		}
    	
		public void detach() {
    		mActivity = null;
    	}
    	
    	public void attach(LoginActivity activity) {
    		mActivity = activity;
    	}    	    
    	
		protected RequestToken doInBackground(Void... param) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
	        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
	        builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
	        Configuration configuration = builder.build();
	        
	        TwitterFactory factory = new TwitterFactory(configuration);
	        twitter = factory.getInstance();
	        
	        RequestToken requestToken = null;
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
        
        protected void onPostExecute(RequestToken result) {
        	if (result != null) {
        		requestToken = result;
        		
        		mActivity.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( requestToken.getAuthenticationURL() ) ) );
        	}
        }
		
    }
    
 // AccessTokenAsyncTask
    private static class AccessTokenAsyncTask extends AsyncTask<String, Void, AccessToken> {
        private LoginActivity mActivity;
        
    	public AccessTokenAsyncTask(LoginActivity activity) {
    		attach(activity);
		}
    	
		public void detach() {
    		mActivity = null;
    	}
    	
    	public void attach(LoginActivity activity) {
    		mActivity = activity;
    	}    	    
    	
		protected AccessToken doInBackground(String... param) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
	        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
	        builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
	        Configuration configuration = builder.build();
	        
	        AccessToken accessToken = null;
	        
	        TwitterFactory factory = new TwitterFactory(configuration);
	        twitter = factory.getInstance();
	        
	        try {
	        	accessToken = twitter.getOAuthAccessToken( requestToken, param[0] );
	        } catch (TwitterException e) {
	            e.printStackTrace();
	        }
	        
            return accessToken;
        }
    	
        @Override
        protected void onProgressUpdate(Void... values) {
        	
        }
        
        protected void onPostExecute(AccessToken result) {
        	if (result != null) {
        		AccessToken accessToken = result;
        		
        		Editor e = mSharedPreferences.edit();
				e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
				e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
				e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
				e.commit();
				
				mActivity.startActivity( new Intent( mActivity.getApplicationContext(), SearchActivity.class ) );
        	}
        }
		
    }
}
