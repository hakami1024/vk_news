package com.hakami1024.vk_news;

import org.json.JSONObject;

import android.app.Activity;

public interface DataUpdating {
	void setNewsfeed(JSONObject resp, boolean isFirst);
	void setNoNewsfeed();
	Activity getActivity();
}
