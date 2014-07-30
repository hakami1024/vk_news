package com.hakami1024.vk_news;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.hakami1024.utils.VKPost;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiUser;

public class SingleNewsActivity extends ActionBarActivity {

	//private static String TAG = "SingleNewsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_news);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.single_news_container, new PlaceholderFragment())
					.commit();
		}

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.single_news, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.home) {
			if (NavUtils.getParentActivityName(this) != null)
				NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements
			DataUpdating {

		NewsfeedCreator newsfeedCreator = new NewsfeedCreator(this);
		View mainView;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			ScrollView rootView = (ScrollView) inflater.inflate(
					R.layout.fragment_single_news, container, false);
			mainView = inflater.inflate(R.layout.news_item, null);

			String newsName = this.getActivity().getIntent().getExtras()
					.getString(VKPost.POST_DATE);
			newsfeedCreator.getNewsFromStorage(newsName);

			rootView.addView(mainView);

			return rootView;
		}

		@Override
		public void setNewsfeed(JSONObject resp, boolean first) {
			VKPost post = new VKPost();
			JSONArray profiles, groups;
			try {
				resp = resp.getJSONObject("response");
				post.parse(resp.getJSONArray("items").optJSONObject(0));
				profiles = resp.getJSONArray("profiles");
				groups = resp.getJSONArray("groups");

				SparseArray<VKApiUser> usersArray = new SparseArray<VKApiUser>();
				;
				SparseArray<VKApiCommunity> groupsArray = new SparseArray<VKApiCommunity>();

				VKApiUser[] users = new VKApiUser[profiles.length()];
				VKApiCommunity[] communities = new VKApiCommunity[groups
						.length()];

				for (int i = 0; i < profiles.length(); i++) {
					users[i] = new VKApiUser();
					users[i].parse(profiles.getJSONObject(i));
					usersArray.append(users[i].id, users[i]);
				}
				// Log.d(TAG, "filled users");

				for (int i = 0; i < groups.length(); i++) {
					communities[i] = new VKApiCommunity();
					communities[i].parse(groups.getJSONObject(i));
					groupsArray.append(communities[i].id, communities[i]);
				}
				VKPostHolder holder = new VKPostHolder(mainView,
						VKPostViewer.countReposts(post), false, -1);
				VKPostViewer viewer = new VKPostViewer(post, usersArray,
						groupsArray, holder, true) {

					@Override
					void changeDataSet() {
						mainView.invalidate();
					}
				};
				viewer.setOnView();
			} catch (JSONException e) {
				Toast.makeText(getActivity(),
						"Error in data parsing. Write to developer.",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}

		}

		@Override
		public void setNoNewsfeed() {
			Toast.makeText(getActivity(), "Error. Try to reload application",
					Toast.LENGTH_LONG).show();
		}
	}
}
