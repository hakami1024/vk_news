package com.hakami1024.vk_news;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.util.SparseArray;

import com.hakami1024.utils.VKPost;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiUser;

public class NewsfeedCreator {

	private final String TAG = "NewsfeedCreator";
	public SparseArray<VKApiUser> usersArray = new SparseArray<VKApiUser>();
	public SparseArray<VKApiCommunity> groupsArray = new SparseArray<VKApiCommunity>();
	public ArrayList<VKPost> news = new ArrayList<VKPost>();
	public String from_news = "";
	public long from_time = 0;
	private DataUpdating fragment;

	public NewsfeedCreator(DataUpdating fragment) {
		this.fragment = fragment;
	}

	protected void getUpdate(long beginTime, long endTime) {
		VKParameters params = VKParameters.from("filters", "post",
				"return_banned", "0", "count", "1", "start_time", ""
						+ (beginTime + 1), "end_time", "" + (endTime - 1));
		final VKRequest newsfeedReq = new VKRequest("newsfeed.get", params);
		Log.d(TAG, newsfeedReq.getPreparedRequest().getURI().toString());
		newsfeedReq.executeWithListener(new VKRequestListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 8979145001352558450L;

			@Override
			public void onComplete(VKResponse response) {
				// Log.i(TAG, response.json.toString());

				JSONArray items = new JSONArray();
				try {
					items = response.json.getJSONObject("response")
							.getJSONArray("items");
				} catch (Exception ex) {
					// Log.d(TAG, "error getting items");
					ex.printStackTrace();
				}
				if (items.length() > 0) {
					writeToStorage(response.json);
					fragment.setNewsfeed(response.json, true);
				} else {
					// Toast.makeText(fragment.getActivity(),
					// "No news received", Toast.LENGTH_LONG ).show();
					fragment.setNoNewsfeed();
				}
			}

			@Override
			public void onError(VKError error) {
				new AlertDialog.Builder(fragment.getActivity())
						.setMessage(
								"Can't get news. " + error.apiError + "\\"
										+ error.httpError + " "
										+ error.errorMessage)
						.setPositiveButton("Exit",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										fragment.getActivity().finish();
									}
								})
						.setNeutralButton("Cancel",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										fragment.setNoNewsfeed();
									}
								})
						.setNegativeButton("Retry",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										newsfeedReq.repeat();
									}
								}).show();
			}
		});
	}

	protected void getNews() {
		getNewsFromStorage("");
	}

	protected void getNewsFromStorage(final String newsName) {
		File dir1 = fragment.getActivity().getFilesDir();// "app_"+VKSdk.getAccessToken().userId,
															// Context.MODE_PRIVATE);
		File dir = new File(dir1, VKSdk.getAccessToken().userId);
		dir.mkdir();
		// Log.d(TAG, dir.getAbsolutePath() );
		// Log.d(TAG, "sorting");

		FilenameFilter timeFilter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				// Log.d(TAG,
				// filename+": from_time = "+from_time+", name to long:"+Long.parseLong(filename)+", the rule: "+
				// (Long.parseLong(filename) < from_time) );
				return Long.parseLong(filename) < from_time;
			}
		};

		FilenameFilter nameFilter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				// Log.d(TAG,
				// "filename = "+filename+", newsName = "+newsName+", result = "+filename.equals(newsName));
				return filename.equals(newsName);
			}
		};

		File[] file;
		if (newsName.equals("")) {
			file = from_time == 0 ? dir.listFiles() : dir.listFiles(timeFilter);
		} else
			file = dir.listFiles(nameFilter);
		Arrays.sort(file);
		if (file == null || file.length == 0) {
			if (newsName.equals(""))
				getNewsFromWeb();
			else
				fragment.setNoNewsfeed();
		} else {
			// Log.d(TAG, "getNews from storage: "+file[0].getAbsolutePath());

			FileInputStream in = null;
			StringBuilder jsonString = new StringBuilder();
			try {
				in = new FileInputStream(file[0].getAbsolutePath());
				byte[] buffer = new byte[1024];
				while (in.read(buffer) != -1)
					jsonString.append(new String(buffer));
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			if (jsonString.toString() != "")
				try {
					JSONObject resp = new JSONObject(jsonString.toString());
					from_news = resp.optJSONObject("response").getString(
							"next_from");
					from_time = resp.optJSONObject("response")
							.optJSONArray("items").getJSONObject(0)
							.getLong("date");
					// / Log.i(TAG, resp.toString());
					// Log.i(TAG, "from_news = "+from_news +
					// ", from_time = "+from_time);
					fragment.setNewsfeed(resp, false);
				} catch (JSONException e) {
					// Log.i(TAG, jsonString.toString() );
					e.printStackTrace();
				}
			else {
				// Log.d(TAG, "empty file");
				if (newsName.equals(""))
					getNewsFromWeb();
				else
					fragment.setNoNewsfeed();
			}
		}
	}

	private void getNewsFromWeb() {
		// Log.d(TAG,
		// "getNewsFromWeb, from_news = "+from_news+", from_time = "+from_time);
		VKParameters params = VKParameters.from("filters", "post",
				"return_banned", "0", "count", "1");

		if (from_news != "")
			params.put("start_from", from_news);
		if (from_time != 0)
			params.put("start_time", "" + from_time + 1);

		final VKRequest newsfeedReq = new VKRequest("newsfeed.get", params);
		// Log.d(TAG, newsfeedReq.getPreparedRequest().getURI().toString());
		newsfeedReq.executeWithListener(new VKRequestListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 8979145001352558450L;

			@Override
			public void onComplete(VKResponse response) {
				// Log.i(TAG, response.json.toString());

				try {
					from_news = response.json.optJSONObject("response")
							.getString("next_from");
					from_time = response.json.optJSONObject("response")
							.optJSONArray("items").getJSONObject(0)
							.getLong("date");
				} catch (JSONException e) {
					// Log.i(TAG, response.json.toString());
					e.printStackTrace();
				}
				// Log.d(TAG, "in onComplete, new from_news = "+from_news );

				JSONArray items = new JSONArray();
				try {
					items = response.json.getJSONObject("response")
							.getJSONArray("items");
				} catch (Exception ex) {
					// Log.d(TAG, "error getting items");
					ex.printStackTrace();
				}
				if (items.length() > 0) {
					writeToStorage(response.json);
					fragment.setNewsfeed(response.json, false);
				} else {
					// Toast.makeText(fragment.getActivity(),
					// "No news received", Toast.LENGTH_LONG ).show();
					fragment.setNoNewsfeed();
				}
			}

			@Override
			public void onError(VKError error) {
				new AlertDialog.Builder(fragment.getActivity())
						.setMessage(
								"Can't get news. " + error.apiError + "\\"
										+ error.httpError + " "
										+ error.errorMessage)
						.setPositiveButton("Exit",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										fragment.getActivity().finish();
									}
								})
						.setNeutralButton("Cancel",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										fragment.setNoNewsfeed();
									}
								})
						.setNegativeButton("Retry",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										newsfeedReq.repeat();
									}
								}).show();
			}
		});
	}

	private void writeToStorage(JSONObject jsonObject) {
		String name = "";
		String path = fragment.getActivity().getFilesDir().getAbsolutePath()
				+ "/" + VKSdk.getAccessToken().userId;
		try {
			name = ""
					+ jsonObject.getJSONObject("response")
							.getJSONArray("items").getJSONObject(0)
							.optInt("date");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Writer writer = null;
		FileOutputStream out = null;
		try {
			// File dir =
			// fragment.getActivity().getDir(VKSdk.getAccessToken().userId,
			// Context.MODE_PRIVATE );
			File dir = fragment.getActivity().getFilesDir();
			dir = new File(dir, VKSdk.getAccessToken().userId);
			dir.mkdirs();
			File file = new File(dir, name);
			file.createNewFile();
			// Log.d(TAG, "write to: "+file.getAbsolutePath());
			out = new FileOutputStream(file);
			writer = new OutputStreamWriter(out);
			writer.write(jsonObject.toString());
			writer.close();
			// Log.d(TAG, "writed on storage");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer == null && out != null && name != "")
				fragment.getActivity().deleteFile(name);
		}
	}

}
