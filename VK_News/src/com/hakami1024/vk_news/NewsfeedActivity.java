package com.hakami1024.vk_news;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.hakami1024.utils.VKAudioPlayer;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;

public class NewsfeedActivity extends ActionBarActivity
{
	private String TAG = "NewsfeedActivity";
	private String FRAGMENT_TAG = "com.hakami1024.vk_news.FragmentTag";
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_newsfeed );
		VKUIHelper.onCreate( this );
		FragmentManager fm = getSupportFragmentManager();
		Log.d(TAG, "in onCreate");
		NewsfeedFragment fragment = (NewsfeedFragment) fm.findFragmentByTag(FRAGMENT_TAG);

	    if (fragment == null) {
	    	fragment = new NewsfeedFragment();
	        fm.beginTransaction().add(R.id.newsfeed_container, fragment, FRAGMENT_TAG).commit();
	    }
	    else
	    {
	    	Log.d(TAG, "fragment != null, just attaching");
	    	fm.beginTransaction().attach(fragment).commit();
	    }

	}
	
	@Override 
	protected void onResume() { 
		super.onResume(); 
		VKUIHelper.onResume(this); 
	} 

	@Override 
	protected void onDestroy() { 
		super.onDestroy(); 
		VKUIHelper.onDestroy(this); 
	} 

	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		super.onActivityResult(requestCode, resultCode, data); 
		VKUIHelper.onActivityResult(requestCode, resultCode, data); 
	} 

	public void callMe()
	{
		
	}
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.newsfeed, menu );
		return true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	  }

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if( id == R.id.newsfeed_logout )
		{
			VKAudioPlayer.getInstance().stop();
			VKSdk.logout();
			Intent i = new Intent( this, LoginActivity.class );
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected( item );
	}
	
}
