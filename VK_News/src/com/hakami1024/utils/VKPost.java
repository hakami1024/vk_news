package com.hakami1024.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.util.Log;

import com.vk.sdk.api.model.Identifiable;
import com.vk.sdk.api.model.VKApiModel;
import com.vk.sdk.api.model.VKApiPlace;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKList;

public class VKPost extends VKApiModel
		/* VKAttachments.VKApiAttachment */implements Identifiable,
		android.os.Parcelable {

	public static String POST_DATE = "com.hakami1024.vk_news.VK_POST_DATE";
	private static String TAG = "VKPost";
	public int source_id;
	public long date;
	public int post_id;
	public VKList<VKPost> copy_history;
	public String text;
	public int reply_owner_id;
	public int comments_count;
	public boolean can_post_comment;
	public int likes_count;
	public boolean user_likes;
	public boolean can_like;
	public boolean can_publish;
	public int reposts_count;
	public boolean user_reposted;
	public boolean likes_exists = false;
	
	public VKAttachments attachments = new VKAttachments() {
		@Override
		public void fill(JSONArray from) {
			super.fill(from);
			for (int i = 0; i < attachments.getCount(); i++)
				try {
					attachments.get(i).fields = from.getJSONObject(i);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "error on Attach filling");
					e.printStackTrace();
				}
		}
	};
	public VKApiPlace geo;

	public VKPost parse(JSONObject source) throws JSONException {
		// Log.d("VKPost", source.toString() );
		post_id = source.optInt("post_id");
		source_id = source.optInt("source_id");
		if (source_id == 0)
			source_id = source.optInt("owner_id");
		if (post_id == 0)
			post_id = source.optInt("id");
		date = source.optLong("date");
		text = source.optString("text");
		reply_owner_id = source.optInt("reply_owner_id");
		JSONObject comments = source.optJSONObject("comments");
		if (comments != null) {
			comments_count = comments.optInt("count");
			can_post_comment = parseBoolean(comments, "can_post");
		}
		JSONObject likes = source.optJSONObject("likes");
		if (likes != null) {
			likes_count = likes.optInt("count");
			user_likes = parseBoolean(likes, "user_likes");
			can_like = parseBoolean(likes, "can_like");
			can_publish = parseBoolean(likes, "can_publish");
			likes_exists = true;
		}
		JSONObject reposts = source.optJSONObject("reposts");
		if (reposts != null) {
			reposts_count = reposts.optInt("count");
			user_reposted = parseBoolean(reposts, "user_reposted");
		}
		attachments.fill(source.optJSONArray("attachments"));

		JSONObject geo = source.optJSONObject("geo");
		if (geo != null) {
			this.geo = new VKApiPlace().parse(geo);
		}
		copy_history = new VKList<VKPost>(source.optJSONArray("copy_history"),
				VKPost.class);

		return this;
	}

	public VKPost(Parcel in) {
		this.post_id = in.readInt();
		this.source_id = in.readInt();
		this.date = in.readLong();
		this.text = in.readString();
		this.reply_owner_id = in.readInt();
		this.comments_count = in.readInt();
		this.can_post_comment = in.readByte() != 0;
		this.likes_exists = in.readByte() != 0;
		this.likes_count = in.readInt();
		this.user_likes = in.readByte() != 0;
		this.can_like = in.readByte() != 0;
		this.can_publish = in.readByte() != 0;
		this.reposts_count = in.readInt();
		this.user_reposted = in.readByte() != 0;
		this.attachments = in.readParcelable(VKAttachments.class
				.getClassLoader());
		this.geo = in.readParcelable(VKApiPlace.class.getClassLoader());
		this.copy_history = in.readParcelable(VKList.class.getClassLoader());
	}

	public VKPost() {
	}

	@Override
	public int getId() {
		return post_id;
	}

	public String getType() {
		return VKAttachments.TYPE_POST;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.post_id);
		dest.writeInt(this.source_id);
		dest.writeLong(this.date);
		dest.writeString(this.text);
		dest.writeInt(this.reply_owner_id);
		dest.writeInt(this.comments_count);
		dest.writeByte(can_post_comment ? (byte) 1 : (byte) 0);
		dest.writeByte(likes_exists ? (byte) 1 : (byte) 0);
		dest.writeInt(this.likes_count);
		dest.writeByte(user_likes ? (byte) 1 : (byte) 0);
		dest.writeByte(can_like ? (byte) 1 : (byte) 0);
		dest.writeByte(can_publish ? (byte) 1 : (byte) 0);
		dest.writeInt(this.reposts_count);
		dest.writeByte(user_reposted ? (byte) 1 : (byte) 0);
		dest.writeParcelable(attachments, flags);
		dest.writeParcelable(this.geo, flags);
		dest.writeParcelable(copy_history, flags);
	}

	public static Creator<VKPost> CREATOR = new Creator<VKPost>() {
		public VKPost createFromParcel(Parcel source) {
			return new VKPost(source);
		}

		public VKPost[] newArray(int size) {
			return new VKPost[size];
		}
	};

	private boolean parseBoolean(JSONObject from, String name) {
		return from != null && from.optInt(name, 0) == 1;
	}

}
