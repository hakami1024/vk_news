package com.hakami1024.vk_news;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import com.hakami1024.utils.VKPost;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiUser;

public class NewsfeedFragment extends ListFragment implements OnScrollListener,
		OnRefreshListener, DataUpdating {
	VkListAdapter adapter;
	private static final String LIST_TOP_INDEX = "com.hakami1024.vk_news.listTopIndex";
	private final int TIME_NOW = 1;
	private String TAG = "NewsfeedFragment";
	public int addNews = 1;
	private int topNewsIndex = 0;

	SparseArray<VKApiUser> usersArray = new SparseArray<VKApiUser>();
	SparseArray<VKApiCommunity> groupsArray = new SparseArray<VKApiCommunity>();
	ArrayList<VKPost> news = new ArrayList<VKPost>();
	ArrayList<VKPost> updates = new ArrayList<VKPost>();
	NewsfeedCreator newsCreator = new NewsfeedCreator(this);
	SwipeRefreshLayout refreshLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d(TAG, "onCreate started");

		// newsCreator.getNews(0);
		setRetainInstance(true);
		/*
		 * if( savedInstanceState != null ) { topNewsIndex =
		 * savedInstanceState.getInt(LIST_TOP_INDEX); Log.d(TAG,
		 * "in onCreate, topNewsIndex = "+topNewsIndex); addNews = 2;
		 * newsCreator.getNews(); } else Log.d(TAG,
		 * "savedInstanceState is null");
		 */

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.photo_loading)
				.cacheInMemory(true).cacheOnDisc(true).build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getActivity().getApplicationContext())
				.defaultDisplayImageOptions(defaultOptions).build();
		ImageLoader.getInstance().init(config);

		// newsCreator.getNews();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_newsfeed, parent, false);
		refreshLayout = (SwipeRefreshLayout) v;
		refreshLayout.setOnRefreshListener(this);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		this.getListView().setOnScrollListener(this);
		//Log.d(TAG, "setting onItemClickListener");
		this.getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//Log.d(TAG, "Item Clicked");
				Intent i = new Intent(NewsfeedFragment.this.getActivity(),
						SingleNewsActivity.class);
				i.putExtra(VKPost.POST_DATE, "" + news.get(position).date);

				NewsfeedFragment.this.getActivity().startActivity(i);
			}

		});

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		savedInstanceState.putInt(LIST_TOP_INDEX, topNewsIndex);
		Log.i(TAG, "onSavedInstanceState");
	}

	public void setNewsfeed(JSONObject resp, boolean isFirst) {
		try {
			//Log.d(TAG, "setNewsfeed: " + resp.toString());
			resp = resp.getJSONObject("response");
			JSONArray items = resp.getJSONArray("items");
			JSONArray profiles = resp.getJSONArray("profiles");
			JSONArray groups = resp.getJSONArray("groups");

			if (items == null || items.length() == 0)
				return;

			VKPost[] posts = new VKPost[items.length()];
			VKApiUser[] users = new VKApiUser[profiles.length()];
			VKApiCommunity[] communities = new VKApiCommunity[groups.length()];
			// Log.d(TAG, "JSONArrays and object arrays created. Filling...");
			for (int i = 0; i < items.length(); i++) {
				try {
					posts[i] = new VKPost();

					posts[i].parse(items.optJSONObject(i));
				} catch (Exception ex) {
					Log.d(TAG, "located");
					ex.printStackTrace();
				}
			}
			// Log.d(TAG, "filled posts");
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
			// Log.d(TAG, "filled groups");

			if (isFirst) {
				// adapter.insert(posts[0], 0);
				updates.add(posts[0]);

				if (updates.size() < 2)
					newsCreator
							.getUpdate(news.get(0).date, updates.get(0).date);
				else
					newsCreator.getUpdate(updates.get(updates.size() - 1).date,
							updates.get(updates.size() - 2).date);
			} else
				news.addAll(Arrays.asList(posts));

			if (adapter == null) {
				adapter = new VkListAdapter(news);
				this.setListAdapter(adapter);
				//Log.d(TAG, "adapter created");
			} // else
			adapter.notifyDataSetChanged();
			//Log.d(TAG, "in setNewsfeed, news.size() = " + news.size());

		} catch (Exception e) {
			Log.e(TAG, "error setting Newsfeed");
			e.printStackTrace();
		}

		Log.d(TAG, "topNewsIndex = " + topNewsIndex);
		if (news.size() > topNewsIndex)
			addNews = 0;
		else {
			newsCreator.getNews();
			// getListView().setSelection( news.size()-1 );
		}
	}

	public void setNoNewsfeed() {
		if (!updates.isEmpty()) {
			Log.d(TAG, "setNoNewsfeed: updates");
			news.addAll(0, updates);
			getListView().setSelection(updates.size());
			getListView().smoothScrollToPosition(updates.size());
			for (int i = 0; i < updates.size(); i++)
				adapter.getView(i, null, null);
			updates.clear();
			adapter.notifyDataSetChanged();
		}
		addNews = 0;
		refreshLayout.setRefreshing(false);
	}

	private class VkListAdapter extends ArrayAdapter<VKPost> {

		public VkListAdapter(ArrayList<VKPost> news) {
			super(getActivity(), 0, news);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			Log.d(TAG, "on position " + position);

			if (convertView == null) {
				Log.d(TAG, "convertView creating");
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.news_item, null);
				VKPostHolder holder = new VKPostHolder(convertView,
						VKPostViewer.countReposts(getItem(position)), false,
						position);
				VKPostViewer viewer = new VKPostViewer(getItem(position),
						usersArray, groupsArray, holder, false) {
					@Override
					void changeDataSet() {
						adapter.notifyDataSetChanged();
						Log.d(TAG, "changeDataSet worked");
					}
				};
				convertView.setTag(holder);
				// convertView.setTag(position);
				// convertView.setTag(viewer);
				viewer.setOnView();
			} else {
				Log.d(TAG, "" + convertView + " " + convertView.getTag());
				VKPostHolder holder;// = (VKPostHolder)convertView.getTag();
				if (convertView.getTag() == null
						|| ((VKPostHolder) convertView.getTag()).time != getItem(position).date) {
					if (convertView.getTag() == null) {
						holder = new VKPostHolder(convertView,
								VKPostViewer.countReposts(getItem(position)),
								false, position);
						Log.e(TAG, "RECREATING HOLDER");
					} else {
						holder = (VKPostHolder) convertView.getTag();
						holder.context = convertView.getContext();
						holder.resetPost(position,
								VKPostViewer.countReposts(getItem(position)),
								convertView);
						Log.i(TAG, "RESET HOLDER");
					}

					VKPostViewer viewer = new VKPostViewer(getItem(position),
							usersArray, groupsArray, holder, false) {
						@Override
						void changeDataSet() {
							adapter.notifyDataSetChanged();
							Log.d(TAG, "changeDataSet worked");
						}
					};
					viewer.setOnView();
					convertView.setTag(holder);
				} else {
					// Log.d(TAG, " "+convertView.getTag().toString());
					Log.d(TAG, "all is already prepared");
					// viewer = (VKPostViewer) convertView.getTag();
				}
			}

			return convertView;
		}

	}

	int lastState = OnScrollListener.SCROLL_STATE_IDLE;

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// Log.d(TAG, "scrollState = "+scrollState);
		if (scrollState == SCROLL_STATE_TOUCH_SCROLL
				&& lastState == SCROLL_STATE_IDLE && addNews == 0)
			addNews = 1;
		lastState = scrollState;

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem + visibleItemCount == totalItemCount
				&& addNews < 2) {
			addNews = 2;
		//	Log.d(TAG, "firstVisible: " + firstVisibleItem);
			topNewsIndex = firstVisibleItem;
			newsCreator.getNews();
		} else if (firstVisibleItem == 0) {
			// newsCreator.getNews( from_time );
		}
	}

	@Override
	public void onRefresh() {
		if (addNews < 2) {
			addNews = 2;
			refreshLayout.setRefreshing(true);
			newsCreator.getUpdate(
					news == null || news.size() == 0 ? 0 : news.get(0).date,
					TIME_NOW);
		}
	}

}
