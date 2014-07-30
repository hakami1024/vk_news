package com.hakami1024.utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.vk.sdk.api.model.VKApiModel;
import com.vk.sdk.api.model.VKList;

public class VKPostArray extends VKList<VKPost> {
	@Override
	public VKApiModel parse(JSONObject response) throws JSONException {
		fill(response, VKPost.class);
		return this;
	}
}
