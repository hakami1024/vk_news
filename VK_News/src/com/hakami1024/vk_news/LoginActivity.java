package com.hakami1024.vk_news;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCaptchaDialog;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;

public class LoginActivity extends ActionBarActivity {

	//private static final String TAG = "LoginActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// VKUIHelper.onCreate( this );

		//String[] fingerprints = VKUtil.getCertificateFingerprint(this,
		//		this.getPackageName());
		// Log.d( TAG, "fingerprints = "+fingerprints[0]);
		
		Fragment fragment = getSupportFragmentManager().findFragmentById(
				R.id.container);

		if (fragment == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		// VKUIHelper.onResume(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// VKUIHelper.onDestroy(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		VKUIHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
			VKUIHelper.onCreate(getActivity());

			final String[] myScope = new String[] { VKScope.WALL,
					VKScope.FRIENDS, VKScope.OFFLINE };

			VKSdk.initialize(new VKSdkListener() {

				@Override
				public void onAccessDenied(VKError authorizationError) {
					// Log.d(TAG, "error: "+authorizationError.errorCode+ ", " +
					// authorizationError.apiError + ", " +
					// authorizationError.httpError);
					/*
					 * new AlertDialog.Builder( getActivity() ) .setMessage(
					 * "Error connecting to the network. " +
					 * authorizationError.errorMessage +
					 * "\n(Access Denied error while initialize" )
					 * .setPositiveButton( "Retry", new
					 * DialogInterface.OnClickListener() {
					 * 
					 * @Override public void onClick( DialogInterface dialog,
					 * int which ) { VKSdk.authorize( myScope, false, false ); }
					 * } ) .setNegativeButton( "Cancel", new
					 * DialogInterface.OnClickListener() {
					 * 
					 * @Override public void onClick( DialogInterface dialog,
					 * int which ) { getActivity().finish(); } } ).show();
					 */
					getActivity().finish();
				}

				@Override
				public void onCaptchaError(VKError captchaError) {
					new VKCaptchaDialog(captchaError).show();
				}

				@Override
				public void onTokenExpired(VKAccessToken arg0) {
					// Log.d( TAG, "onTokenExpired" );

					VKSdk.authorize(myScope, false, false);
					// VKSdk.wakeUpSession();
				}

				@Override
				public void onReceiveNewToken(VKAccessToken newToken) {
					// Log.d(TAG, "starting newsFeed");
					startNewsfeed();
				}

			}, "4453513");

			if (VKSdk.wakeUpSession()) {
				startNewsfeed();
			} else {
				VKSdk.authorize(myScope, false, false);
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_login,
					container, false);
			return rootView;
		}

		private void startNewsfeed() {
			Intent i = new Intent(getActivity(), NewsfeedActivity.class);
			startActivity(i);
			this.getActivity().finish();
		}
	}
}
